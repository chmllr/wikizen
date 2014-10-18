(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [goog.events :as events]
    [wikizen.storage :as storage]
    [wikizen.ui :as ui]
    [wikizen.engine :as engine]))

(enable-console-print!)

(defn process-event
  "Event processor; all events are blocking"
  [event]
  (let [{:keys [id]} event
        mapping {:load-page load-page
                 :new-page edit-page
                 :edit-page edit-page}
        f (mapping id #(println "no handler for event" id "found"))]
    (println "event received:" event)
    (f event)))

(defn display-ui
  "Puts the specified DOM element into the main container"
  [fragment]
  (let [app (dom/getElement "app")]
    (aset app "innerHTML" "")
    (.appendChild app fragment)))

(def root ((storage/get-wiki "fake-id") :root))

(defn load-page
  "Opens the specified page"
  [{:keys [location]}]
  (display-ui 
    (ui/page process-event
             location
             (engine/get-path root location)
             (engine/get-node root location))))

(defn edit-page
  "Opens the editing mask"
  [{:keys [location mode]}]
  (display-ui
        (ui/edit-page process-event location mode)))

(defn bootstrap
  "Starts the app"
  []
  (put! C {:id :load-page :location []}))

