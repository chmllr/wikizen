(ns wikizen.ui
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [goog.dom :as dom]
            [goog.events :as events]))

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
                           #(vector :a {:href (str "/open-page?location=" (first %))}
                                    (second %)) (butlast title-path)))
                (second (last title-path))))]
   [:code (interpose " &middot; "
                                     (map #(vector :a {:href (str "/" % "-page?location=" location)} %)
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
                  [:a {:href (str "/open-page?location="
                                  (concat location [i]))}
                   (child :title)]))
        (map list (range) children))]]))
