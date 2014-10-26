(ns wikizen.engine-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require
    [cemerick.cljs.test :as t]
    [wikizen.engine :as engine]))

(deftest engine-tests
         (let [root { :title "Root Page"
                     :body     "This is the *page body*."
                     :children [{:title    "Nested Page 1"
                                 :body     "The __content__ of _nested_ page 1"
                                 :children [{:title "Nested Page 1_1"
                                             :body  "This _is_ a leaf text"}]}
                                {:title    "Nested Page 2"
                                 :body     "The __content__ of _nested_ page 2"
                                 :children [{:title "Nested Page 2_1"
                                             :body  "This _is_ a leaf text"}]}]}
               rootX (update-in root [:children] conj {:title "regex search" :body "can be used to\n search in textes"})
               page {:title "New Page" :body "The body of the new page."}]
           (testing "search"
                    (is (= [] (engine/search root ["XXX"])))
                    (is (= '([[] "This is the *page body*."]
                             [[0 0] "This _is_ a leaf text"]
                             [[1 0] "This _is_ a leaf text"])
                           (engine/search root ["is"])))
                    (is (= '([[0] "The __content__ of _nested_ page 1"]
                             [[1] "The __content__ of _nested_ page 2"])
                           (engine/search root ["of"])))
                    (is (= '([[] "This is the *page body*."])
                           (engine/search root ["this" "Body"])))
                    (is (= '([[] "Root Page"])
                           (engine/search root ["root"])))
                    (is (= '([[2] "regex search"])
                           (engine/search rootX ["regex"])))
                    (is (= '([[2] "regex search"] [[2] " search in textes"])
                           (engine/search rootX ["search"])))
                    (is (= '([[0 0] "This _is_ a leaf text"] [[1 0] "This _is_ a leaf text"] [[2] " search in textes"])
                           (engine/search rootX ["text"]))))
           (testing "get-node"
                    (is (= "Root Page" (:title (engine/get-node root [])))
                        "extracting the root page")
                    (is (= "Nested Page 1" (:title (engine/get-node root [0])))
                        "extracting 1st child")
                    (is (= "Nested Page 2" (:title (engine/get-node root [1])))
                        "extracting 1st child")
                    (is (= "Nested Page 1_1" (:title (engine/get-node root [0 0])))
                        "extracting 1st child's child"))
           (testing "get-path"
                    (is (= ["Root Page"] (engine/get-path root [])) "get root path")
                    (is (= ["Root Page" "Nested Page 1"] (engine/get-path root [0])) "get path to 1st child")
                    (is (= ["Root Page" "Nested Page 2"] (engine/get-path root [1])) "get path to 2nd child")
                    (is (= ["Root Page" "Nested Page 1" "Nested Page 1_1"] (engine/get-path root [0 0]))
                        "get path to 1st child's child")
                    (is (= ["Root Page" "Nested Page 2" "Nested Page 2_1"] (engine/get-path root [1 0]))
                        "get path to 2nd child's child"))
           (testing "set-nth"
                    (is (= [:X :b :c :d] (engine/set-nth [:a :b :c :d] 0 :X)))
                    (is (= [:a :X :c :d] (engine/set-nth [:a :b :c :d] 1 :X)))
                    (is (= [:a :b :X :d] (engine/set-nth [:a :b :c :d] 2 :X)))
                    (is (= [:a :b :c :X] (engine/set-nth [:a :b :c :d] 3 :X))))
           (testing "diffing and patching"
                    (let [text1 "like alll magnificent theengs ,  >  it iss very simple"
                          text2 "Like all magnificent things, it's very simple."
                          garbage-patch (engine/get-patch text2 text1)
                          patch (engine/get-patch text1 text2)
                          ]
                      (is (= text2 (engine/apply-patch patch text1)))
                      (try (do
                             (engine/apply-patch garbage-patch text1)
                             (is false "This should have never been executed!"))
                           (catch :default e
                             (is (re-matches #".*could not be applied" e))))))
           (testing "set-page"
                    (is (= page (engine/set-page root [] page)))
                    (is (= { :title "Root Page"
                            :body     "This is the *page body*."
                            :children [page
                                       {:title    "Nested Page 2"
                                        :body     "The __content__ of _nested_ page 2"
                                        :children [{:title "Nested Page 2_1"
                                                    :body  "This _is_ a leaf text"}]}]}
                           (engine/set-page root [0] page)))
                    (is (= { :title "Root Page"
                            :body     "This is the *page body*."
                            :children [{:title    "Nested Page 1"
                                        :body     "The __content__ of _nested_ page 1"
                                        :children [{:title "Nested Page 1_1"
                                                    :body  "This _is_ a leaf text"}]}
                                       page]}
                           (engine/set-page root [1] page)))
                    (is (= { :title "Root Page"
                            :body     "This is the *page body*."
                            :children [{:title    "Nested Page 1"
                                        :body     "The __content__ of _nested_ page 1"
                                        :children [{:title "Nested Page 1_1"
                                                    :body  "This _is_ a leaf text"}]}
                                       {:title    "Nested Page 2"
                                        :body     "The __content__ of _nested_ page 2"
                                        :children [page]}]}
                           (engine/set-page root [1 0] page)))
                    (is (= { :title "Root Page"
                            :body     "This is the *page body*."
                            :children [{:title    "Nested Page 1"
                                        :body     "The __content__ of _nested_ page 1"
                                        :children [page]}
                                       {:title    "Nested Page 2"
                                        :body     "The __content__ of _nested_ page 2"
                                        :children [page]}]}
                           (engine/set-page
                             (engine/set-page root [1 0] page)
                             [0 0] page)))
                    (is (= { :title "Root Page"
                            :body     "This is the *page body*."
                            :children [{:title    "Nested Page 1"
                                        :body     "The __content__ of _nested_ page 1"
                                        :children [{:title "Nested Page 1_1"
                                                    :body  "This _is_ a leaf text"}]}
                                       {:title    "Nested Page 2"
                                        :body     "The __content__ of _nested_ page 2"
                                        :children [{:title "Nested Page 2_1"
                                                    :body  "This _is_ a leaf text"}]}
                                       page]}
                           (engine/set-page root [2] page)))
                    (is (= { :title "Root Page"
                            :body     "This is the *page body*."
                            :children [{:title    "Nested Page 1"
                                        :body     "The __content__ of _nested_ page 1"
                                        :children [{:title    "Nested Page 1_1"
                                                    :body     "This _is_ a leaf text"
                                                    :children [page]}]}
                                       {:title    "Nested Page 2"
                                        :body     "The __content__ of _nested_ page 2"
                                        :children [{:title "Nested Page 2_1"
                                                    :body  "This _is_ a leaf text"}]}]}
                           (engine/set-page root [0 0 0] page)))
                    (is (= { :title "Root Page"
                            :body     "This is the *page body*."
                            :children [{:title    "Nested Page 1"
                                        :body     "The __content__ of _nested_ page 1"
                                        :children [{:title "Nested Page 1_1"
                                                    :body  "This _is_ a leaf text"}]}
                                       {:title    "Nested Page 2"
                                        :body     "The __content__ of _nested_ page 2"
                                        :children [{:title "Nested Page 2_1"
                                                    :body  "This _is_ a leaf text"}
                                                   page]}]}
                           (engine/set-page root [1 1] page)))
                    (is (= { :title "Root Page"
                            :body     "This is the *page body*."
                            :children [{:title    "Nested Page 1"
                                        :body     "The __content__ of _nested_ page 1"
                                        :children [{:title "Nested Page 1_1"
                                                    :body  "This _is_ a leaf text"}]}
                                       {:title    "Nested Page 2"
                                        :body     "The __content__ of _nested_ page 2"
                                        :children nil}]}
                           (engine/set-page root [1 0] nil)))
                    (is (= { :title "Root Page"
                            :body     "This is the *page body*."
                            :children [{:title    "Nested Page 1"
                                        :body     "The __content__ of _nested_ page 1"
                                        :children [{:title "Nested Page 1_1"
                                                    :body  "This _is_ a leaf text"}]}]}
                           (engine/set-page root [1] nil))))))