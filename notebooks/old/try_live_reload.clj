;; ^{:clay {:smart-sync true}}
(ns try-live-reload
  (:require [scicloj.kindly.v4.kind :as kind]))

(comment
  (require '[scicloj.clay.v2.old.api :as clay])
  (clay/make! {:source-path "notebooks/try_live_reload.clj"
               :live-reload true}))

(Thread/sleep 1000)

(kind/echarts
 ,,
 {:title {:text "Echarts Example"}
  :tooltip {}
  :legend {:data ["sales"]}
  :xAxis {:data ["Shirts", "Cardigans", "Chiffons",
                 "Pants", "Heels", "Socks"]}
  :yAxis {}
  :series [{:name "sales"
            :type "bar"
            :data [5 20 36
                   10 10 20]}]})

#_(+ 1 2,,,)


(Thread/sleep 5000)



(+ 1 2)
