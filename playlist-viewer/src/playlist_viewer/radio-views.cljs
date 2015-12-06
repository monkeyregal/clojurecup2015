(ns playlist-viewer.radio-views
  (:require
    [playlist-viewer.global :refer [radio-stations]]))


(defn- render-list-item [list-item]
       [:div {:class "rm-spotify-link"}
        [:span {:class ""} (:name list-item)]
        [:a {:class "" :href (:url list-item)}]])

(defn- render-station [station]
      [:div {:class "rm-station-name"} (:name station)
        [:div {:class ""}
         (map #(render-list-item %) (:lists station))
         ]

       ])

(defn render-radio-stations [stations]
      (conj
        [:div {:class "rm-station-heading"} "Radio Stations"]
        (map #(render-station %) stations)))


(defn top-100 []
      [:div {:class ""}])