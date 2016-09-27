(ns foodwich.scraper
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]))

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

(defn delivery-req
  [addr]
  (let [url "https://www.delivery.com/api/merchant/search/delivery"
        opts {:query-params {:address addr
                             :client_id "MDlkMzY3Nzg3MjU1ZjRkNmY4OWZjNDA0NjBjMTI0MWZl"
                             :order_time "ASAP"
                             :order_type "delivery"}}]
      (http/get url opts)))

(defn delivery-parse
  [addr]
  (let [res @(delivery-req addr)
        body (json/decode (res :body) true)
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
               :delivery-time (get-in o [:ordering :availability :delivery_estimate])})))
        options))))

(defn doordash-req
  [addr]
  (let [url "https://api.doordash.com/v1/store_search"
        opts {:query-params {:lat 40.7589
                             :lng -73.9851
                             :promotion 3
                             :limit 100}}]
      (http/get url opts)))

(defn doordash-parse
  [addr]
  (let [res @(doordash-req addr)
        options (json/decode (res :body) true)]
    (remove nil?
      (map
        (fn [o]
          (let [closed (= (o :status_type) "pre-order")]
            (when-not closed
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
               :delivery-time (re-find #"\d+" (o :status))})))
        options))))

(defn grubhub-req
  [addr]
  (let [url "https://api-gtm.grubhub.com/restaurants/search"
        opts {:query-params {:orderMethod "delivery"
                             :pageSize 1000
                             :facet "open_now:true"
                             :location (str "POINT(" -73.9851 " " 40.7589  ")")}
              :headers {"Authorization" "Bearer 2d03aae7-86ce-4bf3-b6fc-eb930b39f4ab"}}]
      (http/get url opts)))

      ; (defn grubhub-parse
      ;   [addr]
      ;   (let [res @(grubhub-req addr)
      ;         body (json/decode (res :body) true)
      ;         options (get-in body [:search_result :results])]
      ;     (map (fn [o]
      ;             {:source
      ;              :id
      ;              :name
      ;              :cuisines
      ;              :logo
      ;              :link
      ;              :cost-range
      ;              :rating
      ;              :delivery-min
      ;              :delivery-fee
      ;              :delivery-time
                    ;  }) options)
      ;
      ;         ))

(defn grubhub-parse
  [addr]
  (let [res @(grubhub-req addr)
        body (json/decode (res :body) true)
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
    (http/get url opts)))

; (def ^:dynamic *headline-selector*
;      #{[html/root :> :h2 :a],
;        [html/root :> :h3 :a]
;        [html/root :> :h5 :a]}))
;
; (defn extract [node]
;   (let [headline (first (html/select [node] *headline-selector*))
;         byline   (first (html/select [node] *byline-selector*))
;         summary  (first (html/select [node] *summary-selector*))
;         result   (map html/text [headline byline summary])]
;     (zipmap [:headline :byline :summary] (map #(str/replace %  #"\n" "") result))))

; (first ((first ((nth (f :content) 9) :content)) :content))
(defn foodler-parse
  [addr zip]
  (let [res @(foodler-req addr zip)
        body (res :body)
        options (html/select (html/html-snippet (res :body)) [:div.foundLineNormal])]
    (distinct
      (map
        (fn [o]
          {:source :foodler
           :id (second (re-find #"\/.+\/(\d+)" (get-in (first (html/select o [:h2 :a])) [:attrs :href])))
           :name (html/text (first (html/select o [:h2 :a])))
           :logo (get-in (first (html/select o [:div.logo :img])) [:attrs :src])
           :link (re-find #"\/.+\/\d+" (get-in (first (html/select o [:h2 :a])) [:attrs :href]))
           :cost-range (let [cost (first (html/select o [:div.cost :img]))]
                        (when cost
                          (int (second (re-find #"cost_(\d)" (get-in cost [:attrs :src]))))))
           :rating (let [rating (re-find #"\d+.\d+" (html/text (first (html/select o [:div.rating :div.average]))))]
                    (when rating (Float. rating)))
           :delivery-min (int (re-find #"\d+" (html/text (first (html/select o [:div.delivery :span.min])))))
           :delivery-fee (or (second (re-find #"\$(\d+)" (html/text (first (html/select o [:div.delivery :span.fee]))))) 0)})
        options))))
