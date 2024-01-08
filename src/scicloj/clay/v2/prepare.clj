(ns scicloj.clay.v2.prepare
  (:require
   [clojure.string :as string]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.table :as table]
   [scicloj.clay.v2.util.walk :as claywalk]
   [scicloj.kindly-advice.v1.api :as kindly-advice]
   [nextjournal.markdown :as md]
   [clojure.walk]
   [hiccup.core :as hiccup]
   [charred.api :as charred]))

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
             {:<> :p})
            (clojure.walk/postwalk-replace
             {:table :table.table}))))

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
  (let [complete-context (-> context
                             (update :kindly/options
                                     merge (-> value meta :kindly/options)))]
    (when-let [preparer (-> complete-context
                            kindly-advice/advise
                            :kind
                            (@*kind->preparer)
                            (or fallback-preparer))]
      (preparer complete-context))))

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

(add-preparer!
 :kind/table
 (fn [{:as context
       :keys [value]}]
   (let [pre-hiccup (table/->table-hiccup value)
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
                               (fn [subvalue]
                                 (let [item (-> context
                                                (dissoc :form)
                                                (assoc :value subvalue)
                                                prepare-or-str)]
                                   (swap! *deps concat (:deps item))
                                   (item->hiccup item nil)))))
                          ;; else - keep it
                          elem))))]
     (if (->> context
              :kindly/options
              :use-datatables)
       {:hiccup (into hiccup
                      [[:script (->> context
                                     :kindly/options
                                     :datatables
                                     charred/write-json-str
                                     (format "new DataTable(document.currentScript.parentElement, %s);"))]])
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

(add-preparer!
 :kind/test
 (fn [{:as context
       :keys [value]}]
   (-> context
       (dissoc :form)
       (assoc :value (-> value
                         meta
                         :test
                         (#(%))))
       prepare-or-pprint)))

(add-preparer!
 :kind/map
 (fn [{:as context
       :keys [value]}]
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
                                                      (let [item (-> context
                                                                     (dissoc :form)
                                                                     (assoc :value v)
                                                                     prepare-or-pprint)]
                                                        (swap! *deps concat (:deps item))
                                                        #_(item->hiccup item nil)
                                                        item))))})))]
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
                                      (item->hiccup pk nil)]
                                     [:td [:div
                                           {:style {:margin-top "10px"
                                                    ;; :border "1px inset"
                                                    }}
                                           (item->hiccup pv nil)]]]])
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

(defn view-sequentially [{:as context
                          :keys [value]}
                         open-mark close-mark]
  (if (->> value
           (some has-kind-with-preparer?))
    (let [*deps (atom []) ; TODO: implement without mutable state
          prepared-parts (->> value
                              (map (fn [subvalue]
                                     (-> context
                                         (dissoc :form)
                                         (assoc :value subvalue)
                                         prepare-or-pprint))))]
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


(add-preparer!
 :kind/vector
 (fn [context]
   (view-sequentially context "[" "]")))

(add-preparer!
 :kind/seq
 (fn [context]
   (view-sequentially context "(" ")")))

(add-preparer!
 :kind/set
 (fn [context]
   (view-sequentially context "#{" "}")))

(def next-id
  (let [*counter (atom 0)]
    #(str "id" (swap! *counter inc))))

(add-preparer-from-value-fn!
 :kind/reagent
 #'item/reagent)

(def non-hiccup-kind?
  (complement #{:kind/vector :kind/map :kind/seq :kind/set
                :kind/hiccup}))

(add-preparer!
 :kind/hiccup
 (fn [{:as context
       :keys [value]}]
   (let [*deps (atom []) ; TODO: implement without mutable state
         hiccup (->> value
                     (claywalk/prewalk
                      (fn [subform]
                        (let [subcontext (-> context
                                             (dissoc :form)
                                             (assoc :value subform))]
                          (if (some-> subcontext
                                      kindly-advice/advise
                                      :kind
                                      non-hiccup-kind?)
                            (let [item (prepare-or-pprint
                                        subcontext)]
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


(add-preparer!
 :kind/cytoscape
 #'item/cytoscape)

(add-preparer!
 :kind/echarts
 #'item/echarts)

(add-preparer!
 :kind/plotly
 #'item/plotly)

(add-preparer!
 :kind/image
 #'item/image)

(add-preparer!
 :kind/vega
 #'item/vega-embed)

(add-preparer!
 :kind/vega-lite
 #'item/vega-embed)
