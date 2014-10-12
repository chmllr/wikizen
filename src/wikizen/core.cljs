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

(def app (dom/getElement "app"))

(def root ((storage/get-wiki "fake-id") :root))

(def C (chan))

(defn load-page
  "Opens the specified page"
  [location]
  (let [location (if (= "" location)
                   []
                   (map js/parseInt
                        (.split location ",")))]
    (aset app "innerHTML"
          (ui/wiki-page
            location
            (engine/get-path root location)
            (engine/get-node root location))))
  (let [node-list (.getElementsByTagName js/document "a")]
    (doseq [i (range 0 (.-length node-list))]
      (let [node (aget node-list i)
            name (.getAttribute node "data-event")
            params (.getAttribute node "data-params")]
        (events/listen node "click"
                       (fn [e]
                         (put! C {:name name :params params})))))))

(go (while true
      (let [{:keys [name params]} (<! C)
            mapping {"load-page" load-page}
            f (mapping name #(println "no handler for event" name "found"))]
        (println "event" name "received with args:" params)
        (f params))))

(put! C {:name "load-page" :params ""})

(tests/run-tests)
