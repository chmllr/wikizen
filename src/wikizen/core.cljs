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

(aset app "innerHTML"
      (let [root (sample-wiki :root)]
        (ui/wiki-page
          (engine/get-path root [1 0])
          root)))

(defn send-event
  "Send generic event to the event bus"
  [event-name & args]
  (apply println "sending event" event-name "with args:" args))

(println ui/css-code)

(tests/run-tests)
