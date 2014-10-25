(ns wikizen.ui
  (:require-macros [wikizen.macros :refer [link]])
  (:require [wikizen.template-engine :as te]
            [goog.dom :as dom]
            [goog.events :as events]))

(defn
  edit-page
  "Generates a page with a text area and a preview for
  editing and creation of pages"
  [channel ref mode wiki]
  (te/template->dom
    [:input#title.full-width.input-fields
     {:type        "text"
      :value       (when (= mode :edit-page) (wiki :title))
      :style       {:font-weight "bold"}
      :autofocus   "autofocus"
      :placeholder "Page name"}]
    [:textarea#body.full-width.input-fields
     {:rows    20
      :onkeyup (fn [_]
                 (aset (dom/getElement "markdown")
                       "innerHTML"
                       (js/marked (aget (dom/getElement "body")
                                        "value"))))}
     (when (= mode :edit-page) (wiki :body))]
    [:br]
    (link "save" channel {:ref   ref
                          :id    mode
                          :title (.-value (dom/getElement "title"))
                          :body  (.-value (dom/getElement "body"))})
    "&nbsp;"
    (link "cancel" channel {:ref (butlast ref) :id :show-page})
    [:hr]
    [:article#markdown]))

(defn- menu
  "Renders the links and menu at the right"
  [channel title-path ref root name]
  [:div#headbar {:style {:display "flex"
                         ;:display "-webkit-flex" (TODO)
                         }}
   [:code {:style {:flex         "2 1 0"
                   ; TODO: the camilization will break here?
                   :-webkit-flex "2 1 0"}}
    name ": "                                               ; TODO: set root title to empty and no return to this page is ever possible
    (interpose " / "
               (concat
                 (map
                   (fn [[i title]]
                     (link title channel {:id :show-page :ref (take i ref)}))
                   (map list (range) (butlast title-path)))
                 [(last title-path)]))]
   [:code
    (interpose " &middot; "
               (remove nil?
                       [(link "new" channel {:id  :show-edit-mask :mode :add-page
                                             :ref (conj ref (count (root :children)))})
                        (link "edit" channel {:id :show-edit-mask :mode :edit-page :ref ref})
                        (when-not (empty? ref)
                          (link "delete" channel {:id :delete-page :ref ref}))
                        (link "search" channel {:id :enable-search})]))]])

(defn
  page
  "Generates a wiki page"
  [channel ref title-path root name]
  (te/template->dom
    (menu channel title-path ref root name)
    [:article#markdown (js/marked (or (root :body) ""))]
    (when-let [children (root :children)]
      [:div
       [:hr]
       [:h3 "Nested Pages"]
       [:ol
        (map
          (fn [[i child]]
            (vector :li (link (child :title) channel {:id :show-page :ref (concat ref [i])})))
          (map list (range) children))]])))
