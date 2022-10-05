(ns scicloj.clay.v2.view
  (:require [scicloj.clay.v2.tool :as tool]
            [scicloj.clay.v2.view.image]
            [scicloj.kindly.v3.api :as kindly]))

(defn open! [tools]
  (doseq [tool tools]
    (tool/open! tool)))

(defn close! [tools]
  (doseq [tool tools]
    (tool/close! tool)))

(defn deref-if-needed [v]
  (if (delay? v)
    (let [_ (println "deref ...")
          dv @v
          _ (println "done.")]
      dv)
    v))

(defn show! [context tools]
  (when-not (-> context
                :value
                meta
                :kindly/kind
                (= :kind/hidden))
    (doseq [tool tools]
      (try
        (tool/show! tool context)
        (catch Exception e
          (println ["Exception while trying to show a value:"
                    e]))))))

(defn setup-extension! [extension]
  (try (require (:ns extension))
       (catch Exception e
         (-> ["Extension unavailable:"
              (select-keys extension [:name :info])]
             pr-str
             println))))

(defn setup! [{:keys [tools extensions events-source]
               :as config}]
  (doseq [tool tools]
    (tool/setup! tool config))
  (doseq [ex extensions]
    (setup-extension! ex)))
