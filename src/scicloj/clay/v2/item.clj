(ns scicloj.clay.v2.item
  (:require [clojure.pprint :as pp]
            [clojure.string :as string]
            [jsonista.core :as jsonista]
            [charred.api :as charred]
            [scicloj.clay.v2.util.image :as util.image]))

(defn in-vector [v]
  (if (vector? v)
    v
    [v]))

(defn clojure-code-item [{:keys [tag hiccup-element md-class]}]
  (fn [string-or-strings]
    (let [strings (in-vector string-or-strings)]
      {tag true
       :hiccup (->> strings
                    (map (fn [s]
                           [:pre ;.card
                            [hiccup-element
                             s]]))
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

(defn escape [string]
  (-> string
      (string/escape
       {\< "&lt;" \> "&gt;"})))

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
   :hicup ""})

(defn structure-mark [string]
  {:md string
   :hiccup string})

(defn vega-embed [spec]
  {:hiccup [:div
            [:script (->> spec
                          charred/write-json-str
                          (format "vegaEmbed(document.currentScript.parentElement, %s);"))]]
   :deps ['vega]})


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
     :deps ['reagent]}))


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
     :deps ['cytoscape]}))


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
     :deps ['plotly]}))
