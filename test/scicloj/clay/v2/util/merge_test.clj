(ns scicloj.clay.v2.util.merge-test
  (:require [clojure.test :refer [deftest is]]
            [scicloj.clay.v2.util.merge :as merge]))

(deftest deep-merge-test
  (is (= :foo (merge/deep-merge :foo))
      "should not break when given non-maps")
  (is (= (merge/deep-merge :foo :bar {:baz 100})
         {:baz 100})
      "should return the last item when merging is not possible")
  (is (= (merge/deep-merge {:a {:b {:c 10}}}
                           {:a {:b {:c 11}}})
         {:a {:b {:c 11}}})
      "should update deeply nested paths")
  (is (= (merge/deep-merge {:a {:b {:c 10}}}
                           {:a {:b nil}})
         {:a {:b {:c 10}}})
      "should merge nil")
  (is (= (merge/deep-merge {:a {:b {:c 10}}}
                           nil)
         {:a {:b {:c 10}}})
      "nils are ignored")
  (is (= (merge/deep-merge {:a {:b {:c 10}}}
                           {:a {:b {:c 9 :d 11}}}
                           {:a {:b {:d 12}}})
         {:a {:b {:c 9 :d 12}}})
      "more than two arguments are allowed")
  (is (= (merge/deep-merge)
         {})
      "no arguments mean an empty map")
  (is (= (merge/expand [:a :b]
                       {:a {:name "Alice"}, :b {:name "Bob"}})
         [{:name "Alice"} {:name "Bob"}]))
  (is (= (merge/deep-merge {:a 1} ^:replace {:b 2})
         {:b 2}))
  (is (= (merge/deep-merge {:dev  {:a 1}
                            :base {:b 2}}
                           ^:use [:dev :base])
         {:a 1 :b 2}))
  (is (= (merge/deep-merge ^:expand {:me {:name "Me"}}
                           {:authors [:me]})
         {:authors [{:name "Me"}]}))
  (is (= (merge/deep-merge ^:expand {:me {:name "Me"
                                          :affiliation [:scicloj]}
                                     :scicloj {:url "https://scicloj.github.io"}}
                           [:me])
         [{:name        "Me"
           :affiliation [{:url "https://scicloj.github.io"}]}])
      "expansions should be expanded"))
