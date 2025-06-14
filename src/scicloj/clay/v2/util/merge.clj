(ns scicloj.clay.v2.util.merge
  (:require [clojure.edn :as edn]
            [clojure.walk :as walk]))

(defn materialize
  "Resolves annotated data from external sources.

  Supported metadata:
  - ^:slurp [\"file.edn\"]: Reads EDN from a file.
  - ^:env   [\"ENV_VAR\"]: Reads EDN from an environment variable.

  The resulting data preserves and merges the original metadata."
  [x]
  (let [mx (meta x)]
    (cond
      (:slurp mx) (-> (edn/read-string (slurp (first x)))
                      (vary-meta merge mx))
      (:env mx) (-> (edn/read-string (System/getenv ^String (first x)))
                    (vary-meta merge mx))
      :else x)))

(defn expand
  "Walks the data structure `x`, replacing any value found in the `expansions` map.
  Useful for expanding identifiers into larger values.

  Example:
    (expand [:a :b]
            {:a {:name \"Alice\"},
             :b {:name \"Bob\"}})
    => [{:name \"Alice\"}
        {:name \"Bob\"}]"
  [x expansions]
  (walk/prewalk (fn [node]
                  (if (contains? expansions node)
                    (get expansions node)
                    node))
                x))

(defn deep-merge
  "Recursively merges values with support for control metadata.

  Merge rules:
  - ^:replace on the right overrides
  - ^:use on the right merges profiles by name or more data
  - ^:expand on either side enables symbolic replacements via the `expand` function
  - Maps and nil are merged recursively
  - Non-maps are replaced (right wins)

  Examples:
  (deep-merge {:a 1} {:a 2}) => {:a 2}
  (deep-merge {:a 1} ^:replace {:b 2}) => {:b 2}
  (deep-merge {:profiles {:dev {:a 1}}}
              ^:use [:dev {:b 2}]) => {:a 1 :b 2}"
  ([] {})
  ([a] a)
  ([a b]
   (let [a (materialize a)
         ma (meta a)
         b (materialize b)
         mb (meta b)]
     (cond
       ;; replace and use are left to right
       (:replace mb) b
       (:use mb) (transduce (map (fn [k]
                                   (or (and (map? a) (get a k))
                                       k)))
                            deep-merge
                            {}
                            b)
       ;; expand can be in either direction
       (:expand ma) (expand b a)
       (:expand mb) (expand a b)
       ;; normal merge
       (and (map? a) (map? b)) (merge-with deep-merge a b)
       (and (map? a) (nil? b)) a
       :else b)))
  ([a b & more]
   (reduce deep-merge (deep-merge a b) more)))
