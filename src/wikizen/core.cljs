(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [wikizen.tests :as tests] 
    [wikizen.storage :as storage]
    [wikizen.ui :as ui]
    [wikizen.engine :as engine]
    [cljs.core.async :refer [put! chan <!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def app (dom/getElement "app"))

(def root ((storage/get-wiki "fake-id") :root))

(def C (chan))

(defn open-page
  "Opens the specified page"
  [location]
  (aset app "innerHTML"
        (ui/wiki-page
          location
          (engine/get-path root location)
          (engine/get-node root location))))

(go (while true
      (let [{:keys [name params]} (<! C)
            mapping {"open-page" open-page}
            f (mapping name #(println "unknonwn event" name "received"))]
        (println "event" name "received with args:" params)
        (apply f params))))

(put! C {:name "open-page" :params []})

(tests/run-tests)
