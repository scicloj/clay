(ns scicloj.clay.v2.notebook
  (:require
   [clojure.string :as string]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.util.path :as path]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.prepare :as prepare]
   [scicloj.clay.v2.read :as read]
   [scicloj.clay.v2.config :as config]
   [scicloj.clay.v2.util.merge :as merge]
   [scicloj.kindly.v4.api :as kindly]
   [scicloj.kindly.v4.kind :as kind]
   [scicloj.kindly-advice.v1.api :as kindly-advice]))

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

(defn info-line [absolute-file-path]
  (let [relative-file-path (path/path-relative-to-repo
                            absolute-file-path)]
    (item/info-line {:path relative-file-path
                     :url (some-> (config/config)
                                  :remote-repo
                                  (path/file-git-url relative-file-path))})))

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

(defn hide-value? [{:as note :keys [form value]} {:as opts :keys [hide-nils hide-vars]}]
  (or (and (sequential? form)
           (-> form first hidden-form-starters))
      (-> note :form meta :kind/hidden)
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

(defn add-info-line [items {:keys [full-source-path hide-info-line]}]
  (if hide-info-line
    items
    (let [il (info-line full-source-path)]
      (concat items
              [item/separator il]))))

(defn ->var-name [i]
  (symbol (str "var" i)))

(defn ->test-name [i]
  (symbol (str "test" i)))

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
            format]}]
   (binding [*ns* *ns*
             *warn-on-reflection* *warn-on-reflection*
             *unchecked-math* *unchecked-math*]
     (let [code (some-> full-source-path slurp)
           notes (cond
                   single-value (conj (when code
                                        [{:form (read/read-ns-form code)}])
                                      {:value single-value})
                   single-form (conj (when code
                                       [{:form (read/read-ns-form code)}])
                                     {:form single-form})
                   :else (read/->safe-notes code))]
       (-> (->> notes
                (reduce (fn [{:as aggregation :keys [i
                                                     items
                                                     test-forms
                                                     last-nontest-i]}
                             note]
                          (let [{:as complete-note :keys [form kind]} (complete note)
                                test-note (test-last? complete-note)
                                new-items (when-not test-note
                                            (-> complete-note
                                                (merge/deep-merge
                                                 (-> options
                                                     (select-keys [:base-target-path
                                                                   :full-target-path
                                                                   :kindly/options
                                                                   :format])))
                                                (note-to-items options)))
                                test-form (if test-note
                                            ;; a deftest form
                                            (deftest-form
                                              (->test-name i)
                                              (->var-name last-nontest-i)
                                              form)
                                            (if (ns-form? form)
                                              ;; the test ns form
                                              (test-ns-form form)
                                              ;; the regular case, just a def
                                              (def-form
                                                (->var-name i)
                                                form)))]
                            {:i              (inc i)
                             :items          (concat items new-items)
                             :test-forms     (conj test-forms test-form)
                             :last-nontest-i (if (or (:comment? complete-note)
                                                     test-note)
                                               last-nontest-i
                                               i)}))
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
                   (fn [test-forms]
                     (when (->> test-forms
                                (some #(-> % first (= 'deftest))))
                       test-forms))))))))


(comment
  (-> "notebooks/scratch.clj"
      (notebook-items {:full-target-path "docs/scratch.html"}))

  (-> "notebooks/scratch.clj"
      (notebook-items {:full-target-path "docs/scratch.html"
                       :single-form '(+ 1 2)})))
