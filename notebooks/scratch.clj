(ns scratch
  (:require [tech.v3.dataset.print]
            [scicloj.kindly-render.notes.to-html-page :as to-html-page]
            [scicloj.clay.v2.read :as read]
            [scicloj.clay.v2.notebook :as notebook]
            [scicloj.kindly-advice.v1.api :as kindly-advice]))

;; Intro


(+ 1 2)


[9]




(comment
  (->> "notebooks/scratch.clj"
       slurp
       read/->safe-notes
       (hash-map :notes)
       to-html-page/render-notebook
       (spit "/tmp/scratch/index.html"))
  
  ) 

