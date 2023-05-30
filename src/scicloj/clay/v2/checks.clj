(ns scicloj.clay.v2.checks
  (:require [scicloj.kindly.v3.api :as kindly]
            [scicloj.clay.v2.tool.scittle.view :as scittle.view]
            [clojure.test :refer [is]]))


(defn value-and-check->hiccup [{:keys [value check]}]
  [:div
   value
   (scittle.view/bool->hiccup check)])

(scittle.view/add-viewer!
 :kind/check
 value-and-check->hiccup)

(defn check [value & predicate-and-args]
  (-> {:value value
       :check (-> (if predicate-and-args
                    (apply (first predicate-and-args)
                           value
                           (rest predicate-and-args))
                    value)
                  (if true false))}
      (vary-meta assoc :kindly/kind :kind/check)))
