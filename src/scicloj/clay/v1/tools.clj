(ns scicloj.clay.v1.tools
  (:require [scicloj.clay.v1.tool.portal :as tool.portal]
            [scicloj.clay.v1.tool.clerk :as tool.clerk]
            [scicloj.clay.v1.tool.scittle :as tool.scittle]))

(def portal tool.portal/tool)

(def clerk tool.clerk/tool)

(def scittle tool.scittle/tool)
