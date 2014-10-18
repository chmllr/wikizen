(ns wikizen.ui
  (:require [wikizen.template-engine :as te]
            [goog.dom :as dom]
            [goog.events :as events]))

(defn
  edit-page
  "Generates a page with a text area and a preview for
  editing and creation of pages"
  [event-processor ref mode]
  (te/template->dom
    [:input#title.full-width.input-fields
     {:type "text"
      :style {:font-weight "bold"}
      :placeholder "Page name"}]
    [:textarea#body.full-width.input-fields
     {:rows 30}]
    [:br]
    [:a {:href "#"
         :onclick
               (fn [e]
                 (event-processor
                   {:ref ref
                    :id mode
                    :title (.-value (dom/getElement "title"))
                    :body (.-value (dom/getElement "body"))}))}
     "save"]))

(defn
  page
  "Generates a wiki page; event-processor is a function receiveing events;
  ref is an index vector of the current page,
  title-path is a vector of [ref title] pairs till the current page,
  wiki is the node of current wiki"
  [event-processor ref title-path wiki]
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
                              (fn [[ref title]]
                                (vector :a {:href "#"
                                            :onclick #(event-processor
                                                       {:id :show-page
                                                        :ref ref})}
                                        title))
                              (butlast title-path)))
                   (second (last title-path))))]
     [:code
      [:a {:href "#"
           :onclick #(event-processor
                      {:id :show-edit-mask
                       :mode :add-page
                       :ref (conj ref (count (wiki :children)))})}
       "new"]
      " &middot; "
      "edit"
      " &middot; "
      "delete"

      ]]
    [:h1 (wiki :title)]
    [:article#markdown (js/marked (wiki :body))]
    (when-let [children (wiki :children)]
      [:div
       [:hr]
       [:h3 "Nested Pages"]
       [:ul
        (map
          (fn [[i child]]
            (vector :li
                    [:a {:href "#"
                         :onclick #(event-processor
                                    {:id :show-page
                                     :ref (vec (concat ref [i]))})}
                     (child :title)]))
          (map list (range) children))]])))
