(ns playlist-viewer.views
  (:require
    [playlist-viewer.global :refer [app-state radio-stations]]
    [playlist-viewer.radio-views :refer [render-radio-stations]]))

(def num-tracks 100)

(defn add-stream [])

(defn add-track [])


(defn navbar []
  [:nav {:class "navbar navbar-default"}
   [:a {:class "navbar-brand" :href "#" } "No DJ's plz" ]
   ;[:button {:class "btn btn-info btn-navbar"
   ;          :on-click #(add-stream)
   ;          } "+ Stream"]
   ;[:button {:class "btn btn-info btn-navbar"
   ;          :on-click #(add-track)
   ;          } "+ Track"] 
   ])


(defn render-playlist-line [item]
  [:div {:class ""} (:name item)
   [:span {:class "badge rm-badge-spacer"} (count (:tracks item))]])


(defn render-all-playlists []
  (map
    #(render-playlist-line %)
    (:playlists @app-state)))


(defn render-track [track]
  [:div {:class "row rm-track"}
   [:span {:class "col-md-6 rm-track-name"} (:name track) ]
   [:span {:class "col-md-4 rm-track-artist"} (:artist track)
    [:span {:class "rm-track-album"} (:album track)]]
   [:span {:class "col-md-2"}
    [:img {:data-src "holder.js/64x64"}]]
   ])

(defn render-track-2 [track]
  [:div {:class "media"}
   [:div {:class "media-left"}
	[:b {:class "station"} (name (:stream-id track))]]
   [:div {:class "media-body"}
    [:h4 {:class "media-heading"} (:track track)]
    [:span {:class "rm-track-artist"} (:artist track)]]])



(defn render-tracks [tracks]
  (map #(render-track-2 %) tracks))


(defn render-playlist-item [item]
  [:div {:class "rm-playlist-name"} 
   (render-tracks (take num-tracks (:tracks item)) )])


(defn render-playlist []
  (render-playlist-item
    (nth
      (:playlists @app-state)
      (:current-playlist @app-state))))


(defn container []
    [:div {:class "container"}
     [:div (navbar)]
     [:div {:class "row"}
      [:div {:id "left" :class "col-md-9"} (render-playlist)]
      [:div {:id "right" :class "col-md-3"} (render-radio-stations @radio-stations)]]])
