(ns wikizen.tests
  (:require [wikizen.engine :as engine]))

(defn- is
  [argument description]
  (let [result (= argument true)]
    (when-not
        result
      (.error js/console
              (print-str "Test failed:" description)))
    result))

(defn- isnt
  [argument description]
  (is (= false argument) description))

(def engine-tests
  (let [root { :title "Root Page"
               :body "This is the *page body*."
               :children [ { :title "Nested Page 1"
                             :body "The __content__ of _nested_ page 1"
                             :children [ { :title "Nested Page 1_1"
                                           :body "This _is_ a leaf" } ] }
                           { :title "Nested Page 2"
                             :body "The __content__ of _nested_ page 2" } ] }]
    [(is (= "Root Page" (:title (engine/get-node root []))) "extracting the root page")
     (is (= "Nested Page 1" (:title (engine/get-node root [0]))) "extracting first child")
     (is (= "Nested Page 2" (:title (engine/get-node root [1]))) "extracting first child")
     (is (= "Nested Page 1_1" (:title (engine/get-node root [0 0]))) "extracting first child")
     ]))

(def tests
  (concat engine-tests []))

(defn run-tests 
  "Unit tests"
  []
  (let [A (count tests)
        N (count (filter identity tests))]
    (println N "/" A "tests executed successfully")))


