(ns wikizen.ui
  (:require [wikizen.template-engine :as te]
            [goog.dom :as dom]
            [goog.events :as events]))

(defn
  edit-page
  "Generates a page with a text area and a preview for
  editing and creation of pages"
  [event-processor ref mode wiki]
  (te/template->dom
    [:input#title.full-width.input-fields
     {:type "text"
      :value (when (= mode :edit-page) (wiki :title))
      :style {:font-weight "bold"}
      :placeholder "Page name"}]
    [:textarea#body.full-width.input-fields
     {:rows 20 
      :onkeyup (fn [_]
                    (aset (dom/getElement "markdown") 
                          "innerHTML" 
                          (js/marked (aget (dom/getElement "body") 
                                           "value"))))}
     (when (= mode :edit-page) (wiki :body))]
    [:br]
    [:a {:href "#"
         :onclick
         (fn [e]
           (event-processor
             {:ref ref
              :id mode
              :title (.-value (dom/getElement "title"))
              :body (.-value (dom/getElement "body"))}))}
     "save"]
    "&nbsp;"
    [:a#cancel-link {:href "#"
         :onclick
         (fn [e]
           (event-processor
             {:ref (butlast ref)
              :id :show-page }))}
     "cancel"]
    [:hr]
    [:article#markdown]))

(defn
  page
  "Generates a wiki page"
  [event-processor ref title-path root name]
  (te/template->dom
    [:div#headbar {:style {:display "flex"
                           ;:display "-webkit-flex" (TODO)
                           }}
     [:code {:style {:flex "2 1 0"
                     ; TODO: the camilization will break here?
                     :-webkit-flex "2 1 0"}}
      name ": " ; TODO: set root title to empty and no return to this page is ever possible
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
      [:a#new-page-link 
       {:href "#"
        :onclick #(event-processor
                    {:id :show-edit-mask
                     :mode :add-page
                     :ref (conj ref (count (root :children)))})} "new"]
      " &middot; "
      [:a#edit-page-link 
       {:href "#"
        :onclick #(event-processor
                    {:id :show-edit-mask
                     :mode :edit-page
                     :ref ref})} "edit"]
      " &middot; "
      ; TODO: hide for root page
      [:a#delete-page-link 
       {:href "#"
        :onclick #(event-processor
                    {:id :delete-page
                     :ref ref})} "delete"]]]
    [:h1 (root :title)]
    [:article#markdown (js/marked (or (root :body) ""))]
    (when-let [children (root :children)]
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
