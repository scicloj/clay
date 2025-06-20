(ns scicloj.clay.v2.util.merge
  (:require [clojure.walk :as walk]))

(defn deep-merge
  "Recursively merges values with support for control metadata.

  Merge rules:
  - ^:replace on the right overrides
  - Maps are merged recursively
  - Non-maps except nil are replaced (right wins)

  Examples:
  (deep-merge {:a 1} {:a 2}) => {:a 2}
  (deep-merge {:a 1} ^:replace {:b 2}) => {:b 2}"
  ([] {})
  ([a] a)
  ([a b]
   (cond
     ;; replace and use are left to right
     (:replace (meta b)) b
     ;; normal merge
     (and (map? a) (map? b)) (merge-with deep-merge a b)
     (and (map? a) (nil? b)) a
     :else b))
  ([a b & more]
   (reduce deep-merge (deep-merge a b) more)))
