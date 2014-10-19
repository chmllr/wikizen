(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [goog.events :as events]
    [wikizen.storage :as storage]
    [wikizen.ui :as ui]
    [wikizen.engine :as engine]))

(enable-console-print!)

(def wiki-id (storage/create-wiki "Test Wiki"
                                  { :title "Root Page"
                                    :body "This is the *page body* of a fake wiki. And __this__ is `code`."
                                    :children [ { :title "Nested Page 1"
                                                  :body "The __content__ of _nested_ page 1"
                                                  :children [ { :title "Nested Page 1_1"
                                                                :body "This _is_ a leaf" } ] }
                                                { :title "Nested Page 2"
                                                  :body "The __content__ of _nested_ page 2"
                                                  :children [ { :title "Nested Page 2_1"
                                                                :body "This _is_ a leaf" } ]} ] }))
(defn display-ui
  "Puts the specified DOM element into the main container"
  [fragment]
  (let [app (dom/getElement "app")]
    (aset app "innerHTML" "")
    (.appendChild app fragment)))

(defn show-page
  "Opens the specified page"
  [wiki event-processor {:keys [ref]}]
  (display-ui
    (ui/page event-processor
             ref
             (engine/get-path wiki ref)
             (engine/get-node wiki ref))))

(defn show-edit-mask
  "Opens the editing mask"
  [_ event-processor {:keys [ref mode]}]
  (display-ui
    (ui/edit-page event-processor ref mode)))

(defn add-page
  "Sends the received contents to the storage"
  [_ event-processor {:keys [ref title body]}]
  (storage/update-wiki wiki-id ref title body)
  (event-processor {:id :show-page :ref ref}))

(defn event-processor
  "Event processor; all events are blocking"
  [event]
  (println "event received:" event)
  (let [{:keys [id]} event
        mapping {:show-page show-page
                 :show-edit-mask show-edit-mask
                 :add-page add-page}
        f (mapping id #(println "no handler for event" id "found"))
        wiki ((storage/get-wiki wiki-id) :root)]
    (f wiki event-processor event)))

(defn bootstrap
  "Starts the app"
  []
  (event-processor {:id :show-page :ref []}))

(defn inspect-deltas []
  (println (.stringify js/JSON (clj->js @storage/sample-deltas))))

