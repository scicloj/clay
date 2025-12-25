(ns scicloj.clay.v2.old.item
  (:require [clojure.pprint :as pp]
            [clojure.string :as str]
            [scicloj.kind-portal.v1.prepare :as kind-portal]
            [scicloj.kindly-render.shared.jso :as jso]
            [scicloj.clay.v2.old.files :as files]
            [scicloj.clay.v2.old.plotly-export :as plotly-export]
            [scicloj.clay.v2.old.util.image :as util.image]
            [scicloj.clay.v2.old.util.meta :as meta]
            [clj-commons.format.exceptions :as fe])
  (:import (javax.sound.sampled AudioFileFormat$Type
                                AudioFormat
                                AudioInputStream)
           (java.io ByteArrayInputStream
                    File)
           (java.nio ByteBuffer
                     ByteOrder)))

(def *id (atom 0))

(defn static? [context]
  (or (#{:pdf :gfm} (last (:format context)))
      (:static (:kindly/options context))))

(defn next-id! []
  (swap! *id inc))

(defn in-vector [v]
  (if (sequential? v)
    v
    [v]))

(defn escape [string]
  (-> string
      (str/escape
       {\< "&lt;"
        \> "&gt;"
        \& "&amp;"
        \" "&quot;"
        \' "&apos;"})))

(defn clojure-code-item [{:keys [tag hiccup-element md-class]}]
  (fn [string-or-strings]
    (let [strings (->> string-or-strings
                       in-vector
                       ;; (map escape)
                       )]
      {tag true
       :code (str
              (case md-class
                :printedClojure ";; =>\n"
                :sourceClojure "")
              (str/join "\n" strings))
       :hiccup (->> strings
                    (map (fn [s]
                           [:pre
                            [hiccup-element
                             (escape s)]]))
                    (into [:div]))
       :md (->> strings
                (map (fn [s]
                       (format "
::: {.%s}
```clojure
%s
```
:::
" (name md-class) s)))
                (str/join "\n"))})))

(def source-clojure
  (clojure-code-item {:tag :source-clojure
                      :hiccup-element :code.sourceCode.language-clojure.source-clojure.bg-light
                      :md-class :sourceClojure}))

(def printed-clojure
  (clojure-code-item {:tag :printed-clojure
                      :hiccup-element :code.sourceCode.language-clojure.printed-clojure
                      :md-class :printedClojure}))

(defn just-println [value]
  (-> value
      println
      with-out-str
      ;; escape
      printed-clojure))

(defn pprint [value]
  (printed-clojure
   (try (-> value
            pp/pprint
            with-out-str)
        (catch java.lang.IllegalArgumentException e
          (str ";; (note: the following value could not be pretty-printed)"
               (-> value
                   println
                   with-out-str))))))

(defn pprint-throwable [ex]
  (binding [fe/*fonts* nil
            fe/*default-frame-rules*
            (conj fe/*default-frame-rules*
                  [:name #"scicloj.clay.v2.old\..*" :terminate])]
    (fe/format-exception ex)))

(defn print-throwable [ex collapsed]
  (let [{:keys [via]} (Throwable->map ex)
        messages (for [{:keys [message type]} via]
                   (or message type))
        ex-detail (pprint-throwable ex)]
    {:printed-clojure true
     :hiccup
     [:div.callout.callout-style-default.callout-titled.callout-important
      [:details.callout-header {:open (not collapsed)}
       [:summary {:style {:list-style :none}}
        [:div.d-flex.align-content-center
         [:div.callout-icon-container [:i.callout-icon]]
         [:div.callout-title-container.flex-fill
          (into [:strong] (interpose [:br]) messages)]
         [:div.callout-btn-toggle.d-inline-block.border-0.py-1.ps-1.pe-0.float-end
          [:i.callout-toggle]]]]
       [:div.callout-body-container.callout-body
        [:pre [:code ex-detail]]]]]
     :md              (format "
::: {.callout-important collapse=%s}
## %s
```
%s
```
:::
" (str (boolean collapsed)) (str/join "\n" messages) ex-detail)}))

(defn print-output [label s]
  {:hiccup [:div
            [:strong label]
            [:pre [:code s]]]
   :md     (format "
::: {.callout-note}
## %s
```
%s
```
:::
" label s)})

(defn md [text]
  {:md (->> text
            in-vector
            (str/join "\n"))})

(defn katex-hiccup [s displayMode]
  [:span
   [:script
    (format
      "katex.render(%s, document.currentScript.parentElement, {displayMode: %s, throwOnError: false});"
      (jso/write-json-str s)
      (str (boolean displayMode)))]])

(defn tex [text]
  {:md     (->> text
                in-vector
                (map (partial format "$$%s$$"))
                (str/join "\n"))
   :hiccup (->> text
                in-vector
                (map (fn [s] (katex-hiccup s true)))
                (into [:div]))
   :deps   [:katex]})

(def separator
  {:hiccup [:div {:style
                  {:height           "2px"
                   :width            "100%"
                   :background-color "grey"}}]})

(def hidden
  {:md     ""
   :hiccup ""})

(defn structure-mark [string]
  {:md     string
   :hiccup [:p string]})

(def scittle-header-form
  '(defn kindly-compute [input callback]
     (ajax.core/POST
       "/kindly-compute"
       {:headers       {"Accept" "application/json"}
        :params        (pr-str input)
        :handler       (fn [response]
                         (-> response
                             read-string
                             callback))
        :error-handler (fn [e]
                         (.log
                          js/console
                          (str "error on compute: " e)))})))

(defn scittle-tag [cljs-form]
  [:script {:type "application/x-scittle"}
   (pr-str cljs-form)])

(defn scittle [{:as context
                :keys [value]}]
  {:hiccup (scittle-tag value)
   :deps (concat [:reagent]
                 (-> context
                     :kindly/options
                     :html/deps)
                 ;; deprecated:
                 (-> context
                     :kindly/options
                     :reagent/deps))})

(def next-id
  (let [*counter (atom 0)]
    #(str "id" (swap! *counter inc))))

(defn reagent [{:as context
                :keys [value]}]
  (let [id (next-id)]
    {:hiccup [:div {:id id}
              (scittle-tag (list 'reagent.dom/render
                                 value
                                 (list 'js/document.getElementById id)))]
     :deps (concat [:reagent]
                   (-> context
                       :kindly/options
                       :html/deps)
                   ;; deprecated:
                   (-> context
                       :kindly/options
                       :reagent/deps))}))

(defn emmy-viewers-expand
  "If `v` has a `fn?` (say, `f`) registered as Clerk viewer metadata, recurses
  with the expanded value `(f v)`. Else, returns `v` unchanged.
  Mimicking the original `emmy.viewer/expand` function."
  [v]
  (let [xform (-> v meta :nextjournal.clerk/viewer)]
    (if (fn? xform)
      (emmy-viewers-expand (xform v))
      v)))

(defn emmy-viewers [{:as context
                     :keys [value]}]
  (let [id (next-id)]
    {:hiccup [:div {:id id}
              [:script {:type "application/x-scittle"}
               (pr-str
                (list 'reagent.dom/render
                      [(list 'fn [] (emmy-viewers-expand
                                     value))]
                      (list 'js/document.getElementById id)))]]
     :deps (concat [:reagent :emmy-viewers]
                   (-> context
                       :kindly/options
                       :html/deps)
                   ;; deprecated:
                   (-> context
                       :kindly/options
                       :reagent/deps))}))

(defn extract-style [context]
  (-> context
      :kindly/options
      :element/style
      (or {:height "400px"
           :width "100%"})))

(defn cytoscape [{:as context
                  :keys [value]}]
  {:hiccup [:div
            {:style (extract-style context)}
            [:script
             (->> value
                  jso/write-json-str
                  (format
                   "
{
  value = %s;
  value['container'] = document.currentScript.parentElement;
  cytoscape(value);
};"))]]
   :deps [:cytoscape]})

(defn echarts [{:as context
                :keys [value]}]
  {:hiccup [:div
            {:style (extract-style context)}
            [:script
             (->> value
                  jso/write-json-str
                  (format
                   "
{
  var myChart = echarts.init(document.currentScript.parentElement);
  myChart.setOption(%s);
};"))]]
   :deps [:echarts]})

(defn mermaid [{:as   _context
                :keys [value]}]
  {:hiccup [:div.mermaid
            (first value)]
   :deps [:mermaid]})

(defn graphviz [{:as   _context
                :keys [value]}]
  {:hiccup [:div
            ;; create a local closure and then execute it immediately
            [:script {:type "text/javascript"}
             (format "(function() {
  const el = document.currentScript.parentElement
  Viz.instance().then(viz => {
    try {
      const svg = viz.renderSVGElement(`%s`);

      el.appendChild(svg);
   } catch (e) {
     console.error(e);
   }
  })})
();" (first value))]]
   :deps   [:graphviz]})

(defn plotly [{:as context
               :keys [kindly/options value]
               {:keys [data layout config]
                :or {layout {}
                     config {}}} :value}]
  (if (static? context)
    (let [[plot-path relative-path]
          (files/next-file! context "plotly-chart" value ".png")
          exit (plotly-export/export-plot! plot-path data layout)]
      (if (zero? exit)
        (println "Clay plotly-export:" [:wrote plot-path])
        (println "Clay plotly-export failed."))
      {:md (str "![" (:caption options) "](" relative-path ")")})
    {:hiccup
     [:div
      {:style (-> context
                  :kindly/options
                  :element/style
                  (or {:height "auto"
                       :width "100%"}))}
      [:script
       (format "Plotly.newPlot(document.currentScript.parentElement, %s, %s, %s);"
               (jso/write-json-str data)
               (jso/write-json-str layout)
               (jso/write-json-str config))]]
     :deps [:plotly]}))

(defn portal [value]
  {:hiccup [:div
            [:script
             (->> {:value value}
                  kind-portal/prepare
                  meta/pr-str-with-meta
                  pr-str
                  (format "portal_api.embed().renderOutputItem(
                {'mime': 'x-application/edn',
                 'text': (() => %s)}
                , document.currentScript.parentElement);"))]]
   :deps [:portal]})

(def loader
  {:hiccup [:div.loader]})

(defn info-line [{:keys [path url]}]
  {:hiccup
   [:div
    (when path
      [:pre
       [:small
        [:small
         "source: "
         (if url
           [:a {:href url} path]
           path)]]])]})

(defn html [html]
  {:html (->> html
              in-vector
              (str/join "\n"))})

(defn image [{:keys [value kindly/options]
              :as context}]
  (if (sequential? value)
    ;; If value is sequential, just handle the first element.
    (-> context
        (update :value first)
        image)
    ;; Figure out what kind of image representation we have.
    (merge
     {:item-class "clay-image"}
     (cond
       ;; An image url:
       (:src value)
       {:hiccup [:img value]}
       ;; A BufferedImage object:
       (util.image/buffered-image? value)
       (let [[png-path relative-path]
             (files/next-file! context "image" value ".png")]
         (when-not (util.image/write! value "png" png-path)
           (throw (ex-message "Failed to save image as PNG.")))
         {:md (str "![" (:caption options) "](" relative-path ")")})
       :else
       {:md (str "unsupported image format: " (type value))}))))

(defmacro shortify
  "Takes a floating-point number f in the range [-1.0, 1.0] and scales
  it to the range of a signed 16-bit integer. Clamps any overflows."
  ;; taken from: https://github.com/harold/clj-simple-wave-file/blob/main/src/clj_simple_wave_file/core.clj
  [f]
  (let [max-short-as-double (double Short/MAX_VALUE)]
    `(let [clamped# (-> ~f (min 1.0) (max -1.0))]
       (short (* ~max-short-as-double clamped#)))))

(defn write-samples-to-wav!
  "Takes float samples between -1 to 1, a sampling rate, and a target path,
  and writes the corresponding single-channel WAV file to that path."
  [{:keys [samples sample-rate target-path]}]
  (let [audio-format (AudioFormat. sample-rate 16 1 true false)
        ;; inspired by: https://github.com/phronmophobic/whisper.clj/blob/4d44aa68c383297fbf3a56d861e1f306abb44aaf/src/com/phronemophobic/whisper.clj#L73
        buf (doto (ByteBuffer/allocate (* 2 (count samples)))
              (.order (ByteOrder/nativeOrder)))
        sbuf (.asShortBuffer buf)
        _ (doseq [f samples]
            (.put sbuf ^short (shortify f)))
        byte-input-stream (ByteArrayInputStream. (.array buf))
        audio-input-stream (AudioInputStream. byte-input-stream
                                              audio-format
                                              (count samples))]
    (javax.sound.sampled.AudioSystem/write audio-input-stream
                                           AudioFileFormat$Type/WAVE
                                           (File. ^String target-path))))

(defn audio [{:keys [value] :as context}]
  (let [{:keys [src
                samples sample-rate]} value]
    (cond
      ;; A video file
      src {:hiccup [:audio {:controls ""}
                    [:source {:src src}]]}
      ;; Audio samples
      samples (let [[wav-path relative-path]
                    (files/next-file! context "audio" value ".wav")]
                (when-not (write-samples-to-wav! {:samples samples
                                                  :sample-rate sample-rate
                                                  :target-path wav-path})
                  (throw (ex-message "Failed to save audio as WAV.")))
                {:hiccup [:audio {:controls ""}
                          [:source {:src relative-path}]]}))))

(defn vega-embed [{:keys [value]
                   :as context}]
  (let [{:keys [data]} value
        data-to-use (or (when-let [{:keys [values format]} data]
                          (when (some-> format :type name (= "csv"))
                            (let [[csv-path relative-path]
                                  (files/next-file! context "data" values ".csv")]
                              (spit csv-path values)
                              {:url relative-path
                               :format format})))
                        data)]
    {:hiccup [:div
              [:script (-> value
                           (assoc :data data-to-use)
                           jso/write-json-str
                           (->> (format "vegaEmbed(document.currentScript.parentElement, %s);")))]]
     :deps [:vega]}))

(defn video [{:keys [src
                     youtube-id
                     iframe-width
                     iframe-height
                     allowfullscreen
                     embed-options]
              :or {allowfullscreen true}}]
  (cond
    ;; A video file
    src {:hiccup [:video {:controls ""}
                  [:source {:src src
                            :type "video/mp4"}]]}
    ;; A youtube video
    youtube-id {:hiccup [:iframe
                         (merge
                          (when iframe-height
                            {:height iframe-height})
                          (when iframe-width
                            {:width iframe-width})
                          {:src (str "https://www.youtube.com/embed/"
                                     youtube-id
                                     (some->> embed-options
                                              (map (fn [[k v]]
                                                     (format "%s=%s" (name k) v)))
                                              (str/join "&")
                                              (str "?")))
                           :allowfullscreen allowfullscreen})]}))



(defn observable [code]
  {:md (->> code
            in-vector
            (str/join "\n")
            (format "
```{ojs}
//| echo: false
%s
```"))})

(defn ggplotly [spec]
  (let [id (str "htmlwidget-1" (next-id!))]
    {:hiccup [:div {:class "plotly html-widget html-fill-item-overflow-hidden html-fill-item"
                    :id id
                    :style "width:100%;height:400px;"}
              [:script {:type "application/htmlwidget-sizing"
                        :data-for id}
               (jso/write-json-str {:viewer  {:width  "100%"
                                              :height  400
                                              :padding "0"
                                              :fille   true}
                                    :browser {:width "100%"
                                              :height 400
                                              :padding "0"
                                              :fille true}})]
              [:script {:type "application/json"
                        :data-for id}
               (jso/write-json-str spec)]]
     :deps [:htmlwidgets-ggplotly]}))

(defn highcharts
  "Prepare Highcharts JSON data for plotting."
  [value]
  {:hiccup [:div
            [:script
             (format
               "Highcharts.chart(document.currentScript.parentElement, %s);"
               (jso/write-json-str value))]]
   :deps [:highcharts]})

(defn dataset [{:as context
                :keys [value kindly/options]}]
  (let [{:keys [dataset/print-range]} options]
    (-> value
        (cond-> print-range
          ((resolve 'tech.v3.dataset.print/print-range) print-range))
        println
        with-out-str
        md
        (merge {:item-class "clay-dataset"}))))
