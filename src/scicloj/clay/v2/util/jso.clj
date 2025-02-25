(ns scicloj.clay.v2.util.jso
  "JavaScript Objects may include functions, unlike JSON which does not.
  Visualizations use functions for behaviors such as tooltips or animation.
  This namespace enhances JSON serialization such that Regex objects are treated as literals.
  Regex is a convenient object to write in objects and treat as a string in serialization."
  (:require [charred.api :as charred]
            [charred.coerce :as coerce])
  (:import (charred JSONWriter)
           (java.util List Map Map$Entry)
           (java.util.function BiConsumer)
           (java.util.regex Pattern)
           (clojure.lang MapEntry)))

(def ^{:tag     BiConsumer
       :private true} obj-fn
  (reify BiConsumer
    (accept [this w value]
      (let [^JSONWriter w w]
        (if (instance? Pattern value)
          (.write (.w w) (str value))
          (let [value (when-not (nil? value) (charred/->json-data value))]
            (cond
              (or (sequential? value)
                  (instance? List value)
                  (.isArray (.getClass ^Object value)))
              (.writeArray w (coerce/->iterator value))
              (instance? Map value)
              (.writeMap w (coerce/map-iter (fn [^Map$Entry e]
                                              (MapEntry. (charred/->json-data (.getKey e))
                                                         (.getValue e)))
                                            (.entrySet ^Map value)))
              :else
              (.writeObject w value))))))))

(defn write-json-str [x]
  (charred/write-json-str x :obj-fn obj-fn))
