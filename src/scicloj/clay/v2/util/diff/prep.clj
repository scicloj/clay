(ns scicloj.clay.v2.util.diff.prep
  (:require [clojure.walk :as walk]
            [clojure.string :as str]))

(defprotocol DiffableBaseType
  (diffable-base-type? [this]))

(extend-protocol DiffableBaseType
  java.util.Set
  (diffable-base-type? [_] true)
  java.util.Map
  (diffable-base-type? [_] true)
  java.util.List
  (diffable-base-type? [_] true)
  clojure.lang.IPersistentVector
  (diffable-base-type? [_] true))

(defn type-str [x]
  (-> x type pr-str))

(defn diffable-type? [x]
  (let [x-type-str (type-str x)]
    (some (partial String/.startsWith x-type-str)
          ["java.util"
           "clojure.lang"])))

(defn describe-type [t]
  (type-str t))

(defn replace-undiffable* [x]
  (cond (fn? x) ::fn
        (and (satisfies? DiffableBaseType x)
             (diffable-base-type? x)
             (not (diffable-type? x))) (describe-type x)
        :else x))

(defn replace-undiffable [notes]
  (walk/prewalk replace-undiffable* notes))
