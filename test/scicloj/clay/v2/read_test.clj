(ns scicloj.clay.v2.read-test
  (:require [scicloj.clay.v2.read :as read]
            [clojure.test :refer [deftest is]]))

(def read-ns-form-code-example
  "
(ns my-namespace
  (:require [clojure.core]))
")

(deftest read-ns-form-test
  (is (= (scicloj.clay.v2.read/read-ns-form
          read-ns-form-code-example)
         '(ns my-namespace
            (:require [clojure.core])))))

(def simple-ns-example
  "
(ns my-namespace
  (:require [clojure.core]))

;; A function that adds 9 to numbers:

(defn abcd [x]
  (+ x 9))
")

(deftest safe-notes-simple-test
  (is
   (=
    (->> simple-ns-example
         scicloj.clay.v2.read/->safe-notes
         (map #(dissoc % :gen)))
    '[{:region [2 1 3 29]
       :code "(ns my-namespace\n  (:require [clojure.core]))"
       :meta
       {:source "(ns my-namespace\n  (:require [clojure.core]))"
        :line 2
        :column 1
        :end-line 3
        :end-column 29}
       :form (ns my-namespace (:require [clojure.core]))}
      {:region [5 1 5 38]
       :code ";; A function that adds 9 to numbers:"
       :comment? true}
      {:region [7 1 8 11]
       :code "(defn abcd [x]\n  (+ x 9))"
       :meta
       {:source "(defn abcd [x]\n  (+ x 9))"
        :line 7
        :column 1
        :end-line 8
        :end-column 11}
       :form (defn abcd [x] (+ x 9))}])))
