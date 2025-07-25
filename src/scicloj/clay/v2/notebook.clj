(ns scicloj.clay.v2.notebook
  (:require [clojure.string :as str]
            [clojure.pprint :as pp]
            [scicloj.clay.v2.util.path :as path]
            [scicloj.clay.v2.item :as item]
            [scicloj.clay.v2.prepare :as prepare]
            [scicloj.clay.v2.read :as read]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly-advice.v1.api :as kindly-advice])
  (:import (java.io StringWriter)))

(set! *warn-on-reflection* true)

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
                     :url  (some-> remote-repo (path/file-git-url relative-file-path))})))

(defn narrowed? [code]
  (some-> code
          (str/includes? ",,")))

(defn narrower? [code]
  (some-> code
          (str/includes? ",,,")))

(defn ns-form? [form]
  (and (sequential? form)
       (-> form first (= 'ns))))

(defn str-and-reset! [w]
  (when (instance? StringWriter *out*)
    (locking w
      (let [s (str w)]
        (.setLength (.getBuffer ^StringWriter w) 0)
        s))))

(defn read-eval-capture
  "Captures stdout and stderr while evaluating a note"
  [{:as   note
    :keys [code form]}]
  (let [out (StringWriter.)
        err (StringWriter.)
        note (try
               (let [x (binding [*out* out
                                 *err* err]
                         (cond form (-> form
                                        eval
                                        deref-if-needed)
                               code (-> code
                                        read-string
                                        eval
                                        deref-if-needed)))]
                 (assoc note :value x))
               (catch Throwable ex
                 (assoc note :exception ex)))
        out-str (str out)
        err-str (str err)
        ;; A notebook may have also printed from a thread,
        ;; *out* and *err* are replaced with StringWriters in with-out-err-capture
        global-out (str-and-reset! *out*)
        global-err (str-and-reset! *err*)
        ;; Don't show output from requiring other namespaces
        show (not (ns-form? form))]
    (if show
      (cond-> note
        (seq out-str) (assoc :out out-str)
        (seq err-str) (assoc :err err-str)
        (seq global-out) (assoc :global-out global-out)
        (seq global-err) (assoc :global-err global-err))
      note)))

(defn complete [{:as   note
                 :keys [comment?]}]
  (let [completed (cond-> note
                    (not (or comment? (contains? note :value)))
                    (read-eval-capture))]
    (cond-> completed
      (and (not comment?) (contains? completed :value))
      (kindly-advice/advise))))

(defn comment->item [comment]
  (-> comment
      (str/split #"\n")
      (->> (map #(-> %
                     (str/replace
                       #"^;+\s?" "")
                     (str/replace
                       #"^#" "\n#")))
           (str/join "\n"))
      item/md))

(defn hide-code? [{:as note :keys [code form value kind narrowed]} {:as opts :keys [hide-code]}]
  (or hide-code
      narrowed
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
                      :keys [comment?
                             code
                             exception
                             err
                             out
                             global-err
                             global-out
                             kindly/options]}
                     {:as opts}]
  (if (and comment? code)
    [(comment->item code)]
    (let [code-item (when-not (hide-code? note opts)
                      (item/source-clojure code))
          value-items (cond-> []
                              err (conj (item/print-output "ERR" err))
                              out (conj (item/print-output "OUT" out))
                              global-err (conj (item/print-output "THREAD ERR" global-err))
                              global-out (conj (item/print-output "THREAD OUT" global-out))
                              exception (conj (item/print-throwable exception))
                              (and (contains? note :value)
                                   (not (hide-value? note opts)))
                              (into (-> note
                                        (update :value deref-if-needed)
                                        (prepare/prepare-or-pprint))))]
      (cond (and (not code-item) (empty? value-items))
            []

            (not code-item)
            value-items

            (empty? value-items)
            [code-item]

            (= :horizontal (or (:code-and-value opts)
                               (:code-and-value options)))
            (side-by-side-items opts code-item value-items)

            :else
            (into [code-item] value-items)))))

(defn add-info-line [items {:as spec :keys [hide-info-line]}]
  (if hide-info-line
    items
    (let [il (info-line spec)]
      (into items [item/separator il]))))

(defn ->var-name [i line-number]
  (symbol (str "v" i
               "_l" line-number)))

(defn ->test-name [i line-number]
  (symbol (str "t" i
               "_l" line-number)))

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

(defn clean-test-last-form [form]
  (case (str (first form))
    "kind/test-last" (second form)
    "kindly/check" (rest form)
    ;; else
    form))

(defn var-based-deftest-form [test-name var-name form]
  (let [[f-symbol & args] form]
    (list 'deftest
          test-name
          (concat (list 'is
                        (concat (list f-symbol
                                      var-name)
                                args))))))

(defn simple-deftest-form [test-name last-form form]
  (let [[f-symbol & args] form]
    (list 'deftest
          test-name
          (concat (list 'is
                        (concat (list f-symbol
                                      last-form)
                                args))))))

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
             {:code                 new-code
              :first-line-of-change (first-line-of-change
                                      code new-code)})))
  (@*path->last path))

(defmacro with-out-err-captured
  "Evaluates and computes the items for a notebook of notes"
  [& body]
  ;; For a notebook, we capture output globally, and per note.
  ;; see read-eval-capture for why this is relevant.
  `(let [out# (StringWriter.)
         err# (StringWriter.)]
     ;; Threads may inherit only the root binding
     (with-redefs [*out* out#
                   *err* err#]
       ;; Futures will inherit the current binding,
       ;; which was not affected by altering the root.
       (binding [*out* out#
                 *err* err#]
         ~@body))))

(defn itemize-notes
  "Evaluates and computes the items for a notebook of notes"
  [relevant-notes some-narrowed options]
  (reduce (fn [{:as aggregation :keys [i
                                       items
                                       test-forms
                                       last-nontest-varname
                                       last-nontest-form]}
               complete-note]
            (let [{:keys [form region narrowed exception comment?]} complete-note
                  {:keys [test-mode]} (:kindly/options complete-note)
                  test-note (test-last? complete-note)
                  new-items (when (or (not some-narrowed)
                                      narrowed)
                              (when-not test-note
                                (note-to-items complete-note options)))
                  line-number (first region)
                  varname (->var-name i line-number)
                  test-form (cond
                              ;; a deftest form
                              test-note (vary-meta
                                         (let [test-name (->test-name i line-number)
                                               ctlf (clean-test-last-form form)]
                                           (case test-mode
                                             :sequential (var-based-deftest-form
                                                          test-name
                                                          last-nontest-varname
                                                          ctlf)
                                             :simple (simple-deftest-form
                                                      test-name
                                                      last-nontest-form
                                                      ctlf)))
                                         assoc :test-mode test-mode)
                              ;; the test ns form
                              (ns-form? form) (test-ns-form form)
                              ;; a comment
                              comment? nil
                              ;; the regular case, just a def
                              :else (def-form varname form))
                  step {:i                    (inc i)
                        :items                (into items (remove nil?) new-items)
                        :test-forms           (if test-form
                                                (conj test-forms test-form)
                                                test-forms)
                        :last-nontest-varname (if (or comment? test-note)
                                                last-nontest-varname
                                                varname)
                        :last-nontest-form    (if (or comment? test-note)
                                                last-nontest-form
                                                form)}]
              (if (and exception (not (:exception-continue options)))
                (reduced (assoc step :exception exception))
                step)))
          ;; initial value
          {:i              0
           :items          []
           :test-forms     []
           :last-nontest-i nil}
          ;; sequence
          relevant-notes))

(defn complete-notes [notes options]
  (let [opts (select-keys options
                          [:base-target-path
                           :full-target-path
                           :kindly/options
                           :format])]
    (doall
      (for [note notes]
        (complete (kindly/deep-merge opts note))))))

(defn relevant-notes [{:keys [full-source-path
                         single-form
                         single-value
                         smart-sync
                         pprint-margin]
                  :or        {pprint-margin pp/*print-right-margin*}}]
  (let [{:keys [code first-line-of-change]} (some-> full-source-path slurp-and-compare)
        notes (->> (cond single-value (conj (when code
                                              [{:form (read/read-ns-form code)}])
                                            {:value single-value})
                         single-form (conj (when code
                                             [{:form (read/read-ns-form code)}])
                                           {:form single-form})
                         :else (read/->notes code))
                   (map-indexed (fn [i {:as   note
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
        last-narrowed-index (last narrowed-indices)]
    (cond-> notes
      some-narrower
      (->> (filter (fn [{:keys [narrower form]}]
                     (or narrower
                         (ns-form? form)))))

      (and some-narrowed smart-sync)
      (->> (take (inc last-narrowed-index))
           (filter (fn [{:keys [i code form region]}]
                     (or (ns-form? form)
                         (>= i first-narrowed-index)
                         (-> region
                             (nth 2)  ;last region line
                             (> first-line-of-change)))))))))

(defn spec-notes [{:as spec
                   :keys      [pprint-margin]
                   :or        {pprint-margin pp/*print-right-margin*}}]
  (binding [*ns* *ns*
            *warn-on-reflection* *warn-on-reflection*
            *unchecked-math* *unchecked-math*
            pp/*print-right-margin* pprint-margin]
    (-> (relevant-notes spec)
        (complete-notes spec)
        (with-out-err-captured))))

(defn items-and-test-forms
  [notes spec]
  (let [some-narrowed (some :narrowed notes)]
    (-> notes
        (itemize-notes some-narrowed spec)
        (update :items add-info-line spec)
        (update :test-forms
                ;; Leave the test-form only when
                ;; at least one of them is a `deftest`.
                (if some-narrowed
                  (constantly nil)
                  (fn [test-forms]
                    (let [deftest-forms (->> test-forms
                                             (filter #(-> % first (= 'deftest))))]
                      (when                                 ;; there are some actual test forms
                        (seq deftest-forms)
                        #_(prn [:test-forms test-forms
                                :deftest-forms deftest-forms
                                :check (->> deftest-forms
                                            (map (comp :test-mode meta))
                                            (some #(= % :sequential)))])
                        (if (->> deftest-forms
                                 (map (comp :test-mode meta))
                                 (some #(= % :sequential)))
                          ;; Some tests are of `:sequential` mode,
                          ;; so we need all the intermediate `def` forms,
                          ;; not just the `deftest` forms.
                          test-forms
                          ;; Else - all tests are of `:simple` mode,
                          ;; so we only need the `ns` definition and the `deftest` forms.
                          (cons (first test-forms)
                                deftest-forms))))))))))


(comment
  (-> "notebooks/scratch.clj"
      (notebook-items {:full-target-path "docs/scratch.html"}))

  (-> "notebooks/scratch.clj"
      (notebook-items {:full-target-path "docs/scratch.html"
                       :single-form      '(+ 1 2)})))
