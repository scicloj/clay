(ns scicloj.clay.v2.tool.scittle.view
  (:require [scicloj.clay.v2.tool.scittle.widget :as widget]
            [scicloj.kindly-advice.v1.api :as kindly-advice]
            [scicloj.clay.v2.html.table :as table]
            [clojure.string :as string]
            [scicloj.clay.v2.util.image :as util.image]
            [clojure.walk :as walk]
            [jsonista.core :as jsonista])
  (:import java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(def *kind->viewer
  (atom {}))

(defn add-viewer!
  [kind viewer]
  (swap! *kind->viewer assoc kind viewer)
  [:ok])

(defn value->kind [v]
  (-> {:value v}
      kindly-advice/advise
      :kind))

(defn prepare [{:as context
                :keys [value]}
               {:keys [fallback-viewer]}]
  (when-let [viewer (-> context
                        kindly-advice/advise
                        :kind
                        (@*kind->viewer)
                        (or fallback-viewer))]
    (viewer value)))

(defn prepare-or-pprint [context]
  (prepare context {:fallback-viewer widget/pprint}))

(defn prepare-or-str [context]
  (prepare context {:fallback-viewer str}))

(defn prepare-or-keep [context]
  (prepare context {:fallback-viewer identity}))

(defn has-kind-with-viewer? [value]
  (some-> value
          value->kind
          (@*kind->viewer)))

(add-viewer!
 :kind/println
 widget/just-println)

(add-viewer!
 :kind/pprint
 widget/pprint)

(add-viewer!
 :kind/void
 (constantly
  (widget/mark-plain-html
   [:p ""])))

(add-viewer!
 :kind/md
 widget/md)

(add-viewer!
 :kind/table
 (fn [table-spec]
   (widget/mark-plain-html
    [:div
     (let [pre-hiccup (table/->table-hiccup
                       table-spec)
           hiccup (->> pre-hiccup
                       (walk/prewalk (fn [elem]
                                       (if (and (vector? elem)
                                                (-> elem first (= :td)))
                                         ;; a table data cell - handle it
                                         (-> elem
                                             (update
                                              1
                                              (fn [value]
                                                (prepare-or-str
                                                 {:value value}))))
                                         ;; else - keep it
                                         elem))))]
       (if (-> hiccup
               last ; the :tbody part
               count
               (> 20)) ; a big table
         (into hiccup
               [[:script "new DataTable(document.currentScript.parentElement,
 {\"sPaginationType\": \"full_numbers\", \"order\": []}});"]
                'datatables ; to help Clay realize that th dependency is needed
                ])
         hiccup))])))

(defn view-sequentially [value open-mark close-mark]
  (if (->> value
           (some has-kind-with-viewer?))
    (let [prepared-parts (->> value
                              (map (fn [subvalue]
                                     (prepare-or-pprint {:value subvalue}))))]
      (if (->> prepared-parts
               (some (fn [part]
                       (-> part meta :clay/printed-clojure? not))))
        ;; some parts are not just printed values - handle recursively
        [:div
         (widget/structure-mark open-mark)
         (into [:div {:style {}}]
               prepared-parts)
         (widget/structure-mark close-mark)]
        ;; else -- just print the whole value
        (widget/pprint value)))
    ;; else -- just print the whole value
    (widget/pprint value)))


(defn vega-embed [spec]
  (widget/mark-plain-html
   [:div
    'vega ; to help Clay realize that the dependency is needed
    [:script (->> spec
                  jsonista/write-value-as-string
                  (format "vegaEmbed(document.currentScript.parentElement, %s);"))]]))

(add-viewer!
 :kind/vega
 vega-embed)

(add-viewer!
 :kind/vega-lite
 vega-embed)

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
 :kind/plotly
 (partial
  expand-options-if-vector
  'plotly))

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
   [:div
    (-> v
        println
        with-out-str
        widget/md)]))

(add-viewer!
 :kind/image
 (fn [image]
   [:img {:src (-> image
                   util.image/buffered-image->byte-array
                   util.image/byte-array->data-uri)}]))

(defn bool->hiccup [bool]
  (widget/mark-plain-html
   [:div
    [:big [:big (if bool
                  [:big {:style {:color "darkgreen"}}
                   "✓"]
                  [:big {:style {:color "darkred"}}
                   "❌"])]]]))

(add-viewer!
 :kind/test
 (fn [t]
   (let [ret (-> t
                 meta
                 :test
                 (#(%)))]
     (if (boolean? ret)
       (bool->hiccup ret)
       (prepare-or-pprint {:value ret})))))

(add-viewer!
 :kind/map
 (fn [t]
   [:p "NA"]))

(defn spy [x tag]
  (clojure.pprint/pprint [tag x])
  x)

(add-viewer!
 :kind/map
 (fn [value]
   (if (->> value
            (apply concat)
            (some has-kind-with-viewer?))
     (let [prepared-kv-pairs (->> value
                                  (map (fn [kv]
                                         {:kv kv
                                          :prepared-kv (->> kv
                                                            (map #(prepare-or-pprint {:value %})))})))]
       (if (->> prepared-kv-pairs
                (map :prepared-kv)
                (apply concat)
                (some #(-> % meta :clay/printed-clojure? not)))
         ;; some parts are not just printed values - handle recursively
         [:div
          (widget/structure-mark "{")
          (->> prepared-kv-pairs
               (map (fn [{:keys [kv prepared-kv]}]
                      (if (->> prepared-kv
                               (some #(-> % meta :clay/printed-clojure? not)))
                        (let [[pk pv] prepared-kv]
                          [:table
                           [:tr
                            [:td {:valign :top}
                             pk]
                            [:td [:div
                                  {:style {:margin-top "10px"
                                           ;; :border "1px inset"
                                           }}
                                  pv]]]])
                        ;; else
                        (->> kv
                             (map pr-str)
                             (string/join " ")
                             widget/printed-clojure))))
               (into [:div
                      {:style {:margin-left "10%"
                               :width "110%"}}]))
          (widget/structure-mark "}")]
         ;; else -- just print the whole value
         (widget/pprint value)))
     ;; else -- just print the whole value
     (widget/pprint value))))


(defn view-sequentially [value open-mark close-mark]
  (if (->> value
           (some has-kind-with-viewer?))
    (let [prepared-parts (->> value
                              (map (fn [subvalue]
                                     (prepare-or-pprint {:value subvalue}))))]
      (if (->> prepared-parts
               (some (fn [part]
                       (-> part meta :clay/printed-clojure? not))))
        ;; some parts are not just printed values - handle recursively
        [:div
         (widget/structure-mark open-mark)
         (into [:div {:style {:margin-left "10%"
                              :width "110%"}}]
               prepared-parts)
         (widget/structure-mark close-mark)]
        ;; else -- just print the whole value
        (widget/pprint value)))
    ;; else -- just print the whole value
    (widget/pprint value)))

(add-viewer!
 :kind/vector
 (fn [value]
   (view-sequentially value "[" "]")))

(add-viewer!
 :kind/seq
 (fn [value]
   (view-sequentially value "(" ")")))

(add-viewer!
 :kind/set
 (fn [value]
   (view-sequentially value "#{" "}")))

(def non-hiccup-kind?
  (complement #{:kind/vector :kind/map :kind/seq :kind/set
                :kind/hiccup}))

(add-viewer!
 :kind/hiccup
 (fn [form]
   (let [result (->> form
                     (walk/postwalk
                      (fn [subform]
                        (let [context {:value subform}]
                          (if (some-> context
                                      kindly-advice/advise
                                      :kind
                                      non-hiccup-kind?)
                            (prepare-or-pprint context)
                            subform)))))]
     result)))
