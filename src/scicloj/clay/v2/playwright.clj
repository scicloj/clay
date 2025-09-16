(ns scicloj.clay.v2.playwright
  (:require [scicloj.kindly-render.shared.jso :as jso]
            [clojure.java.io :as io])
  (:import (com.microsoft.playwright Playwright Browser Page)))

(defn try-launch [browser-key playwright]
  (let [browser-type (case browser-key
                       :chromium (.chromium playwright)
                       :firefox (.firefox playwright)
                       :webkit (.webkit playwright))]
    (try
      (.launch browser-type)
      (catch Exception _ nil))))

(defn find-browser [playwright]
  (or (try-launch :chromium playwright)
      (try-launch :firefox playwright)
      (try-launch :webkit playwright)
      (throw (ex-info "No browser found" {:id ::no-browser}))))

(defn generate-plotly-html [data layout]
  (str "<html><body>"
       "<div id='myDiv' style='width:600px;height:400px;'></div>"
       "<script src='https://cdn.plot.ly/plotly-latest.min.js'></script>"
       "<script>"
       "  var data = " (jso/write-json-str data) ";"
       "  var layout = " (jso/write-json-str layout) ";"
       "  Plotly.newPlot('myDiv', data, layout);"
       "</script>"
       "</body></html>"))

(defn extract-plotly-svg [filename data layout]
  (with-open [playwright (Playwright/create)]
    (let [browser (find-browser playwright)
          page (.newPage browser)]
      (try
        (.setContent page (generate-plotly-html data layout))
        (let [locator (.locator page "#myDiv")
              wait-opts (doto (com.microsoft.playwright.Locator$WaitForOptions.)
                          (.setTimeout 30000))]
          (.waitFor locator wait-opts))
        (let [svg-content (.evaluate page "document.querySelector('#myDiv svg')?.outerHTML;")]
          (if (seq svg-content)
            (do
              (io/make-parents filename)
              (spit filename svg-content)
              (println "SVG saved to" filename))
            (throw (ex-info "SVG not found in rendered Plotly chart." {:filename filename}))))
        (finally
          (when browser
            (.close browser)))))))

(comment
  (extract-plotly-svg "plotly_chart.svg"
                      [{:y [1 2 3 4 5] :type "bar"}]
                      {:title "Clojure + Playwright Plotly"})
  :-)
