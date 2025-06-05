(ns scicloj.clay.v2.util.merge
  (:require [clojure.walk :as walk]
            [scicloj.kindly.v4.api :as kindly]))

(defn deep-merge
  "Recursively merges maps"
  [& maps]
  (apply kindly/deep-merge maps))

(defn dissoc-nils
  "Removes entries with nil values from a map"
  [m]
  (reduce (fn [acc [k v]]
            (if (nil? v)
              (dissoc acc k)
              acc))
          m
          m))

(defn prune-nils
  "Walks a data structure, ensuring maps do not have nil values"
  [data]
  (walk/postwalk
    (fn [x]
      (if (map? x)
        (dissoc-nils x)
        x))
    data))

(comment
  (prune-nils [{:a {:b [{:c {:d nil}}]}}])
  :-)
