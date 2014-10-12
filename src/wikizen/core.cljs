(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [wikizen.ui :as ui]))

(enable-console-print!)

(def app (dom/getElement "app"))

; add CSS styling
(let [style-element (dom/createElement "style")]
  (aset style-element "type" "text/css")
  (aset style-element "innerHTML" ui/css-code)
  (dom/appendChild
    (.-head js/document)
    style-element))

(def sample-wiki
  { :name SampleWiki
    :root { :title "Root Page"
            :body "This is the *page body*."
            :children [ { :title "Nested Page 1"
                        :body "The __content__ of _nested_ page 1" }
                      { :title "Nested Page 2"
                        :body "The __content__ of _nested_ page 2" } ] } })

(aset app "innerHTML"
      (ui/wiki-page (sample-wiki :root)))