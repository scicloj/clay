(ns scicloj.clay.v2.snippets
  (:require [scicloj.clay.v2.api :as api]))

;; 1. Less code in custom commands and snippets
;;   - Make sure it works for Cursive/VSCode/Emacs users
;; 2. More alignment on the standard ways to use Clay
;;   - File cleanup may be confusing for some users
;; 3. Common pattern of [save-file, (do (require ...) (invoke ...))] -- can it be simpler?
;;   - Emacs does not save automatically
;;   - Emacs more than one meaning to the current form (top-level, before cursor) <-- keys, not snippets


(defn form-as-html
  ([form file] (form-as-html form file nil))
  ([form file options]
   (prn ["Clay make current form as HTML" file form])
   (api/make! (merge {:base-source-path nil
                      :source-path      file
                      :single-form      (quote form)}
                     options))))

(defn form-as-quarto
  ([form file] (form-as-quarto form file nil))
  ([form file options]
   (prn ["Clay make current form as HTML" "$file" form])
   (scicloj.clay.v2.api/make! (merge {:base-source-path nil
                                      :source-path      file
                                      :single-form      (quote form)}
                                     options))))

(defn file-as-quarto-revealjs
  ([file] (file-as-quarto-revealjs file nil))
  ([file options]
   (prn ["Clay make namespace as Quarto, then reveal.js" file])
   (api/make! (merge {:base-source-path nil
                      :source-path      file
                      :format           [:quarto :revealjs]}
                     options))))

(defn file-as-quarto-html
  ([file] (file-as-quarto-html file nil))
  ([file options]
   (prn ["Clay make namespace as Quarto, then HTML" file])
   (api/make! (merge {:base-source-path nil
                      :source-path      file
                      :format           [:quarto :html]}
                     options))))

(defn file-as-html
  ([file] (file-as-html file nil))
  ([file options]
   (prn ["Clay make namespace as HTML" file])
   (require '[scicloj.clay.v2.api])
   (api/make! (merge {:base-source-path nil
                      :source-path      file}
                     options))))
