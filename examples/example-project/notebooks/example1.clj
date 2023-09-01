;; # Clay demo

;; ## Setup

(ns example1
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.kindly.v4.kind :as kind]))

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

  ;; Show the whole namespace
  (clay/show-namespace! "notebooks/example1.clj")

  ;; Show a given value
  (clay/handle-value!
   (kind/hiccup
    [:div {:style
           {:background-color "#eeddcc"}}
     [:p "hello"]]))
  ,)
