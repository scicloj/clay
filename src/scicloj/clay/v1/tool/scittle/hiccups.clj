(ns scicloj.clay.v1.tool.scittle.hiccups)

(defn code [string]
  [:pre [:code.language-clojure string]])

(defn structure-mark [string]
  [:big string])
