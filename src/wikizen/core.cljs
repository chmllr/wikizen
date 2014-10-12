(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [wikizen.tests :as tests] 
    [wikizen.storage :as storage]
    [wikizen.ui :as ui]
    [wikizen.engine :as engine]))

(enable-console-print!)

(def app (dom/getElement "app"))

(def root ((storage/get-wiki "fake-id") :root))

(defn open-page
  "Opens the specified page"
  [& location]
  (aset app "innerHTML"
        (ui/wiki-page
          location
          (engine/get-path root location)
          (engine/get-node root location))))

(defn send-event
  "Send generic event to the event bus"
  [event-name & args]
  (apply println "sending event" event-name "with args:" args)
  (let [mapping {"open-page" open-page}
        f (mapping event-name #(println "event" event-name "is unknown"))]
    (apply f args)))

(send-event "open-page")

(tests/run-tests)
