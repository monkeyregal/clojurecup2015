(ns playlist-viewer.radio-views
  (:require
    [playlist-viewer.global :refer [radio-stations]]))



(defn- render-list-item [list-item]
       [:div {:class "rm-spotify-link"}
        [:a {:class "" :href (:url list-item)} (:name list-item)]])


(defn- render-station [station]
      [:div {:class "rm-station-name"} (:name station)
        [:div {:class ""}
         (map #(render-list-item %) (:lists station))]])


(defn top-100 []
      (conj [:div {:class "rm-station-name"} "Top 100"]
            (render-list-item {:name "Last day" :url ""})
            (render-list-item {:name "Last week" :url ""})
            (render-list-item {:name "All time" :url ""})))


(defn render-radio-stations [stations]
      (conj
        [:div {:class "rm-station-heading"} "Radio Stations"]
        (map #(render-station %) stations)
        (top-100)))

