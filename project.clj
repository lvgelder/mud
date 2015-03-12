(defproject mud "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.ninjudd/ring-async "0.2.0"]
                 [com.ninjudd/eventual "0.5.0"]
                 [compojure "1.3.2"]
                 [korma "0.4.0"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [hiccup "1.0.5"]
                 [valip "0.2.0"]
                 [environ "1.0.0"]
                 [com.cemerick/friend "0.2.1" :exclusions [[commons-logging] [xml-apis] [org.clojure/core.cache]]]
                 [ring "1.3.2"]]
  :plugins [[lein-ring "0.9.2"]
            [lein-environ "1.0.0"]]
  :ring {:handler mud.routes/app}
  :jvm-opts ["-Xmx1g" "-server"]
  :main mud.routes
  :profiles
  {:test {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]
                        [midje "1.6.3"]
                        [environ "1.0.0"]]}
   :production {:dependencies [[org.clojure/clojure "1.6.0"]
                               [compojure "1.3.2"]
                               [korma "0.4.0"]
                               [org.xerial/sqlite-jdbc "3.7.2"]
                               [org.postgresql/postgresql "9.2-1002-jdbc4"]
                               [hiccup "1.0.5"]
                               [valip "0.2.0"]
                               [environ "1.0.0"]
                               [ring "1.3.2"]]}})
