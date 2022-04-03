(ns scicloj.clay.v1.tools
  (:require [scicloj.clay.v1.tool.portal :as tool.portal]
            [scicloj.clay.v1.tool.clerk :as tool.clerk]))

(def portal tool.portal/tool)

(def clerk tool.clerk/tool)
