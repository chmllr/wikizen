(ns wikizen.ui
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [goog.dom :as dom]
            [garden.core :refer [css]]))

(defonce css-code
         (css
           [:body
            { :font-family "\"Palatino Linotype\", \"Book Antiqua\", Palatino, serif, Georgia"
             :font-size "1.1em" }]
           [:a:link { :color "#0a0" }]
           [:a:visited { :color "#070" }]
           [:a:hover { :color "#000" }]))

(hiccups/defhtml
  wiki-page [wiki]
  [:h1 (wiki :title)]
  [:article (wiki :body)]
  (when-let [children (wiki :children)]
    [:div
     [:hr]
    [:h4 "Nested Pages"]
    [:ul
      (map
        #(vector :li [:a
                     {:href ""}
                      (% :title)])
        children)]]))
