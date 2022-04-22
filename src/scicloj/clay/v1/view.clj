(ns scicloj.clay.v1.view
  (:require [scicloj.clay.v1.tool :as tool]
            [scicloj.clay.v1.view.image]
            [scicloj.kindly.v2.api :as kindly]))

(defn setup! [tools config]
  (doseq [tool tools]
    (tool/setup! tool config)))

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

(kindly/define-kind! :kind/hidden)

(defn show! [value code tools]
  (let [form (read-string code)]
    (when-not (or (->> code
                       (re-matches #".*nextjournal.clerk/show!.*"))
                  (-> form
                      meta
                      :kind/hidden)
                  (-> value
                      kindly/kind
                      (= :kind/hidden)))
      (let [value-to-show (deref-if-needed value)
            code-to-show nil #_(when-not (-> form
                                             meta
                                             :kind/hide-code)
                                 code)]
        (doseq [tool tools]
          (try
            (tool/show! tool value-to-show code-to-show)
            (catch Exception e
              (println ["Exception while trying to show value:"
                        e]))))))))
