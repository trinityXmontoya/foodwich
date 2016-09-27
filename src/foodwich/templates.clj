(ns foodwich.templates
  (:require [hiccup.core :as hc]
            [environ.core :refer [env]]))

(defn search-template
  []
  (hc/html [:form
              [:div.row
                [:div.medium-6.columns
                  [:input {:id "pac-input" :type "text" :placeholder "123 Main St"}]
                  [:button {:type "submit" :class "button"} "Search"]]]]))

(defn results-template
  [results]
  (hc/html [:div.row.small-up-2.medium-up-3.large-up-4
            (for [result results]
              [:div.column {:id (str (result :source) "-" (result :id))}
                [:img {:src (result :logo)}]
                [:h2
                  [:a {:href (result :link)} (result :name)]]
                [:span (clojure.string/join ", " (result :cuisines))]
                [:br]
                [:span.rating (str (result :rating) "/5")]
                [:span.cost-range (result :cost-range)]
                [:div.delivery-info
                  [:span.min (str "$" (or (result :delivery-min) 0))]
                  [:span.fee (str "$" (result :delivery-fee))]
                  [:span.time (str (result :delivery-time) " mins")]]
                [:h5 (result :source)]])]))

(defn page-template
  [body]
  (hc/html [:head
            [:title "Foodwich"]
            [:link {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/foundation/6.2.3/foundation.min.css"}]
            [:link {:rel "stylesheet" :href "/css/app.css"}]
            [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.2/jquery.min.js"}]
            [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/foundation/6.2.3/foundation.min.js"}]
            [:script {:src "/js/app.js"}]
            [:script {:src "/js/autocomplete.js"}]
            ]
           [:body
            [:div.callout.primary
              [:div.row.column
                [:h1 "Foodwich"]]]
            [:div.search
              (search-template)]
            body
            [:script {:src (str "https://maps.googleapis.com/maps/api/js?key=" (env :google-api-key) "&libraries=places&callback=initAutocomplete"}]]))
