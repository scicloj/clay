(ns index1
  (:require [scicloj.clay.v2.api :as clay]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.kindly.v4.kind :as kind]
            [scicloj.kind-portal.v1.impl :as kind-portal.impl]
            [scicloj.noj.v1.vis :as vis]
            [tablecloth.api :as tc]))

(clay/swap-options!
 assoc
 :quarto {:format {:html {:toc true
                          :theme :spacelab
                          :embed-resources true}}
          :highlight-style :solarized
          :code-block-background true
          :embed-resources true})

(defn pr-str-with-meta [value]
  (binding [*print-meta* true]
    (pr-str value)))

(defn in-portal [value]
  (kind/hiccup
   ['(fn [{:keys [edn-str]}]
       (let [api (js/portal.extensions.vs_code_notebook.activate)]
         [:div
          [:div
           {:ref (fn [el]
                   (.renderOutputItem api
                                      (clj->js {:mime "x-application/edn"
                                                :text (fn [] edn-str)})
                                      el))}]]))
    {:edn-str (-> value
                  kind-portal.impl/prepare-value
                  pr-str-with-meta)}]))


(def myplot
  (-> {:x (repeatedly 999 rand)}
      tc/dataset
      (vis/hanami-histogram :x {:nbins 50})
      (assoc :height 100)))

(def my-images
  [(-> [:img {:height 50 :width 50
              :src "https://clojure.org/images/clojure-logo-120b.png"}]
       kind/hiccup)
   (-> [:img {:height 50 :width 50
              :src "https://raw.githubusercontent.com/djblue/portal/fbc54632adc06c6e94a3d059c858419f0063d1cf/resources/splash.svg"}]
       kind/hiccup)])

{:x (range 3)}

(in-portal {:x (range 3)})

my-images

(in-portal my-images)

myplot

(in-portal myplot)



(in-portal
 [(kind/hiccup
   [:div
    [:h1 "ab"]
    [:h2 "cd"]
    [:img {:height 50 :width 50
           :src "https://clojure.org/images/clojure-logo-120b.png"}]
    [:img {:height 50 :width 50
           :src "https://raw.githubusercontent.com/djblue/portal/fbc54632adc06c6e94a3d059c858419f0063d1cf/resources/splash.svg"}]])])


(in-portal
 [(kind/hiccup
   [:div
    [:h1 "ab"]
    [:h2 "cd"]
    [:img {:height 50 :width 50
           :src "https://clojure.org/images/clojure-logo-120b.png"}]
    [:img {:height 50 :width 50
           :src "https://raw.githubusercontent.com/djblue/portal/fbc54632adc06c6e94a3d059c858419f0063d1cf/resources/splash.svg"}]])])
