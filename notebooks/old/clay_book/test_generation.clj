;; # Test generation
;; (experimental 🛠)

;; Using Kindly annotations, Clay can automatically generate
;; tests out of notebooks.

;; This allows one to make sure that a piece of documentation
;; or tutorial remains correct after code changes, etc.
;; This can also be seen as a literate way to create tests for a library.

^:kindly/hide-code
(ns clay-book.test-generation
  (:require [scicloj.kindly.v4.api :as kindly]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.clay.v2.old.api :as clay]
            [clojure.string :as str]))

^:kindly/hide-code
(defn make-notebook-and-show-tests
  "An auxialiary function to render a given notebook and show its code
  next to the code of the generated tests."
  [notebook-path]
  (let [test-path (->>
                   ;; Render the notebook, suppressing printing to stdout
                   ;; to keep our output here succinct.
                   (binding [*out* (java.io.StringWriter.)] ; avoid printing
                     (clay/make! {:source-path notebook-path
                                  :show false}))
                   ;; Extract the generated test file path from the information
                   ;; returned by `make!`:
                   :info
                   flatten
                   (filter (fn [v]
                             (and (string? v)
                                  (re-matches #"test/.*generated_test\.clj" v))))
                   first)]
    (kindly/hide-code
     (kind/fragment
      [(kind/md ["::: {.grid}"
                 "::: {.g-col-6}"
                 (format "**%s**" (-> notebook-path
                                      (str/split #"/")
                                      last))])
       (kind/code (slurp notebook-path))
       (kind/md [":::"
                 "::: {.g-col-6}"
                 (format "**%s**" (-> test-path
                                      (str/split #"/")
                                      last))])
       (kind/code (slurp test-path))
       (kind/md [":::"
                 ":::"])]))))

;; ## The idea

;; Sometimes, we have some expectations regargings the forms we have in our
;; notebook.
;; For example, we know that `(rand)` should result in a number between zero and one,
;; and we know that `(filter pos? (range -4 4))` should result in a nonempty sequence.

;; We can express such expectations by adding Kindly-annotated forms
;; following the forms of interest. Then, every time the notebook is rendered,
;; Clay will generate tests to verify our assumptions.
;; The tests will be collected in a regular
;; [clojure.test](https://clojure.github.io/clojure/clojure.test-api.html) namespace
;; with standard `deftest` forms as one may expect.

;; ## Expressing a test

;; Assume we have a couple of expressions in our notebook:

(def x 11)

(* x x)

;; If we think about it, we know the resulting number has to be more than 100.
;; Thus, we may add a test for that by taking the result of the last form,
;; infoking the `>` function with the additional argument `100`,
;; and making sure the result is truthy.

;; This can be expressed in any one of a few equivalent ways:

;; * `(kind/test-last [> 100])`
;; * `^kind/test-last [> 100]`
;; * `(kindly/check > 100)`

;; In the rendered namespace, such forms will be hidden.
;; However, as a side effect of rendering, we will get a test namespace
;; with something like the following:

;; ```clj
;; (def v1_l4 (def x 11))
;; (def v3_l8 (* x x))
;; (deftest t4_l10 (is (> v3_l8 100)))
;; ```

;; Here, the forms of the notebook are added as `def` forms,
;; except fot the test itself which is added as a `deftest`.

;; ## The different test modes

;; Clay currently supports two modes for test generation:
;; `:sequential` (which is the default) and `:simple`.

;; The idea of `:sequential` test generation is that, in general,
;; the original notebook may evolve a certain state sequentially.
;; E.g., it may define some vars and mutate some atoms.
;; In general, the correctness of the tests may rely on this state,
;; so the test namespace has to go through all forms of the original
;; notebook one by one, and interlace the `deftest` forms between them.

;; The idea of `:simple` test generation is that sometimes, the situation
;; is simpler. The tests can be standalone invocations of certain library
;; functions, so their correctness wuold not rely on anything else in
;; the namespace. This allows us to write tests which are more readable.
;; If all tests are simple, it makes the whole test namespace much simpler.

;; The testing mode can be specified in the `:kind/options` of the project-level
;; configuration or the namespace level configuration.
;; It can also be specified for a specific test. We'll see that in the examples
;; below.

;; ## Examples

;; ### Sequential tests

;; Here is a namespace with a few sequential tests, that depend
;; on the state evolving throughout the notebook.

(make-notebook-and-show-tests
 "notebooks/test_gen/sequential.clj")

;; ### Mixed sequential and simple tests
;; Here is a namespace that involves both sequential and simple tests.
;; This is expressed through `:test-mode :simple` in the kindly options
;; part of the call to `kind/test-last`.

(make-notebook-and-show-tests
 "notebooks/test_gen/sequential_and_simple.clj")

;; ### Only simple tests
;; Here is a namespace where all tests are simple.
;; This can be expressed through `:test-mode :simple`,
;; which can be specified either in the namespace-level
;; `:kindly/options` (as we do here),
;; or in each and every `kind/test-last` call.

(make-notebook-and-show-tests
 "notebooks/test_gen/simple.clj")

;; ## More examples

;; For a detailed example using this mechanism,
;; see [the source](https://github.com/scicloj/clojisr/blob/master/notebooks/clojisr/v1/tutorials/main.clj)
;; of the [ClojisR tutorial](https://scicloj.github.io/clojisr/clojisr.v1.tutorials.main.html).
