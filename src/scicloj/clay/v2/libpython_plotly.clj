(ns ^:no-docs scicloj.clay.v2.libpython-plotly
  (:require [libpython-clj2.require :refer [require-python]]
            [libpython-clj2.python :as py]))

;; Make sure you have installed plotly and kaleido in your Python environment:
;;     pip install plotly kaleido

;; If you prefer to use a virtual environment:
;;     python3 -m venv .venv
;;     source .venv/bin/activate
;;     pip install plotly kaleido

(py/initialize!)

(require-python 'plotly.graph_objects)
(require-python 'kaleido)

(def go (py/import-module "plotly.graph_objects"))

(defn plotly-export
  [{:keys [data layout config]} filename]
  (let [fig (py/call-attr go "Figure"
                          ;; TODO: should figure out why these invalid properties are on data
                          (update data 0 dissoc :r :fill :mode :theta :z :lon :lat :width)
                          (dissoc layout :automargin)
                          config)]
    (py/call-attr fig "write_image" filename)))
