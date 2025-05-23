(ns scicloj.clay.v2.util.merge
  (:require [clojure.walk :as walk]))

(defn deep-merge
  "Recursively merges maps.
  See https://dnaeon.github.io/recursively-merging-maps-in-clojure/. "
  [& maps]
  (letfn [(m [& xs]
            (if (some #(and (map? %) (not (record? %))) xs)
              (apply merge-with m xs)
              (last xs)))]
    (reduce m maps)))

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
