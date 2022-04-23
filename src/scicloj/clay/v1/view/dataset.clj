(ns scicloj.clay.v1.view.dataset
  (:require [tech.v3.dataset :as tmd]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kindness :as kindness]))

(extend-protocol kindness/Kindness
  tech.v3.dataset.impl.dataset.Dataset
  (kind [this]
    :kind/dataset))

(kindly/define-kind-behaviour!
  :kind/dataset
  {:portal.viewer (fn [v]
                    [:portal.viewer/table
                     (seq (tmd/mapseq-reader v))])})

(kindly/define-kind-behaviour!
  :kind/dataset
  {:clerk.viewer (fn [v]
                   #:nextjournal{:value {:head (tmd/column-names v)
                                         :rows (vec (tmd/rowvecs v))}
                                 :viewer :table})})

(kindly/define-kind-behaviour!
  :kind/dataset
  {:scittle.viewer (fn [v]
                     (-> v
                         println
                         with-out-str
                         vector
                         (kindly/consider :kind/table-md))
                     #_(-> {:column-names (tmd/column-names v)
                            :row-vectors (vec (tmd/rowvecs v))}
                           (kindly/consider :kind/table)))})
