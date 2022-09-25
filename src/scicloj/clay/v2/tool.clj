(ns scicloj.clay.v2.tool)

(defprotocol Tool
  (setup! [this config])
  (open! [this])
  (close! [this])
  (show! [this context]))
