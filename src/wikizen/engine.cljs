(ns wikizen.engine)

(defn get-node
  "Traverses the wiki tree and returns the 
  node according the specified path (vector of ints)"
  [root path]
  (if (empty? path)
    root
    (get-node
      (nth (root :children) (first path))
      (rest path))))

(defn get-path
  "Returns the traversed path in form of [<index> <title>] pairs"
  ([root path]
   (cons [[] (root :title)]
         (get-path root path [])))
  ([root path acc]
   (if (empty? path)
     []
     (let [index (first path)
           acc (conj acc index)
           child (nth (root :children) index)
           title (:title child)]
       (cons [acc title]
             (get-path child (rest path) acc))))))