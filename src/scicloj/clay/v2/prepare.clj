(ns scicloj.clay.v2.prepare
  (:require [scicloj.clay.v2.widget :as widget]
            [scicloj.kindly-advice.v1.api :as kindly-advice]
            [scicloj.clay.v2.table :as table]
            [clojure.string :as string]
            [scicloj.clay.v2.util.image :as util.image]
            [scicloj.clay.v2.walk :as claywalk]
            [jsonista.core :as jsonista])
  (:import java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(def *kind->preparer
  (atom {}))

(defn add-preparer!
  [kind preparer]
  (swap! *kind->preparer assoc kind preparer)
  [:ok])

(defn value->kind [v]
  (-> {:value v}
      kindly-advice/advise
      :kind))

(defn prepare [{:as context
                :keys [value]}
               {:keys [fallback-preparer]}]
  (when-let [preparer (-> context
                          kindly-advice/advise
                          :kind
                          (@*kind->preparer)
                          (or fallback-preparer))]
    (preparer value)))

(defn prepare-or-pprint [context]
  (prepare context {:fallback-preparer widget/pprint}))

(defn prepare-or-str [context]
  (prepare context {:fallback-preparer str}))

(defn has-kind-with-preparer? [value]
  (some-> value
          value->kind
          (@*kind->preparer)))

(add-preparer!
 :kind/println
 widget/just-println)

(add-preparer!
 :kind/pprint
 widget/pprint)

(add-preparer!
 :kind/void
 (constantly
  [:p ""]))

(add-preparer!
 :kind/md
 widget/md)

(add-preparer!
 :kind/table
 (fn [table-spec]
   (let [pre-hiccup (table/->table-hiccup
                     table-spec)
         hiccup (->> pre-hiccup
                     (claywalk/prewalk
                      (fn [elem]
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
 {\"sPaginationType\": \"full_numbers\", \"order\": []});"]
              'datatables ; to help Clay realize that th dependency is needed
              ])
       hiccup))))

(defn structure-mark [string]
  string)

(defn view-sequentially [value open-mark close-mark]
  (if (->> value
           (some has-kind-with-preparer?))
    (let [prepared-parts (->> value
                              (map (fn [subvalue]
                                     (prepare-or-pprint {:value subvalue}))))]
      (if (->> prepared-parts
               (some (fn [part]
                       (-> part meta :clay/printed-clojure? not))))
        ;; some parts are not just printed values - handle recursively
        [:div
         (structure-mark open-mark)
         (into [:div {:style {}}]
               prepared-parts)
         (structure-mark close-mark)]
        ;; else -- just print the whole value
        (widget/pprint value)))
    ;; else -- just print the whole value
    (widget/pprint value)))


(defn vega-embed [spec]
  [:div
   'vega ; to help Clay realize that the dependency is needed
   [:script (->> spec
                 jsonista/write-value-as-string
                 (format "vegaEmbed(document.currentScript.parentElement, %s);"))]])

(add-preparer!
 :kind/vega
 vega-embed)

(add-preparer!
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

(add-preparer!
 :kind/cytoscape
 (partial
  expand-options-if-vector
  'cytoscape))

(add-preparer!
 :kind/echarts
 (partial
  expand-options-if-vector
  'echarts))

(add-preparer!
 :kind/plotly
 (partial
  expand-options-if-vector
  'plotly))

(add-preparer!
 :kind/code
 (fn [codes]
   (->> codes
        (map widget/source-clojure)
        (into [:div]))))

(add-preparer!
 :kind/dataset
 (fn [v]
   (-> v
       println
       with-out-str
       widget/md)))

(add-preparer!
 :kind/image
 (fn [image]
   [:img {:src (-> image
                   util.image/buffered-image->byte-array
                   util.image/byte-array->data-uri)}]))

(defn bool->hiccup [bool]
  [:div
   [:big [:big (if bool
                 [:big {:style {:color "darkgreen"}}
                  "✓"]
                 [:big {:style {:color "darkred"}}
                  "❌"])]]])

(add-preparer!
 :kind/test
 (fn [t]
   (let [ret (-> t
                 meta
                 :test
                 (#(%)))]
     (if (boolean? ret)
       (bool->hiccup ret)
       (prepare-or-pprint {:value ret})))))

(add-preparer!
 :kind/map
 (fn [t]
   [:p "NA"]))

(defn spy [x tag]
  (clojure.pprint/pprint [tag x])
  x)

(add-preparer!
 :kind/map
 (fn [value]
   (if (->> value
            (apply concat)
            (some has-kind-with-preparer?))
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
          (structure-mark "{")
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
          (structure-mark "}")]
         ;; else -- just print the whole value
         (widget/pprint value)))
     ;; else -- just print the whole value
     (widget/pprint value))))



(defn view-sequentially [value open-mark close-mark]
  (if (->> value
           (some has-kind-with-preparer?))
    (let [prepared-parts (->> value
                              (map (fn [subvalue]
                                     (prepare-or-pprint {:value subvalue}))))]
      (if (->> prepared-parts
               (some (fn [part]
                       (-> part meta :clay/printed-clojure? not))))
        ;; some parts are not just printed values - handle recursively
        [:div
         (structure-mark open-mark)
         (into [:div {:style {:margin-left "10%"
                              :width "110%"}}]
               prepared-parts)
         (structure-mark close-mark)]
        ;; else -- just print the whole value
        (widget/pprint value)))
    ;; else -- just print the whole value
    (widget/pprint value)))

(add-preparer!
 :kind/vector
 (fn [value]
   (view-sequentially value "[" "]")))

(add-preparer!
 :kind/seq
 (fn [value]
   (view-sequentially value "(" ")")))

(add-preparer!
 :kind/set
 (fn [value]
   (view-sequentially value "#{" "}")))

(def non-hiccup-kind?
  (complement #{:kind/vector :kind/map :kind/seq :kind/set
                :kind/hiccup}))

(add-preparer!
 :kind/hiccup
 (fn [form]
   (->> form
        (claywalk/prewalk
         (fn [subform]
           (let [context {:value subform}]
             (if (some-> context
                         kindly-advice/advise
                         :kind
                         non-hiccup-kind?)
               (prepare-or-pprint context)
               subform)))))))
