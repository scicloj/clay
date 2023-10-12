(ns scicloj.clay.v2.prepare
  (:require
   [clojure.string :as string]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.table :as table]
   [scicloj.clay.v2.walk :as claywalk]
   [scicloj.kindly-advice.v1.api :as kindly-advice]
   [nextjournal.markdown :as md]
   [scicloj.clay.v2.portal :as portal]))

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

(defn item->hiccup [item {:keys [id]}]
  (cond
    ;;
    (:reagent item)
    [:div {:id id}
     [:code "reagent-based components are unsuppored in this version"]]
    ;;
    (:hiccup item)
    (:hiccup item)
    ;;
    (:md item)
    (-> item
        :md
        md/->hiccup)))

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
  (prepare context {:fallback-preparer item/pprint}))

(defn prepare-or-str [context]
  (prepare context {:fallback-preparer str}))

(defn has-kind-with-preparer? [value]
  (some-> value
          value->kind
          (@*kind->preparer)))

(add-preparer!
 :kind/println
 #'item/just-println)

(add-preparer!
 :kind/pprint
 #'item/pprint)

(add-preparer!
 :kind/void
 (constantly item/void))

(add-preparer!
 :kind/md
 #'item/md)

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
     {:hiccup (if (-> hiccup
                      last ; the :tbody part
                      count
                      (> 20)) ; a big table
                (into hiccup
                      [[:script "new DataTable(document.currentScript.parentElement,
 {\"sPaginationType\": \"full_numbers\", \"order\": []});"]
                       'datatables ; to help Clay realize that th dependency is needed
                       ])
                hiccup)
      :deps ['datatables]})))



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
         (item/structure-mark open-mark)
         (into [:div {:style {}}]
               prepared-parts)
         (item/structure-mark close-mark)]
        ;; else -- just print the whole value
        (item/pprint value)))
    ;; else -- just print the whole value
    (item/pprint value)))

(add-preparer!
 :kind/vega
 #'item/vega-embed)

(add-preparer!
 :kind/vega-lite
 #'item/vega-embed)

(add-preparer!
 :kind/cytoscape
 (partial #'item/reagent 'cytoscape))

(add-preparer!
 :kind/echarts
 (partial #'item/reagent 'echarts))

(add-preparer!
 :kind/plotly
 (partial #'item/reagent 'plotly))

(add-preparer!
 :kind/code
 (fn [codes]
   (->> codes
        item/source-clojure)))

(add-preparer!
 :kind/dataset
 (fn [v]
   (-> v
       println
       with-out-str
       item/md)))

(add-preparer!
 :kind/image
 #'item/image)

(add-preparer!
 :kind/test
 (fn [t]
   (prepare-or-pprint
    {:value (-> t
                meta
                :test
                (#(%)))})))

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
                (some (complement :printed-clojure)))
         ;; some parts are not just printed values - handle recursively
         {:hiccup [:div
                   (item/structure-mark "{")
                   (->> prepared-kv-pairs
                        (map (fn [{:keys [kv prepared-kv]}]
                               (if (->> prepared-kv
                                        (some (complement :printed-clojure)))
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
                                      item/printed-clojure))))
                        (into [:div
                               {:style {:margin-left "10%"
                                        :width "110%"}}]))
                   (item/structure-mark "}")]}
         ;; else -- just print the whole value
         (item/pprint value)))
     ;; else -- just print the whole value
     (item/pprint value))))



(defn view-sequentially [value open-mark close-mark]
  (if (->> value
           (some has-kind-with-preparer?))
    (let [prepared-parts (->> value
                              (map (fn [subvalue]
                                     (prepare-or-pprint {:value subvalue}))))]
      (if (->> prepared-parts
               (some (complement :printed-clojure)))
        ;; some parts are not just printed values - handle recursively
        {:hiccup [:div
                  (item/structure-mark open-mark)
                  (into [:div {:style {:margin-left "10%"
                                       :width "110%"}}]
                        prepared-parts)
                  (item/structure-mark close-mark)]}
        ;; else -- just print the whole value
        (item/pprint value)))
    ;; else -- just print the whole value
    (item/pprint value)))

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
   {:hiccup (->> form
                 (claywalk/prewalk
                  (fn [subform]
                    (let [context {:value subform}]
                      (if (some-> context
                                  kindly-advice/advise
                                  :kind
                                  non-hiccup-kind?)
                        (-> context
                            prepare-or-pprint
                            (item->hiccup nil))
                        subform)))))}))



(add-preparer!
 :kind/portal
 (fn [value]
   {:hiccup (-> value
                (vary-meta dissoc :kindly/kind)
                portal/in-portal)
    :deps ['portal]}))
