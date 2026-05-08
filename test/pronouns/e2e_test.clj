(ns pronouns.e2e-test
  (:require [clojure.test :refer [deftest testing is]]
            [clj-http.lite.client :as client]
            [clojure.tools.logging :as log]
            [clojure.java.shell :refer [sh]]
            [clojure.java.process :as p]
            [clojure.java.io :as io]))

(defn tcp-bound? [port]
  (zero? (:exit (sh "fuser" (str port "/tcp")))))

(defn find-open-port []
  (loop [port 3000]
    (if-not (tcp-bound? port)
      port
      (recur (+ 1024 (rand-int 8900))))))

(defn log-reader [proc]
  (-> proc p/stderr io/reader))

(defn wait-for-server [timeout-ms port sproc]
  (let [start-time (System/currentTimeMillis)
        exp-time (+ timeout-ms start-time)]

    (log/info "Waiting for server to start on" port start-time exp-time)
    (loop []
      (when (> (System/currentTimeMillis) exp-time)
        (with-open [r (log-reader sproc)]
          ;; We assume there will always be 4 lines to take and that's enough
          ;; to hint at the issue. It would be better if we could read
          ;; the whole log. Why can't we? (.destroy sproc) disappears the
          ;; stderr and stdout streams
          (let [log-lines (take 4 (line-seq r))]
            (log/warn log-lines)
            (throw (ex-info "Server startup timeout" {})))))
      (Thread/sleep 500)
      (if (tcp-bound? port)
        (do (log/info "Server process took"
                      (- (System/currentTimeMillis) start-time)
                      "ms to bind port")
            true)
        (recur)))))

(defn start-server [port]
  (p/start {:env {"PORT" (str port)
                  "CLASSPATH" ""}}
           "lein" "run"))

(deftest ^:e2e e2e-server-test
  (let [maxwait 120000
        port (find-open-port)
        sproc (start-server port)]

    (wait-for-server maxwait port sproc)

    (testing "Boot server in child process and get front page")
    (let [response (client/get (str "http://localhost:" port)
                               {:throw-exceptions false})]

      (testing "Response"
        (is (= 200 (:status response)))
        (is (re-find #"<a href=\"all-pronouns\">" (:body response))))

      (testing "Server logs")
      (let [final-log (future
                        (with-open [r (log-reader sproc)]
                          (slurp r)))]

        ;;NOTE: You may ask why not simply `(.destroy sproc)`?
        ;; The issue is that this prevents the io/reader from reading
        ;; the stderr stream for some reason, so we would lose the
        ;; server process's logs.
        (sh "fuser" "-k" (str port "/tcp"))
        (log/info "*** Subprocess log follows ***\n" @final-log)
        (is (re-find #":request-method :get, :uri \"/\"" @final-log))
        (is (not (re-find #"ERROR" @final-log))))))

  (shutdown-agents))
