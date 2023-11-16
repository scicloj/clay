(ns scicloj.clay.v2.server.state)

(defonce *state
  (atom {:port nil
         :counter 0
         :html-path nil}))

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

(defn reset-html-path! [path]
  (swap-state-and-increment! assoc :html-path path))

(defn set-port! [port]
  (swap-state! assoc :port port))
