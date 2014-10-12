(ns wikizen.ui
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [goog.dom :as dom]))

(defn- event-sender
  [text event-name event-params]
  [:a
   {:href "#"
    :onclick
          (str "javascript:wikizen.core.send_event('" event-name "',"
               (clj->js event-params) ")")} text])

(hiccups/defhtml
  wiki-page
  "Generates a wiki page; location is an index vector of the current page,
  title-path is a vactor pf [index title] pairs till the current page,
  wiki is the node of current wiki"
  [location title-path wiki]
  [:code
   (interpose " / "
              (map
                #(event-sender
                  (second %)
                  "open-page"
                  (first %)) title-path))]
  [:h1 (wiki :title)]
  [:article (wiki :body)]
  (when-let [children (wiki :children)]
    [:div
     [:hr]
     [:h4 "Nested Pages"]
     [:ul
      (map
        #(let [[i child] %]
          (vector :li
                  (event-sender (child :title)
                                "open-page"
                                (concat location [i]))))
        (map list (range) children))]]))