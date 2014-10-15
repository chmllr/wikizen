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

(defn- register-listeners
  "Registers listener on all found links"
  []
  (let [node-list (.getElementsByTagName js/document "a")]
    (doseq [i (range 0 (.-length node-list))]
      (let [node (aget node-list i)
            name (.getAttribute node "data-event")
            params (.getAttribute node "data-params")]
        (events/listen node "click"
                       (fn [e]
                         (put! C {:name name :params params})))))))

(defn load-page
  "Opens the specified page"
  [location]
  (aset (dom/getElement "app") "innerHTML"
        (ui/page
          location
          (engine/get-path root location)
          (engine/get-node root location)))
  (register-listeners))

(defn edit-page
  "Opens the editing mask"
  [location mode]
  (display-ui
        (ui/edit-page #(put! C %) location mode)))

(go (while true
      (let [event (<! C)
            mapping {"load-page" load-page
                     "new-page" edit-page}
            ;f (mapping event #(println "no handler for event" name "found"))
            ]
        (println "event received:" event)
        #_(f params))))

;(put! C {:name "load-page" :params "[]"})

(edit-page [] "add")

(tests/run-tests)
