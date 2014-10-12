(ns wikizen.ui
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [goog.dom :as dom]))

(defn- event-sender
  [text event-name event-params]
  [:a
   {:href
     (str "javascript:wikizen.core.send_event('" event-name "',"
             (clj->js event-params) ")")} text])

(hiccups/defhtml
  wiki-page [path wiki]
  [:code
   (interpose " / "
              (map #(event-sender (second %) :open-page (first %)) path))]
  [:h1 (wiki :title)]
  [:article (wiki :body)]
  (when-let [children (wiki :children)]
    [:div
     [:hr]
    [:h4 "Nested Pages"]
    [:ul
      (map
        #(vector :li (event-sender (% :title) :open-page "ka"))
        children)]]))