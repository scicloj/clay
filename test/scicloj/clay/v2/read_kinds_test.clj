(ns scicloj.clay.v2.read-kinds-test
  (:require [scicloj.clay.v2.read-kinds :as sut]
            [clojure.java.io :as io]
            [clojure.test :as t]))

(def read-ns-form-code-example
  (slurp (io/resource "resources/my_namespace.clj")))

(t/deftest read-ns-form-test
  (t/is (= (sut/read-ns-form
            read-ns-form-code-example)
           '(ns my-namespace
              (:require [clojure.core])))))

(def simple-ns-example
  (slurp (io/resource "resources/simple_ns.clj")))

(t/deftest read-string-all-test
  (t/is
   (=
    (sut/read-string-all simple-ns-example {})
    [{:code "\n", :kind :kind/whitespace}
     {:region [2 1 3 29],
      :code "(ns simple-ns\n  (:require [clojure.core]))",
      :meta
      {:source "(ns simple-ns\n  (:require [clojure.core]))",
       :line 2,
       :column 1,
       :end-line 3,
       :end-column 29},
      :form '(ns simple-ns (:require [clojure.core])),
      :value nil}
     {:code "\n\n", :kind :kind/whitespace}
     {:code ";; A function that adds 9 to numbers:\n",
      :kind :kind/comment,
      :value "A function that adds 9 to numbers:\n"}
     {:code "\n", :kind :kind/whitespace}
     {:region [7 1 8 11],
      :code "(defn abcd [x]\n  (+ x 9))",
      :meta
      {:source "(defn abcd [x]\n  (+ x 9))",
       :line 7,
       :column 1,
       :end-line 8,
       :end-column 11},
      :form '(defn abcd [x] (+ x 9)),
      :value #'simple-ns/abcd}
     {:code "\n", :kind :kind/whitespace}])))
