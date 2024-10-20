^{:clay {:live-reload true}}
(ns dummy
  (:require [scicloj.clay.v2.api :as clay]))


(comment
  (clay/make! {:live-reload true
               :source-path ["notebooks/dummy.clj"]}))


(+ 1 32)
