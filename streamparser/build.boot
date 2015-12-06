(set-env!
 :source-paths #{"src" "test"}
 :dependencies '[[org.clojure/clojure "1.7.0" :scope "provided"]

                 [me.raynes/conch "0.8.0"]
                 [byte-streams "0.2.0"]

                 [environ "1.0.1"]
                 [boot-environ "1.0.1"]
                 [gnsdk "3.07.7"]
                 [gnsdk/gnsdk-helpers "3.07.7"]

                 [org.clojure/core.async "0.2.374"]
                 [ring/ring-core "1.4.0"]
                 [compojure "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [amazonica "0.3.39"]
                 [org.clojure/data.json "0.2.6"]
                 [clj-http "2.0.0"]
                 [http-kit "2.1.18"]
                 [org.clojure/tools.logging "0.2.4"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]

                 [org.apache.commons/commons-daemon "1.0.9"]])

(require '[environ.boot :refer [environ]])
(require '[environ.core :refer [env]])
(require '[boot.pod :as pod])
(require '[streamparser])
