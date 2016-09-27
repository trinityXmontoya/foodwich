(defproject foodwich "0.1.0-SNAPSHOT"
  :description "See README"
  :url "https://github.com/trinityXmontoya/foodwich.git"
  :min-lein-version "2.0.0"
  :license {:name "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International"
            :url "https://creativecommons.org/licenses/by-nc-sa/4.0/legalcode"
            :author "Trinity Montoya"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.1.18"]
                 [cheshire "5.6.3"]
                 [hiccup "1.0.5"]
                 [enlive "1.1.6"]
                 [environ "1.1.0"]]
  :plugins [[lein-environ "1.1.0"]]
  :main ^:skip-aot foodwich.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
