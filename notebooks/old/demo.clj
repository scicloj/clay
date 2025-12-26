(ns old.demo)

;; This file merely shows how to use a Clojure file to write docs in Clay,
;; with no additional dependencies required.

;; # This Is a H1 Title
;; ## And a H2 Title
;; Here, we can write comments in [Markdown](https://en.wikipedia.org/wiki/Markdown) format,
;; and normal Clojure code along the side,
;; both of which and the evaluated results of the code will show up in the redendered docs.

;; Below are a few simple examples:

(+ 1 2)

;; (/ 1 0) ; un-comment and evaluate to observe error handling

(str "Hello, " "Clay!")

(defn greet [name]
  (str "Hello, " name "!"))

(greet "Clay")
