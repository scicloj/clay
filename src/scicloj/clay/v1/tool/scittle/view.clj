(ns scicloj.clay.v1.tool.scittle.view
  (:require [scicloj.clay.v1.tool.scittle.widget :as widget]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.clay.v1.html.table :as table]
            [cybermonday.core :as cybermonday]
            [clojure.string :as string]))




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

(defn div? [v]
  (and (vector? v)
       (-> v first (= :div))))

(defn prepare
  ([value]
   (prepare value nil))
  ([value kind-override]
   (-> (let [kind (or kind-override
                      (kindly/kind value))]
         (cond (div? value) (prepare-div value)
               kind (maybe-apply-viewer value kind)
               (vector? value) (prepare-vector value)
               (seq? value) (prepare-seq value)
               (map? value) (prepare-map value)
               :else (widget/naive value)))
       ;; keep all metadata except for maybe the kind,
       ;; that might have been replaced
       (vary-meta merge
                  (-> value meta (dissoc :kindly/kind))))))

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
  (if (->> value
           (some kindly/kind))
    [:div
     (widget/structure-mark "[")
     (->> value
          (map prepare)
          (into [:div
                 {:style {:margin-left "10%"}}]))
     (widget/structure-mark "]")]
    ;; else
    (widget/naive value)))

(defn prepare-seq [value]
  (if (->> value
           (some kindly/kind))
    [:div
     (widget/structure-mark "(")
     (->> value
          (map prepare)
          (into [:div
                 {:style {:margin-left "10%"}}]))
     (widget/structure-mark ")")]
    ;; else
    (widget/naive value)))

(defn prepare-map [value]
  (if (or (->> value
               vals
               (some kindly/kind))
          (->> value
               keys
               (some kindly/kind)))
    [:div
     (widget/structure-mark "{")
     (->> value
          (map (fn [[k v]]
                 (if (->> [k v]
                          (some kindly/kind))
                   [:div
                    (prepare k)
                    (prepare v)]
                   ;; else
                   (->> [k v]
                        (map pr-str)
                        (string/join " ")
                        widget/clojure))))
          (into [:div
                 {:style {:margin-left "10%"}}]))
     (widget/structure-mark "}")]
    ;; else
    (widget/naive value)))

(defn expand-options-if-vector [component-symbol options]
  (cond ;;
    (vector? options)
    (->> options
         (map (fn [option]
                (list 'quote option)))
         (into [component-symbol]))
    ;;
    (map? options)
    [component-symbol (list 'quote options)]))


(kindly/define-kind-behaviour! :kind/naive
  {:scittle.viewer widget/naive})

(kindly/define-kind-behaviour! :kind/hiccup
  {:scittle.viewer (fn [v] v)})

(defn render-md [v]
  (->> v
       ((fn [v]
          (if (vector? v) v [v])))
       (map (fn [md]
              (->> md
                   println
                   with-out-str
                   cybermonday/parse-md
                   :body)))
       (into [:div])
       widget/mark-plain-html))

(kindly/define-kind-behaviour! :kind/md
  {:scittle.viewer render-md})


(kindly/define-kind-behaviour! :kind/table-md
  {:scittle.viewer
   (fn [v]
     [:code (render-md v)])})

(kindly/define-kind-behaviour! :kind/table
  {:scittle.viewer
   (fn [table-spec]
     [:div
      (let [hiccup (table/->table-hiccup
                    table-spec)]
        (if (or (some-> table-spec
                        :row-maps
                        count
                        (> 20))
                (some-> table-spec
                        :row-vectors
                        count
                        (> 20)))
          ['datatables hiccup]
          hiccup))])})



(kindly/define-kind-behaviour! :kind/vega
  {:scittle.viewer (fn [spec]
                     [:div
                      ['vega (list 'quote spec)]])})

(kindly/define-kind-behaviour! :kind/cytoscape
  {:scittle.viewer  (partial
                     expand-options-if-vector
                     'cytoscape)})

(kindly/define-kind-behaviour! :kind/echarts
  {:scittle.viewer (partial
                    expand-options-if-vector
                    'echarts)})

(kindly/define-kind-behaviour! :kind/code
  {:scittle.viewer (fn [codes]
                     (->> codes
                          (map widget/code)
                          (into [:div])
                          widget/mark-plain-html))})
