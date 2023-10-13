(ns scicloj.clay.v2.item
  (:require [clojure.pprint :as pp]
            [clojure.string :as string]
            [jsonista.core :as jsonista]
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
                          jsonista/write-value-as-string
                          (format "vegaEmbed(document.currentScript.parentElement, %s);"))]]
   :deps ['vega]})

(defn reagent [component-symbol data]
  {:reagent (cond
              ;;
              (vector? data)
              (into [component-symbol] data)
              ;;
              (map? data)
              [component-symbol data])})

(defn image [buffered-image]
  {:hiccup [:img {:src (-> buffered-image
                           util.image/buffered-image->byte-array
                           util.image/byte-array->data-uri)}]})
