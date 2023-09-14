(ns scicloj.clay.v2.util.meta)

(defn pr-str-with-meta [value]
  (binding [*print-meta* true]
    (pr-str value)))
