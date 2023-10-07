(ns scicloj.clay.v2.item
  (:require [clojure.pprint :as pp]
            [clojure.string :as string]
            [jsonista.core :as jsonista]))

(defn source-clojure [string]
  {:hiccup [:pre.card
            [:code.language-clojure.bg-light
             string]]
   :md (format "
<div class=\"sourceClojure\">
```clojure
%s
```
</div>
" string)})

(defn printed-clojure [string]
  {:hiccup [:pre.card
            [:code.language-clojure
             string]]
   :md (format "
<div class=\"printedClojure\">
```clojure
%s
```
</div>
" string)})

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
  {:md text})

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
            'vega ; to help Clay realize that the dependency is needed
            [:script (->> spec
                          jsonista/write-value-as-string
                          (format "vegaEmbed(document.currentScript.parentElement, %s);"))]]
   :deps [:vega]})

(defn reagent [component-symbol options]
  {:reagent (cond
              ;;
              (vector? options)
              (into [component-symbol] options)
              ;;
              (map? options)
              [component-symbol options])})
