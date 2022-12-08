(ns scicloj.clay.v2.tool.scittle.server
  (:require [org.httpkit.server :as httpkit]
            [cognitect.transit :as transit]
            [clojure.java.browse :as browse]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.clay.v2.tool.scittle.page :as page]
            [scicloj.clay.v2.tool.scittle.view :as view]))


(def default-port 1971)

(defonce *clients (atom #{}))

(defn broadcast! [msg]
  (doseq [ch @*clients]
    (httpkit/send! ch msg)))

(def *state
  (atom {:port nil
         :widgets [[:div
                    [:p [:code (str (java.util.Date.))]]
                    [:p [:code [:a {:href "https://scicloj.github.io/clay/"}
                                "Clay"]
                         " is ready, waiting for interaction."]]]]
         :fns {}}))

(defn get-free-port []
  ;; https://gist.github.com/apeckham/78da0a59076a4b91b1f5acf40a96de69
  (let [socket (java.net.ServerSocket. 0)]
    (.close socket)
    (.getLocalPort socket)))

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

(defn core-http-server [port]
  (httpkit/run-server #'routes {:port port}))

(defn open! []
  (let [port (get-free-port)
        url (str "http://localhost:" port "/")
        server (core-http-server port)]
    (swap! *state assoc :port port)
    (reset! *stop-server! port)
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

(defn show! [context]
  (-> context
      view/prepare
      vector
      show-widgets!))

(comment
  (close!)
  (open!)

  (show-widgets!
   [[:h1 "hi"]]))
