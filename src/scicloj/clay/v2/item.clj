(ns scicloj.clay.v2.item
  (:require [clojure.pprint :as pp]
            [clojure.string :as string]
            [charred.api :as charred]
            [scicloj.clay.v2.files :as files]
            [scicloj.clay.v2.util.image :as util.image]
            [scicloj.kind-portal.v1.api :as kind-portal]
            [scicloj.clay.v2.util.meta :as meta]
            [hiccup.page]))

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
<div class=\"%s\">
```clojure
%s
```
</div>
" (name md-class) s)))
                (string/join "\n"))})))

(def source-clojure
  (clojure-code-item {:tag :source-clojure
                      :hiccup-element :code.language-clojure.bg-light
                      :md-class :sourceClojure}))

(def printed-clojure
  (clojure-code-item {:tag :printed-clojure
                      :hiccup-element :code.language-clojure
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

(defn reagent [form]
  (let [id (next-id)]
    {:hiccup [:div {:id id}
              [:script {:type "application/x-scittle"}
               (pr-str
                (list 'reagent.dom/render
                      form
                      (list 'js/document.getElementById id)))]]
     :deps (cons :reagent
                 (-> form meta :deps))}))

(defn extract-options-and-spec [data
                                default-options]
  (if (vector? data)
    data
    [data default-options]))


(defn cytoscape [data]
  (let [[spec options] (extract-options-and-spec
                        data
                        {:style {:height "400px"
                                 :width "400px"}})]
    {:hiccup [:div
              options
              [:script
               (->> spec
                    charred/write-json-str
                    (format
                     "
{
  value = %s;
  value['container'] = document.currentScript.parentElement;
  cytoscape(value);
};"))]]
     :deps [:cytoscape]}))


(defn echarts [data]
  (let [[spec options] (extract-options-and-spec
                        data
                        {:style {:height "400px"
                                 :width "400px"}})]
    {:hiccup [:div
              options
              [:script
               (->> spec
                    charred/write-json-str
                    (format
                     "
{
  var myChart = echarts.init(document.currentScript.parentElement);
  myChart.setOption(%s);
};"))]]
     :deps [:echarts]}))


(defn plotly [data]
  (let [[spec options] (extract-options-and-spec
                        data
                        {:style {:height "400px"
                                 :width "400px"}})]
    {:hiccup [:div
              options
              [:script
               (->> spec
                    charred/write-json-str
                    (format
                     "
Plotly.newPlot(document.currentScript.parentElement,
 %s['data']
);
"))]]
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
