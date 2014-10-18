(ns wikizen.storage
  (:require [wikizen.engine :as engine]))

(enable-console-print!)

(def sample-wiki
  { :name "SampleWiki"
    :root { :title "Root Page"
            :body "This is the *page body* of a fake wiki. And __this__ is `code`."
            :children [ { :title "Nested Page 1"
                          :body "The __content__ of _nested_ page 1"
                          :children [ { :title "Nested Page 1_1"
                                        :body "This _is_ a leaf" } ] }
                        { :title "Nested Page 2"
                          :body "The __content__ of _nested_ page 2"
                          :children [ { :title "Nested Page 2_1"
                                        :body "This _is_ a leaf" } ]} ] } })

(def sample-deltas (atom []))

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
    (let [[result status] (.patch_apply dmp patch text)]
      (if (every? identity (js->clj status))
        result
        (throw (print-str "Patch" (.stringify js/JSON patch) "could not be applied"))))))

(defn get-wiki
  "Returns the wiki object"
  []
  sample-wiki)

(defn update-wiki
  "Applies the passed deltas"
  [ref title body]
  (let [wiki (get-wiki)
        page (or (engine/get-node wiki ref) {})
        deltas (if (= title (page :title))
                 []
                 [{:ref ref, :title title}])
        deltas (if (= body (page :body))
                 deltas
                 (conj deltas {:ref ref,
                        :body (get-patch (page :body) body)}))]
    (swap! sample-deltas concat deltas)))
