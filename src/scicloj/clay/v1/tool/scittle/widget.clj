(ns scicloj.clay.v1.tool.scittle.widget)

(defn mark-plain-html [hiccup]
  (-> hiccup
      (vary-meta assoc :clay/plain-html true)))

(defn plain-html? [hiccup]
  (-> hiccup
      meta
      :clay/plain-html))

(defn code [string]
  (mark-plain-html
   [:pre
    [:code.language-clojure
     #_{:style {:background "#ddd"}}
     string]]))

(defn clojure [string]
  (mark-plain-html
   [:pre
    [:code.language-clojure
     {:style {:background "#f1f1f1"}}
     string]]))

(defn structure-mark [string]
  (mark-plain-html
   [:div string]
   #_[:big string]))

(defn naive [value]
  (-> value
      println
      with-out-str
      clojure))
