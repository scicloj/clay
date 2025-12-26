(ns scicloj.clay.v2.old.util.meta)

(defn pr-str-with-meta [value]
  (binding [*print-meta* true]
    (pr-str value)))
