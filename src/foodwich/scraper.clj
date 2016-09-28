(ns foodwich.scraper
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]
            [net.cgrand.enlive-html :as html]
              [environ.core :refer [env]]
            [clojure.string :as str]))

(defn req-wrap
  [url opts method]
  (let [{:keys [status body error]} @(http/request (merge {:method method :url url} opts))]
    (if-not (= status 200) nil body)))

(defn uber-req
  [addr coords]
  (let [url "https://eats.uber.com/"
        opts {:query-params {:location addr
                             :latLng coords}}]
  (req-wrap url opts :get)))

(defn uber-parse
  [addr coords]
  (let [res (uber-req addr coords)
        body (json/decode res true)]))

(defn delivery-req
  [addr]
  (let [url "https://www.delivery.com/api/merchant/search/delivery"
        opts {:query-params {:address addr
                             :client_id (env :delivery-key)
                             :order_time "ASAP"
                             :order_type "delivery"}}]
      (req-wrap url opts :get)))

(defn delivery-parse
  [addr]
  (let [res (delivery-req addr)
        body (json/decode res true)
        options (body :merchants)]
    (remove nil?
      (map
        (fn [o]
          (let [restaurant? (= "Restaurant" (get-in o [:summary :type_label]))
                available? (get-in o [:ordering :availability :delivery])]
            (when (and restaurant? available?)
              {:source :delivery
               :id (o :id)
               :name (get-in o [:summary :name])
               :cuisines (get-in o [:summary :cuisines])
               :logo (get-in o [:summary :merchant_logo])
               :link (get-in o [:summary :url :complete])
               :cost-range (get-in o [:summary :price_rating])
               :rating (get-in o [:summary :star_ratings])
               :delivery-min (get-in o [:ordering :minimum])
               :delivery-fee (get-in o [:ordering :delivery_charge])
               :delivery-time (get-in o [:ordering :availability :delivery_estimate])}))) options))))

(defn doordash-req
  [coords]
  (let [url "https://api.doordash.com/v1/store_search"
        opts {:query-params {:lat (coords 0)
                             :lng (coords 1)
                             :promotion 3
                             :limit 100}}]
      (req-wrap url opts :get)))

(defn doordash-parse
  [coords]
  (let [res (doordash-req coords)
        options (json/decode res true)]
    (remove nil?
      (map
        (fn [o]
          (when (not (o :status_type) "pre-order")
            {:source :doordash
             :id (get-in o [:business :id])
             :name (o :name)
             :cuisines (o :tags)
             :logo (o :cover_img_url)
             :link (str "https://www.doordash.com" (o :url))
             :cost-range (o :price_range)
             :rating (float (* 5 (/ (o :composite_score) 10)))
             :delivery-min nil
             :delivery-fee (* (o :delivery_fee) 0.01)
             :delivery-time (re-find #"\d+" (o :status))})) options))))

(defn grubhub-req
  [coords]
  (let [url "https://api-gtm.grubhub.com/restaurants/search"
        opts {:query-params {:orderMethod "delivery"
                             :pageSize 1000
                             :facet "open_now:true"
                             :location (str "POINT(" (coords 1) " " (coords 0)  ")")}
              :headers {"Authorization" (str "Bearer " (env :grubhub-key))}}]
      (req-wrap url opts :get)))

(defn grubhub-parse
  [coords]
  (let [res (grubhub-req coords)
        body (json/decode res true)
        options (get-in body [:search_result :results])]
    (map
      (fn [o]
        {:source :grubhub
         :id (o :restaurant_id)
         :name (o :name)
         :cuisines (o :cuisines)
         :logo (o :logo)
         :link nil
         :cost-range (int (o :price_rating))
         :rating (get-in o [:ratings :rating_bayesian_half_point])
         :delivery-min (* (get-in o [:delivery_minimum :price]) 0.01)
         :delivery-fee (* (get-in o [:delivery_fee :price]) 0.01)
         :delivery-time (o :delivery_time_estimate)})
      options)))

(defn foodler-req
  [addr zip]
  (let [url "https://www.foodler.com/load.do"
        opts {:query-params {:waCityZip addr :searchType "wa" :pacZip zip}}]
    (req-wrap url opts :get)))

(defn foodler-parse
  [addr zip]
  (let [res (foodler-req addr zip)
        options (html/select (html/html-snippet res) [:div.foundLineNormal])]
    (distinct
      (map
        (fn [o]
          {:source :foodler
           :id (second (re-find #"\/.+\/(\d+)" (get-in (first (html/select o [:h2 :a])) [:attrs :href])))
           :name (html/text (first (html/select o [:h2 :a])))
           :logo (get-in (first (html/select o [:div.logo :img])) [:attrs :src])
           :link (str "https://www.foodler.com/" (re-find #"\/.+\/\d+" (get-in (first (html/select o [:h2 :a])) [:attrs :href])))
           :cost-range (when-let [cost (first (html/select o [:div.cost :img]))]
                          (Integer. (second (re-find #"cost_(\d)" (get-in cost [:attrs :src])))))
           :rating (when-let [rating (re-find #"\d+.\d+" (html/text (first (html/select o [:div.rating :div.average]))))]
                    (Float. rating))
           :delivery-min (Integer. (re-find #"\d+" (html/text (first (html/select o [:div.delivery :span.min])))))
           :delivery-fee (or (second (re-find #"\$(\d+)" (html/text (first (html/select o [:div.delivery :span.fee]))))) 0)}) options))))

(defn search
  [params]
  (let [{:keys [addr zip coords]} params
        coords (clojure.string/split coords #",")]
    (remove nil?
      (concat (grubhub-parse coords)
              (doordash-parse coords)
              (delivery-parse addr)
              (foodler-parse addr zip)))))
