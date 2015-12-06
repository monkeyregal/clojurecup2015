(ns server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.adapter.jetty :as rj]
            [ring.util.response :as r]
            [ring.middleware.json :refer [wrap-json-response]]))

(def jetty (atom nil))
(def last-100-songs (atom '()))

(defn render-last-tracks [request]
  (-> (r/response (deref last-100-songs))
      (r/content-type "application/json; charset=utf-8")))

(defroutes app-routes
  (GET "/last" [] #'render-last-tracks)
  (route/not-found "<h1>Page not found.</h1>"))

(def app (-> (handler/api app-routes)
             (wrap-json-response)))

(defn run []
  (rj/run-jetty app {:port 3000 :join? false}))
