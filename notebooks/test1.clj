(ns test1
  (:require [scicloj.kindly.v4.kind :as kind]))

(def people-as-maps
  (->> (range 29)
       (mapv (fn [i]
               {:preferred-language (["clojure" "clojurescript" "babashka"]
                                     (rand-int 3))
                :age (rand-int 100)}))))

(kind/table
 {:column-names [:preferred-language :age]
  :row-maps (take 10 people-as-maps)})
