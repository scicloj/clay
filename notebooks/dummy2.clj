(ns dummy2
  (:require [scicloj.kindly.v4.kind :as kind]))


[:div
 (kind/hiccup
  [:div
   [:h1 ".."]
   ;; (kind/tex "x^2=\\alpha")
   (kind/md "hello $x^2=\\alpha$")])]
