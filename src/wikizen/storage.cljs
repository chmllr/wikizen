(ns wikizen.storage
  (:require [wikizen.engine :as engine]))

(enable-console-print!)

(def default-root
  {:title "&#9775; WikiZen"
   :body (str "> _\"One machine can do the work of **fifty** ordinary men.  \n"
              "> No machine can do the work of **one** extraordinary man.\"_  \n"
              "> — Elbert Hubbard\n\n"
              "This is default **root page** of your Wiki powered by [WikiZen](https://github.com/chmllr/wikizen).  \n")})

(def dao (atom {}))

(defn- store-wiki
  "Stores a new wiki"
  [id wiki]
  (swap! dao assoc id wiki))

(defn- restore-wiki
  "Restores a wiki from id"
  [id]
  (get-in @dao [id :wiki]))

(defn- store-deltas
  "Saves deltas"
  [id deltas]
  (swap! dao (fn [storage deltas]
               (update-in storage
                          [id :deltas]
                          concat deltas)) deltas))

(defn- restore-deltas
  "Restores all deltas for a wiki id"
  [id]
  (get-in @dao [id :deltas]))

(defn create-wiki
  "Creates ne wiki in the persistence layer"
  ([name id] (create-wiki name id default-root))
  ([name id root]
   (store-wiki id {:wiki {:name name :root root}
                        :deltas []})
   id))

(defn get-wiki
  "Returns the Wiki object"
  [id]
  (let [wiki (restore-wiki id)
        deltas (restore-deltas id)]
    (assoc wiki :root (reduce engine/apply-delta (wiki :root) deltas))))

(defn update-page
  "Applies the passed deltas"
  [id ref title body]
  (let [wiki-root ((get-wiki id) :root)
        page (or (engine/get-node wiki-root ref) {})
        deltas (if (= title (page :title))
                 []
                 [{:ref ref, :property :title, :value title}])
        deltas (if (= body (page :body))
                 deltas
                 (conj deltas {:ref ref,
                               :property :body,
                               :value (engine/get-patch (page :body) body)}))]
    (store-deltas id deltas)))

(defn delete-page
  "Applies the passed deltas"
  [id ref]
  (store-deltas id [{:ref ref :property :page :value nil}]))
