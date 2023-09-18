(ns scicloj.clay.v2.tool.scittle.server
  (:require [org.httpkit.server :as httpkit]
            [cognitect.transit :as transit]
            [clojure.java.browse :as browse]
            [scicloj.kindly.v4.api :as kindly]
            [scicloj.clay.v2.tool.scittle.page :as page]
            [scicloj.clay.v2.tool.scittle.view :as view]
            [scicloj.clay.v2.path :as path]
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
         :widgets nil
         :fns {}
         :counter 0}))

;; (defn counter-watch [key atom old-value new-value]
;;   (->> [old-value new-value]
;;        (map :counter)
;;        ((fn [[x y]]
;;           (if (not= x y)
;;             (println [x y]))))))

;; (defonce once1
;;   (add-watch *state :watcher #'counter-watch))

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


(defn set-widgets! [widgets]
  (swap-state-and-increment! assoc :widgets widgets))

(def welcome-widget
  [:div
   [:p [:code (str (java.util.Date.))]]
   [:p [:code [:a {:href "https://scicloj.github.io/clay/"}
               "Clay"]
        " is ready, waiting for interaction."]]])

(set-widgets! [welcome-widget])

(comment
  (set-widgets! [welcome-widget welcome-widget])
  (broadcast! "refresh"))

(def default-options
  {:quarto {:format {:html {:toc true}}
            :code-block-background true
            :embed-resources false
            :execute {:freeze true}}})

(defn swap-options! [f & args]
  (apply swap-state! update :options f args)
  :ok)

(swap-options! (constantly default-options))

(defn reset-quarto-html-path! [path]
  (swap-state! assoc :quarto-html-path path))

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
      [:get "/counter"] {:body (-> @*state
                                   :counter
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
      {:body (let [base-path (or (some-> @*state
                                         :quarto-html-path
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

(defn port []
  (:port @*state))

(defn url []
  (port->url (port)))

(defn browse! []
  (browse/browse-url (url)))

(defn open! []
  (let [port (get-free-port)
        server (core-http-server port)]
    (swap-state! assoc :port port)
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
   (swap-state-and-increment!
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
      view/prepare-or-pprint
      vector
      show-widgets!))

(defn ns->target-path
  ([base the-ns ext]
   (str base
        (-> the-ns
            str
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
   (->> @*state
        page/page
        (spit path))
   (println [:wrote path (now)])
   (-> [:wrote path]
       (kindly/consider :kind/hidden))))

(defn write-quarto! [widgets]
  (let [qmd-path (ns->target-path "docs/" *ns* "_quarto.qmd")
        html-path (-> qmd-path
                      (string/replace #"\.qmd$" ".html"))]

    (io/make-parents qmd-path)
    (-> @*state
        (page/qmd widgets)
        (->> (spit qmd-path)))
    (println [:wrote qmd-path (now)])
    (->> (sh/sh "quarto" "render" qmd-path)
         ((juxt :err :out))
         (mapv println))
    (println [:created html-path (now)])
    (swap-state! assoc :quarto-html-path html-path)
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
    - index.qmd
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

(defn write-light-quarto! [widgets]
  (let [chapter-path (ns->target-path "" *ns* ".qmd")
        qmd-path (str "book/" chapter-path)]
    (io/make-parents qmd-path)
    (-> @*state
        (page/light-qmd widgets)
        (->> (spit qmd-path)))
    (update-quarto-config! chapter-path)
    (println [:wrote qmd-path (now)])))

(defn show-message! [hiccup]
  (set-widgets! [hiccup])
  (reset-quarto-html-path! nil)
  (broadcast! "refresh"))

(defn options []
  (:options @*state))
