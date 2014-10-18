(ns wikizen.engine)

(defn get-node
  "Traverses the wiki tree and returns the 
  node according the specified path (vector of ints)"
  [node path]
  (if (empty? path)
    node
    (let [[index & indeces] path
          children (node :children)]
      (when (< index (count children))
        (get-node (nth children index) indeces)))))

(defn get-path
  "Returns the traversed path in form of [<index> <title>] pairs"
  ([node path]
   (cons [[] (node :title)]
         (get-path node path [])))
  ([node path acc]
   (if (empty? path)
     []
     (let [index (first path)
           acc (conj acc index)
           child (nth (node :children) index)
           title (:title child)]
       (cons [acc title]
             (get-path child (rest path) acc))))))

(defn- set-nth
  "Sets nth element of the given vector to the given value"
  [vec nth value]
  (let [[left right] (split-at (inc nth) vec)]
    (concat (butlast left) [value] right)))

(defn set-page
  "Sets the page to the given reference"
  [wiki path page]
  (if (empty? path)
    page
    (let [[index & indeces] path
          children (vec (wiki :children))
          new-children (if (= (count children) index)
                         (conj children page)
                         (set-nth children
                                  index
                                  (set-page (nth children index)
                                            indeces
                                            page)))]
      (assoc wiki :children new-children))))