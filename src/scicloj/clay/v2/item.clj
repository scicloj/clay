(ns scicloj.clay.v2.item
  (:require [clojure.pprint :as pp]
            [clojure.string :as string]
            [charred.api :as charred]
            [scicloj.clay.v2.files :as files]
            [scicloj.clay.v2.util.image :as util.image]
            [scicloj.kind-portal.v1.api :as kind-portal]
            [scicloj.clay.v2.util.meta :as meta]
            [hiccup.page]
            [clojure.string :as str]))

(defn in-vector [v]
  (if (vector? v)
    v
    [v]))

(defn escape [string]
  (-> string
      (string/escape
       {\< "&lt;" \> "&gt;"})))

(defn clojure-code-item [{:keys [tag hiccup-element md-class]}]
  (fn [string-or-strings]
    (let [strings (->> string-or-strings
                       in-vector
                       ;; (map escape)
                       )]
      {tag true
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
                (string/join "\n"))})))

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
      escape
      printed-clojure))

(defn pprint [value]
  (-> value
      pp/pprint
      with-out-str
      escape
      printed-clojure))

(defn hiccup [hiccup]
  {:hiccup hiccup
   :deps (-> hiccup meta :deps)})

(defn md [text]
  {:md (->> text
            in-vector
            (string/join "\n"))})

(def separator
  {:hiccup [:div {:style
                  {:height "2px"
                   :width "100%"
                   :background-color "grey"}}]})

(def hidden
  {:md ""
   :hiccup ""})

(defn structure-mark [string]
  {:md string
   :hiccup [:p string]})


(def next-id
  (let [*counter (atom 0)]
    #(str "id" (swap! *counter inc))))

(defn reagent [{:as context
                :keys [value]}]
  (let [id (next-id)]
    {:hiccup [:div {:id id}
              [:script {:type "application/x-scittle"}
               (pr-str
                (list 'reagent.dom/render
                      value
                      (list 'js/document.getElementById id)))]]
     :deps (cons :reagent
                 (-> context
                     :kindly/options
                     :reagent/deps))}))

(defn extract-style [context]
  (-> context
      :kindly/options
      :element/style
      (or {:height "400px"
           :width "400px"})))

(defn cytoscape [{:as context
                  :keys [value]}]
  {:hiccup [:div
            {:style (extract-style context)}
            [:script
             (->> value
                  charred/write-json-str
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
                  charred/write-json-str
                  (format
                   "
{
  var myChart = echarts.init(document.currentScript.parentElement);
  myChart.setOption(%s);
};"))]]
   :deps [:echarts]})


(defn plotly [{:as context
               :keys [value]}]
  {:hiccup [:div
            {:style (extract-style context)}
            [:script
             (->> value
                  charred/write-json-str
                  (format
                   "
Plotly.newPlot(document.currentScript.parentElement,
 %s['data']
);
"))]]
   :deps [:plotly]})


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
              (string/join "\n"))})

(defn image [{:keys [value
                     full-target-path
                     base-target-path]
              :as context}]
  (let [png-path (files/next-file!
                  full-target-path
                  ""
                  value
                  ".png")]
    (when-not
        (util.image/write! value "png" png-path)
      (throw (ex-message "Failed to save image as PNG.")))
    {:hiccup [:img {:style {:width "100%"}
                    :src (-> png-path
                             (string/replace
                              (re-pattern (str "^"
                                               base-target-path
                                               "/"))
                              ""))}]}))


(defn vega-embed [{:keys [value
                          full-target-path
                          base-target-path]
                   :as context}]
  (let [{:keys [data]} value
        data-to-use (or (when-let [{:keys [values format]} data]
                          (when (some-> format :type name (= "csv"))
                            (let [csv-path (files/next-file!
                                            full-target-path
                                            ""
                                            values
                                            ".csv")]
                              (spit csv-path values)
                              {:url (-> csv-path
                                        (string/replace
                                         (re-pattern (str "^"
                                                          base-target-path
                                                          "/"))
                                         ""))
                               :format format})))
                        data)]
    {:hiccup [:div
              [:script (-> value
                           (assoc :data data-to-use)
                           charred/write-json-str
                           (->> (format "vegaEmbed(document.currentScript.parentElement, %s);")))]]
     :deps [:vega]}))


(defn video [{:keys [youtube-id
                     iframe-width
                     iframe-height
                     allowfullscreen
                     embed-options]
              :or {allowfullscreen true}}]
  {:hiccup [:iframe
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
              :allowfullscreen allowfullscreen})]})
