(ns dummy4
  (:require [scicloj.kindly.v4.kind :as kind]
            [clojure.string :as str]))

(defn splice-code [code]
  (-> code
      (str/replace
       "(multi\n ["
       "  ")
      (#(subs % 0 (-> % count dec dec)))))

(defn multi [values]
  (with-meta values
    {:kindly/format-code splice-code
     :kind/fragment true}))

(multi
 [(inc 9)
  (inc 4)])
