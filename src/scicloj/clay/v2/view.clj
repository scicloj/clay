(ns scicloj.clay.v2.view
  (:require [scicloj.clay.v2.tool.scittle.server :as server]))

(defn deref-if-needed [v]
  (if (delay? v)
    (let [_ (println "deref ...")
          dv @v
          _ (println "done.")]
      dv)
    v))

(defn show! [context]
  (when-not (-> context
                :value
                meta
                :kindly/kind
                (= :kind/hidden))
    (let [context-to-show
          (-> context
              (update :value deref-if-needed))]
      (try
        (server/show! context-to-show)
        (catch Exception e
          (println ["Exception while trying to show a value:"
                    e]))))))
