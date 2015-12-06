(ns server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.adapter.jetty :as rj]))

(def jetty (atom nil))
(def last-100-songs (atom '()))

(defn render-last-tracks [request]
  (deref last-100-songs))

(defroutes app-routes
  (GET "/last" [] #'render-last-tracks)
  (route/not-found "<h1>Page not found (datahandler)</h1>"))

(def app (handler/site app-routes))

(defn run []
  (rj/run-jetty app {:port 3000 :join? false}))
