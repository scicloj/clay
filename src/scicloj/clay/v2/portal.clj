(ns scicloj.clay.v2.portal
  (:require [portal.api :as portal]
            [clojure.string :as string]
            [scicloj.clay.v2.util.meta :as meta]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kind-portal.v1.api :as kind-portal]
            [scicloj.clay.v2.widget :as widget]
            [hiccup.page]))

(defonce dev
  (portal/url
   (portal/start {})))

(def url (let [[host query] (string/split dev #"\?")]
           (str host "/main.js?" query)))



(defn in-portal [value]
  (->> {:value value}
       kind-portal/prepare
       meta/pr-str-with-meta
       pr-str
       (format "portal.extensions.vs_code_notebook.activate().renderOutputItem(
                {\"mime\": \"x-application/edn\",
                 \"text\": (() => %s)}
                , document.currentScript.parentElement);")
       (vector :script)
       (vector :div)
       kind/hiccup
       widget/mark-plain-html))
