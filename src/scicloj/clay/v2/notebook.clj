(ns scicloj.clay.v2.notebook
  (:require [clojure.string :as string]
            [scicloj.clay.v2.item :as item]
            [scicloj.clay.v2.util.path :as path]
            [scicloj.clay.v2.item :as item]
            [scicloj.clay.v2.prepare :as prepare]
            [scicloj.clay.v2.read :as read]
            [scicloj.clay.v2.config :as config]
            [scicloj.clay.v2.util.merge :as merge]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly-advice.v1.api :as kindly-advice]
            [clojure.string :as str]
            [clojure.pprint :as pp]))

(defn deref-if-needed [v]
  (if (delay? v)
    @v
    v))

(def hidden-form-starters
  #{'ns 'comment
    'def 'defonce 'defn 'defmacro
    'defrecord 'defprotocol 'deftype
    'extend-protocol 'extend
    'require})

(defn info-line [{:keys [full-source-path
                         remote-repo]}]
  (let [relative-file-path (path/path-relative-to-repo full-source-path)]
    (item/info-line {:path relative-file-path
                     :url (some-> remote-repo (path/file-git-url relative-file-path))})))

(defn narrowed? [code]
  (some-> code
          (str/includes? ",,")))

(defn narrower? [code]
  (some-> code
          (str/includes? ",,,")))

(defn complete [{:as note
                 :keys [comment? code form value]}]
  (-> (if (or value comment?)
        note
        (assoc note
               :value (cond code (-> code
                                     read-string
                                     eval
                                     deref-if-needed)
                            form (-> form
                                     eval
                                     deref-if-needed))))
      (cond-> (not comment?)
        kindly-advice/advise)))

(defn comment->item [comment]
  (-> comment
      (string/split #"\n")
      (->> (map #(-> %
                     (string/replace
                      #"^;+\s?" "")
                     (string/replace
                      #"^#" "\n#")))
           (string/join "\n"))
      item/md))

(defn hide-code? [{:as note :keys [code form value kind]} {:as opts :keys [hide-code]}]
  (or hide-code
      (-> form meta :kindly/hide-code)
      (-> form meta :kindly/hide-code?)                     ; legacy convention
      (-> value meta :kindly/hide-code)
      (-> value meta :kindly/hide-code?)                    ; legacy convention
      (when kind
        (some-> note
                :kindly/options
                :kinds-that-hide-code
                kind))
      (nil? code)))

(defn hide-value? [{:as complete-note :keys [form value kind]}
                   {:as opts :keys [hide-nils hide-vars]}]
  (or (and (sequential? form)
           (-> form first hidden-form-starters))
      (= kind :kind/hidden)
      (and hide-nils (nil? value))
      (and hide-vars (var? value))))

(defn side-by-side-items [{:as spec :keys [format]} code-item value-items]
  ;; markdown grids are not structurally nested, but hiccup grids are
  (if (= :quarto (first format))
    `[{:md "::: {.grid .clay-side-by-side}"}
      {:md "::: {.g-col-6}"}
      ~code-item
      {:md ":::"}
      {:md "::: {.g-col-6}"}
      ~@value-items
      {:md ":::"}
      {:md ":::"}]
    [{:hiccup [:div.grid
               [:div.g-col-6 (:hiccup code-item)]
               (->> (map #(prepare/item->hiccup % spec) value-items)
                    (into [:div.g-col-6]))]
      :deps   (set (mapcat :deps value-items))}]))

(defn note-to-items [{:as   note
                      :keys [comment? code
                             kindly/options]}
                     {:as   opts}]
  (if (and comment? code)
    [(comment->item code)]
    (let [code-item (when-not (hide-code? note opts)
                      (item/source-clojure code))
          value-items (when-not (hide-value? note opts)
                        (-> note
                            (select-keys [:value :code :form
                                          :base-target-path
                                          :full-target-path
                                          :kindly/options
                                          :format])
                            (update :value deref-if-needed)
                            prepare/prepare-or-pprint))]
      (cond (and (not code-item) (empty? value-items))
            []

            (not code-item)
            value-items

            (empty? value-items)
            [code-item]

            (= :horizontal (or (:code-and-value opts) (:code-and-value options)))
            (side-by-side-items opts code-item value-items)

            :else
            (into [code-item] value-items)))))

(defn add-info-line [items {:as spec :keys [hide-info-line]}]
  (if hide-info-line
    items
    (let [il (info-line spec)]
      (concat items
              [item/separator il]))))

(defn ->var-name [i line-number]
  (symbol (str "var" i
               "_line" line-number)))

(defn ->test-name [i line-number]
  (symbol (str "test" i
               "_line" line-number)))

(defn test-last? [complete-note]
  (and (-> complete-note
           :comment?
           not)
       (-> complete-note
           :kind
           (= :kind/test-last))))

(defn def-form [var-name form]
  (list 'def
        var-name
        form))

(defn deftest-form [test-name var-name form]
  (cond
    ;;
    (-> form first (= 'kind/test-last))
    (deftest-form test-name var-name (second form))
    ;;
    (-> form first (= 'kindly/check))
    (deftest-form test-name var-name (rest form))
    ;;
    :else
    (let [[f-symbol & args] form]
      (list 'deftest
            test-name
            (concat (list 'is
                          (concat (list f-symbol
                                        var-name)
                                  args)))))))

(defn ns-form? [form]
  (and (sequential? form)
       (-> form first (= 'ns))))

(defn test-ns-form [[_ ns-symbol & rest-ns-form]]
  (concat (list 'ns
                (-> ns-symbol
                    (str "-generated-test")
                    symbol))
          (->> rest-ns-form
               (map (fn [part]
                      (if (and (list? part)
                               (-> part first (= :require)))
                        (concat part
                                '[[clojure.test :refer [deftest is]]])
                        part))))))

(defn first-line-of-change [code new-code]
  (if code
    (->> [code new-code]
         (map str/split-lines)
         (apply map =)
         (take-while true?)
         count)
    0))

(def *path->last (atom {}))

(defn slurp-and-compare [path]
  (swap! *path->last
         update
         path
         (fn [{:keys [code]}]
           (let [new-code (slurp path)]
             {:code new-code
              :first-line-of-change (first-line-of-change
                                     code new-code)})))
  (@*path->last path))

(defn items-and-test-forms
  ([{:as options
     :keys [full-source-path
            hide-info-line
            hide-code hide-nils hide-vars
            title toc?
            base-target-path
            full-target-path
            single-form
            single-value
            format
            smart-sync
            pprint-margin]
     :or {pprint-margin pp/*print-right-margin*}}]
   (binding [*ns* *ns*
             *warn-on-reflection* *warn-on-reflection*
             *unchecked-math* *unchecked-math*
             pp/*print-right-margin* pprint-margin]
     (let [{:keys [code
                   first-line-of-change]} (some-> full-source-path
                                                  slurp-and-compare)
           notes (->> (cond single-value (conj (when code
                                                 [{:form (read/read-ns-form code)}])
                                               {:value single-value})
                            single-form (conj (when code
                                                [{:form (read/read-ns-form code)}])
                                              {:form single-form})
                            :else (read/->notes code))
                      (map-indexed (fn [i {:as note
                                           :keys [code]}]
                                     (merge note
                                            {:i i}
                                            (when-not (:comment? note)
                                              {:narrowed (narrowed? code)
                                               :narrower (narrower? code)})))))
           some-narrowed (some :narrowed notes)
           some-narrower (some :narrower notes)
           narrowed-indices (when some-narrowed
                              (->> notes
                                   (map (fn [{:keys [i narrowed]}]
                                          (when narrowed i)))
                                   (remove nil?)))
           first-narrowed-index (first narrowed-indices)
           last-narrowed-index (last narrowed-indices)
           relevant-notes (cond
                            ;;
                            some-narrower
                            (->> notes
                                 (filter (fn [{:keys [narrower form]}]
                                           (or narrower
                                               (ns-form? form)))))
                            ;;
                            (and some-narrowed smart-sync)
                            (->> notes
                                 (take (inc last-narrowed-index))
                                 (filter (fn [{:keys [i code form region]}]
                                           (or (ns-form? form)
                                               (>= i first-narrowed-index)
                                               (-> region
                                                   (nth 2) ;last region line
                                                   (> first-line-of-change))))))
                            ;;
                            :else
                            notes)]
       (-> (->> relevant-notes
                (reduce (fn [{:as aggregation :keys [i
                                                     items
                                                     test-forms
                                                     last-nontest-varname]}
                             note]
                          (let [{:as complete-note :keys [form kind region narrowed]} (complete note)
                                test-note (test-last? complete-note)
                                comment (:comment? complete-note)
                                new-items (when (or (not some-narrowed)
                                                    narrowed)
                                            (when-not test-note
                                              (-> complete-note
                                                  (merge/deep-merge
                                                   (-> options
                                                       (select-keys [:base-target-path
                                                                     :full-target-path
                                                                     :kindly/options
                                                                     :format])))
                                                  (note-to-items
                                                   (merge options
                                                          (when narrowed
                                                            {:hide-code true}))))))
                                line-number (first region)
                                varname (->var-name i line-number)
                                test-form (cond
                                            ;; a deftest form
                                            test-note (deftest-form
                                                        (->test-name i line-number)
                                                        last-nontest-varname
                                                        form)
                                            ;; the test ns form
                                            (ns-form? form) (test-ns-form form)
                                            ;; a comment
                                            comment nil
                                            ;; the regular case, just a def
                                            :else (def-form
                                                    varname
                                                    form))]
                            {:i              (inc i)
                             :items          (concat items new-items)
                             :test-forms     (if test-form
                                               (conj test-forms test-form)
                                               test-forms)
                             :last-nontest-varname (if (or comment
                                                           test-note)
                                                     last-nontest-varname
                                                     varname)}))
                        ;; initial value
                        {:i              0
                         :items          []
                         :test-forms     []
                         :last-nontest-i nil}))
           (update :items
                   ;; final processing of items
                   (fn [items]
                     (-> items
                         (->> (remove nil?))
                         (add-info-line options)
                         doall)))
           (update :test-forms
                   ;; Leave the test-form only when
                   ;; at least one of them is a `deftest`.
                   (if some-narrowed
                     (constantly nil)
                     (fn [test-forms]
                       (when (->> test-forms
                                  (some #(-> % first (= 'deftest))))
                         test-forms)))))))))


(comment
  (-> "notebooks/scratch.clj"
      (notebook-items {:full-target-path "docs/scratch.html"}))

  (-> "notebooks/scratch.clj"
      (notebook-items {:full-target-path "docs/scratch.html"
                       :single-form '(+ 1 2)})))
