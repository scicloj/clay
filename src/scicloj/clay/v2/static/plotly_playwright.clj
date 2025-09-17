(ns scicloj.clay.v2.static.plotly-playwright
  (:require [scicloj.kindly-render.shared.jso :as jso]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [hiccup.core :as hiccup])
  (:import (com.microsoft.playwright Playwright Locator$WaitForOptions)
           (java.net URLDecoder)
           (java.util Base64)))

(set! *warn-on-reflection* true)

(defn plotly-html [data layout]
  (hiccup/html
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     ;; TODO: This is duplicated from `scicloj.clay.v2.page`,
     ;; it should be a shared def but under the current structure that causes a cyclic dependency.
     [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/plotly.js/2.20.0/plotly.min.js"}]]
    [:body
     [:div#myDiv]]
    [:script
     (str "var data = " (jso/write-json-str data) ";"
          "var layout = " (jso/write-json-str layout) ";"
          "Plotly.newPlot('myDiv', data, layout, {staticPlot: true});")]]))

(defn export-plot! [filename data layout]
  (let [ext (fs/extension filename)]
    (with-open [playwright (Playwright/create)
                browser (.launch (.chromium playwright))]
      (let [page (.newPage browser)]
        (.setContent page (plotly-html data layout))
        (let [locator (.locator page "#myDiv")
              wait-opts (doto (Locator$WaitForOptions.)
                          (.setTimeout 10000))]
          (.waitFor locator wait-opts)
          ;; Plotly.toImage on the rendered div returns encoded image
          (let [img-data-uri (.evaluate page (str "Plotly.toImage(document.getElementById('myDiv'), {format: '" ext "'})"))
                [_header payload] (str/split img-data-uri #"," 2)]
            (io/make-parents filename)
            (if (= ext "svg")
              (->> (URLDecoder/decode ^String payload "UTF-8")
                   (spit filename))
              (with-open [out (io/output-stream filename)]
                (.write out (.decode (Base64/getDecoder) ^String payload))))
            :success))))))

(comment
  (export-plot! "plotly_chart.svg"
                [{:y [1 2 3 4 5] :type "bar"}]
                {:title "Playwright Plotly"})
  (export-plot! "plotly_chart.png"
                [{:y [1 2 3 4 5] :type "bar"}]
                {:title "Playwright Plotly"})
  :-)
