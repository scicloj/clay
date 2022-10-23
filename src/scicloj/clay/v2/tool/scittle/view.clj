(ns scicloj.clay.v2.tool.scittle.view
  (:require [scicloj.clay.v2.tool.scittle.widget :as widget]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.clay.v2.html.table :as table]
            [nextjournal.markdown :as md]
            [nextjournal.markdown.transform :as md.transform]
            [clojure.string :as string]
            [scicloj.clay.v2.util.image :as util.image])
  (:import java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(def *kind->viewer
  (atom {}))

(defn add-viewer!
  [kind viewer]
  (kindly/add-kind! kind)
  (swap! *kind->viewer assoc kind viewer))

(defn value->kind [v]
  (-> {:value v}
      kindly/advice
      :kind))

(defn maybe-apply-viewer
  ([{:as context
     :keys [value kind]}]
   (if-let [viewer (@*kind->viewer kind)]
     (let [value1 (viewer value)
           kind1 (value->kind value1)]
       (if (some-> kind1 (not= kind))
         (maybe-apply-viewer (assoc context
                                    :value value1
                                    :kind kind1))
         value1))
     value)))


(declare prepare-vector)
(declare prepare-seq)
(declare prepare-map)
(declare prepare-div)

(defn div? [v]
  (and (vector? v)
       (-> v first (= :div))))


(defn prepare
  ([context]
   (let [{:as context1
          :keys [value kind]} (kindly/advice context)]
     (cond kind (maybe-apply-viewer context1)
           (div? value) (prepare-div value)
           (vector? value) (prepare-vector value)
           (seq? value) (prepare-seq value)
           (map? value) (prepare-map value)
           :else (widget/pprint value)))))

(defn prepare-value [v]
  (prepare {:value v}))

(defn has-kind-with-viewer? [value]
  (some-> value
          value->kind
          (@*kind->viewer)))

(defn prepare-div [v]
  (maybe-apply-viewer
   {:value (-> (let [r (rest v)
                     fr (first r)]
                 (if (map? fr)
                   (->> r
                        rest
                        (map (fn [subv]
                               (if (or (has-kind-with-viewer? subv)
                                       (div? subv))
                                 (prepare-value subv)
                                 subv)))
                        (into [:div fr]))
                   (->> r
                        (map (fn [subv]
                               (if (or (has-kind-with-viewer? subv)
                                       (div? subv))
                                 (prepare-value subv)
                                 subv)))
                        (into [:div])))))
    :kind (value->kind v)}))

(defn prepare-vector [value]
  (if (->> value
           (some has-kind-with-viewer?))
    [:div
     (widget/structure-mark "[")
     (->> value
          (map prepare-value)
          (into [:div
                 {:style {:margin-left "10%"}}]))
     (widget/structure-mark "]")]
    ;; else
    (widget/pprint value)))


(defn prepare-seq [value]
  (if (->> value
           (some has-kind-with-viewer?))
    [:div
     (widget/structure-mark "(")
     (->> value
          (map prepare-value)
          (into [:div
                 {:style {:margin-left "10%"}}]))
     (widget/structure-mark ")")]
    ;; else
    (widget/pprint value)))

(defn prepare-map [value]
  (if (or (->> value
               vals
               (some has-kind-with-viewer?))
          (->> value
               keys
               (some has-kind-with-viewer?)))
    [:div
     (widget/structure-mark "{")
     (->> value
          (map (fn [[k v]]
                 (if (->> [k v]
                          (some value->kind))
                   [:div
                    (prepare-value k)
                    (prepare-value v)]
                   ;; else
                   (->> [k v]
                        (map pr-str)
                        (string/join " ")
                        widget/clojure))))
          (into [:div
                 {:style {:margin-left "10%"}}]))
     (widget/structure-mark "}")]
    ;; else
    (widget/pprint value)))

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

(add-viewer!
 :kind/println
 widget/just-println)

(add-viewer!
 :kind/pprint
 widget/pprint)

(add-viewer!
 :kind/hiccup
 (fn [v] v))

(defn render-md [v]
  (->> v
       ((fn [v]
          (if (vector? v) v [v])))
       (map (fn [md]
              (->> md
                   println
                   with-out-str
                   md/parse
                   md.transform/->hiccup)))
       (into [:div])
       widget/mark-plain-html))

(add-viewer!
 :kind/md
 render-md)


(add-viewer!
 :kind/table-md
 (fn [v]
   [:code (render-md v)]))

(add-viewer!
 :kind/table
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
        hiccup))]))

(add-viewer!
 :kind/vega
 (fn [spec]
   [:div
    ['vega (list 'quote spec)]]))

(add-viewer!
 :kind/cytoscape
 (partial
  expand-options-if-vector
  'cytoscape))

(add-viewer!
 :kind/echarts
 (partial
  expand-options-if-vector
  'echarts))

(add-viewer!
 :kind/code
 (fn [codes]
   (->> codes
        (map widget/code)
        (into [:div])
        widget/mark-plain-html)))

(add-viewer!
 :kind/dataset
 (fn [v]
   (-> v
       println
       with-out-str
       vector
       (kindly/consider :kind/table-md))
   #_(-> {:column-names (tmd/column-names v)
          :row-vectors (vec (tmd/rowvecs v))}
         (kindly/consider :kind/table))))


(add-viewer!
 :kind/buffered-image
 (fn [image]
   [:img {:src (-> image
                   util.image/buffered-image->byte-array
                   util.image/byte-array->data-uri)}]))
