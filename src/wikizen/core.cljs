(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [goog.events :as events]
    [wikizen.storage :as storage]
    [wikizen.log :as log]
    [wikizen.ui :as ui]
    [wikizen.engine :as engine]))

(enable-console-print!)

(def wiki-id (storage/create-wiki "dev-wiki" "Test Wiki"))

(def current-page (atom nil))

(def key->link-id
  (reduce
    (fn [m i] (assoc m (+ 48 i)
                       {:id :show-page
                        :compute-ref (fn [page] (conj (page :ref) (dec i)))}))
    {27 {:id :show-page :compute-ref (fn [page] (drop-last (page :ref)))}  ; cancel
     37 {:id :show-page :compute-ref (fn [page] (drop-last (page :ref)))}  ; back
     68 {:id :delete-page}                                  ; delete
     69 {:mode :edit-page :id :show-edit-mask}              ; edit
     78 {:mode :add-page                                    ; new
         :id :show-edit-mask
         :compute-ref (fn [{:keys [ref page]}]
                        (conj ref
                              (count (page :children))))}}
    (range 1 10)) )

(defn display-ui
  "Puts the specified DOM element into the main container"
  [fragment]
  (log/! "setting a new ui")
  (let [app (dom/getElement "app")]
    (aset app "innerHTML" "")
    (.appendChild app fragment)))

(defn show-page
  "Opens the specified page"
  [_ {:keys [name root]} event-processor {:keys [ref]}]
  (log/! "show-page called with params:" :name name :ref ref)
  (let [page (engine/get-node root ref)]
    (reset! current-page {:page page
                          :ref ref
                          :shortcuts (into #{37 68 69 78}
                                           (range 49 (+ 49 (count (page :children)))))})
    (display-ui
      (ui/page event-processor
               ref
               (engine/get-path root ref)
               page
               name))))

(defn show-edit-mask
  "Opens the editing mask"
  [_ {:keys [root]} event-processor {:keys [ref mode]}]
  (log/! "show-edit-mask called with params:" :ref ref :mode mode)
  (reset! current-page {:ref ref :shortcuts #{27}})
  (display-ui
    (ui/edit-page event-processor ref mode
                  (engine/get-node root ref))))

(defn save-page
  "Sends the received contents to the storage"
  [wiki-id _ event-processor {:keys [ref title body]}]
  (log/! "save-page called with params:" :wiki-id wiki-id :ref ref :title title :body body)
  (storage/update-page wiki-id ref title body)
  (event-processor {:id :show-page :ref ref}))

(defn delete-page
  "Deletes the page if it is not the root page"
  [wiki-id _ event-processor {:keys [ref]}]
  (log/! "delete-page called with params:" :wiki-id wiki-id :ref ref)
  (if (empty? ref)
    (js/alert "Root page cannot be deleted.")
    (when (js/confirm "Do you really want to delete this page?")
      (do
        (storage/delete-page wiki-id ref)
        (event-processor {:id :show-page :ref (butlast ref)})))))

(def event->fn
  {:show-page show-page
   :show-edit-mask show-edit-mask
   :delete-page delete-page
   :add-page save-page
   :edit-page save-page})

; TODO: add eventing unit tests
(defn event-processor
  "Event processor; all events are blocking"
  [event]
  (log/! "event received:" event)
  (let [{:keys [id]} event
        f (event->fn id #(log/error "no handler for event" id "found"))
        wiki (storage/get-wiki wiki-id)]
    (log/! "apply event handler for" id)
    (try
      (f wiki-id wiki event-processor event)
      (catch :default e
        (log/error "Error during execution of event" id ":" (.-message e))))))

(defn bootstrap
  "Starts the app"
  []
  (log/! "bootstrapping the app...")
  (events/listen (dom/getWindow)
                 "keydown"
                 (fn [e]
                   (let [code (.-keyCode e)]
                     (log/! "keydown event send with keycode" code)
                     (when (contains? (@current-page :shortcuts) code)
                       (when-let [event (key->link-id code)]
                         (let [f (event :compute-ref #(% :ref))
                               event (assoc (select-keys event [:id :mode])
                                       :ref (f @current-page))]
                           (event-processor event)))))))
  (event-processor {:id :show-page :ref []}))

;(log/enable-log)