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

(aset app "innerHTML" (ui/wiki-page))