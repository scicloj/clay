(ns scicloj.clay.v2.item
  (:require [clojure.pprint :as pp]
            [clojure.string :as string]
            [charred.api :as charred]
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
  {:hiccup hiccup})

(defn md [text]
  {:md (->> text
            in-vector
            (string/join "\n"))})

(def separator
  {:hiccup [:div {:style
                  {:height "2px"
                   :width "100%"
                   :background-color "grey"}}]})

(def void
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


(defn image [buffered-image]
  {:hiccup [:img {:src (-> buffered-image
                           util.image/buffered-image->byte-array
                           util.image/byte-array->data-uri)}]})

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
     :deps [:reagent]}))


(defn reagent-based-item [component-symbol data]
  (reagent
   (cond
     ;;
     (vector? data)
     (into [component-symbol] data)
     ;;
     (map? data)
     [component-symbol data])))


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
