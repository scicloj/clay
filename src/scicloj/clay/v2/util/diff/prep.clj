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
  (diffable-base-type? [_] true))

(defn type-str [x]
  (-> x type pr-str))

(defn describe-type [t]
  (type-str t))

(declare replaced-value=)

(defprotocol PReplacedValue
  (value? [this]))

(deftype ReplacedValue [value value-type]
  PReplacedValue
  (value? [_]
    (= (describe-type value) value-type))

  Object
  (equals [this other]
    (or (not (value? this))
        (replaced-value= this other)))
  (hashCode [this]
    (hash-combine (hash value-type)
                  (if (value? this)
                    (hash value)
                    value)))
  (toString [this]
    (pr-str (.hashCode this)
            value-type)))

(defn replaced-value= [^ReplacedValue this other]
  (and (instance? ReplacedValue this)
       (instance? ReplacedValue other)
       (= (.-value-type this) (.-value-type ^ReplacedValue other))
       (= (.-value this) (.-value ^ReplacedValue other))))

(defn diffable-type? [x]
  (let [x-type-str (type-str x)]
    (some (partial String/.startsWith x-type-str)
          ["java.util"
           "clojure.lang"])))

(defn replace-undiffable* [x]
  (cond (fn? x) ::replaced-fn
        (and (satisfies? DiffableBaseType x)
             (diffable-base-type? x)
             (not (diffable-type? x)))
        (->ReplacedValue x (describe-type x))
        :else x))

(defn replace-undiffable [notes]
  (walk/prewalk replace-undiffable* notes))
