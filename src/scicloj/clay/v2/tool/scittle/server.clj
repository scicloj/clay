(ns scicloj.clay.v2.tool.scittle.server
  (:require [org.httpkit.server :as httpkit]
            [cognitect.transit :as transit]
            [clojure.java.browse :as browse]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.clay.v2.tool.scittle.page :as page]
            [scicloj.clay.v2.tool.scittle.view :as view]
            [clojure.java.shell :as sh]
            [clojure.string :as string]
            [clojure.java.io :as io]))

(def default-port 1971)

(defonce *clients (atom #{}))

(defn broadcast! [msg]
  (doseq [ch @*clients]
    (httpkit/send! ch msg)))

(defonce *state
  (atom {:port nil
         :widgets [[:div
                    [:p [:code (str (java.util.Date.))]]
                    [:p [:code [:a {:href "https://scicloj.github.io/clay/"}
                                "Clay"]
                         " is ready, waiting for interaction."]]]]
         :fns {}}))

(defn get-free-port []
  (loop [port 1971]
    ;; Check if the port is free:
    ;; (https://codereview.stackexchange.com/a/31591)
    (or (try (do (.close (java.net.ServerSocket. port))
                 port)
             (catch Exception e nil))
        (recur (inc port)))))

(defn routes [{:keys [:body :request-method :uri]
               :as req}]
  (if (:websocket? req)
    (httpkit/as-channel req {:on-open (fn [ch] (swap! *clients conj ch))
                             :on-close (fn [ch _reason] (swap! *clients disj ch))
                             :on-receive (fn [_ch msg])})
    (case [request-method uri]
      [:get "/"] {:body (or (some-> @*state
                                    :quarto-html-path
                                    slurp)
                            (page/page @*state))
                  :status 200}
      [:get "/favicon.ico"] {:body nil
                             :status 200}
      [:post "/compute"] (let [{:keys [fname args]} (-> body
                                                        (transit/reader :json)
                                                        transit/read
                                                        read-string)
                               f (-> @*state :fns (get fname))]
                           {:body (pr-str (when (and f args)
                                            (apply f args)))
                            :status 200})
      ;; else
      {:body (->> uri
                  (str "docs")
                  slurp)
       :status 200})))

(defonce *stop-server! (atom nil))

(defn core-http-server [port]
  (httpkit/run-server #'routes {:port port}))

(defn port->url [port]
  (str "http://localhost:" port "/"))

(defn port []
  (:port @*state))

(defn url []
  (port->url (port)))

(defn browse! []
  (browse/browse-url (url)))

(defn open! []
  (let [port (get-free-port)
        server (core-http-server port)]
    (swap! *state assoc :port port)
    (reset! *stop-server! port)
    (println "serving scittle at " (port->url port))
    (browse!)))


(defn close! []
  (when-let [s @*stop-server!]
    (s))
  (reset! *stop-server! nil))

(defn show-widgets!
  ([widgets]
   (show-widgets! widgets nil))
  ([widgets options]
   (swap! *state
          (fn [state]
            (-> state
                (assoc :quarto-html-path nil)
                (assoc :date (java.util.Date.))
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

(defn ns->target-path [the-ns ext]
  (format "docs/%s%s"
          (-> the-ns
              str
              (string/split #"\.")
              (->> (string/join "/")))
          ext))

(defn now []
  (java.util.Date.))

(defn write-html!
  ([]
   (write-html! (ns->target-path *ns* ".html")))
  ([path]
   (io/make-parents path)
   (->> @*state
        page/page
        (spit path))
   (println [:wrote path (now)])
   (-> [:wrote path]
       (kindly/consider :kind/hidden))))

(defn write-quarto! []
  (let [qmd-path (ns->target-path *ns* "_quarto.qmd")
        html-path (-> qmd-path
                      (string/replace #"\.qmd$" ".html"))]

    (io/make-parents qmd-path)
    (->> @*state
         :date
         (vector :state-date)
         println)
    (->> @*state
         page/qmd
         (spit qmd-path))
    (println [:wrote qmd-path (now)])
    (->> (sh/sh "quarto" "render" qmd-path)
         ((juxt :err :out))
         (mapv println))
    (println [:created html-path (now)])
    (swap! *state assoc :quarto-html-path html-path)
    (broadcast! "refresh")
    :ok))
