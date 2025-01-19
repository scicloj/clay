(ns compute-examples
  (:require [scicloj.kindly.v4.kind :as kind]))

(defn ^:kindly/servable
  add [a b]
  (+ a b))

(defn ^:kindly/servable
  calc-click-and-open-rate [data]
  (let [total-emails (count data)
        opened-emails (count (filter #(nth % 2) data))
        clicked-emails (count (filter #(nth % 3) data))
        open-rate (if (pos? total-emails) (double (* 100 (/ opened-emails total-emails))) 0.0)
        click-rate (if (pos? total-emails) (double (* 100 (/ clicked-emails total-emails))) 0.0)]
    {:open-rate open-rate
     :click-rate click-rate}))

(kind/reagent
 ['(fn []
     (let [*a1 (reagent.core/atom 10)]
       (fn []
         [:div
          [:p @*a1]
          [:input {:type     "button" :value "Click me!"
                   :on-click (fn []
                               (kindly-compute
                                {:func 'compute-examples/add
                                 :args [@*a1 20]}
                                (fn [response]
                                  (reset! *a1 response))))}]])))])


(kind/md
 "### New example of a function that will calculate the open and click rates of the email data below")

(def email-data
  [["email1@example.com" "19-12-2024 11:46:05" "19-12-2024 12:00:00" "19-12-2024 12:05:00"]
   ["email2@example.com" "19-12-2024 11:46:06" "20-12-2024 12:00:00" nil]
   ["email3@example.com" "19-12-2024 11:46:07" "21-12-2024 12:00:00" "21-12-2024 12:05:00"]
   ["email4@example.com" "19-12-2024 11:46:08" "22-12-2024 12:00:00" "22-12-2024 12:05:00"]
   ["email5@example.com" "19-12-2024 11:46:09" nil nil]
   ["email6@example.com" "19-12-2024 11:46:10" "24-12-2024 12:00:00" "24-12-2024 12:05:00"]
   ["email7@example.com" "19-12-2024 11:46:11" nil nil]
   ["email8@example.com" "19-12-2024 11:46:12" "26-12-2024 12:00:00" "26-12-2024 12:05:00"]
   ["email9@example.com" "19-12-2024 11:46:13" "27-12-2024 12:00:00" nil]
   ["email10@example.com" "19-12-2024 11:46:14" "28-12-2024 12:00:00" nil]])

(kind/table
 {:column-names [:email :sent-at :opened-at :clicked-at]
  :row-vectors  email-data})

(kind/hiccup
 [:div
  '(def email-data
     [["email1@example.com" "19-12-2024 11:46:05" "19-12-2024 12:00:00" "19-12-2024 12:05:00"]
      ["email2@example.com" "19-12-2024 11:46:06" "20-12-2024 12:00:00" nil]
      ["email3@example.com" "19-12-2024 11:46:07" "21-12-2024 12:00:00" "21-12-2024 12:05:00"]
      ["email4@example.com" "19-12-2024 11:46:08" "22-12-2024 12:00:00" "22-12-2024 12:05:00"]
      ["email5@example.com" "19-12-2024 11:46:09" nil nil]
      ["email6@example.com" "19-12-2024 11:46:10" "24-12-2024 12:00:00" "24-12-2024 12:05:00"]
      ["email7@example.com" "19-12-2024 11:46:11" nil nil]
      ["email8@example.com" "19-12-2024 11:46:12" "26-12-2024 12:00:00" "26-12-2024 12:05:00"]
      ["email9@example.com" "19-12-2024 11:46:13" "27-12-2024 12:00:00" nil]
      ["email10@example.com" "19-12-2024 11:46:14" "28-12-2024 12:00:00" nil]])
  ['(fn []
      (let [*rates (reagent.core/atom {})]
        (fn []
          [:div
           [:p (str "Open rate " (:open-rate @*rates))]
           [:p (str "Click rate " (:click-rate @*rates))]
           [:input {:type "button" :value "Click to calculate click and open rate"
                    :on-click (fn []
                                (kindly-compute
                                 {:func 'compute-examples/calc-click-and-open-rate
                                  :args [email-data]}
                                 (fn [response]
                                   (reset! *rates response))))}]])))]])

