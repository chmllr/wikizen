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

