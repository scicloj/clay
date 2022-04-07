(ns example1
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.clay.v1.tools :as tools]))

(clay/start! {:tools [tools/clerk
                      tools/portal]})

(comment
  (clay/restart! {:tools [tools/clerk
                          tools/portal]}))

(+ 1 2)
