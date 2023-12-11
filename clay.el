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
