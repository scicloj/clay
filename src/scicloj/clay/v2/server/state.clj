(ns scicloj.clay.v2.server.state)

(defonce *state
  (atom {:port nil
         :counter 0
         :full-target-path nil
         :base-target-path nil}))

(defn swap-state! [f & args]
  (-> *state
      (swap!
       (fn [state]
         (-> state
             (#(apply f % args)))))))

(defn swap-state-and-increment! [f & args]
  (swap-state!
   (fn [state]
     (-> state
         (update :counter inc)
         (#(apply f % args))))))

(defn reset-full-target-path! [path]
  (swap-state-and-increment! assoc :full-target-path path))

(defn set-port! [port]
  (swap-state! assoc :port port))

(defn set-base-target-path! [path]
  (swap-state! assoc :base-target-path path))

(defn base-target-path []
  (:base-target-path @*state))
