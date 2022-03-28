(ns scicloj.clay.v1.view
  (:require [portal.api :as portal]
            [nextjournal.clerk :as clerk]
            [scicloj.clay.v1.view.portal :as view.portal]
            [scicloj.clay.v1.view.clerk :as view.clerk]))

(defn open []
  ;; (portal/open)
  (clerk/serve! {;; :browse? true
                 }))

(defn close []
  ;; (portal/close)
  )

(defn deref-if-needed [v]
  (if (delay? v)
    (let [_ (println "deref ...")
          dv @v
          _ (println "done.")]
      dv)
    v))

(defn show [value code]
  (let [form (read-string code)]
    (when-not (or (->> code
                       (re-matches #".*nextjournal.clerk/show!.*"))
                  (-> form
                      meta
                      :kind/hidden))
      (let [value-to-show (deref-if-needed value)
            code-to-show nil #_(when-not (-> form
                                             meta
                                             :kind/hide-code)
                                 code)]
        #_(view.portal/show! value-to-show code-to-show)
        (view.clerk/show! value-to-show code-to-show)))))
