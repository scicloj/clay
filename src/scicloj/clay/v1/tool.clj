(ns scicloj.clay.v1.tool)

(defprotocol Tool
  (setup! [this config])
  (open! [this])
  (close! [this])
  (show! [this value kind-override]))
