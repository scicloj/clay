(ns scicloj.clay.v2.checks
  (:require [scicloj.kindly.v3.api :as kindly]
            [scicloj.clay.v2.tool.scittle.view :as scittle.view]))

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

(scittle.view/add-viewer!
 :kind/check
 (fn [value-and-check]
   (->> value-and-check
        value-and-check->hiccup)))

(defn check [value & predicate-and-args]
  (-> {:value value
       :check (-> (if predicate-and-args
                    (apply (first predicate-and-args)
                           value
                           (rest predicate-and-args))
                    value)
                  (if true false))}
      (vary-meta assoc :kindly/kind :kind/check)))
