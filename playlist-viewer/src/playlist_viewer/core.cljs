(ns playlist-viewer.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [playlist-viewer.global :refer [app-state]]
    [playlist-viewer.views :refer [container render-playlist render-all-playlists]]))

(enable-console-print!)
(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload


(defn hello-world []
  [:h1 (:text @app-state)])


(reagent/render-component [container]
                          (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
