(ns scicloj.clay.v2.prepare
  (:require
   [clojure.string :as string]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.table :as table]
   [scicloj.clay.v2.util.walk :as claywalk]
   [scicloj.kindly-advice.v1.api :as kindly-advice]
   [nextjournal.markdown :as md]
   [nextjournal.markdown.transform :as mdt]
   [clojure.walk]
   [hiccup.core :as hiccup]
   [charred.api :as charred]
   [scicloj.clay.v2.util.merge :as merge]
   [scicloj.kindly.v4.kind :as kind]))

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

(defn add-class-to-class-str [class-str cls]
  (if class-str
    (str class-str " " cls)
    cls))

(defn add-class-to-hiccup [hiccup cls]
  (if cls
    (if (-> hiccup second map?)
      (update-in hiccup [1 :class] add-class-to-class-str cls)
      (into [(first hiccup) {:class cls}] (rest hiccup)))
    hiccup))

(defn merge-attrs [hiccup x]
  (if (and x (vector? hiccup))
    (if (-> hiccup second map?)
      (update-in hiccup [1] merge/deep-merge x)
      (into [(first hiccup) x] (rest hiccup)))
    hiccup))

(defn vector-that-starts-with? [v x]
  (and (vector? v)
       (-> v first (= x))))

(def mdctx
  "NextJournal Markdown produces fragments (called :plain) which are rendered as :<>
  These are better represented as a sequence, which hiccup will treat as a fragment."
  (assoc mdt/default-hiccup-renderers
    :plain (fn [ctx {:keys [text content]}]
             (or text (map #(mdt/->hiccup ctx %) content)))))

(defn md->hiccup
  "Converts markdown to hiccup, producing sequences instead of fragments, which hiccup prefers."
  [md]
  (md/->hiccup mdctx md))


(defn complete-hiccup [hiccup {:as item
                               :keys [item-class script]}]
  (-> hiccup
      (add-class-to-hiccup item-class)
      (merge-attrs (some-> (:kindly/options item)
                           (select-keys [:style :class])))
      (cond-> script
        (conj script))))

(defn item->hiccup [{:as item
                     :keys [hiccup html md
                            inside-a-table]}
                    {:as clay-options
                     :keys [format kind]}]
  (-> (or hiccup
          (some->> html
                   (vector :div))
          (when md
            (if (and inside-a-table
                     (vector-that-starts-with?
                      format
                      :quarto))
              [:span {:data-qmd md}]
              (->> (md->hiccup md)
                   (clojure.walk/postwalk-replace {:table :table.table})
                   (claywalk/postwalk (fn [form]
                                        (if (vector-that-starts-with?
                                             form
                                             :span.formula)
                                          (-> form
                                              second
                                              item/katex-hiccup)
                                          form)))))))
      (complete-hiccup item)))

(defn item->md [{:as item
                 :keys [hiccup html md
                        script
                        item-class]}]
  (-> (if script
        (-> item
            (dissoc :script)
            (update :hiccup conj script)
            item->md)
        (-> (or md
                (format "\n```{=html}\n%s\n```\n"
                        (or html
                            (some-> hiccup
                                    (complete-hiccup item)
                                    hiccup/html))))))
      (cond-> item-class
        (#(format "::: {.%s}\n%s\n:::\n" item-class %)))))

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
  (if (:advice context)
    context
    (kindly-advice/advise context)))

(defn has-preparable-kind? [value]
  (when-let [kind (value->kind value)]
    (or (@*kind->preparer kind)
        (#{:kind/fragment :kind/fn} kind))))

(defn prepare [{:as context
                :keys [value]}
               {:keys [fallback-preparer]}]
  (let [{:as context-with-advice :keys [kind]} (advise-if-needed context)]
    (case kind
      :kind/fragment (->> value
                          ;; splice the fragment
                          (mapcat (fn [subvalue]
                                    (-> context
                                        (dissoc :form :kind :advice)
                                        (assoc :value subvalue)
                                        (prepare {:fallback-preparer fallback-preparer})))))
      :kind/fn (let [new-value (or (when-let [f (-> context-with-advice
                                                    :kindly/options
                                                    :kindly/f)]
                                     (f value))
                                   (when (vector? value) (let [[f & args] value]
                                                           (apply f args)))
                                   (when (map? value) (let [{:keys [kindly/f]} value]
                                                        (-> value
                                                            (dissoc :kindly/f)
                                                            f)))
                                   (throw (ex-message "missing function for :kind/fn")))]
                 (-> context
                     (assoc :value new-value)
                     (dissoc :form :kind :advice)
                     (prepare {:fallback-preparer fallback-preparer})
                     (->> (map #(assoc % :prepared-by-fn true)))))
      ;; else - a regular kind
      (when-let [preparer (-> kind
                              (@*kind->preparer)
                              (or fallback-preparer))]
        [(-> context-with-advice
             preparer
             ;; returns an item
             (update :hiccup limit-hiccup-height context)
             (update :md limit-md-height context)
             ;; items need the options from the context
             (assoc :kindly/options (:kindly/options context)))]))))


(defn prepare-or-pprint [context]
  (prepare context {:fallback-preparer
                    (preparer-from-value-fn #'item/pprint)}))

(defn prepare-or-str [context]
  (prepare context {:fallback-preparer
                    (preparer-from-value-fn #'item/md)}))



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
 :kind/tex
 #'item/tex)

(add-preparer!
 :kind/table
 (fn [{:as context
       :keys [value]}]
   (let [use-datatables (->> context
                             :kindly/options
                             :use-datatables)
         pre-hiccup (table/->table-hiccup value)
         *deps (atom []) ; TODO: implement without mutable state
         hiccup (->> pre-hiccup
                     (claywalk/postwalk
                      (fn [elem]
                        (if (and (vector? elem)
                                 (-> elem first (#{:th :td})))
                          ;; a table cell - handle it
                          [(first elem) (if-let [items (-> context
                                                           (dissoc :form :kind :advice)
                                                           (update :kindly/options dissoc :element/max-height)
                                                           (assoc :value (second elem))
                                                           (prepare {}))]
                                          (do (swap! *deps concat (mapcat :deps items))
                                              (map (fn [item]
                                                     (-> item
                                                         (assoc :inside-a-table true)
                                                         (item->hiccup
                                                          (-> context
                                                              (cond-> use-datatables
                                                                (assoc :format [:html]))))))
                                                   items))
                                          ;; else - a plain value
                                          (second elem))]
                          ;; else - keep it
                          elem))))]
     (merge
      {:hiccup hiccup
       :item-class "clay-table"}
      (if use-datatables
        {:script [:script
                  (->> context
                       :kindly/options
                       :datatables
                       charred/write-json-str
                       (format "new DataTable(document.currentScript.parentElement, %s);"))]
         :deps (->> @*deps
                    (cons :datatables)
                    distinct)}
        ;; else
        {:deps (distinct @*deps)})))))

(defn structure-mark-hiccup [mark]
  (-> mark
      item/structure-mark
      :hiccup))

(add-preparer-from-value-fn!
 :kind/code
 (fn [codes]
   (->> codes
        item/source-clojure)))

(add-preparer!
 :kind/dataset
 #'item/dataset)

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
       (update :kindly/options dissoc :element/max-height)
       (assoc :value (-> value
                         meta
                         :test
                         (#(%))))
       prepare-or-pprint)))

(defn not-all-plain-values? [items]
  (->> items
       (every? (fn [item]
                 (and (:printed-clojure item)
                      (not (:prepared-by-fn item)))))
       not))

(add-preparer!
 :kind/map
 (fn [{:as context
       :keys [value]}]
   (if (->> value
            (apply concat)
            (some has-preparable-kind?))
     (let [*deps (atom []) ; TODO: implement without mutable state
           prepared-kv-pairs (->> value
                                  (map (fn [kv]
                                         {:kv kv
                                          :prepared-kv
                                          (->> kv
                                               (mapcat (fn [v]
                                                         (let [items (-> context
                                                                         (dissoc :form :kind :advice)
                                                                         (update :kindly/options dissoc :element/max-height)
                                                                         (assoc :value v)
                                                                         prepare-or-pprint)]
                                                           (swap! *deps concat (mapcat :deps items))
                                                           items))))})))]
       (if (->> prepared-kv-pairs
                (mapcat :prepared-kv)
                not-all-plain-values?)
         ;; some parts are not just plain Clojure values - handle recursively
         {:hiccup [:div
                   (structure-mark-hiccup "{")
                   (->> prepared-kv-pairs
                        (map (fn [{:keys [kv prepared-kv]}]
                               (if (->> prepared-kv
                                        not-all-plain-values?)
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
                        (into [:div.clay-map
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
           (some has-preparable-kind?))
    (let [*deps (atom []) ; TODO: implement without mutable state
          prepared-parts (->> value
                              (mapcat (fn [subvalue]
                                        (-> context
                                            (dissoc :form :kind :advice)
                                            (update :kindly/options dissoc :element/max-height)
                                            (assoc :value subvalue)
                                            prepare-or-pprint))))]
      (if (->> prepared-parts
               not-all-plain-values?)
        ;; some parts are not just plain Clojure values - handle recursively
        {:hiccup [:div
                  (structure-mark-hiccup open-mark)
                  (->> prepared-parts
                       (map #(item->hiccup % context))
                       (into [:div.clay-sequential {:style {:margin-left "10%"
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

(add-preparer!
 :kind/emmy-viewers
 #'item/emmy-viewers)

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
   (let [*deps (atom
                (-> context
                    :kindly/options
                    :html/deps)) ; TODO: implement without mutable state
         hiccup (->> value
                     (claywalk/prewalk
                      (fn [subform]
                        (let [subcontext (-> context
                                             (dissoc :form :kind :advice)
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

(add-preparer-from-value-fn!
 :kind/htmlwidgets-ggplotly
 #'item/ggplotly)

(add-preparer-from-value-fn!
 :kind/highcharts
 #'item/highcharts)
