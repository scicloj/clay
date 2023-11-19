(ns scicloj.clay.v2.item
  (:require [clojure.pprint :as pp]
            [clojure.string :as string]
            [charred.api :as charred]
            [scicloj.clay.v2.files :as files]
            [scicloj.clay.v2.util.image :as util.image]
            [scicloj.kind-portal.v1.api :as kind-portal]
            [scicloj.clay.v2.util.meta :as meta]))

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

(defn vega-embed [spec]
  {:hiccup [:div
            [:script (->> spec
                          charred/write-json-str
                          (format "vegaEmbed(document.currentScript.parentElement, %s);"))]]
   :deps [:vega]})


(defn image [{:keys [value
                     target-path]}]
  (let [jpg-path (files/next-tempfile!
                  target-path
                  value
                  ".jpg")]
    (util.image/write! value jpg-path)
    {:hiccup [:img {:style {:width "100%"}
                    :src (-> jpg-path
                             (string/replace
                              #"^docs/" ""))}]}))

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

(defn extract-options-and-spec [data]
  (if (vector? data)
    data
    [{:style {:height "400px"
              :width "400px"}}
     data]))


(defn cytoscape [data]
  (let [[options spec] (extract-options-and-spec data)]
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
  (let [[options spec] (extract-options-and-spec data)]
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
  (let [[options spec] (extract-options-and-spec data)]
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
  {:hiccup (->> {:value value}
                kind-portal/prepare
                meta/pr-str-with-meta
                pr-str
                (format "portal_api.embed().renderOutputItem(
                {'mime': 'x-application/edn',
                 'text': (() => %s)}
                , document.currentScript.parentElement);")
                (vector :script)
                (vector :div))
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
