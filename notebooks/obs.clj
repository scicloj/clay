^{:clay {:quarto {:format {:html {:echo false
                                  :keep-hidden true}}}}}
(ns obs
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]
            [tablecloth.api :as tc]))


;; https://quarto.org/docs/interactive/ojs/examples/penguins.html

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
                      {y: \"count\"},
                      {x: \"body_mass_g\", fill: \"species\", thresholds: 20}
                      ))
 .plot({
        facet: {
                data: filtered,
                x: \"sex\",
                y: \"species\",
                marginRight: 80
                },
        marks: [
                Plot.frame(),
                ]
        }
       )
Inputs.table(filtered)
data = FileAttachment(\"palmer-penguins.csv\").csv({ typed: true })
filtered = data.filter(function(penguin) {
                                           return bill_length_min < penguin.bill_length_mm &&
                                           islands.includes(penguin.island);
                                           })
")
