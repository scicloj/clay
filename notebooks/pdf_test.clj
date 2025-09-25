(ns pdf-test
  {:clay {:format [:quarto :pdf]
          :quarto {
                   ;; TODO: for some reason the clay.edn causes the format to be unknown, setting it explicitly for now
                   :format {:pdf {}}
                   ;;:documentclass "article"
                   ;;:classoption   ["twocolumn"]
                   ;;:geometry      ["top=30mm" "left=20mm" "heightrounded"]

                   ;; TODO: again this is just necessary because the project clay.edn puts some html in the include-in-header
                   :include-in-header {:text "\\AddToHook{env/Highlighting/begin}{\\small}"}}}}
  (:require [tablecloth.api :as tc]
            [scicloj.tableplot.v1.plotly :as tp]))

;; this is an example pdf

;; ---

;; if we want a new page, use latex

;; \newpage

(def scatter-ds
  (tc/dataset {:x [1 2 3 4 5]
               :y [10 20 15 25 18]}))

(-> scatter-ds
    (tp/base {:=title "Sample Scatter Plot"})
    (tp/layer-point {:=x :x
                     :=y :y}))

(comment
  (require '[scicloj.clay.v2.api :as clay])

  ;; use playwright
  (clay/make! {:source-path "notebooks/pdf_test.clj"
               :kindly/options {:playwright true
                                :plotly-ext "svg"}})
  (clay/make! {:source-path "notebooks/pdf_test.clj"
               :kindly/options {:playwright true
                                :plotly-ext "png"}})

  ;; use python
  (clay/make! {:source-path "notebooks/pdf_test.clj"
               :kindly/options {:plotly-ext "svg"}})
  (clay/make! {:source-path "notebooks/pdf_test.clj"
               :kindly/options {:plotly-ext "png"}})

  )
