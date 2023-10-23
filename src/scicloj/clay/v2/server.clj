(ns scicloj.clay.v2.server
  (:require
   [clojure.java.browse :as browse]
   [clojure.java.io :as io]
   [clojure.java.shell :as sh]
   [clojure.string :as string]
   [cognitect.transit :as transit]
   [org.httpkit.server :as httpkit]
   [scicloj.clay.v2.item :as item]
   [scicloj.clay.v2.page :as page]
   [scicloj.clay.v2.path :as path]
   [scicloj.clay.v2.prepare :as prepare]
   [scicloj.clay.v2.state :as state]
   [scicloj.clay.v2.util.time :as time]
   [scicloj.kindly.v4.api :as kindly]))

(def default-port 1971)

(defonce *clients (atom #{}))

(defn broadcast! [msg]
  (doseq [ch @*clients]
    (httpkit/send! ch msg)))

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
      [:get "/"] {:body (or (some-> (state/quarto-html-path)
                                    slurp)
                            (:page @state/*state))
                  :status 200}
      [:get "/counter"] {:body (-> (state/counter)
                                   str)
                         :status 200}
      [:get "/favicon.ico"] {:body nil
                             :status 200}
      [:post "/compute"] (let [{:keys [form]} (-> body
                                                  (transit/reader :json)
                                                  transit/read
                                                  read-string)]
                           (println [:compute form (java.util.Date.)])
                           {:body (-> form
                                      eval
                                      pr-str)
                            :status 200})
      ;; else
      {:body (let [base-path (or (some-> (state/quarto-html-path)
                                         path/path->parent)
                                 "docs")]
               #_(println [:uri uri])
               (try (->> uri
                         (str base-path)
                         (java.io.FileInputStream.))
                    (catch java.io.FileNotFoundException e
                      ;; Ignoring missing source maps.
                      ;; TODO: Figure this problem out.
                      (if (.endsWith ^String uri ".map")
                        nil
                        (throw e)))))
       :status 200})))

(defonce *stop-server! (atom nil))

(defn core-http-server [port]
  (httpkit/run-server #'routes {:port port}))

(defn port->url [port]
  (str "http://localhost:" port "/"))

(defn url []
  (port->url (state/port)))

(defn browse! []
  (browse/browse-url (url)))

(defn open! []
  (let [port (get-free-port)
        server (core-http-server port)]
    (state/set-port! port)
    (reset! *stop-server! port)
    (println "serving scittle at " (port->url port))
    (-> @state/*state
        (assoc :items [item/welcome])
        page/html
        state/set-page!)
    (browse!)))

(defn close! []
  (when-let [s @*stop-server!]
    (s))
  (reset! *stop-server! nil))
