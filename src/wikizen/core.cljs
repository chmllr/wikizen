(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [goog.events :as events]
    [wikizen.tests :as tests]
    [wikizen.storage :as storage]
    [wikizen.ui :as ui]
    [wikizen.engine :as engine]
    [cljs.core.async :refer [put! chan <!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(defn display-ui
  "Puts the specified DOM element into the main container"
  [fragment]
  (let [app (dom/getElement "app")]
    (aset app "innerHTML" "")
    (.appendChild app fragment)))

(def root ((storage/get-wiki "fake-id") :root))

(def C (chan))

(defn load-page
  "Opens the specified page"
  [{:keys [location]}]
  (display-ui 
    (ui/page #(put! C %)
             location
             (engine/get-path root location)
             (engine/get-node root location))))

(defn edit-page
  "Opens the editing mask"
  [{:keys [location mode]}]
  (display-ui
        (ui/edit-page #(put! C %) location mode)))

(go (while true
      (let [{:keys [id] :as event} (<! C)
            mapping {:load-page load-page
                     :new-page edit-page
                     :edit-page edit-page}
            f (mapping id #(println "no handler for event" id "found"))]
        (println "event received:" event)
        (f event))))

(put! C {:id :load-page :location []})

(tests/run-tests)
