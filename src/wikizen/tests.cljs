(ns wikizen.tests
  (:require [wikizen.engine :as engine]))

(defn- eq
  [expected got description]
  (let [result (= expected got)]
    (when-not
        result
      (.error js/console
              (print-str "Test failed:" description "->"
                         "expected:" expected "but got" got)))
    result))

(def engine-tests
  (let [root { :title "Root Page"
               :body "This is the *page body*."
               :children [ { :title "Nested Page 1"
                             :body "The __content__ of _nested_ page 1"
                             :children [ { :title "Nested Page 1_1"
                                           :body "This _is_ a leaf" } ] }
                           { :title "Nested Page 2"
                             :body "The __content__ of _nested_ page 2"
                             :children [ { :title "Nested Page 2_1"
                                           :body "This _is_ a leaf" } ]} ] }]
    [(eq "Root Page" (:title (engine/get-node root [])) "extracting the root page")
     (eq "Nested Page 1" (:title (engine/get-node root [0])) "extracting 1sr child")
     (eq "Nested Page 2" (:title (engine/get-node root [1])) "extracting 1st child")
     (eq "Nested Page 1_1" (:title (engine/get-node root [0 0])) "extracting 1st child's child")
     (eq [[[] "Root Page"]] (engine/get-path root []) "get root path")
     (eq [[[] "Root Page"] [[0] "Nested Page 1"]] (engine/get-path root [0]) "get path to 1st child")
     (eq [[[] "Root Page"] [[1] "Nested Page 2"]] (engine/get-path root [1]) "get path to 2nd child")
     (eq [[[] "Root Page"] [[0] "Nested Page 1"] [[0 0] "Nested Page 1_1"]] (engine/get-path root [0 0]) "get path to 1st child's child")
     (eq [[[] "Root Page"] [[1] "Nested Page 2"] [[1 0] "Nested Page 2_1"]] (engine/get-path root [1 0]) "get path to 2nd child's child")
     ]))

(def tests
  (concat engine-tests []))

(defn run-tests 
  "Unit tests"
  []
  (let [A (count tests)
        N (count (filter identity tests))]
    (println N "/" A "tests executed successfully")))


