(ns scicloj.clay.v1.checks
  (:require [scicloj.kindly.v2.api :as kindly]
            [nextjournal.clerk :as clerk]))

(defn bool->symbol [bool]
  [:big [:big (if bool
                [:big {:style {:color "darkgreen"}}
                 "✓"]
                [:big {:style {:color "darkred"}}
                 "❌"])]])

(defn check-boolean->hiccup [bool]
  [:div
   (bool->symbol bool)])

(defn value-and-check->hiccup [{:keys [value check]}]
  [:div
   value
   (-> check
       check-boolean->hiccup)])

(kindly/define-kind-behaviour!
  :kind/check
  {:portal.viewer (fn [value-and-check]
                    (->> value-and-check
                         value-and-check->hiccup
                         (vector :portal.viewer/hiccup)))
   :clerk.viewer (fn [value-and-check]
                   (->> value-and-check
                        value-and-check->hiccup
                        clerk/html))
   :scittle.viewer (fn [value-and-check]
                     (->> value-and-check
                          value-and-check->hiccup))})

(defn check [value & predicate-and-args]
  (-> {:value value
       :check (-> (if predicate-and-args
                    (apply (first predicate-and-args)
                           value
                           (rest predicate-and-args))
                    value)
                  (if true false))}
      (vary-meta assoc :kindly/kind :kind/check)))
