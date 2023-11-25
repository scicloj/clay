
(defun clay/start ()
  (interactive)
  (cider-interactive-eval "
    (require '[scicloj.clay.v2.api])
    (scicloj.clay.v2.api/start!)")
  t)

(defun clay/make-ns (format)
  (save-buffer)
  (let
      ((filename
        (buffer-file-name)))
    (when filename
      (cider-interactive-eval
       (concat "(scicloj.clay.v2.api/make! {:format " format " :source-path \"" filename "\" })")))))

(defun clay/make-ns-html ()
  (interactive)
  (clay/start)
  (clay/make-ns "[:html]"))

(defun clay/make-ns-quarto-html ()
  (interactive)
  (clay/start)
  (clay/make-ns "[:quarto :html]"))

(defun clay/make-ns-quarto-revealjs ()
  (interactive)
  (clay/start)
  (clay/make-ns "[:quarto :revealjs]"))

(defun clay/cider-interactive-notify-and-eval (code)
  (cider-interactive-eval
   code
   (cider-interactive-eval-handler nil (point))
   nil
   nil))

(defun clay/make-form (code)
  (clay/start)
  (let
      ((filename
        (buffer-file-name)))
    (clay/cider-interactive-notify-and-eval
     (concat "(scicloj.clay.v2.api/make! {:format [:html] :source-path \"" filename "\" :single-form (quote " code")})"))))

(defun clay/make-last-sexp ()
  (interactive)
  (clay/make-form (cider-last-sexp)))

(defun clay/make-defun-at-point ()
  (interactive)
  (clay/make-form (thing-at-point 'defun)))
