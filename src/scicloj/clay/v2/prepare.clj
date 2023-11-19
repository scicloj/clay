(ns scicloj.clay.v2.prepare
  (:require
   [clojure.string :as string]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.table :as table]
   [scicloj.clay.v2.util.walk :as claywalk]
   [scicloj.kindly-advice.v1.api :as kindly-advice]
   [nextjournal.markdown :as md]
   [clojure.walk]
   [hiccup.core :as hiccup]))

(def *kind->preparer
  (atom {}))

(defn add-preparer!
  [kind preparer]
  (swap! *kind->preparer assoc kind preparer)
  [:ok])

(defn preparer-from-value-fn [f]
  (comp f :value))

(defn add-preparer-from-value-fn!
  [kind f]
  (->> f
       preparer-from-value-fn
       (add-preparer! kind)))

(defn value->kind [v]
  (-> {:value v}
      kindly-advice/advise
      :kind))

(defn item->hiccup [item {:keys [id]}]
  (or
   (:hiccup item)
   (some->> item
            :html
            (vector :div))
   (some->> item
            :md
            md/->hiccup
            (clojure.walk/postwalk-replace
             {:<> :p}))))

(defn item->md [item {:keys [id]}]
  (or
   (:md item)
   (:html item)
   (some->> item
            :hiccup
            (clojure.walk/postwalk
             (fn [subform]
               (if (and (vector? subform)
                        (-> subform first (= :p)))
                 (->> subform
                      (mapv (fn [subsubform]
                              (if (string? subsubform)
                                (-> subsubform
                                    (clojure.string/replace "[" (str \\ "["))
                                    (clojure.string/replace "]" (str \\ "]")))
                                subsubform))))
                 ;; else
                 subform)))
            hiccup/html)))


(defn prepare [{:as context
                :keys [value]}
               {:keys [fallback-preparer]}]
  (when-let [preparer (-> context
                          kindly-advice/advise
                          :kind
                          (@*kind->preparer)
                          (or fallback-preparer))]
    (preparer context)))

(defn prepare-or-pprint [context]
  (prepare context {:fallback-preparer
                    (preparer-from-value-fn #'item/pprint)}))

(defn prepare-or-str [context]
  (prepare context {:fallback-preparer
                    (preparer-from-value-fn #'item/md)}))

(defn has-kind-with-preparer? [value]
  (some-> value
          value->kind
          (@*kind->preparer)))

(add-preparer-from-value-fn!
 :kind/println
 #'item/just-println)

(add-preparer-from-value-fn!
 :kind/pprint
 #'item/pprint)

(add-preparer-from-value-fn!
 :kind/hidden
 (constantly item/hidden))

(add-preparer-from-value-fn!
 :kind/md
 #'item/md)

(add-preparer-from-value-fn!
 :kind/table
 (fn [table-spec]
   (let [pre-hiccup (table/->table-hiccup
                     table-spec)
         *deps (atom []) ; TODO: implement without mutable state
         hiccup (->> pre-hiccup
                     (claywalk/postwalk
                      (fn [elem]
                        (if (and (vector? elem)
                                 (-> elem first (= :td)))
                          ;; a table data cell - handle it
                          (-> elem
                              (update
                               1
                               (fn [value]
                                 (let [item (prepare-or-str
                                             {:value value})]
                                   (swap! *deps concat (:deps item))
                                   (item->hiccup item nil)))))
                          ;; else - keep it
                          elem))))]
     (if (-> hiccup
             last ; the :tbody part
             count
             (> 20)) ; a big table
       ;; a big table
       {:hiccup (into hiccup
                      [[:script "new DataTable(document.currentScript.parentElement,
 {\"sPaginationType\": \"full_numbers\", \"order\": []});"]
                       'datatables ; to help Clay realize that th dependency is needed
                       ])
        :deps (->> @*deps
                   (cons :datatables)
                   distinct)}
       ;; else - a small table
       {:hiccup hiccup
        :deps (distinct @*deps)}))))

(defn structure-mark-hiccup [mark]
  (-> mark
      item/structure-mark
      :hiccup))

(add-preparer-from-value-fn!
 :kind/vega
 #'item/vega-embed)

(add-preparer-from-value-fn!
 :kind/vega-lite
 #'item/vega-embed)

(add-preparer-from-value-fn!
 :kind/code
 (fn [codes]
   (->> codes
        item/source-clojure)))

(add-preparer-from-value-fn!
 :kind/dataset
 (fn [v]
   (-> v
       println
       with-out-str
       item/md)))



(add-preparer-from-value-fn!
 :kind/test
 (fn [t]
   (prepare-or-pprint
    {:value (-> t
                meta
                :test
                (#(%)))})))

(add-preparer-from-value-fn!
 :kind/map
 (fn [value]
   (if (->> value
            (apply concat)
            (some has-kind-with-preparer?))
     (let [*deps (atom []) ; TODO: implement without mutable state
           prepared-kv-pairs (->> value
                                  (map (fn [kv]
                                         {:kv kv
                                          :prepared-kv
                                          (->> kv
                                               (map (fn [v]
                                                      (let [item (prepare-or-pprint
                                                                  {:value v})]
                                                        (swap! *deps concat (:deps item))
                                                        (item->hiccup item nil)))))})))]
       (if (->> prepared-kv-pairs
                (map :prepared-kv)
                (apply concat)
                (some (complement :printed-clojure)))
         ;; some parts are not just printed values - handle recursively
         {:hiccup [:div
                   (structure-mark-hiccup "{")
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
                                      item/printed-clojure
                                      (item->hiccup nil)))))
                        (into [:div
                               {:style {:margin-left "10%"
                                        :width "110%"}}]))
                   (structure-mark-hiccup "}")]
          :deps (distinct @*deps)}
         ;; else -- just print the whole value
         (item/pprint value)))
     ;; else -- just print the whole value
     (item/pprint value))))

(defn view-sequentially [value open-mark close-mark]
  (if (->> value
           (some has-kind-with-preparer?))
    (let [*deps (atom []) ; TODO: implement without mutable state
          prepared-parts (->> value
                              (map (fn [subvalue]
                                     (prepare-or-pprint {:value subvalue}))))]
      (if (->> prepared-parts
               (some (complement :printed-clojure)))
        ;; some parts are not just printed values - handle recursively
        {:hiccup [:div
                  (structure-mark-hiccup open-mark)
                  (into [:div {:style {:margin-left "10%"
                                       :width "110%"}}]
                        (->> prepared-parts
                             (map #(item->hiccup % nil))))
                  (structure-mark-hiccup close-mark)]
         :deps (->> prepared-parts
                    (mapcat :deps)
                    distinct)}
        ;; else -- just print the whole value
        (item/pprint value)))
    ;; else -- just print the whole value
    (item/pprint value)))


(add-preparer-from-value-fn!
 :kind/vector
 (fn [value]
   (view-sequentially value "[" "]")))

(add-preparer-from-value-fn!
 :kind/seq
 (fn [value]
   (view-sequentially value "(" ")")))

(add-preparer-from-value-fn!
 :kind/set
 (fn [value]
   (view-sequentially value "#{" "}")))

(def next-id
  (let [*counter (atom 0)]
    #(str "id" (swap! *counter inc))))

(add-preparer-from-value-fn!
 :kind/reagent
 #'item/reagent)

(def non-hiccup-kind?
  (complement #{:kind/vector :kind/map :kind/seq :kind/set
                :kind/hiccup}))

(add-preparer-from-value-fn!
 :kind/hiccup
 (fn [form]
   (let [*deps (atom []) ; TODO: implement without mutable state
         hiccup (->> form
                     (claywalk/prewalk
                      (fn [subform]
                        (let [context {:value subform}]
                          (if (some-> context
                                      kindly-advice/advise
                                      :kind
                                      non-hiccup-kind?)
                            (let [item (prepare-or-pprint
                                        context)]
                              (swap! *deps concat (:deps item))
                              (item->hiccup item nil))
                            subform)))))]
     {:hiccup hiccup
      :deps (distinct @*deps)})))

(add-preparer-from-value-fn!
 :kind/html
 #'item/html)

(add-preparer-from-value-fn!
 :kind/portal
 (fn [value]
   (-> value
       (vary-meta dissoc :kindly/kind)
       item/portal)))


(add-preparer-from-value-fn!
 :kind/cytoscape
 #'item/cytoscape)

(add-preparer-from-value-fn!
 :kind/echarts
 #'item/echarts)

(add-preparer-from-value-fn!
 :kind/plotly
 #'item/plotly)

(add-preparer!
 :kind/image
 #'item/image)
