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
       :form (ns my-namespace (:require [clojure.core]))}
      {:region [5 1 5 38]
       :code ";; A function that adds 9 to numbers:"
       :comment? true}
      {:region [7 1 8 11]
       :code "(defn abcd [x]\n  (+ x 9))"
       :form (defn abcd [x] (+ x 9))}])))

(def detailed-ns-example
  "

;; # A notebook

(ns my-namespace
  (:require [clojure.core]))

;; ## Intro

;; Let us write a function that adds 9 to numbers.
;; We will call it `abcd`.

(defn abcd [x]
  (+ x
     9))

(abcd 9)

;; ## More examples

;; Form metadata

^:kind/hiccup
[:div
  [:p \"hello\"]]

;; A symbol

a-symbol

;; Comments using #_ should be ignored:

#_(+ 1 2)

#_#_ (+ 1 2) (+ 3 4)


")


(deftest safe-notes-detailed-test
  (is
   (= (->> detailed-ns-example
           scicloj.clay.v2.read/->safe-notes
           (map #(dissoc % :gen)))
      '[{:region [3 1 3 16], :code ";; # A notebook", :comment? true}
        {:region [5 1 6 29],
         :code "(ns my-namespace\n  (:require [clojure.core]))",
         :form (ns my-namespace (:require [clojure.core]))}
        {:region [8 1 11 27],
         :code
         ";; ## Intro\n\n;; Let us write a function that adds 9 to numbers.\n;; We will call it `abcd`.",
         :comment? true}
        {:region [13 1 15 9],
         :code "(defn abcd [x]\n  (+ x\n     9))",
         :form (defn abcd [x] (+ x 9))}
        {:region [17 1 17 9], :code "(abcd 9)", :form (abcd 9)}
        {:region [19 1 21 17],
         :code ";; ## More examples\n\n;; Form metadata",
         :comment? true}
        {:region [24 1 25 16],
         :code "^:kind/hiccup\n[:div\n  [:p \"hello\"]]",
         :form [:div [:p "hello"]]}
        {:region [27 1 27 12], :code ";; A symbol", :comment? true}
        {:region [29 1 29 9], :code "a-symbol", :form a-symbol}
        {:region [31 1 31 40],
         :code ";; Comments using #_ should be ignored:",
         :comment? true}])))
