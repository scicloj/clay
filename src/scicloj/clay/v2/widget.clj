(ns scicloj.clay.v2.widget
  (:require [clojure.pprint :as pp]
            [clojure.string :as string]))

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
