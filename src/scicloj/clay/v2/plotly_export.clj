(ns scicloj.clay.v2.plotly-export
  (:require [scicloj.kindly-render.shared.jso :as jso]
            [clojure.java.shell :as shell]))

;; Make sure you have installed plotly and kaleido in your Python environment:
;;     pip install plotly kaleido

;; If you prefer to use a virtual environment:
;;     python3 -m venv .venv
;;     source .venv/bin/activate
;;     pip install plotly kaleido

(def python-script
  "import sys, json, plotly.graph_objects as go
payload = json.load(sys.stdin)
data = payload.get('data', [])
layout = payload.get('layout', {})
config = payload.get('config', {})
filename = payload['filename']
fig = go.Figure(data=data, layout=layout, config=config)
fig.write_image(filename)
")

(defn export-plot! [filename data layout config]
  ;; TODO: should figure out why these invalid properties are on data
  (let [data (update data 0 dissoc :r :fill :mode :theta :z :lon :lat :width)
        layout (dissoc layout :automargin)
        payload (jso/write-json-str {:data data
                                     :layout layout
                                     :config config
                                     :filename filename})]
    (shell/sh "python3" "-c" python-script :in payload)))
