(ns scicloj.clay.v2.read-kinds
  "Convert code into contexts.
  Contexts are maps that contain top-level forms and their evaluated value,
  which will be further annotated with more information."
  (:refer-clojure :exclude [read-string])
  (:require [clojure.string :as str]
            [rewrite-clj.parser :as parser]
            [rewrite-clj.node :as node])
  (:import (java.io StringWriter)))

(def evaluators #{:clojure :babashka})

(defn- validate-options [{:keys [evaluator]}]
  (when evaluator
    (assert (contains? evaluators evaluator)
            (str "evaluator must be one of: " evaluators))))

(defn ^:dynamic *on-eval-error*
  "By default, eval errors will be rethrown.
  When *on-eval-error* is bound to nil or a function,
  The exception will be added to the context as an `:error` instead.
  *on-eval-error* may be bound to a function to provide alternative behavior like warning.
  When bound to a function the result will be ignored, but subsequent exceptions will propagate."
  [context ex]
  (throw (ex-info (str "Eval failed: " (ex-message ex))
                  {:id      ::eval-failed
                   :context context}
                  ex)))

;; FIXME: Add more test cases to verify that it works as intended.
(defn read-ns-form [code]
  (let [form (->> code
                  parser/parse-string
                  node/sexpr)]
    (when (= 'ns (first form))
      form)))

(defn- eval-node
  "Given an Abstract Syntax Tree node, returns a context.
  A context represents a top level form evaluation."
  [node options]
  (let [tag (node/tag node)
        code (node/string node)]
    (case tag
      (:newline :whitespace) {:code code
                              :kind :kind/whitespace}

      :uneval {:code code
               :kind :kind/uneval}

      ;; extract text from comments
      :comment {:code  code
                :kind  :kind/comment
                ;; remove leading semicolons or shebangs, and one non-newline space if present.
                :value (str/replace-first code #"^(;|#!)*[^\S\r\n]?" "")}

      ;; evaluate for value, taking care to capture stderr/stdout and exceptions
      (let [form (node/sexpr node)
            {:keys [row col end-row end-col]} (meta node)
            region [row col end-row end-col]
            meta {:source code
                  :line row
                  :column col
                  :end-line end-row
                  :end-column end-col}
            context {:region region
                     :code code
                     :meta meta
                     :form form}
            out (new StringWriter)
            err (new StringWriter)
            result (try
                     ;; TODO: capture `tap` or not?
                     (let [x (binding [*out* out
                                       *err* err]
                               (eval form))]
                       {:value x})
                     (catch Throwable ex
                       (when *on-eval-error*
                         (*on-eval-error* context ex))
                       {:exception ex}))
            out-str (str out)
            err-str (str err)]
        (merge context result
               (when (seq out-str) {:out out-str})
               (when (seq err-str) {:err err-str}))))))

(defn- babashka? [node]
  (-> (node/string node)
      (str/starts-with? "#!/usr/bin/env bb")))

(defn- eval-ast [ast options]
  "Given the root Abstract Syntax Tree node,
  returns a vector of contexts that represent evaluation"
  (let [top-level-nodes (node/children ast)
        ;; TODO: maybe some people want to include the header?
        babashka (some-> (first top-level-nodes) (babashka?))
        nodes (if babashka
                (rest top-level-nodes)
                top-level-nodes)]
    ;; Babashka and Clojure can evaluate files with or without the header present,
    ;; it is up to the user to specify which evaluator to use in the options.
    #_(when (and babashka (not= evaluator :babashka))
        (println "Warning: Babashka header detected while evaluating in Clojure"))
    ;; must be eager to restore current bindings
    (mapv #(eval-node % options) nodes)))


;; TODO: DRY
(defn read-string
  "Parse and evaluate the first form in a string.
  Suitable for sending text representing one thing for visualization."
  ([code] (read-string code {}))
  ([code options]
   (validate-options options)
   ;; preserve current bindings (they will be reset to original)
   (binding [*ns* *ns*
             *warn-on-reflection* *warn-on-reflection*
             *unchecked-math* *unchecked-math*]
     (-> (parser/parse-string code)
         (eval-node options)))))

(defn read-string-all
  "Parse and evaluate all forms in a string.
  Suitable for sending a selection of text for visualization.
  When reading a file, prefer using `read-file` to preserve the current ns bindings."
  ([code] (read-string-all code {}))
  ([code options]
   (validate-options options)
   ;; preserve current bindings (they will be reset to original)
   (binding [*ns* *ns*
             *warn-on-reflection* *warn-on-reflection*
             *unchecked-math* *unchecked-math*]
     (-> (parser/parse-string-all code)
         (eval-ast options)))))

;; TODO: DRY
(defn read-file
  "Similar to `clojure.core/load-file`,
  but returns a representation of the forms and results of evaluation.
  Suitable for processing an entire namespace."
  [file options]
  (validate-options options)
  ;; preserve current bindings (they will be reset to original)
  (binding [*ns* *ns*
            *warn-on-reflection* *warn-on-reflection*
            *unchecked-math* *unchecked-math*]
    (-> (parser/parse-file-all file)
        (eval-ast options))))
