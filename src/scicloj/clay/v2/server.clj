(ns scicloj.clay.v2.server
  (:require [org.httpkit.server :as httpkit]
            [cognitect.transit :as transit]
            [clojure.java.browse :as browse]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.clay.v2.page :as page]
            [scicloj.clay.v2.item :as item]
            [scicloj.clay.v2.prepare :as prepare]
            [scicloj.clay.v2.path :as path]
            [scicloj.clay.v2.state :as state]
            [clojure.java.shell :as sh]
            [clojure.string :as string]
            [clojure.java.io :as io]))

(def default-port 1971)

(defonce *clients (atom #{}))

(defn broadcast! [msg]
  (doseq [ch @*clients]
    (httpkit/send! ch msg)))

(state/set-items! [item/welcome])

(def default-options
  {:quarto {:format {:html {:toc true}}
            :code-block-background true
            :embed-resources false
            :execute {:freeze true}}})
(state/swap-options! (constantly default-options))


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
                            (page/page @state/*state))
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
    (browse!)))


(defn close! []
  (when-let [s @*stop-server!]
    (s))
  (reset! *stop-server! nil))

(defn show-items!
  ([items]
   (show-items! items nil))
  ([items options]
   (state/swap-state-and-increment!
    (fn [state]
      (-> state
          (assoc :quarto-html-path nil)
          (assoc :date (java.util.Date.))
          (assoc :items items)
          (merge options))))
   (broadcast! "refresh")
   [:ok]))

(defn show! [context]
  (-> context
      prepare/prepare-or-pprint
      vector
      show-items!))

(defn ns->target-path
  ([base the-ns ext]
   (str base
        (-> the-ns
            str
            (string/replace #"-" "_")
            (string/split #"\.")
            (->> (string/join "/")))
        ext)))

(defn now []
  (java.util.Date.))

(defn write-html!
  ([]
   (write-html! (ns->target-path "docs/" *ns* ".html")))
  ([path]
   (io/make-parents path)
   (->> @state/*state
        page/page
        (spit path))
   (println [:wrote path (now)])
   [:wrote path]))

(defn render-quarto! [items]
  (let [md-path (ns->target-path "docs/" *ns* "_quarto.md")
        html-path (-> md-path
                      (string/replace #"\.md$" ".html"))]

    (io/make-parents md-path)
    (-> @state/*state
        (page/qmd items)
        (->> (spit md-path)))
    (println [:wrote md-path (now)])
    (->> (sh/sh "quarto" "render" md-path)
         ((juxt :err :out))
         (mapv println))
    (println [:created html-path (now)])
    (state/reset-quarto-html-path! html-path)
    (broadcast! "refresh")
    :ok))



(def base-quarto-config
  "
project:
  type: book

format:
  html:
    theme: cosmo

book:
  title: \"book\"
  chapters:
    - index.md
")

(def base-quarto-index
  "
---
format:
  html: {toc: true}
embed-resources: true
---
# book index
  ")

(defn update-quarto-config! [chapter-path]
  (let [index-path "book/index.md"
        config-path "book/_quarto.yml"
        current-config (if (-> config-path io/file .exists)
                         (slurp config-path)
                         (do (spit config-path base-quarto-config)
                             (println [:created config-path])
                             base-quarto-config))
        chapter-line (str "    - " chapter-path)]
    (when-not (-> index-path io/file .exists)
      (spit index-path base-quarto-index)
      (println [:created index-path]))
    (when-not (-> current-config
                  (string/split #"\n")
                  (->> (some (partial = chapter-line))))
      (->> chapter-line
           (str current-config "\n")
           (spit config-path))
      (println [:updated config-path
                :with chapter-path]))))




(defn write-quarto! [items]
  (let [chapter-path (if (-> *ns*
                             str
                             (string/split #"\.")
                             last
                             (= "index"))
                       (ns->target-path "" *ns* ".md")
                       (ns->target-path "" *ns* "/index.md"))
        md-path (str "book/" chapter-path)]
    (io/make-parents md-path)
    (-> @state/*state
        (page/qmd items)
        (->> (spit md-path)))
    (update-quarto-config! chapter-path)
    (println [:wrote md-path (now)])))

(defn show-message! [hiccup]
  (state/set-items! [hiccup])
  (state/reset-quarto-html-path! nil)
  (broadcast! "refresh"))
