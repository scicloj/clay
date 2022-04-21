(ns scicloj.clay.v1.tool.clerk
  (:require [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind]
            [nextjournal.clerk :as clerk]
            [scicloj.clay.v1.walk]
            [nextjournal.clerk.webserver :as webserver]
            [nextjournal.clerk.view :as view]
            [scicloj.clay.v1.tool :as tool]))

(def *notes (atom []))

(defn get-value-with-id! [value]
  (let [blob-id (str (java.util.UUID/randomUUID))
        value-with-id #:nextjournal{:value value
                                    :blob-id blob-id}]
    (swap! webserver/!doc
           (fn [doc]
             (cond-> doc
               (not (:blob->result doc)) (assoc
                                          :blob->result {})
               true (assoc-in
                     [:blob->result blob-id] value-with-id)
               true (assoc :ns *ns*))))
    value-with-id))

(defn sync-notes! [notes]
  (webserver/broadcast!
   {:doc
    {:nextjournal/viewer :clerk/notebook,
     :nextjournal/value
     {:blocks
      (->> notes
           (mapv (fn [{:keys [value]}]
                   (view/->result
                    *ns*
                    (get-value-with-id! value)
                    true))))}}}))

(defn swap-notes! [f]
  (swap! *notes f)
  (sync-notes! @*notes))

(defn show-values! [values]
  (swap! *notes (constantly (->> values
                                 (mapv (fn [value]
                                         {:value value})))))
  (sync-notes! @*notes))

(defn maybe-apply-viewer [value kind]
  (if-let [viewer (-> kind kindly/kind->behaviour :clerk.viewer)]
    (viewer value)
    value))

(defn prepare [value]
  (->> value
       (scicloj.clay.v1.walk/postwalk
        (fn [subvalue]
          (->> subvalue
               kindly/kind
               (maybe-apply-viewer subvalue))))))

(defn setup! []
  (clerk/set-viewers!
   [{:pred (fn [v]
             (some-> v kindly/kind kindly/kind->behaviour :clerk.viewer))
     :transform-fn prepare}
    {:pred delay?
     :transform-fn (fn [v]
                     (let [dv @v]
                       (if (kindly/kind dv)
                         (prepare dv)
                         dv)))}])
  :ok)

(defn show! [value code]
  (show-values! (concat (when code
                          [(clerk/code code)
                           (clerk/html [:hr])])
                        [value])))

(def tool
  (reify tool/Tool
    (setup! [this config]
      (setup!))
    (open! [this]
      (clerk/serve! {:browse? true}))
    (close! [this])
    (show! [this value code]
      (show! value code))))


(kindly/define-kind-behaviour! :kind/naive
  {:clerk.viewer (fn [v]
                   (->> v
                        println
                        with-out-str
                        (vector :pre)
                        clerk/html))})

(kindly/define-kind-behaviour! :kind/hiccup
  {:clerk.viewer (fn [v]
                   (clerk/html v))})

(kindly/define-kind-behaviour! :kind/table
  {:clerk.viewer (fn [{:keys [row-maps row-vectors column-names]}]
                   (clerk/table {:head column-names
                                 :rows (or row-vectors
                                           (map (fn [row-map]
                                                  (map row-map column-names))
                                                row-maps))}))})

(kindly/define-kind-behaviour! :kind/vega
  {:clerk.viewer (fn [v]
                   (clerk/vl v))})

(def cytoscape
  {:fetch-fn (fn [_ x] x)
   :render-fn
   '(fn [value]
      (v/html
       (when value
         [v/with-d3-require {:package ["cytoscape@3.21.0"]}
          (fn [cytoscape]
            [:div {:style {:height "500px"}
                   :ref (fn [el]
                          (when el
                            (-> value
                                (assoc :container el)
                                clj->js
                                cytoscape)))}])])))})

(kindly/define-kind-behaviour! :kind/cytoscape
  {:clerk.viewer (fn [v]
                   (clerk/with-viewer cytoscape v))})


(def echarts
  {:pred string?
   :fetch-fn (fn [_ x] x)
   :render-fn
   '(fn [value]
      (v/html
       (when value
         [v/with-d3-require {:package ["echarts@5.3.2"]}
          (fn [echarts]
            [:div {:style {:height "500px"}
                   :ref (fn [el]
                          (when el
                            (let [chart (.init echarts el)]
                              (-> chart
                                  (.setOption (clj->js
                                               value))))))}])])))})

(kindly/define-kind-behaviour! :kind/echarts
  {:clerk.viewer (fn [v]
                   (clerk/with-viewer echarts v))})
