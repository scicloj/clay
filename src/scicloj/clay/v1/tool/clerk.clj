(ns scicloj.clay.v1.tool.clerk
  (:require [babashka.fs :as fs]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.analyzer :as analyzer]
            [nextjournal.clerk.config :as config]
            [nextjournal.clerk.eval :as eval]
            [nextjournal.clerk.parser :as parser]
            [nextjournal.clerk.view :as view]
            [nextjournal.clerk.viewer :as v]
            [nextjournal.clerk.webserver :as webserver]
            [scicloj.clay.v1.tool :as tool]
            [scicloj.clay.v1.walk]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind]))

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
                   (v/->result
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
  (clerk/add-viewers!
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

(defn show! [value]
  (show-values! [value]))

(def tool
  (reify tool/Tool
    (setup! [this config]
      (setup!))
    (open! [this]
      (clerk/serve! {:browse? true}))
    (close! [this])
    (show! [this value kind-override]
      (show! value))))


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
  {:transform-fn clerk/mark-presented
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
   :transform-fn clerk/mark-presented
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


;; Patching Clerk to pass metadata freely:

(in-ns 'nextjournal.clerk.eval)

;; identical to `nextjournal.clerk.eval/read+eval-cached` but keeping all metadata on form
;; see removed in clay comment
(defn read+eval-cached [{:as _doc doc-visibility :visibility :keys [blob->result ->analysis-info ->hash]} codeblock]
  (let [{:keys [form vars var deref-deps]} codeblock
        {:as form-info :keys [ns-effect? no-cache? freezable?]} (->analysis-info (if (seq vars) (first vars) form))
        no-cache?      (or ns-effect? no-cache?)
        hash           (when-not no-cache? (or (get ->hash (if var var form))
                                               (analyzer/hash-codeblock ->hash codeblock)))
        digest-file    (when hash (->cache-file (str "@" hash)))
        cas-hash       (when (and digest-file (fs/exists? digest-file)) (slurp digest-file))
        visibility     (if-let [fv (parser/->visibility form)] fv doc-visibility)
        cached-result? (and (not no-cache?)
                            cas-hash
                            (-> cas-hash ->cache-file fs/exists?))
        opts-from-form-meta (-> (meta form)
                                #_(select-keys [:nextjournal.clerk/viewer :nextjournal.clerk/viewers :nextjournal.clerk/width :nextjournal.clerk/opts]) ;; removed in clay
                                v/normalize-viewer-opts
                                maybe-eval-viewers)]
    #_(prn :cached? (cond no-cache? :no-cache
                          cached-result? true
                          cas-hash :no-cas-file
                          :else :no-digest-file)
           :hash hash :cas-hash cas-hash :form form :var var :ns-effect? ns-effect?)
    (fs/create-dirs config/cache-dir)
    (cond-> (or (when-let [blob->result (and (not no-cache?) (get-in blob->result [hash :nextjournal/value]))]
                  (wrapped-with-metadata blob->result visibility hash))
                (when (and cached-result? freezable?)
                  (lookup-cached-result var hash cas-hash visibility))
                (eval+cache! form-info hash digest-file visibility))
      (seq opts-from-form-meta)
      (merge opts-from-form-meta))))
