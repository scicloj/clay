(ns scicloj.clay.v1.tool.scittle.view
  (:require [scicloj.clay.v1.tool.scittle.hiccups :as hiccups]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.clay.v1.html.table :as table]))

(defn maybe-apply-viewer
  ([value]
   (maybe-apply-viewer value (kindly/kind value)))
  ([value kind]
   (if-let [viewer (-> kind kindly/kind->behaviour :scittle.viewer)]
     (let [new-value (viewer value)
           new-kind (kindly/kind new-value)]
       (if (some-> new-kind (not= kind))
         (maybe-apply-viewer new-value new-kind)
         new-value))
     value)))

(declare prepare-vector)
(declare prepare-seq)
(declare prepare-map)
(declare prepare-div)

(defn prepare-naive [value]
  (hiccups/code (pr-str value)))

(defn div? [v]
  (and (vector? v)
       (-> v first (= :div))))

(defn prepare [value]
  (let [kind (kindly/kind value)]
    (cond (div? value) (prepare-div value)
          kind (maybe-apply-viewer value kind)
          (vector? value) (prepare-vector value)
          (seq? value) (prepare-seq value)
          (map? value) (prepare-map value)
          :else (prepare-naive value))))


(defn prepare-div [v]
  (let [kind (kindly/kind v)]
    (-> (let [r (rest v)
              fr (first r)]
          (if (map? fr)
            (->> r
                 rest
                 (map (fn [subv]
                        (if (or (kindly/kind subv)
                                (div? subv))
                          (prepare subv)
                          subv)))
                 (into [:div fr]))
            (->> r
                 (map (fn [subv]
                        (if (or (kindly/kind subv)
                                (div? subv))
                          (prepare subv)
                          subv)))
                 (into [:div]))))
        (maybe-apply-viewer kind))))

(defn prepare-vector [value]
  [:div
   (hiccups/structure-mark "[")
   (->> value
        (map prepare)
        (into [:div
               {:style {:margin-left "10%"}}]))
   (hiccups/structure-mark "]")])

(defn prepare-seq [value]
  [:div
   (hiccups/structure-mark "(")
   (->> value
        (map prepare)
        (into [:div
               {:style {:margin-left "10%"}}]))
   (hiccups/structure-mark ")")])

(defn prepare-map [value]
  [:div
   (hiccups/structure-mark "{")
   (->> value
        (map (fn [[k v]]
               [:div
                (prepare k)
                (prepare v)]))
        (into [:div
               {:style {:margin-left "10%"}}]))
   (hiccups/structure-mark "}")])


(kindly/define-kind-behaviour! :kind/naive
  {:scittle.viewer (fn [v]
                     (hiccups/code (pr-str v)))})

(kindly/define-kind-behaviour! :kind/hiccup
  {:scittle.viewer (fn [v] v)})

(kindly/define-kind-behaviour! :kind/table
  {:scittle.viewer (fn [table-spec]
                     ['datatables
                      (-> table-spec
                          table/->table-hiccup)])})

(kindly/define-kind-behaviour! :kind/vega
  {:scittle.viewer (fn [spec]
                     [:div
                      ['vega (list 'quote spec)]])})

(kindly/define-kind-behaviour! :kind/cytoscape
  {:scittle.viewer (fn [spec]
                     [:div
                      ['cytoscape (list 'quote spec)]])})

(kindly/define-kind-behaviour! :kind/echarts
  {:scittle.viewer (fn [option]
                     (if (map? option)
                       ['echarts (list 'quote option)]
                       (->> option
                            (map (fn [op]
                                   (list 'quote op)))
                            (into ['echarts]))))})
