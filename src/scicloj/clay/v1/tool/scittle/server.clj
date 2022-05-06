(ns scicloj.clay.v1.tool.scittle.server
  (:require [org.httpkit.server :as httpkit]
            [cognitect.transit :as transit]
            [clojure.java.browse :as browse]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.clay.v1.tool.scittle.page :as page]
            [scicloj.clay.v1.tool.scittle.view :as view]))

(def default-port 1971)

(defonce *clients (atom #{}))

(defn broadcast! [msg]
  (doseq [ch @*clients]
    (httpkit/send! ch msg)))

(def *state
  (atom {:port default-port
         :widgets []
         :fns {}}))

(defn routes [{:keys [:body :request-method :uri]
               :as req}]
  (if (:websocket? req)
    (httpkit/as-channel req {:on-open (fn [ch] (swap! *clients conj ch))
                             :on-close (fn [ch _reason] (swap! *clients disj ch))
                             :on-receive (fn [_ch msg])})
    (case [request-method uri]
      [:get "/"] {:body (page/page @*state)
                  :status 200}
      [:post "/compute"] (let [{:keys [fname args]} (-> body
                                                        (transit/reader :json)
                                                        transit/read
                                                        read-string)
                               f (-> @*state :fns (get fname))]
                           {:body (pr-str (when (and f args)
                                            (apply f args)))
                            :status 200}))))

(defonce *stop-server! (atom nil))

(defn core-http-server []
  (httpkit/run-server #'routes {:port (:port @*state)}))

(defn open! []
  (let [url (str "http://localhost:" (:port @*state) "/")]
    (reset! *stop-server! (core-http-server))
    (println "serving scittle at " url)
    (browse/browse-url url)))

(defn close! []
  (when-let [s @*stop-server!]
    (s))
  (reset! *stop-server! nil))

(defn write-html! [path]
  (->> @*state
       page/page
       (spit path))
  (-> [:ok] (kindly/consider :kind/hidden)))

(defn show-widgets!
  ([widgets]
   (show-widgets! widgets nil))
  ([widgets options]
   (swap! *state
          (fn [state]
            (-> state
                (assoc :widgets widgets)
                (merge options))))
   (broadcast! "refresh")
   (-> [:ok] (kindly/consider :kind/hidden))))

#_(defn reveal! [state]
    (future (Thread/sleep 2000)
            (reset! *state
                    (assoc state :reveal? true))
            (println (pr-str @*state))
            (broadcast! "refresh")))

(defn show! [value kind-override]
  (-> value
      (view/prepare kind-override)
      vector
      show-widgets!))

(comment
  (close!)
  (open!)

  (show-widget!
   [:h1 "hi"]))
