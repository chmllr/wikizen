(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [goog.events :as events]
    [wikizen.storage :as storage]
    [wikizen.ui :as ui]
    [wikizen.engine :as engine]))

(enable-console-print!)

(defn display-ui
  "Puts the specified DOM element into the main container"
  [fragment]
  (let [app (dom/getElement "app")]
    (aset app "innerHTML" "")
    (.appendChild app fragment)))

(def root ((storage/get-wiki "fake-id") :root))

(defn load-page
  "Opens the specified page"
  [event-processor {:keys [location]}]
  (display-ui 
    (ui/page event-processor
             location
             (engine/get-path root location)
             (engine/get-node root location))))

(defn edit-page
  "Opens the editing mask"
  [event-processor {:keys [location mode]}]
  (display-ui
        (ui/edit-page event-processor location mode)))

(defn event-processor
  "Event processor; all events are blocking"
  [event]
  (let [{:keys [id]} event
        mapping {:load-page load-page
                 :new-page edit-page
                 :edit-page edit-page}
        f (mapping id #(println "no handler for event" id "found"))]
    (println "event received:" event)
    (f event-processor event)))

(defn bootstrap
  "Starts the app"
  []
  (event-processor {:id :load-page :location []}))

