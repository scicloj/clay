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
   [charred.api :as charred]
   [scicloj.clay.v2.util.merge :as merge]
   [scicloj.clay.v2.files :as files]
   [clojure.string :as str]))

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

(defn item->hiccup [{:keys [hiccup html md
                            script]}
                    {:as context
                     :keys [format]}]
  (-> (or hiccup
          (some->> html
                   (vector :div))
          (when md
            (if (and (vector? format)
                     (-> format first (= :quarto)))
              [:span {:data-qmd md}]
              (->> md
                   md/->hiccup
                   (clojure.walk/postwalk-replace {:<> :p})
                   (clojure.walk/postwalk-replace {:table :table.table})))))
      (cond-> script
        (conj script))))


(defn item->md [{:keys [hiccup html md
                        script]}]
  (if script
    (-> hiccup
        (conj script)
        hiccup/html)
    (-> (or md
            (format "\n```{=html}\n%s\n```\n"
                    (or html
                        (some-> hiccup hiccup/html)))))))

(defn limit-hiccup-height [hiccup context]
  (when hiccup
    (if-let [max-element-height (-> context
                                    :kindly/options
                                    :element/max-height)]
      [:div {:style {:max-height max-element-height
                     :overflow-y :auto}}
       hiccup]
      hiccup)))

(defn limit-md-height [md context]
  (when md
    (if-let [max-element-height (-> context
                                    :kindly/options
                                    :element/max-height)]
      (hiccup/html
       [:div {:style {:max-height max-element-height
                      :overflow-y :auto}}
        md])
      md)))

(defn advise-if-needed [context]
  (if (:advise context)
    context
    (kindly-advice/advise context)))

(defn prepare [{:as context
                :keys [value]}
               {:keys [fallback-preparer]}]
  (let [complete-context (-> context
                             (update :kindly/options
                                     merge/deep-merge
                                     (-> value meta :kindly/options)))
        kind (-> complete-context
                 advise-if-needed
                 :kind)]
    (if (= kind :kind/fragment)
      ;; splice the fragment
      (->> value
           (mapcat (fn [subvalue]
                     (-> context
                         (assoc :value subvalue)
                         (prepare {fallback-preparer fallback-preparer})))))
      ;; else - not a fragment
      (when-let [preparer (-> kind
                              (@*kind->preparer)
                              (or fallback-preparer))]
        [(-> complete-context
             preparer
             (update :hiccup limit-hiccup-height complete-context)
             (update :md limit-md-height complete-context))]))))

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

(defn table-hiccup->paged-reagent-table [table-hiccup
                                         {:as context
                                          :keys [base-target-path
                                                 full-target-path]}]
  (let [page-size (-> context
                      :kindly/options
                      :table/page-size)
        tbody-idx (->> table-hiccup
                       (map-indexed (fn [i v]
                                      (when (and (vector? v)
                                                 (-> v first (= :tbody)))
                                        i)))
                       (filter some?)
                       first)
        rows (-> table-hiccup
                 (get tbody-idx)
                 rest
                 vec)
        table-hiccup-without-rows (-> table-hiccup
                                      (assoc tbody-idx '[:tbody]))
        page-paths (->> rows
                        (partition page-size)
                        (mapv (fn [page-rows]
                                (let [full-path (files/next-file!
                                                 full-target-path
                                                 ""
                                                 page-rows
                                                 ".edn")
                                      path (string/replace
                                            full-path
                                            (re-pattern
                                             (str "^"
                                                  base-target-path
                                                  "/"))
                                            "")]
                                  (->> page-rows
                                       pr-str
                                       (spit full-path))
                                  path))))]
    ['(fn [[table-hiccup-without-rows
            tbody-idx
            page-paths]]
        (let [*state (reagent.core/atom
                      {:pages (-> page-paths
                                  count
                                  (repeat nil)
                                  vec)
                       :max-idx-fetched -1
                       :current-idx 0})
              ;;
              page-component
              (fn [i path]
                (fn []
                  (reagent.core/create-class
                   {:reagent-render
                    (fn []
                      (let [{:keys [pages max-idx-fetched current-idx]} @*state
                            page (pages i)]
                        [:div
                         (if page
                           (into (->> table-hiccup-without-rows
                                      (filter (fn [v]
                                                (not
                                                 (and (vector? v)
                                                      (-> v first (= :thead))))))
                                      vec)
                                 page)
                           ;; else
                           [:div {:style {:height "20px"}
                                  :on-click
                                  (fn []
                                    (when (> i current-idx)
                                      (swap! *state assoc :current-idx i)))}
                            [:p "..."]])]))
                    :component-did-update
                    (fn []
                      (let [{:keys [pages max-idx-fetched current-idx]} @*state]
                        (when (> current-idx max-idx-fetched)
                          (->> (range (inc max-idx-fetched)
                                      (inc current-idx))
                               (run!
                                (fn [j]
                                  (promesa.core/let [_ (promesa.core/delay 100)
                                                     response (-> j
                                                                  page-paths
                                                                  js/fetch)
                                                     edn (.text response)]
                                    (swap! *state
                                           (fn [state]
                                             (-> state
                                                 (assoc-in [:pages j]
                                                           (read-string edn))
                                                 (assoc :max-idx-fetched current-idx)))))))))))})))]
          (fn []
            (into [:div
                   (-> @*state
                       (select-keys [:max-idx-fetched
                                     :current-idx])
                       pr-str)
                   table-hiccup-without-rows]
                  (->> page-paths
                       (map-indexed (fn [i path]
                                      ^{:key i}
                                      [page-component i path])))))))
     [table-hiccup-without-rows
      tbody-idx
      page-paths]]))

(defn prepare-table [{:as context
                      :keys [value]}]
  (let [page-size (-> context
                      :kindly/options
                      :table/page-size)
        {:keys [use-datatables]} (:kindly/options context)
        pre-hiccup (table/->table-hiccup value)
        *deps (atom []) ; TODO: implement without mutable state
        hiccup (->> pre-hiccup
                    (claywalk/postwalk
                     (fn [elem]
                       (if (and (vector? elem)
                                (-> elem first (= :td)))
                         ;; a table data cell - handle it
                         (let [items (-> context
                                         (dissoc :form)
                                         (update :kindly/options :dissoc :element/max-height)
                                         (assoc :value (second elem))
                                         prepare-or-str)]
                           (swap! *deps concat (mapcat :deps items))
                           (->> items
                                (map #(item->hiccup
                                       %
                                       (-> context
                                           (cond-> (or page-size use-datatables)
                                             (assoc :format [:html])))))
                                (into [:td])))
                         ;; else - keep it
                         elem))))]
    (cond page-size (-> context
                        (assoc :value (table-hiccup->paged-reagent-table
                                       hiccup
                                       context))
                        item/reagent)
          use-datatables {:hiccup hiccup
                          :script [:script
                                   (->> context
                                        :kindly/options
                                        :datatables
                                        charred/write-json-str
                                        (format "new DataTable(document.currentScript.parentElement, %s);"))]
                          :deps (->> @*deps
                                     (cons :datatables)
                                     distinct)}

          :else {:hiccup hiccup
                 :deps (distinct @*deps)})))

(add-preparer!
 :kind/table
 #'prepare-table)

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

(add-preparer-from-value-fn!
 :kind/smile-model
 (fn [v]
   (-> v
       str
       item/printed-clojure)))

(add-preparer!
 :kind/test
 (fn [{:as context
       :keys [value]}]
   (-> context
       (dissoc :form)
       (update :kindly/options :dissoc :element/max-height)
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
                                               (mapcat (fn [v]
                                                         (let [items (-> context
                                                                         (dissoc :form)
                                                                         (update :kindly/options :dissoc :element/max-height)
                                                                         (assoc :value v)
                                                                         prepare-or-pprint)]
                                                           (swap! *deps concat (mapcat :deps items))
                                                           items))))})))]
       (if (->> prepared-kv-pairs
                (mapcat :prepared-kv)
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
                                      (item->hiccup pk context)]
                                     [:td [:div
                                           {:style {:margin-left "10px"}}
                                           (item->hiccup pv context)]]]])
                                 ;; else
                                 (item->hiccup (->> kv
                                                    (map pr-str)
                                                    (string/join " ")
                                                    item/printed-clojure)
                                               context))))
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
                              (mapcat (fn [subvalue]
                                        (-> context
                                            (dissoc :form)
                                            (update :kindly/options :dissoc :element/max-height)
                                            (assoc :value subvalue)
                                            prepare-or-pprint))))]
      (if (->> prepared-parts
               (some (complement :printed-clojure)))
        ;; some parts are not just printed values - handle recursively
        {:hiccup [:div
                  (structure-mark-hiccup open-mark)
                  (->> prepared-parts
                       (map #(item->hiccup % context))
                       (into [:div {:style {:margin-left "10%"
                                            :width "110%"}}]))
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

(add-preparer!
 :kind/reagent
 #'item/reagent)

(def non-hiccup-kind?
  (complement #{:kind/vector :kind/map :kind/seq :kind/set
                :kind/hiccup}))

(defn wrap-with-div-if-many [hiccups]
  (if (-> hiccups
          count
          (> 1))
    (into [:div] hiccups)
    (first hiccups)))

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
                                             (update :kindly/options dissoc :element/max-height)
                                             (assoc :value subform))]
                          (if (some-> subcontext
                                      kindly-advice/advise
                                      :kind
                                      non-hiccup-kind?)
                            (let [items (prepare-or-pprint
                                         subcontext)]
                              (swap! *deps concat (mapcat :deps items))
                              (->> items
                                   (map #(item->hiccup % context))
                                   wrap-with-div-if-many))
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

(add-preparer-from-value-fn!
 :kind/video
 #'item/video)

(add-preparer-from-value-fn!
 :kind/observable
 #'item/observable)
