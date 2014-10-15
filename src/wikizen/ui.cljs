(ns wikizen.ui
  (:require [wikizen.template-engine :as te]
            [goog.dom :as dom]
            [goog.events :as events]))

(defn- create-link
  [text event params]
  [:a {:href "#"
       :data-event event
       :data-params (.stringify js/JSON (clj->js params))} text])

(defn
  edit-page
  "Generates a page with a text area and a preview for
  editing and creation of pages"
  [location mode]
  (te/template->dom
    [:input#title.full-width.input-fields {:type "text"
                                           :style {:font-weight "bold"}
                                           :placeholder "Page name"}]
    [:textarea#body.full-width.input-fields {:rows 30}]
    [:br]
    [:a {:href "#" :onclick (fn [e] (js/alert "hi there"))} "save"]))

(defn
  page
  "generates a wiki page; dalocatioddn is an index vector of the current page,
  title-path is a vector of [location title] pairs till the current page,
  wiki is the node of current wiki"
  [event-sender location title-path wiki]
  (te/template->dom
    [:div#headbar {:style {:display "flex" 
                           ;:display "-webkit-flex" TODO
                           }}
     [:code {:style {:flex "2 1 0" 
                     ; TODO: the camilization will break here?
                     :-webkit-flex "2 1 0"}}
      (interpose " / "
                 (conj
                   (into [] (map
                              (fn [[location title]] 
                                (vector :a {:href "#"
                                            :onclick #(event-sender {:id :load-page
                                                                     :location location})} 
                                        title))
                              (butlast title-path)))
                   (second (last title-path))))]
     [:code (interpose " &middot; "
                       (map #(create-link % (str % "-page")
                                          {:location location
                                           :mode %})
                            ["new" "edit" "delete"]))]]
    [:h1 (wiki :title)]
    [:article#markdown (.marked js/window (wiki :body))]
    (when-let [children (wiki :children)]
      [:div
       [:hr]
       [:h3 "Nested Pages"]
       [:ul
        (map
          (fn [[i child]]
            (vector :li
                    [:a {:href "#"
                         :onclick #(event-sender {:id :load-page
                                                  :location (concat location [i])})}
                     (child :title)]))
          (map list (range) children))]])))
