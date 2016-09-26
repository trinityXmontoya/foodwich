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
  [addr zip]
  (let [res @(foodler-req addr zip)
        body (res :body)
        foods (html/select (html/html-snippet (res :body)) [:div.foundLineNormal])]
    (distinct
      (map
        (fn [f]
            {:source :foodler
             :id (second (re-find #"\/.+\/(\d+)" (get-in (first (html/select f [:h2 :a])) [:attrs :href])))
             :name (html/text (first (html/select f [:h2 :a])))
             :logo (get-in (first (html/select f [:div.logo :img])) [:attrs :src])
             :link (re-find #"\/.+\/\d+" (get-in (first (html/select f [:h2 :a])) [:attrs :href]))
             :cost-range (let [cost (first (html/select f [:div.cost :img]))]
                          (when cost
                            (Integer. (second (re-find #"cost_(\d)" (get-in cost [:attrs :src]))))))
             :rating (let [rating (re-find #"\d+.\d+" (html/text (first (html/select f [:div.rating :div.average]))))]
                      (when rating (Float. rating)))
             :delivery-min (Integer. (re-find #"\d+" (html/text (first (html/select f [:div.delivery :span.min])))))
             :delivery-fee (or (second (re-find #"\$(\d+)" (html/text (first (html/select f [:div.delivery :span.fee]))))) 0)}) foods))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
