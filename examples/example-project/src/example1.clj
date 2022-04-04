;; # Clay

(ns intro
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.clay.v1.tools :as tools]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind]
            [nextjournal.clerk :as clerk]))

(clay/start! {:tools [tools/clerk
                      #_tools/portal]})

(comment
  (clay/restart! {:tools [tools/clerk
                          tools/portal]}))

(+ 1 2)
