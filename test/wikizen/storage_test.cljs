(ns wikizen.storage-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require
    [cemerick.cljs.test :as t]
    [wikizen.storage :as storage]))

(deftest storage-test
         (testing "diffing and patching")
         (let [text1 "like alll magnificent theengs ,  >  it iss very simple"
               text2 "Like all magnificent things, it's very simple."
               garbage-patch (storage/get-patch text2 text1)
               patch (storage/get-patch text1 text2)
               ]
           (is (= text2 (storage/apply-patch patch text1)))
           (try (do
                  (storage/apply-patch garbage-patch text1)
                  (is false "This should have never been executed!"))
                (catch :default e
                  (is (re-matches #".*could not be applied" e))))))