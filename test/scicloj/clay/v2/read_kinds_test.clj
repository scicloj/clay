(ns scicloj.clay.v2.read-kinds-test
  (:require [scicloj.clay.v2.read-kinds :as sut]
            [clojure.test :as t]))

;; FIXME: Cider evals ns form strings starting with newline
;; in 'user namespace
(def read-ns-form-code-example
  "(ns my-namespace
  (:require [clojure.core]))
")

(t/deftest read-ns-form-test
  (t/is (= (sut/read-ns-form
          read-ns-form-code-example)
         '(ns my-namespace
            (:require [clojure.core])))))

(def simple-ns-example "(ns my-namespace
  (:require [clojure.core]))

;; A function that adds 9 to numbers:

(defn abcd [x]
  (+ x 9))
")

(t/deftest read-string-all-test
  (t/is
   (=
    (sut/read-string-all simple-ns-example {})
    [{:region [1 1 2 29],
      :code "(ns my-namespace\n  (:require [clojure.core]))",
      :meta
      {:source "(ns my-namespace\n  (:require [clojure.core]))",
       :line 1,
       :column 1,
       :end-line 2,
       :end-column 29},
      :form '(ns my-namespace (:require [clojure.core])),
      :value nil}
     {:code "\n\n", :kind :kind/whitespace}
     {:code ";; A function that adds 9 to numbers:\n",
      :kind :kind/comment,
      :value "A function that adds 9 to numbers:\n"}
     {:code "\n", :kind :kind/whitespace}
     {:region [6 1 7 11],
      :code "(defn abcd [x]\n  (+ x 9))",
      :meta
      {:source "(defn abcd [x]\n  (+ x 9))",
       :line 6,
       :column 1,
       :end-line 7,
       :end-column 11},
      :form '(defn abcd [x] (+ x 9)),
      :value #'my-namespace/abcd}
     {:code "\n", :kind :kind/whitespace}])))
