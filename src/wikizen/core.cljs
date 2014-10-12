(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [wikizen.tests :as tests] 
    [wikizen.ui :as ui]
    [wikizen.engine :as engine]))

(enable-console-print!)

(def app (dom/getElement "app"))

(def sample-wiki
  { :name "SampleWiki"
    :root { :title "Root Page"
            :body "This is the *page body*."
            :children [ { :title "Nested Page 1"
                          :body "The __content__ of _nested_ page 1"
                          :children [ { :title "Nested Page 1_1"
                                        :body "This _is_ a leaf" } ] }
                        { :title "Nested Page 2"
                          :body "The __content__ of _nested_ page 2"
                          :children [ { :title "Nested Page 2_1"
                                        :body "This _is_ a leaf" } ]} ] } })

(def root (sample-wiki :root))

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
