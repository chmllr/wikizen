(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [wikizen.ui :as ui]))

(enable-console-print!)

(def app (dom/getElement "app"))
(aset app "innerHTML" (ui/wiki-page))