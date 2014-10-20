(ns wikizen.engine
  (:require
    [wikizen.log :as log]))

(defn get-node
  "Traverses the wiki tree and returns the 
  node according the specified path (vector of ints)"
  [root ref]
  (log/! "get-node called for" :ref ref)
  (if (empty? ref)
    root
    (let [[index & indeces] ref
          children (root :children)]
      (when (< index (count children))
        (get-node (nth children index) indeces)))))

(defn get-path
  "Returns the traversed ref in form of [<index> <title>] pairs"
  ([root ref]
   (cons [[] (root :title)]
         (get-path root ref [])))
  ([root ref acc]
   (log/! "get-path called for" :ref ref)
   (if (empty? ref)
     []
     (let [index (first ref)
           acc (conj acc index)
           child (nth (root :children) index)
           title (:title child)]
       (cons [acc title]
             (get-path child (rest ref) acc))))))

(defn- set-nth
  "Sets nth element of the given vector to the given value"
  [vec nth value]
  (let [[left right] (split-at (inc nth) vec)]
    (concat (butlast left) [value] right)))

(let [dmp (js/diff_match_patch.)]
  (defn- get-patch
    "Diffs two texts and returns the delta"
    [from to]
    (.patch_toText dmp
                   (.patch_make dmp (or from "") (or to ""))))
  (defn- apply-patch
    "Applies deltas to get the next version"
    [patch text]
    (let [[result status] (.patch_apply dmp (.patch_fromText dmp patch) (or text ""))]
      (if (every? identity (js->clj status))
        result
        (throw (print-str "Patch" (.stringify js/JSON patch) "could not be applied"))))))

(defn set-page
  "Sets the page to the given reference"
  [root ref page]
  (log/! "set-page called for" :ref ref)
  (if (empty? ref)
    page
    (let [[index & indeces] ref
          children (vec (root :children))
          new-children (if (= (count children) index)
                         (conj children page)
                         (set-nth children
                                  index
                                  (set-page (nth children index)
                                            indeces
                                            page)))
          new-children (remove nil? new-children)]
      (assoc root :children
                  (when-not (empty? new-children) new-children)))))

(defn- apply-delta
  "Apply given delta to the given Wiki"
  [root {:keys [ref property value]}]
  (log/! "apply-delta called for" :ref ref :property property)
  (let [page (get-node root ref)
        page (cond
               (= property "title") (assoc page :title value)
               (= property "body") (assoc page :body (apply-patch value (page :body)))
               :otherwise value)]
    (set-page root ref page)))
