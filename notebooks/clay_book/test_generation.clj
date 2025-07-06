;; # Test generation
;; (experimental 🛠)

;; In this chapter, we discuss Clay's option to automatically generate
;; tests out of notebooks.

(ns clay-book.test-generation
  (:require
   [scicloj.kindly.v4.kind :as kind]
   [scicloj.kindly.v4.api :as kindly]))

(+ 1 2)

(kind/test-last [> 2.9])

^kind/test-last
[> 2.9]

(kindly/check > 2.9)

;; We generate tests checking whether
;; this last value is greater than 2.9.
;; We can do it in a few ways.

;; We include the test annotations in the markdown text,
;; since the annotations themselves are invisible.

(kindly/hide-code
 (kind/code
  "(kind/test-last [> 2.9])

^kind/test-last
[> 2.9]

(kindly/check > 2.9)"))

;; See the generated [test/index_generated_test.clj](https://github.com/scicloj/clay/blob/main/test/index_generated_test.clj).

;; For a detailed example using this mechanism, see [the source](https://github.com/scicloj/clojisr/blob/master/notebooks/clojisr/v1/tutorials/main.clj) of the [ClojisR tutorial](https://scicloj.github.io/clojisr/clojisr.v1.tutorials.main.html).

