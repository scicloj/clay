(ns scicloj.clay.v2.view.dataset
  (:require [tech.v3.dataset :as tmd]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.kindly.v3.kindness :as kindness]
            [scicloj.clay.v2.tool.scittle.view :as scittle.view]))

(scittle.view/add-viewer!
 :kind/dataset
 (fn [v]
   (-> v
       println
       with-out-str
       vector
       (kindly/consider :kind/table-md))
   #_(-> {:column-names (tmd/column-names v)
          :row-vectors (vec (tmd/rowvecs v))}
         (kindly/consider :kind/table))))