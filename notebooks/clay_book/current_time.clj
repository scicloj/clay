^:kindly/servable
(ns clay-book.current-time)

;; The namespace is annotated with `^:kindly/serve`,
;; so this notebook is evaluated every time it is viewed.

(str (java.time.LocalTime/now))

;; We can define some persistent state:

(defonce state (atom 0))

(def messages
  ["You will have good luck today"
   "A pleasant surprise awaits you"
   "Your hard work will pay off"])

;; Make use of that state:

^:kind/hiccup
[:div
 [:strong "Welcome!" (messages @state)]]

;; Modify the state every time the page is viewed:

(swap! state (fn [x]
               (mod (inc x) (count messages))))

;; So that the message cycles every time you view the notebook.
