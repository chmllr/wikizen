(ns wikizen.engine
  (:require
    [wikizen.log :as log]))

(defn get-node
  "Traverses the wiki tree and returns the 
  node according the specified path (vector of ints)"
  [root ref]
  (if (empty? ref)
    root
    (let [[index & indeces] ref
          children (root :children)]
      (when (< index (count children))
        (get-node (nth children index) indeces)))))

(defn get-path
  "Returns all titles of all pages down to the page under ref"
  [{:keys [title children]} [i & is :as ref]]
  (if (empty? ref)
    [title]
    (cons title (get-path (nth children i) is))))

(defn- set-nth
  "Sets nth element of the given vector to the given value"
  [vec nth value]
  (let [[left right] (split-at (inc nth) vec)]
    (concat (butlast left) [value] right)))

(let [dmp (js/diff_match_patch.)]
  (defn- get-patch
    "Diffs two texts and returns the delta as text"
    [from to]
    (.patch_toText dmp
                   (.patch_make dmp (or from "") (or to ""))))
  (defn- apply-patch
    "Applies delta as text to get the next version"
    [patch text]
    (let [[result status] (.patch_apply dmp (.patch_fromText dmp patch) (or text ""))]
      (if (every? identity (js->clj status))
        result
        (throw (print-str "Patch" (.stringify js/JSON patch) "could not be applied"))))))

(defn set-page
  "Sets the specifed ref to the specified page"
  [root ref page]
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
  "Applies given delta to the given Wiki"
  [root {:keys [ref property value]}]
  (log/! "apply-delta called for" :ref ref :property property)
  (let [page (get-node root ref)
        page (cond
               (= property "title") (assoc page :title value)
               (= property "body") (assoc page :body (apply-patch value (page :body)))
               :otherwise value)]
    (set-page root ref page)))

(defn- create-re
  "Creates one RE from the given terms"
  [terms]
  (let [re-terms (apply str (interpose ".*" terms))
        re-terms (str "(?i).*" re-terms ".*")]
    (re-pattern re-terms)))

(defn- do-search
  "Helper of the fn search"
  [root ref pattern]
  (concat (map #(vector ref %) (re-seq pattern
                                       (str (root :title) "\n" (root :body))))
          (mapcat (fn [[i child]] (do-search child (conj ref i) pattern))
                  (map list (range) (root :children)))))

(defn search
  "Simple regex-based text search over the wiki"
  [root terms]
  (do-search root [] (create-re terms)))