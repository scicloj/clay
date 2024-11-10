(ns dummy
  (:require [scicloj.kindly.v4.kind :as kind]))



;; Interpreted by the Scittle interpreter
;; in the browser.
(kind/reagent
 '(def *a1 (reagent.core/atom 0)))

;; Just JVM Clojure code.
(def *a2 (atom 0))


(kind/reagent
 '(defn compute [input callback]
    (ajax.core/POST
       "/compute"
       {:headers {"Accept" "application/json"}
        :params (pr-str input)
        :handler (fn [response]
                   (-> response
                       read-string
                       callback))
        :error-handler (fn [e]
                         (.log
                          js/console
                          (str "error on reset: " e)))})))


(kind/reagent
 ['(fn []
         [:div
          [:p @*a1]
          [:input {:type "button" :value "Click me!"
                   :on-click (fn []
                               (compute
                                @*a1
                                (fn [response]
                                  (reset! *a1 response))))}]])])



