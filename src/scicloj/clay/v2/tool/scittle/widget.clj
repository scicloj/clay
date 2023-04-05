(ns scicloj.clay.v2.tool.scittle.widget
  (:require [clojure.pprint :as pp]
            [clojure.string :as string]))

(defn mark [hiccup & keywords]
  (reduce (fn [h kw]
            (-> h
                (vary-meta assoc kw true)))
          hiccup
          keywords))

(defn mark-plain-html [hiccup]
  (mark hiccup :clay/plain-html?))

(defn check [hiccup kw]
  (-> hiccup
      meta
      kw))

(defn code [string]
  (-> [:pre.card
       [:code.language-clojure.bg-light
        string]]
      (mark :clay/plain-html?
            :clay/source-clojure?)))

(defn printed-clojure [string]
  (-> [:pre
       [:code.language-clojure
        string]]
      (mark :clay/plain-html?
            :clay/printed-clojure?)
      (vary-meta
       assoc :clay/text string)))

(defn escape [string]
  (-> string
      (string/escape
       {\< "&lt;" \> "&gt;"})))

(defn structure-mark [string]
  (-> [:div string]
      #_[:big string]
      mark-plain-html))

(defn just-println [value]
  (-> value
      println
      with-out-str
      escape
      printed-clojure))

(defn pprint [value]
  (-> value
      pp/pprint
      with-out-str
      escape
      printed-clojure))

(defn in-div [widget]
  (with-meta
    [:div widget]
    (meta widget)))
