(ns playlist-viewer.views
  (:require
    [playlist-viewer.global :refer [app-state radio-stations]]
    [playlist-viewer.radio-views :refer [render-radio-stations]]))

(def num-tracks 5)

(defn play-sound []
  (.play
    (.createElement js/document "AUDIO") "sfx/spider_monkey_screech_or_howl.mp3"))


(defn add-stream [])


(defn add-track [])


(defn navbar []
  [:nav {:class "navbar navbar-default"}
   [:a {:class "navbar-brand" :href "#" } "Regal Monkey" ]
   ;[:button {:class "btn btn-info btn-navbar"
   ;          :on-click #(add-stream)
   ;          } "+ Stream"]
   ;[:button {:class "btn btn-info btn-navbar"
   ;          :on-click #(add-track)
   ;          } "+ Track"]
   [:img {:class "navbar-right rm-nav-image"
          :on-click #(play-sound)
          :src "sfx/Monkey-37-277x300.jpg"}]
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
    [:a {:href "#"}
     [:img {:class "media-object" :data-src "holder.js/64x64" :alt "64x64"}]]]
   [:div {:class "media-body"}
    [:h4 {:class "media-heading"} (:name track)]
    [:span {:class "rm-track-artist"} (:artist track)]
    [:span {:class "rm-track-album"} (:album track)]]])



(defn render-tracks [tracks]
  (map #(render-track-2 %) tracks))


(defn render-playlist-item [item]
  [:div {:class "rm-playlist-name"} (str "Current playlist: "  (:name item))
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

