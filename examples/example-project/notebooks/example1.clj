;; # Clay demo

;; ## Setup

;; Note that Clay does not need to be required
;; in a namespace to be used as a notebook.

(ns example1
  (:require [scicloj.kindly.v4.kind :as kind]))

;; ## Examples

(+ 1 2)

(kind/hiccup
 [:div {:style
        {:background-color "#eeddcc"}}
  [:p "hello"]])

(kind/md
 ["hello *hello* **hello**"])



(comment
  ;; Instead of using your editor setup and keybindings,
  ;; you can also use the Clay API directly:

  (require '[scicloj.clay.v2.api :as clay])

  ;; Show the whole namespace
  (clay/make! {:source-path "notebooks/example1.clj"})

  ;; Show a given value
  (clay/make! {:single-value (kind/hiccup
                              [:div {:style
                                     {:background-color "#eeddcc"}}
                               [:p "hello"]])})
  ,)
