(ns scicloj.clay.v2.notebook-test
  (:require [clojure.test :refer [deftest is testing]]
            [scicloj.clay.v2.notebook :as notebook]))

(deftest strip-string-literals-test
  (testing "string literals are removed before marker detection"
    (is (= "(println )"
           (notebook/strip-string-literals "(println \",,\")")))
    (is (= "(println  ,, 3)"
           (notebook/strip-string-literals "(println \"x\" ,, 3)")))
    (is (= "(println )"
           (notebook/strip-string-literals "(println \"a \\\" ,, \\\" b\")")))))

(deftest marker-outside-string-test
  (testing "markers inside strings do not trigger narrowing"
    (is (false? (notebook/narrowed? "(println \",,\")")))
    (is (false? (notebook/narrower? "(println \",,,\")")))
    (is (false? (notebook/narrowed? "(println \"a \\\" ,, \\\" b\")"))))
  (testing "markers in code still trigger narrowing"
    (is (true? (notebook/narrowed? "(+ 1 ,, 2)")))
    (is (true? (notebook/narrower? "(+ 1 ,,, 2)")))
    (is (true? (notebook/narrowed? "(println \",,\")\n(+ 1 ,, 2)")))))
