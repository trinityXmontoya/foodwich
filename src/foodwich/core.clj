(ns foodwich.core
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str])
  (:gen-class))

(defn uber-req
  [addr coords]
  (let [url "https://eats.uber.com/"
        opts {:query-params {:location addr
                             :latLng coords}}]
  (http/get url opts)))

(defn uber-parse
  [addr coords]
  (let [res @(uber-req addr coords)
        body (json/decode (res :body) true)]
    ))

(defn foodler-req
  [addr zip]
  (let [url "https://www.foodler.com/load.do"
        opts {:query-params {:waCityZip addr :searchType "wa" :pacZip zip}}]
    (http/get url opts)))

; (first ((first ((nth (f :content) 9) :content)) :content))
(defn foodler-parse
  [addr]
  (let [res @(foodler-req addr zip)
        body (res :body)
        foods (html/select (html/html-snippet (res :body)) [:div.foundLine])]
    (map (fn [f]
            {:name (html/text (first (html/select f [:h2 :a])))
             :logo (get-in (first (html/select f [:div.logo :img])) [:attrs :src])
             :link (re-find #"\/.+\/\w+\/" (get-in (first (html/select f [:h2 :a])) [:attrs :href]))
             :cost-range (second (re-find #"cost_(\d)" (get-in (first (html/select f [:div.cost :img])) [:attrs :src])))
             :rating (re-find #"\d+.\d+" (html/text (first (html/select f [:div.rating :div.average]))))
             :delivery-min (str/trim (str/replace (html/text (first (html/select f [:div.delivery :span.min]))) #"minimum" ""))
             :delivery-fee (re-find #"\$\d+" (html/text (first (html/select f [:div.delivery :span.fee]))))}) foods)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
