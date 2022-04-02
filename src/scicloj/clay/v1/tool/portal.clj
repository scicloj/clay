(ns scicloj.clay.v1.tool.portal
  (:require [portal.api :as portal]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.clay.v1.walk]
            [scicloj.clay.v1.tool :as tool]
            [scicloj.clay.v1.tool.html.table :as table]))

(kindly/define-kind-behaviour! :kind/hiccup
  {:portal.viewer (fn [v]
                    [:portal.viewer/hiccup v])})

(kindly/define-kind-behaviour! :kind/table
  {:portal.viewer (fn [table-spec]
                    [:portal.viewer/hiccup
                     ;; TODO: Use Portal's Table viewer
                     (table/->table-hiccup
                      table-spec)])})

(kindly/define-kind-behaviour! :kind/vega
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

(def tool
  (reify tool/Tool
    (setup! [this config])
    (open! [this]
      (portal/open))
    (close! [this]
      (portal/close))
    (show! [this value code]
      (show! value code))))
