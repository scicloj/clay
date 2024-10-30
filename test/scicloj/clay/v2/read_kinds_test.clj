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

(def detailed-ns-example
  (slurp (io/resource "resources/detailed_ns.clj")))

(t/deftest safe-notes-detailed-test
  (t/is
   (=
    (sut/read-string-all detailed-ns-example {})
    [{:code "\n\n", :kind :kind/whitespace}
     {:code ";; # A notebook\n",
      :kind :kind/comment,
      :value "# A notebook\n"}
     {:code "\n", :kind :kind/whitespace}
     {:region [5 1 6 29],
      :code "(ns detailed-ns\n  (:require [clojure.core]))",
      :meta
      {:source "(ns detailed-ns\n  (:require [clojure.core]))",
       :line 5,
       :column 1,
       :end-line 6,
       :end-column 29},
      :form '(ns detailed-ns (:require [clojure.core])),
      :value nil}
     {:code "\n\n", :kind :kind/whitespace}
     {:code ";; ## Intro\n", :kind :kind/comment, :value "## Intro\n"}
     {:code "\n", :kind :kind/whitespace}
     {:code ";; Let us write a function that adds 9 to numbers.\n",
      :kind :kind/comment,
      :value "Let us write a function that adds 9 to numbers.\n"}
     {:code ";; We will call it `abcd`.\n",
      :kind :kind/comment,
      :value "We will call it `abcd`.\n"}
     {:code "\n", :kind :kind/whitespace}
     {:region [13 1 15 9],
      :code "(defn abcd [x]\n  (+ x\n     9))",
      :meta
      {:source "(defn abcd [x]\n  (+ x\n     9))",
       :line 13,
       :column 1,
       :end-line 15,
       :end-column 9},
      :form '(defn abcd [x] (+ x 9)),
      :value #'detailed-ns/abcd}
     {:code "\n\n", :kind :kind/whitespace}
     {:region [17 1 17 9],
      :code "(abcd 9)",
      :meta
      {:source "(abcd 9)",
       :line 17,
       :column 1,
       :end-line 17,
       :end-column 9},
      :form '(abcd 9),
      :value 18}
     {:code "\n\n", :kind :kind/whitespace}
     {:code ";; ## More examples\n",
      :kind :kind/comment,
      :value "## More examples\n"}
     {:code "\n", :kind :kind/whitespace}
     {:code ";; Form metadata\n",
      :kind :kind/comment,
      :value "Form metadata\n"}
     {:code "\n", :kind :kind/whitespace}
     {:region [23 1 25 16],
      :code "^:kind/hiccup\n[:div\n  [:p \"hello\"]]",
      :meta
      {:source "^:kind/hiccup\n[:div\n  [:p \"hello\"]]",
       :line 23,
       :column 1,
       :end-line 25,
       :end-column 16},
      :form [:div [:p "hello"]],
      :value [:div [:p "hello"]]}
     {:code "\n\n", :kind :kind/whitespace}
     {:code ";; A symbol\n", :kind :kind/comment, :value "A symbol\n"}
     {:code "\n", :kind :kind/whitespace}
     {:region [29 1 29 10],
      :code ":a-symbol",
      :meta
      {:source ":a-symbol",
       :line 29,
       :column 1,
       :end-line 29,
       :end-column 10},
      :form :a-symbol,
      :value :a-symbol}
     {:code "\n\n", :kind :kind/whitespace}
     {:code ";; Comments using #_ should be ignored:\n",
      :kind :kind/comment,
      :value "Comments using #_ should be ignored:\n"}
     {:code "\n", :kind :kind/whitespace}
     {:code "#_(+ 1 2)", :kind :kind/uneval}
     {:code "\n\n", :kind :kind/whitespace}
     {:code "#_#_ (+ 1 2) (+ 3 4)", :kind :kind/uneval}
     {:code "\n", :kind :kind/whitespace}])))
