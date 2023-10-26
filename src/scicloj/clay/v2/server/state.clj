(ns scicloj.clay.v2.server.state)

(defonce *state
  (atom {:port nil
         :counter 0
         :page nil
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

(defn set-page! [page]
  (swap-state-and-increment! assoc :page page))

(defn reset-html-path! [path]
  (swap-state! assoc :html-path path))

(defn set-port! [port]
  (swap-state! assoc :port port))
