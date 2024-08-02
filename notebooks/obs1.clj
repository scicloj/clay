(ns obs1
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]
            [tablecloth.api :as tc]))
50




(kind/hiccup [:script {:src "https://cdn.jsdelivr.net/npm/d3@7"}])
(kind/hiccup [:script {:src "https://cdn.jsdelivr.net/npm/@observablehq/plot@0.6"}])
(kind/hiccup [:script {:src "https://cdn.jsdelivr.net/npm/@observablehq/stdlib@5.8.6"}])


(kind/hiccup
 [:div
  [:script
   "plot = Plot.rectY(
              {length: 10000},
              Plot.binX({y: 'count'}, {x: Math.random})
           ).plot();
document.currentScript.parentElement.append(plot);
"]])

(kind/hiccup
 [:div
  [:script
   "
penguins = observablehq.FileAttachments('notebooks/datasets/iris.csv');
plot = Plot.dot(penguins, {x: 'culmen_length_mm', y: 'culmen_depth_mm', stroke: 'species'}).plot();
document.currentScript.parentElement.append(plot);
 "]])


;; (kind/reagent
;;  '[(fn []
;;      [:div
;;       {:ref
;;        (fn [el]
;;          (let [plot
;;                (-> (.rectY js/Plot
;;                            (clj->js {:length 10000})
;;                            (.binX js/Plot
;;                                   (clj->js {:y "count"})
;;                                   (clj->js {:x (.random js/Math)})))
;;                    .plot)]
;;            (.append el plot)))}])])


;; (kind/reagent
;;  '[(fn []
;;      [:div {:ref (fn [el]
;;                    (let [penguins (-> "notebooks/datasets/palmer-penguins.csv"
;;                                       js/FileAttachment
;;                                       (.csv (clj->js {:typed true})))
;;                          ;; plot (-> (.dot js/Plot
;;                          ;;                (clj->js
;;                          ;;                 {:x "culmen_length_mm"
;;                          ;;                  :y "culmen_depth_mm"
;;                          ;;                  :stroke "species"}))
;;                          ;;          (.plot))
;;                          ]
;;                      #_(.append el plot)))}])])


(kind/observable
 "
//| panel: input
viewof bill_length_min = Inputs.range(
                                      [32, 50],
                                      {value: 35, step: 1, label: 'Bill length (min):'}
                                      )
viewof islands = Inputs.checkbox(
                                 ['Torgersen', 'Biscoe', 'Dream'],
                                 { value: ['Torgersen', 'Biscoe'],
                                  label: 'Islands:'
                                  }
                                 )

Plot.rectY(filtered,
            Plot.binX(
                      {y: 'count'},
                      {x: 'body_mass_g', fill: 'species', thresholds: 20}
                      ))
 .plot({
        facet: {
                data: filtered,
                x: 'sex',
                y: 'species',
                marginRight: 80
                },
        marks: [
                Plot.frame(),
                ]
        }
       )
Inputs.table(filtered)
data = FileAttachment('notebooks/datasets/palmer-penguins.csv').csv({ typed: true })
filtered = data.filter(function(penguin) {
                                           return bill_length_min < penguin.bill_length_mm &&
                                           islands.includes(penguin.island);
                                           })
")


(kind/reagent
 ['(fn []
     [:div (pr-str js/Plot)])])








(-> {:x (range 100)
     :y (repeatedly 100 rand)}
    tc/dataset
    (tc/write! "notebooks/datasets/temp.csv"))

(kind/observable
 "
//| panel: input
Inputs.table(FileAttachment('notebooks/datasets/temp.csv').csv({ typed: true }))
")
