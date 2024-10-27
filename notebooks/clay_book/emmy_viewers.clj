;; # Emmy-viewers

;; This namespace discusses Clay's support for
;; [Emmy-viewers](https://github.com/mentat-collective/emmy-viewers).

(ns clay-book.emmy-viewers
  (:require
   [scicloj.kindly.v4.kind :as kind]
   [emmy.env :as e :refer [D cube tanh cos sin]]
   [emmy.viewer :as ev]
   [emmy.mafs :as mafs]
   [emmy.mathbox.plot :as plot]
   [emmy.leva :as leva]))


(ev/with-let [!phase [0 0]]
  (let [shifted (ev/with-params {:atom !phase :params [0]}
                  (fn [shift]
                    (fn [x]
                      (((cube D) tanh) (e/- x shift)))))]
    (mafs/mafs
     {:height 400}
     (mafs/cartesian)
     (mafs/of-x shifted)
     (mafs/movable-point
      {:atom !phase :constrain "horizontal"})
     (mafs/inequality
      {:y {:<= shifted :> cos} :color :blue}))))

;;
;; Try moving the pink mark. 👆
;;

;; In the example above, we used emmy-viewers
;; to generate a Clojurescript expression
;; that can be interpreted as a Reagent component.
;; Here is the actual expression:

(kind/pprint
 (ev/with-let [!phase [0 0]]
   (let [shifted (ev/with-params {:atom !phase :params [0]}
                   (fn [shift]
                     (fn [x]
                       (((cube D) tanh) (e/- x shift)))))]
     (mafs/mafs
      {:height 400}
      (mafs/cartesian)
      (mafs/of-x shifted)
      (mafs/movable-point
       {:atom !phase :constrain "horizontal"})
      (mafs/inequality
       {:y {:<= shifted :> cos} :color :blue})))))

;; By default, it is inferred to be of `:kind/emmy-viewers`,
;; and is handle accordingly.

;; Equivalently, we could also handle it more explicitly with `:kind/reagent`:

(kind/reagent
 [`(fn []
     ~(ev/with-let [!phase [0 0]]
        (let [shifted (ev/with-params {:atom !phase :params [0]}
                        (fn [shift]
                          (fn [x]
                            (((cube D) tanh) (e/- x shift)))))]
          (mafs/mafs
           {:height 400}
           (mafs/cartesian)
           (mafs/of-x shifted)
           (mafs/movable-point
            {:atom !phase :constrain "horizontal"})
           (mafs/inequality
            {:y {:<= shifted :> cos} :color :blue})))))]
 {:html/deps [:emmy-viewers]})
