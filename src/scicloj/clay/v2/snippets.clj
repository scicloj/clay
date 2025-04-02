(ns scicloj.clay.v2.snippets
  "Recommended entry points for creating editor bindings for Clay.
  Status: experimental."
  (:require [scicloj.clay.v2.api :as api]))

(defn- make-form!
  "Make a given Clojure form in a given format."
  ([form file format options]
   (println "Clay make current form as " (pr-str format) file form)
   (api/make! (merge {:base-source-path nil
                      :source-path      file
                      :single-form      form
                      :format format}
                     (when (:ide options)
                       {:hide-ui-header true
                        :hide-info-line true})
                     options))))

(defn make-form-html!
  "Make a given Clojure form in HTML format."
  [form file options]
  (make-form! form file [:html] options))

(defn make-form-quarto-html!
  "Make a given Clojure form in Quarto format, then render it as HTML."
  [form file options]
  (make-form! form file [:quarto :revealjs] options))

(defn make-form-quarto-revealjs!
  "Make a given Clojure form in Quarto format, then render it as reveal.js."
  [form file options]
  (make-form! form file [:quarto :revealjs] options))

(defn make-ns!
  "Make a given Clojure file in a given format."
  [file format options]
  (println "Clay make current namespace as " (pr-str format) file)
  (api/make! (merge {:base-source-path nil
                     :source-path      file
                     :format           format}
                    options)))

(defn make-ns-html!
  "Make a given Clojure file in HTML format."
  [file options]
  (make-ns! file [:html] options))

(defn make-ns-quarto-html!
  "Make a given Clojure file in Quarto format, then render it as HTML."
  [file options]
  (make-ns! file [:quarto :html] options))

(defn make-ns-quarto-revealjs!
  "Make a given Clojure file in Quarto format, then render it as reveal.js."
  [file options]
  (make-ns! file [:quarto :revealjs] options))

(defn browse! []
  "Open the Clay browser view."
  (api/browse!))

(defn watch! [options]
  (api/make! (merge {:live-reload true
                     :watch-dirs ["notebooks"]}
                    options)))
