(ns wikizen.template-engine-test
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require
    [goog.dom :as dom]
    [cemerick.cljs.test :as t]
    [wikizen.template-engine :as te]))

(def test-mode true)

(def div (dom/createElement "div"))

(defn is* [expected actual]
  (do
    (aset div "innerHTML" "")
    (.appendChild div actual)
    (is (= expected (aget div "innerHTML")))))

(deftest template-engine-test
         (is* "<hr>" (te/template->dom [:hr]))
         (is* "<br>" (te/template->dom [:br]))
         (is* "<br><hr>" (te/template->dom [:br] [:hr]))
         (is* "<br><span></span><hr>" (te/template->dom [:br] nil [:hr]))
         (is* "<div></div>" (te/template->dom [:div]))
         (is* "<div>test</div>" (te/template->dom [:div "test"]))
         (is* "<div>test</div><span>test2</span>" (te/template->dom [:div "test"] "test2"))
         (is* "<div>:test</div>" (te/template->dom [:div :test]))
         (is* "<div>:a<span>:B</span><span>:c</span></div>" (te/template->dom [:div '(:a :B :c)]))
         (is* "<div><br><span>:a</span><span>:B</span><span>:c</span><span>some</span><span>test</span></div>"
              (te/template->dom [:div [:br] '(:a :B :c) "some" nil "test"]))
         (is* "<a href=\"http://notehub.org\"></a>" (te/template->dom [:a {:href "http://notehub.org"}]))
         (is* "<a href=\"http://notehub.org\" name=\"someName\"></a>"
              (te/template->dom [:a {:href "http://notehub.org" :name "someName"}]))
         (is* "<div id=\"id5\">test</div>" (te/template->dom [:div#id5 "test"]))
         (is* "<div id=\"id5\">test<div>text 2</div></div>"
              (te/template->dom [:div#id5 "test" [:div "text 2"]]))
         (is* "<div id=\"id\" class=\"test\"><p>hi</p><span>there</span></div>"
              (te/template->dom [:div#id.test [:p "hi"] nil 'there]))
         (is* "<div id=\"id5\">test<div>text 2</div><span>another text</span></div>"
              (te/template->dom [:div#id5 "test" [:div "text 2"] "another text"])))
