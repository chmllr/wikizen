(ns wikizen.storage-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require
    [cemerick.cljs.test :as t]
    [wikizen.storage :as storage]))

(deftest storage-test
         (testing "storage and updates"
                  (let [id (storage/create-wiki "Test")]
                    (storage/update-page id [] "Root" "Body")
                    (is (= {:name "Test"
                            :root {:title "Root" :body "Body"}} (storage/get-wiki id)))
                    (storage/update-page id [0] "Child 1" "X")
                    (storage/update-page id [1] "Child 2" "Y")
                    (storage/update-page id [1 0] "Child 2-1" "Y Y")
                    (is (= {:name "Test"
                            :root {:title "Root" :body "Body"
                                   :children [ {:title "Child 1" :body "X"}
                                               {:title "Child 2" :body "Y"
                                                :children [{:title "Child 2-1" :body "Y Y"}]}]}}
                           (storage/get-wiki id)))
                    (storage/update-page id [0] "Child 1" "XXX")
                    (storage/update-page id [1 0] "Child 2-1" "YYY")
                    (is (= {:name "Test"
                            :root {:title "Root" :body "Body"
                                   :children [ {:title "Child 1" :body "XXX"}
                                               {:title "Child 2" :body "Y"
                                                :children [{:title "Child 2-1" :body "YYY"}]}]}}
                           (storage/get-wiki id)))
                    (storage/update-page id [1 0 0] "Child 2-1-0" "0")
                    (is (= {:name "Test"
                            :root {:title "Root" :body "Body"
                                   :children [ {:title "Child 1" :body "XXX"}
                                               {:title "Child 2" :body "Y"
                                                :children [{:title "Child 2-1" :body "YYY"
                                                           :children [{:title "Child 2-1-0" :body "0"}]}]}]}}
                           (storage/get-wiki id)))
                    (storage/update-page id [1 0 0] "New" "Content")
                    (is (= {:name "Test"
                            :root {:title "Root" :body "Body"
                                   :children [ {:title "Child 1" :body "XXX"}
                                               {:title "Child 2" :body "Y"
                                                :children [{:title "Child 2-1" :body "YYY"
                                                            :children [{:title "New" :body "Content"}]}]}]}}
                           (storage/get-wiki id)))
                    (storage/delete-page id [0])
                    (is (= {:name "Test"
                            :root {:title "Root" :body "Body"
                                   :children [{:title    "Child 2" :body "Y"
                                               :children [{:title "Child 2-1" :body "YYY"
                                                            :children [{:title "New" :body "Content"}]}]}]}}
                           (storage/get-wiki id)))
                    (storage/delete-page id [0 0 0])
                    (is (= {:name "Test"
                            :root {:title "Root" :body "Body"
                                   :children [{:title    "Child 2" :body "Y"
                                               :children [{:title "Child 2-1" :body "YYY"
                                                           :children nil}]}]}}
                           (storage/get-wiki id)))
                    (storage/delete-page id [0 0])
                    (is (= {:name "Test"
                            :root {:title "Root" :body "Body"
                                   :children [{:title    "Child 2" :body "Y"
                                               :children nil}]}}
                           (storage/get-wiki id)))
                    (storage/delete-page id [0])
                    (is (= {:name "Test"
                            :root {:title "Root" :body "Body"
                                   :children nil}}
                           (storage/get-wiki id))))))