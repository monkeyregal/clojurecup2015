(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'playlist-viewer.core
   :output-to "out/playlist_viewer.js"
   :output-dir "out"})
