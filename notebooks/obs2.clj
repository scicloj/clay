(ns obs2
  (:require [scicloj.kindly.v4.kind :as kind]
            [clojure.string :as str]))

(kind/observable
 "athletes = FileAttachment('notebooks/datasets/athletes.csv').csv({typed: true})")

(kind/observable
 "athletes")

(kind/observable
 "Inputs.table(athletes)")

(kind/observable
 "
Plot.plot({
  grid: true,
  facet: {
    data: athletes,
    y: \"sex\"
  },
  marks: [
    Plot.rectY(
      athletes,
      Plot.binX({y: \"count\"}, {x: \"weight\", fill: \"sex\"})
    ),
    Plot.ruleY([0])
  ]
})
")


(defn list-starting-with? [prefix form]
  (and (list? form)
       (-> form first (= prefix))))

(defn vector-starting-with? [prefix form]
  (and (vector? form)
       (-> form first (= prefix))))

(defn dot? [form]
  (list-starting-with? '. form))

(defn def? [form]
  (list-starting-with? 'def form))

(defn viewof? [form]
  (list-starting-with? 'viewof form))

(defn generated? [form]
  (vector-starting-with? :generated form))

(defn generated [string]
  (assert (string? string))
  [:generated string])

(defn generated->str [form]
  (second form))

(defn js? [form]
  (vector-starting-with? :js form))


(defn primitive? [form]
  (or (string? form)
      (number? form)
      (boolean? form)
      (symbol? form)))


(defn handle-form [form]
  (cond (generated? form) (generated->str form)
        (map? form) (->> form
                         (map (fn [[k v]]
                                (format "%s: %s"
                                        (name k) (handle-form v))))
                         (str/join ", ")
                         (format "{%s}"))
        (dot? form) (->> form
                         rest
                         (map handle-form)
                         (str/join "."))
        (def? form) (let [[lhs rhs] (rest form)]
                      (format "%s = %s"
                              (name lhs)
                              (handle-form rhs)))
        (viewof? form) (let [[lhs rhs] (rest form)]
                         (format "viewof %s = %s"
                                 (name lhs)
                                 (handle-form rhs)))
        (js? form) (-> form
                       second
                       str)
        (list? form)  (->> form
                           rest
                           (map handle-form)
                           (str/join ", ")
                           (format "%s(%s)" (-> form first name)))
        (vector? form) (->> form
                            (map handle-form)
                            (str/join ", ")
                            (format "[%s]"))
        (primitive? form) (pr-str form)))



(defn generate-from-forms [f-name & forms]
  [:generated
   (->> forms
        (map handle-form)
        (str/join ", ")
        (format "%s(%s)" f-name))])

;; Using functions:

(def plot (partial generate-from-forms "Plot.plot"))
(def rect-y (partial generate-from-forms "Plot.rectY"))
(def bin-x (partial generate-from-forms "Plot.binX"))
(def rule-y (partial generate-from-forms "Plot.ruleY"))

(-> (plot {:grid true
           :facet {:data 'athletes
                   :y "sex"}
           :marks [(rect-y 'athletes
                           (bin-x {:y "count"}
                                  {:x "weight" :fill "sex"}))
                   (rule-y [0])]})
    generated->str
    kind/observable)

;; Using symbols rather than functions:

(defn obs [& forms]
  (->> forms
       (map handle-form)
       (str/join "\n\n")
       kind/observable))

(obs
 '(Plot.plot {:grid true
              :facet {:data athletes
                      :y "sex"}
              :marks [(Plot.rectY athletes
                                  (Plot.binX {:y "count"}
                                             {:x "weight" :fill "sex"}))
                      (Plot.ruleY [0])]}))


;; A complicated example:

(obs
 '(viewof bill_length_min
          (Inputs.range [32 50]
                        {:value 35
                         :step 1
                         :label "Bill length (min):"}))
 '(viewof islands
          (Inputs.checkbox ["Torgersen" "Biscoe" "Dream"]
                           {:value ["Torgensen" "Biscoe"]
                            :lable "Islands:"}))
 '(. (Plot.rectY filtered
                 (Plot.binX {:y "count"}
                            {:x "body_mass_g"
                             :fill "species"
                             :thresholds 20}))
     (plot {:facet {:data filtered
                    :x "sex"
                    :y "species"
                    :marginRight 80}
            :marks [(Plot.frame)]}))
 '(Inputs.table filtered)
 '(def penguins (. (FileAttachment "notebooks/datasets/palmer-penguins.csv")
                   (csv {:typed true})))
 '(def filtered (. penguins
                   (filter [:js "function(penguin) {
                                           return bill_length_min < penguin.bill_length_mm &&
                                           islands.includes(penguin.island);
                                           }"]))))
