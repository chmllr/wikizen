(ns wikizen.engine-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require
    [cemerick.cljs.test :as t]
    [wikizen.engine :as engine]))

(deftest engine-tests
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
           [(testing "get-node"
                     (is (= "Root Page" (:title (engine/get-node root [])))
                         "extracting the root page")
                     (is (= "Nested Page 1" (:title (engine/get-node root [0])))
                         "extracting 1st child")
                     (is (= "Nested Page 2" (:title (engine/get-node root [1])))
                         "extracting 1st child")
                     (is (= "Nested Page 1_1" (:title (engine/get-node root [0 0])))
                         "extracting 1st child's child"))
            (testing "get-path"
                     (is (= [[[] "Root Page"]] (engine/get-path root []))
                         "get root path")
                     (is (= [[[] "Root Page"] [[0] "Nested Page 1"]] (engine/get-path root [0]))
                         "get path to 1st child")
                     (is (= [[[] "Root Page"] [[1] "Nested Page 2"]] (engine/get-path root [1]))
                         "get path to 2nd child")
                     (is (= [[[] "Root Page"] [[0] "Nested Page 1"] [[0 0] "Nested Page 1_1"]]
                            (engine/get-path root [0 0]))
                         "get path to 1st child's child")
                     (is (= [[[] "Root Page"] [[1] "Nested Page 2"] [[1 0] "Nested Page 2_1"]]
                            (engine/get-path root [1 0]))
                         "get path to 2nd child's child"))]))