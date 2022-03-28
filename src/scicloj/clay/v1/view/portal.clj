(ns scicloj.clay.v1.view.portal
  (:require [portal.api :as portal]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.clay.v1.walk]))

(kindly/define-kind-behaviour! :kind/hiccup
  {:portal.viewer (fn [v]
                    [:portal.viewer/hiccup v])})

(kindly/define-kind-behaviour! :kind/vega-lite
  {:portal.viewer (fn [v]
                    [:portal.viewer/vega-lite v])})

(defn maybe-apply-viewer [value kind]
  (if-let [viewer (-> kind kindly/kind->behaviour :portal.viewer)]
    (viewer value)
    value))

(defn prepare [value code]
  (let [v (if-let [code-kind (kindly/code->kind code)]
            (maybe-apply-viewer value code-kind)
            (->> value
                 (scicloj.clay.v1.walk/postwalk
                  (fn [subvalue]
                    (->> subvalue
                         kindly/kind
                         (maybe-apply-viewer subvalue))))))]
    (if (and (vector? v)
             (-> v first keyword?)
             (-> v first namespace (= "portal.viewer")))
      (-> v
          (vary-meta assoc
                     :portal.viewer/default
                     :portal.viewer/hiccup))
      v)))

(defn show! [value code]
  (-> [:div
       (when code
         [:portal.viewer/code code])
       [:portal.viewer/inspector
        (-> value
            (prepare code))]]
      (with-meta
        {:portal.viewer/default :portal.viewer/hiccup})
      portal/submit))
