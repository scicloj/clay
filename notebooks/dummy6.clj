^{:clay {:kindly/options {:kinds-that-hide-code #{:kind/hiccup}}}}
(ns dummy6
  (:require [scicloj.kindly.v4.kind :as kind]))

(kind/hiccup
 [:h1 "A"])

^{:kind/hide-code false}
(kind/hiccup
 [:h1 "B"])
