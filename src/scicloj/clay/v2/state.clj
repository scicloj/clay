(ns scicloj.clay.v2.state)

(defonce *state
  (atom {:port nil
         :items nil
         :fns {}
         :counter 0
         :page-cache nil}))

(defn counter []
  (:counter @*state))

(defn swap-state! [f & args]
  (-> *state
      (swap!
       (fn [state]
         (-> state
             (#(apply f % args))
             (assoc :page-cache nil))))))

(defn swap-state-and-increment! [f & args]
  (swap-state!
   (fn [state]
     (-> state
         (update :counter inc)
         (#(apply f % args))))))


(defn set-items! [items]
  (swap-state-and-increment! assoc :items items))

(defn swap-options! [f & args]
  (apply swap-state! update :options f args)
  :ok)

(defn options []
  (:options @*state))

(defn reset-quarto-html-path! [path]
  (swap-state! assoc :quarto-html-path path))

(defn quarto-html-path []
  (:quarto-html-path @*state))

(defn set-port! [port]
  (swap-state! assoc :port port))

(defn port []
  (:port @*state))
