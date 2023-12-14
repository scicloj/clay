;;; clay.el --- Emacs commands for the Clay tool for literate programming and data visualization in Clojure  -*- lexical-binding: t; -*-

;; Copyright (C) 2023  daslu

;; Author: daslu
;; Keywords: lisp

;; This program is free software; you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.

;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.

;; You should have received a copy of the GNU General Public License
;; along with this program.  If not, see <https://www.gnu.org/licenses/>.

;;; Commentary:

;;

;;; Code:

(defun clay/clean-buffer-file-name ()
  ;; Clean up the buffer file name in TRAMP situations.
  ;; E.g., "/ssh:myserver:/home/myuser/myfile" --> "/home/myuser/myfile"
  (replace-regexp-in-string "^.*:"
                            ""
                            (buffer-file-name)))

(defun clay/require ()
  (interactive)
  (cider-interactive-eval "
    (require '[scicloj.clay.v2.api])")
  t)

(defun clay/start ()
  (interactive)
  (clay/require)
  (cider-interactive-eval "
    (scicloj.clay.v2.api/start!)")
  t)

(defun clay/make-ns (format)
  (save-buffer)
  (clay/require)
  (let
      ((filename
        (clay/clean-buffer-file-name)))
    (when filename
      (cider-interactive-eval
       (concat "(scicloj.clay.v2.api/make! {:format " format " :source-path \"" filename "\" })")))))

(defun clay/make-ns-html ()
  (interactive)
  (clay/require)
  (clay/make-ns "[:html]"))

(defun clay/make-ns-quarto-html ()
  (interactive)
  (clay/require)
  (clay/make-ns "[:quarto :html]"))

(defun clay/make-ns-quarto-revealjs ()
  (interactive)
  (clay/require)
  (clay/make-ns "[:quarto :revealjs]"))

(defun clay/cider-interactive-notify-and-eval (code)
  (cider-interactive-eval
   code
   (cider-interactive-eval-handler nil (point))
   nil
   nil))

(defun clay/make-form (code)
  (clay/require)
  (let
      ((filename
        (clay/clean-buffer-file-name)))
    (clay/cider-interactive-notify-and-eval
     (concat "(scicloj.clay.v2.api/make! {:format [:html] :source-path \"" filename "\" :single-form (quote " code")})"))))

(defun clay/make-last-sexp ()
  (interactive)
  (clay/make-form (cider-last-sexp)))

(defun clay/make-defun-at-point ()
  (interactive)
  (clay/make-form (thing-at-point 'defun)))

(provide 'clay)
;;; clay.el ends here
