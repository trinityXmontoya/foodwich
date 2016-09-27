(ns foodwich.core
  (:require [org.httpkit.server :as server]
            [foodwich.scraper :as scraper]
            [foodwich.templates :as tmplts])
  (:import [java.io File])
  (:gen-class))

(defn app [req]
  (let [uri (req :uri)]
    ; (println req)
    (if (boolean (re-find #"(.js|.css)" uri))
    {:status 200
    ;  :headers {"Content-Type" "application/javascript"}
     :body (File. "public" uri)}
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body (tmplts/page-template (tmplts/results-template (scraper/doordash-parse "cat")))}
    )
  )
)

(defonce server (atom nil))

(defn stop-server []
 (when-not (nil? @server)
   (@server)
   (reset! server nil)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (reset! server (server/run-server #'app {:port 8080})))
