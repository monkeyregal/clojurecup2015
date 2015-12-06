(def libpath (System/getenv "GNSDK_LIB"))
(defproject streamparser "0.1.0-SNAPSHOT"
  ;:exclusions   [[org.clojure/clojure]]
  :dependencies [;-----------------------------
                 [org.clojure/clojure "1.8.0-RC3"]

                 [me.raynes/conch "0.8.0"]
                 [byte-streams "0.2.0"]

                 [environ "1.0.1"]
                 [gnsdk "3.07.7"]
                 [gnsdk/gnsdk-helpers "3.07.7"]

                 [org.clojure/tools.logging "0.3.1"]

                 [org.clojure/core.async "0.2.374"]]
                 ;--------------------------------

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

  :java-source-paths ["src-java"]
  :global-vars {*warn-on-reflection* true}
  :repl-options {:port 8081})
