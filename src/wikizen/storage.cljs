(ns wikizen.storage
  (:require
    [wikizen.engine :as engine]
    [wikizen.log :as log]))

(enable-console-print!)

(def default-root
  {:title "&#9775; WikiZen"
   :body (str "> _\"One machine can do the work of **fifty** ordinary men.  \n"
              "> No machine can do the work of **one** extraordinary man.\"_  \n"
              "> — Elbert Hubbard\n\n"
              "This is default **root page** of your Wiki powered by [WikiZen](https://github.com/chmllr/wikizen).\n")})

(def dao (atom {}))

; to avoid delta applications if no updates happened
(def cache (atom {}))

(defn- load
  [id]
  (log/! "load called for wiki" id)
  (when-not (.-testMode js/window)
      (let [string (.getItem js/localStorage id)
            data-structure (js->clj (.parse js/JSON string) :keywordize-keys true)]
        (swap! dao assoc id data-structure))))

(defn- save
  [id]
  (log/! "save called for wiki" id)
  (swap! cache hash-map)
  (when-not (.-testMode js/window)
    (let [string (.stringify js/JSON (clj->js (@dao id)))]
      (.setItem js/localStorage id string))))

(defn create-wiki
  "Stores a new wiki"
  ([id name] (create-wiki id name default-root))
  ([id name root]
   (log/! "create-wiki called for wiki" id)
   (let [obj {:deltas []
              :wiki {:name name :root root}}] 
     (load id)
     (if (@dao id)
       (println "Wiki" id "exists already.")
       (do
         (swap! dao assoc id obj)
         (save id)))
     id)))

(defn- store-deltas
  "Saves deltas"
  [id deltas]
  (log/! "store-deltas called for wiki" id)
  (swap! dao (fn [storage deltas]
               (update-in storage
                          [id :deltas]
                          concat deltas)) deltas)
  (save id))

(defn get-wiki
  "Returns the Wiki object"
  [id]
  (log/! "get-wiki called for wiki" id)
  (if (@cache id) 
    (@cache id) 
    (let [obj (@dao id)
          wiki (obj :wiki)
          deltas (obj :deltas)
          assembled-wiki (assoc wiki :root (reduce engine/apply-delta (wiki :root) deltas))]
      (swap! cache assoc id assembled-wiki)
      assembled-wiki)))

(defn update-page
  "Applies the passed deltas"
  [id ref title body]
  (log/! "update-page called for wiki" id "with params" :ref ref :title title :body body)
  (let [wiki-root ((get-wiki id) :root)
        page (or (engine/get-node wiki-root ref) {})
        deltas (if (= title (page :title))
                 []
                 [{:ref ref, :property "title", :value title}])
        deltas (if (= body (page :body))
                 deltas
                 (conj deltas {:ref ref,
                               :property "body",
                               :value (engine/get-patch (page :body) body)}))]
    (store-deltas id deltas)))

(defn delete-page
  "Applies the passed deltas"
  [id ref]
  (log/! "delete-page called for wiki" id "with" :ref ref)
  (store-deltas id [{:ref ref :property "page" :value nil}]))
