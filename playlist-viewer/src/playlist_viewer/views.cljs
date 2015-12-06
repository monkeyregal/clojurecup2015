(ns playlist-viewer.views
  (:require
    [playlist-viewer.global :refer [app-state radio-stations]]
    [playlist-viewer.radio-views :refer [render-radio-stations]]))


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
  [:div {:class "rm-track"}
   [:div {:class "rm-track-name"} (:name track) ]
   [:div {:class "rm-track-artist"} (:artist track)]
   [:div {:class "rm-track-album"} (:album track)]
   [:div {:class "media media-right"}
    [:img {:class "media-object" :data-src "holder.js/64x64"}]]
   ])


(defn render-tracks [tracks]
  (map #(render-track %) tracks))


(defn render-playlist-item [item]
  [:div {:class "rm-playlist-name"} (str (:name item) " ... Currently Playing")
   (render-tracks (:tracks item))])


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

