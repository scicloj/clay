(ns scicloj.clay.v2.util.merge)

(defn deep-merge
  "Recursively merges maps.
  See https://dnaeon.github.io/recursively-merging-maps-in-clojure/. "
  [& maps]
  (letfn [(m [& xs]
            (if (some #(and (map? %) (not (record? %))) xs)
              (apply merge-with m xs)
              (last xs)))]
    (reduce m maps)))
