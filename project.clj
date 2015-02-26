(defproject mud "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [korma "0.4.0"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [hiccup "1.0.5"]
                 [valip "0.2.0"]
                 [ring/ring-defaults "0.1.2"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler mud.routes/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]
                        [midje "1.5.0"]]}})
