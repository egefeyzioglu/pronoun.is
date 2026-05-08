;; pronoun.is - a website for pronoun usage examples
;; Copyright (C) 2014 - 2026 Morgan Astra

;; This program is free software: you can redistribute it and/or modify
;; it under the terms of the GNU Affero General Public License as
;; published by the Free Software Foundation, either version 3 of the
;; License, or (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Affero General Public License for more details.

;; You should have received a copy of the GNU Affero General Public License
;; along with this program.  If not, see <https://www.gnu.org/licenses/>

(ns pronouns.web
  (:require [compojure.core :refer [defroutes GET ANY]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.logger :as logger]
            [environ.core :refer [env]]
            [pronouns.pages :as pages])
  (:gen-class))

(defroutes app-routes
  (GET "/" []
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (pages/front)})

  (GET "/all-pronouns" []
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (pages/all-pronouns)})

  (GET "/pronouns.css" []
     {:status 200
     :headers {"Content-Type" "text/css"}
     :body (slurp (io/resource "pronouns.css"))})

  (GET "/*" {params :params}
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (pages/pronouns params)})

  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn wrap-gnu-natalie-nguyen [handler]
  (fn [req]
    (when-let [resp (handler req)]
      (assoc-in resp [:headers "X-Clacks-Overhead"] "GNU Natalie Nguyen"))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           (log/error e)
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(def base-middleware
  #(-> %
       wrap-content-type
       wrap-not-modified
       logger/wrap-with-logger
       wrap-error-page
       wrap-gnu-natalie-nguyen
       wrap-params))

(def prod-app
  (base-middleware app-routes))

(def dev-app
  (-> app-routes
      base-middleware
      wrap-stacktrace))

(defn no-port []
  (log/fatal "PORT environment variable is required"
             "Example: PORT=3000 lein run")
  (System/exit 1))

(defn -main []
  (if-let [port (:port env)]
    (jetty/run-jetty prod-app
                     {:port (Integer/parseInt port)})
    (no-port)))
