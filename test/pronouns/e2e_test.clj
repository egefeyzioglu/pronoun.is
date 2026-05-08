(ns pronouns.e2e-test
  (:require [clojure.test :refer [deftest testing is]]
            [clj-http.lite.client :as client]
            ;; [clojure.edn :as edn]
            [clojure.java.shell :refer [sh]]
            [clojure.java.process :as p]
            [clojure.java.io :as io]))

(defn tcp-bound? [port]
  (zero? (:exit (sh "fuser" (str port "/tcp")))))

(defn find-open-port []
  (loop []
    (let [port (+ 1000 (rand-int 8999))]
      (if-not (tcp-bound? port)
        port
        (recur)))))

(defn wait-for-server [timeout-ms port]
  (let [start-time (System/currentTimeMillis)
        exp-time (+ timeout-ms start-time)]
    (loop []
      (when (> (System/currentTimeMillis) exp-time)
        (throw (ex-info "Server startup timeout" {})))
      (Thread/sleep 500)
      (if (tcp-bound? port)
        true
        (recur)))))

(defn server [port]
  (p/start {:clear-env true
            :env {"PORT" (str port)}}
           "lein" "run"))

(deftest ^:e2e e2e-server-test
  (let [maxwait 60000
        port (find-open-port)
        sproc (server port)]

    (wait-for-server maxwait port)

    (testing (str "Boot server " port " and get front page"))
    (let [response (client/get (str "http://localhost:" port)
                               {:throw-exceptions false})]

      (testing "Response"
        (is (= 200 (:status response)))
        (is (re-find #"<a href=\"all-pronouns\">" (:body response))))

      (testing "Server logs")
      (let [final-log (future
                        (with-open [log-reader (-> (p/stderr sproc)
                                                   io/reader)]
                          (slurp log-reader)))]

        (sh "fuser" "-k" (str port "/tcp"))
        (is (re-find #":request-method :get, :uri \"/\"" @final-log))
        (is (not (re-find #"ERROR" @final-log))))))
  (shutdown-agents))


