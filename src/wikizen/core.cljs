(ns wikizen.core
  (:require
    [goog.dom :as dom]
    [goog.style :as style]
    [goog.events :as events]
    [wikizen.storage :as storage]
    [wikizen.log :as log]
    [wikizen.ui :as ui]
    [wikizen.engine :as engine]))

(enable-console-print!)

(def wiki-id
  (let [id (clojure.string/replace (.-search js/location) #"\?" "")
        response (storage/load id)]
    (when-not response
      (storage/create-wiki id id))
    id))

; holds the relevant information for the currently displayed UI
(def current-ui (atom nil))

; establish some references to DOM elements
(def app (dom/getElement "app"))
(def overlay (dom/getElement "overlay"))
(def modal (dom/getElement "modal"))

; maps keycode to the corresponding event;
; some events contain a function computing the necessary ref
; from the ref of the page the key code was issued
(def key->link-id
  (reduce
    (fn [m i] (assoc m (+ 48 i)
                       {:id          :show-page
                        :compute-ref (fn [page] (conj (page :ref) (dec i)))}))
    {37 {:id :show-page :compute-ref (fn [page] (drop-last (page :ref)))} ; back
     27 {:id :show-page :compute-ref (fn [page] ((if (= :edit-page (@current-ui :mode))
                                                   identity
                                                   drop-last) (page :ref)))} ; cancel
     68 {:id :delete-page}                                  ; delete
     69 {:mode :edit-page :id :show-edit-mask}              ; edit
     78 {:mode        :add-page                             ; new
         :id          :show-edit-mask
         :compute-ref (fn [{:keys [ref page]}]
                        (conj ref
                              (count (page :children))))}}
    (range 1 10)))

(defn display-in
  "Puts the specified DOM element into the main container"
  [id fragment]
  (log/! "setting a new ui into" id)
  (let [element ({:app app :modal modal} id)]
    (aset element "innerHTML" "")
    (.appendChild element fragment)
    (channel {:id (if (= :modal id)
                    :show-modal
                    :close-modal)})))

(defn show-page
  "Opens the page under the specified ref"
  [_ {:keys [name root]} channel {:keys [ref]}]
  (log/! "show-page called with params:" :name name :ref ref)
  (let [page (engine/get-node root ref)]
    (reset! current-ui {:page      page
                        :ref       ref
                        :shortcuts (into #{37 68 69 78}
                                         (range 49 (+ 49 (count (page :children)))))})
    (display-in :app
                (ui/page channel
                         ref
                         (engine/get-path root ref)
                         page
                         name))))

(defn show-edit-mask
  "Opens the UI with the editing mask"
  [_ {:keys [root]} channel {:keys [ref mode]}]
  (log/! "show-edit-mask called with params:" :ref ref :mode mode)
  (reset! current-ui {:ref ref :mode mode :shortcuts #{27}})
  (display-in :app
              (ui/edit-page channel ref mode
                            (engine/get-node root ref))))

(defn save-page
  "Sends the received contents to the storage and opens the new page;
  storaging is intended to be synchronous!"
  [wiki-id _ channel {:keys [ref title body]}]
  (log/! "save-page called with params:" :wiki-id wiki-id :ref ref :title title :body body)
  (storage/update-page wiki-id ref title body)
  (channel {:id :show-page :ref ref}))

(defn delete-page
  "Deletes the page if it is not the root page"
  [wiki-id _ channel {:keys [ref]}]
  (log/! "delete-page called with params:" :wiki-id wiki-id :ref ref)
  (if (empty? ref)
    (js/alert "Root page cannot be deleted.")
    (when (js/confirm "Do you really want to delete this page?")
      (do
        (storage/delete-page wiki-id ref)
        (channel {:id :show-page :ref (butlast ref)})))))

(defn enable-search
  "Shows the search mask in a modal window"
  [_ _ channel _]
  (log/! "enable-search called with params")
  (display-in :modal (ui/search-mask channel))
  (channel {:id :show-modal}))

(defn search
  "Adds search results to the modal dialog"
  [_ wiki channel {:keys [terms]}]
  (when (< 3 (count terms))
    (let [results (engine/search
                    (wiki :root)
                    (clojure.string/split terms #"\s+"))
          dom-elem (dom/getElement "search-results")]
      (println :DEBUG results)
      (aset dom-elem "innerHTML" "")
      (.appendChild dom-elem
                    (ui/search-results channel results)))))

; event ID -> handler mapping
(def event->fn
  {:show-page      show-page
   :show-edit-mask show-edit-mask
   :delete-page    delete-page
   :add-page       save-page
   :edit-page     save-page
   :enable-search enable-search
   :search     search
   :show-modal #(style/setStyle overlay "display" "block")
   :close-modal   #(style/setStyle overlay "display" "none")})

; TODO: add eventing unit tests
(defn channel
  "Event processor; all events are blocking!"
  [event]
  (log/! "event received:" event)
  (let [{:keys [id]} event
        f (event->fn id #(log/error "no handler for event" id "found"))
        wiki (storage/get-wiki wiki-id)]
    (log/! "applying event handler for" id)
    (try
      (f wiki-id wiki channel (update-in event [:ref] vec))
      (catch :default e
        (log/error "Error during execution of event" id ":" (.-message e))))))

(defn bootstrap
  "Starts the app, sets shortcut listener, open the main page"
  []
  (log/! "bootstrapping the app...")
  (events/listen (dom/getWindow)
                 "keydown"
                 (fn [e]
                   (let [code (.-keyCode e)]
                     (log/! "keydown event send with keycode" code)
                     (when (and (not= "block" (style/getStyle overlay "display"))
                                (contains? (@current-ui :shortcuts) code))
                       (when-let [event (key->link-id code)]
                         (let [f (event :compute-ref #(% :ref))
                               event (assoc (select-keys event [:id :mode])
                                       :ref (f @current-ui))]
                           (channel event)))))))
  (channel {:id :show-page :ref []}))

;(log/enable-log)