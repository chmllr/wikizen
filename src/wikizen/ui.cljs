(ns wikizen.ui
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [goog.dom :as dom]
            [goog.events :as events]))

(defn- create-link
  [text event params]
  [:a {:href "#"
       :data-event event
       :data-params (clj->js params)} text])

(hiccups/defhtml
  wiki-page
  "Generates a wiki page; location is an index vector of the current page,
  title-path is a vector of [index title] pairs till the current page,
  wiki is the node of current wiki"
  [location title-path wiki]
  [:div {:style "display: flex; display: -webkit-flex;"}
   [:code {:style "flex: 2 1 0; -webkit-flex: 2 1 0;"}
    (interpose " / "
               (conj
                 (into [] (map
                            #(create-link (second %)
                                          "load-page"
                                          (first %))
                            (butlast title-path)))
                 (second (last title-path))))]
   [:code (interpose " &middot; "
                     (map #(vector :a {:href (str "/" % "-page" location)} %)
                          ["new" "edit" "delete"]))]]
  [:h1 (wiki :title)]
  [:article (.marked js/window (wiki :body))]
  (when-let [children (wiki :children)]
    [:div
     [:hr]
     [:h3 "Nested Pages"]
     [:ul
      (map
        #(let [[i child] %]
          (vector :li
                  (create-link (child :title)
                               "load-page"
                               (concat location [i]))))
        (map list (range) children))]]))
