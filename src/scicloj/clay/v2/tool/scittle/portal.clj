(ns scicloj.clay.v2.tool.scittle.portal
  (:require [portal.api :as portal]
            [clojure.string :as string]
            [scicloj.clay.v2.util.meta :as meta]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kind-portal.v1.api :as kind-portal]))

(defonce dev
  (portal/url
   (portal/start {})))

(def url (let [[host query] (string/split dev #"\?")]
           (str host "/main.js?" query)))



(defn in-portal [value]
  (kind/hiccup
   ['(fn [edn-str]
       (let [api (js/portal.extensions.vs_code_notebook.activate)]
         [:div
          [:div
           {:ref (fn [el]
                   (.renderOutputItem api
                                      (clj->js {:mime "x-application/edn"
                                                :text (fn [] edn-str)})
                                      el))}]]))
    (-> {:value value}
        kind-portal/prepare
        meta/pr-str-with-meta)]))
