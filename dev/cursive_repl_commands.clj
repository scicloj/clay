(ns cursive-repl-commands
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [hiccup.core :as hiccup]
            [scicloj.kindly.v4.kind :as kind])
  (:import (java.util.zip ZipEntry ZipOutputStream)))

;; We use REPL commands to perform Clay actions on forms and files.
;; They all have similar configuration, except for the :commandText Clojure snippet and name.

(def command-template
  {:executionType    "EXPRESSION"
   :echo             "MESSAGE"
   :addToHistory     "true"
   :executeBefore    "SYNC_ALL"
   :executeAfter     "SYNC_ALL"
   :executionNs      "REPL_CURRENT"
   :replSelection    "REPL_SELECTOR"
   :replLanguage     "CLJ"
   :saveAll          "true"
   :clearRepl        "false"
   :printOutput      "true"
   :replaceForm      "false"
   :copyResult       "false"
   :insertIntoEditor "false"
   ;; The result of REPL commands are flares, intended to be shown, so this setting needs to be true
   :showResultInline "true"})

;; Clay has a namespace `scicloj.clay.v2.snippets` dedicated to exposing the 9 common Clay commands.
;; The REPL Commands call these snippets with relevant details like the file-path.

(defn format-snippet
  [clay-fn-name & args]
  ;; The tool needs to be required, and invoked with appropriate arguments.
  ;; `require` is fully qualified so it will work from uninitialized namespaces.
  (str "(do (clojure.core/require '[scicloj.clay.v2.old.snippets])" \newline
       "    (scicloj.clay.v2.old.snippets/" clay-fn-name (when args " ") (str/join " " args) "))" \newline))

(def file "\"~file-path\"")
(def form-before-caret "(quote ~form-before-caret)")
(def top-level-form "(quote ~top-level-form)")
(def options "{:ide :cursive}")

(def clay-commands
  "Connects the command name with the snippet function to call and the relevant arguments"
  [["Clay Make File" "f" ["make-ns-html!" file options]]
   ["Clay Make File Quarto" "q" ["make-ns-quarto-html!" file options]]
   ["Clay Make File RevealJS" "r" ["make-ns-quarto-revealjs!" file options]]
   ["Clay Make Form Before Caret" "c" ["make-form-html!" form-before-caret file options]]
   ["Clay Make Form Before Caret Quarto" "C" ["make-form-quarto-html!" form-before-caret file options]]
   ["Clay Make Top Level Form" "t" ["make-form-html!" top-level-form file options]]
   ["Clay Make Top Level Form Quarto" "T" ["make-form-quarto-html!" top-level-form file options]]
   ["Clay Browse" "b" ["browse!"]]
   ["Clay Watch" "w" ["watch!" options]]])

(kind/table clay-commands)

(defn repl-commands [project]
  (hiccup/html {:mode :xml}
               [:application
                [:component {:name (if project
                                     "ReplProjectCommandManager"
                                     "ReplCommandManager")}
                 (for [[action-name _ snippet-args] clay-commands]
                   [:repl-command (assoc command-template
                                    :name action-name
                                    :commandText (apply format-snippet snippet-args))])]]))

(defn write-repl-commands! []
  (spit (doto (io/file ".idea" "repl-commands.xml")
          (io/make-parents))
        (repl-commands true)))

;; Workspace configuration
(comment
  (write-repl-commands!))

(defmacro ^:private with-entry
  [zip entry-name & body]
  `(let [^ZipOutputStream zip# ~zip]
     (try
       (.putNextEntry zip# (ZipEntry. ~entry-name))
       ~@body
       (finally
         (.closeEntry zip#)))))

(defn write-zip!
  "Makes a file suitable for File -> Manage IDE Settings -> Import Settings"
  []
  (with-open [file (io/output-stream "clay-settings.zip")
              zip (ZipOutputStream. file)]
    (doto zip
      (with-entry "options/repl-commands.xml"
                  (.write zip (.getBytes (repl-commands false))))
      (with-entry "installed.txt"
                  (.write zip (.getBytes "com.cursiveclojure.cursive")))
      (with-entry "IntelliJ IDEA Global Settings"))))

;; Global configuration
(comment
  (write-zip!))
