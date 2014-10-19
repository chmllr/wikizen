(ns wikizen.storage
  (:require [wikizen.engine :as engine]))

(enable-console-print!)

(def dao (atom {}))

(defn create-wiki
  "Creates ne wiki in the persistence layer"
  ([name] (create-wiki name {}))
  ([name root]
   (let [id "sample-wiki"]                                   ; TODO: generate the id somehow
     (swap! dao assoc id {:wiki {:name name :root root}
                          :deltas  []})
     id)))

(let [dmp (js/diff_match_patch.)]
  ;(aset dmp "Diff_Timeout" 1.0)
  ;(aset dmp "Diff_EditCost" "raw")
  (defn- get-patch
    "Diffs two texts and returns the delta"
    [from to]
    (.patch_make dmp (or from "") (or to "")))
  (defn- apply-patch
    "Applies deltas to get the next version"
    [patch text]
    (let [[result status] (.patch_apply dmp patch (or text ""))]
      (if (every? identity (js->clj status))
        result
        (throw (print-str "Patch" (.stringify js/JSON patch) "could not be applied"))))))

(defn apply-delta
  "Apply given delta to the given Wiki"
  [wiki {:keys [ref property value]}]
  (let [page (engine/get-node wiki ref)
        page (cond
               (= property :title) (assoc page :title value)
               (= property :body) (assoc page :body (apply-patch value (page :body)))
               :otherwise value)]
    (engine/set-page wiki ref page)))

(defn get-root-page
  "Returns the Wiki object"
  [id]
  (let [wiki (get-in @dao [id :wiki])
        deltas (get-in @dao [id :deltas])]
    (assoc wiki :root (reduce apply-delta (wiki :root) deltas))))

(defn update-page
  "Applies the passed deltas"
  [id ref title body]
  (let [wiki-root ((get-root-page id) :root)
        page (or (engine/get-node wiki-root ref) {})
        deltas (if (= title (page :title))
                 []
                 [{:ref ref, :property :title, :value title}])
        deltas (if (= body (page :body))
                 deltas
                 (conj deltas {:ref ref,
                               :property :body,
                               :value (get-patch (page :body) body)}))]
    (swap! dao (fn [storage deltas]
                 (update-in storage
                            [id :deltas]
                            concat deltas)) deltas)))

(defn delete-page
  "Applies the passed deltas"
  [id ref]
  (swap! dao (fn [storage deltas]
               (update-in storage
                          [id :deltas]
                          concat deltas))
         [{:ref ref :property :page :value nil}]))
