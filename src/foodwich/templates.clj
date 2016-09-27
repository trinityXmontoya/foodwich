(ns foodwich.templates
  (:require [hiccup.core :as hc]))

(defn page-template
  [body]
  (hc/html [:head
            [:title "Foodwich"]
            [:link {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/foundation/6.2.3/foundation.min.css"}]
            [:link {:rel "stylesheet" :href "/css/app.css"}]
            [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.2/jquery.min.js"}]
            [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/foundation/6.2.3/foundation.min.js"}]]
            [:script {:src "/js/app.js"}]
          [:body
            [:div.callout.primary
              [:div.row.column
                [:h1 "Foodwich"]]]
            body]))

(defn results-template
  [results]
  (hc/html [:div.row.small-up-2.medium-up-3.large-up-4
            (for [result results]
              [:div.column
                [:img {:src (result :logo)}]
                [:h2
                  [:a {:href (result :link)} (result :name)]]
                [:h5 (result :source)]])]))


                ; :source
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
