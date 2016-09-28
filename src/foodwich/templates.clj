(ns foodwich.templates
  (:require [hiccup.core :as hc]
            [foodwich.emoji :as emoji]
            [environ.core :refer [env]]))

(defn search-template
  []
  (hc/html [:form {:method "POST" :action "/search" :id "address-form"}
              [:div.row
                [:div.small-6.small-centered.columns
                  [:input {:id "zip-input" :name "zip" :type "hidden"}]
                  [:input {:id "coords-input" :name "coords" :type "hidden"}]
                  [:div.input-group
                    [:input {:id "address-input" :name "addr" :type "text" :placeholder "123 Main St" :class "input-group-field"}]
                    [:div.input-group-button
                      [:input {:type "submit" :value "Search" :class "button submit-btn"}]]]]]]))

(defn results-template
  [results]
  (hc/html (for [result results]
              (let [{:keys [id source link cuisines rating cost-range
                            delivery-time delivery-fee delivery-min]} result]
                [:div.column.merchant {:id (str (name (result :name)) "-" id)}
                  [:a.merchant-link {:href link}
                    [:div.merchant-info.column.name (result :name)]
                    [:div.merchant-info.column (clojure.string/join " " (map #(or (emoji/match %) %) cuisines))]
                    [:div.merchant-info.column (when  delivery-time (str delivery-time "🕑"))]
                    [:div.merchant-info.column (repeat (or cost-range 0) "💰")]]]))))

(defn page-template
  [body]
  (hc/html [:head
            [:title "Foodwich"]
            [:meta {:charset "UTF-8"}]
            [:link {:rel "stylesheet" :href "https://cdnjs.cloudflare.com/ajax/libs/foundation/6.2.3/foundation.min.css"}]
            [:link {:rel "stylesheet" :href "/css/app.css"}]
            [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.2/jquery.min.js"}]
            [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/foundation/6.2.3/foundation.min.js"}]
            [:script {:src "/js/app.js"}]
            [:script {:src "/js/autocomplete.js"}]]
           [:body
            [:div.callout
              [:div.row.column
                [:div.header-and-logo
                  [:img {:src "/images/logo.png" :id "logo"}]
                  [:h1 {:style "display:inline"}"Foodw.ch"]]]]
            [:div.search.row
              (search-template)]
            [:div#results.row.small-up-2.medium-up-3.large-up-4
              body]
            [:div.callout
              [:div.row
                [:div.large-6.columns "Site: <a href='http://baddadjok.es'>Trinity Montoya</a>"]
                [:div.large-6.columns "Logo: Fast Food by <a href='https://thenounproject.com/hugugolplex/'>Hugo Alberto</a> via <a href='https://thenounproject.com/'>the Noun Project</a>"]]]
            [:script {:src (str "https://maps.googleapis.com/maps/api/js?key=" (env :google-api-key) "&libraries=places&callback=initAutocomplete")}]]))
