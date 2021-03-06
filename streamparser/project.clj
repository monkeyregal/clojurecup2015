(def libpath (System/getenv "GNSDK_LIB"))
(defproject streamparser "0.1.0-SNAPSHOT"
  ;:exclusions   [[org.clojure/clojure]]
  :dependencies [;-----------------------------
                 [org.clojure/clojure "1.7.0"]

                 [ring/ring-jetty-adapter "1.4.0"]
                 [compojure "1.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [ring/ring-json "0.4.0"]

                 [me.raynes/conch "0.8.0"]
                 [byte-streams "0.2.0"]

                 [environ "1.0.1"]
                 [gnsdk "3.07.7"]
                 [gnsdk/gnsdk-helpers "3.07.7"]

                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17"]

                 [org.clojure/core.async "0.2.374"]

                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.codec "0.1.0"]
                 [clj-time "0.11.0"]
                 [cheshire "5.5.0"]
                 [clj-http "2.0.0"]]

  :jvm-opts     ["-Xmx4G"
                 "-XX:MaxPermSize=256m"
                 "-Djava.awt.headless=true"
                 "-Djava.net.preferIPv4Stack=true"
                 ~(str "-Djava.library.path=" libpath)]

  :plugins [[lein-libdir "0.1.1"]
            [lein-pprint "1.1.2"]
            [lein-shell "0.5.0"]]

  :main streamparser
  ;:aot [streamparser]
  :libdir-path "jars"
  :release-tasks [["jar"]
                  ["libdir"]
                  ["shell" "sh" "-c" "mv target/*.jar jars/"]]
  :resource-paths ["resources"]
  :java-source-paths ["src-java"]
  ;:global-vars {*warn-on-reflection* true}
  :repl-options {:port 8081}
  :profiles {:uberjar {:aot :all}})
