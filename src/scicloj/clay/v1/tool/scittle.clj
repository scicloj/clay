(ns scicloj.clay.v1.tool.scittle
  (:require [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind]
            [scicloj.kindly.v2.kindness :as kindness]
            [scicloj.clay.v1.walk]
            [scicloj.clay.v1.tool :as tool]
            [org.httpkit.server :as httpkit]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]
            [clojure.string :as string]
            [cognitect.transit :as transit]
            [clojure.java.browse :as browse]
            [scicloj.clay.v1.html.table :as table]))

(def port 1971)

(defonce *clients (atom #{}))

(defn broadcast! [msg]
  (doseq [ch @*clients]
    (httpkit/send! ch msg)))

(def datatables-cljs
  '(defn datatables [table]
     (let [[_ options thead tbody] table]
       [:div
        [:table
         (merge options
                {:ref (fn [el]
                        (-> el
                            js/$
                            (.DataTable (clj->js {:sPaginationType "full_numbers"
                                                  :bSort false}))))})
         thead
         tbody]])))

(def vega-cljs
  '(defn vega [spec]
     [:div
      {:ref (fn [el]
              (-> el
                  (js/vegaEmbed (clj->js spec)
                                (clj->js {:renderer :svg}))
                  (.then (fn [res]))
                  (.catch (fn [err] (println (str "vegaEmbed error: " err))))))}]))

(def echarts-cljs
  '(defn echarts
     ([echart-option]
      (echarts echart-option nil))
     ([echart-option {:keys [on register-map]
                      :as options}]
      (fn []
        [:div
         {:style {:height "500px"}
          :ref (fn [el]
                 (when el
                   (when-let [[map-name map-options] register-map]
                     (.registerMap js/echarts
                                   map-name (clj->js map-options)))
                   (let [chart (.init js/echarts el)]
                     (->> echart-option
                          clj->js
                          (.setOption chart))
                     (some->> on
                              (run! (fn [[event-type f]]
                                      (.on chart
                                           (name event-type)
                                           f)))))))}]))))

(defn widgets-cljs [widgets data]
  (let [widget-id (str (java.util.UUID/randomUUID))]
    (concat ['(ns main
                (:require [reagent.core :as r]
                          [reagent.dom :as dom]
                          [ajax.core :refer [GET POST]]
                          [clojure.string :as string]))
             (list 'def 'data
                   (list 'quote data))
             (list 'def 'widget-id widget-id)
             `(let [socket (js/WebSocket. (str "ws://localhost:" ~port))]
                (.addEventListener socket "open" (fn [event]
                                                   (.send socket "Hello Server!")))
                (.addEventListener socket "message" (fn [event]
                                                      (case (.-data event)
                                                        "refresh" (.reload js/location)
                                                        (println [:unknown-ws-message (.-data event)])))))

             '(defn compute [[fname & args] result-state result-path]
                (POST "/compute"
                      {:headers {"Accept" #_"application/transit+json" "application/json"}
                       :params (pr-str {:fname fname
                                        :args args})
                       :handler (fn [response]
                                  (swap! result-state
                                         assoc-in
                                         result-path (read-string response)))
                       :error-handler (fn [e]
                                        (.log js/console (str e)))}))]
            [datatables-cljs
             vega-cljs
             echarts-cljs]
            (->> widgets
                 (map-indexed
                  (fn [i widget]
                    (let [widget-name (str "widget" i)
                          widget-symbol (symbol (str "widget" i))]
                      [(list 'def widget-symbol widget)
                       (list 'dom/render
                             (list 'fn []
                                   widget-symbol)
                             (list '.getElementById 'js/document widget-name))])))
                 (apply concat)))))

(defn page [{:keys [widgets data reveal?]}]
  (page/html5
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:link {:rel "shortcut icon" :href "data:,"}]
     [:link {:rel "apple-touch-icon" :href "data:,"}]
     ;; [:link {:rel "stylesheet" :href "https://cdn.jsdelivr.net/npm/bulma@0.9.0/css/bulma.min.css"}]

     [:script {:crossorigin nil :src "https://unpkg.com/react@17/umd/react.production.min.js"}]
     [:script {:crossorigin nil :src "https://unpkg.com/react-dom@17/umd/react-dom.production.min.js"}]
     [:script {:src "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.1/js/scittle.js" :type "application/javascript"}]
     [:script {:src "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.1/js/scittle.reagent.js" :type "application/javascript"}]
     [:script {:src "https://cdn.jsdelivr.net/gh/borkdude/scittle@0.0.1/js/scittle.cljs-ajax.js" :type "application/javascript"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/echarts@5.3.2/dist/echarts.min.js"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/vega@5.22.1"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/vega-lite@5.2.0"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/vega-embed@6.20.8"}]
     [:script {:src "https://code.jquery.com/jquery-3.6.0.min.js"
               :integrity "sha256-/xUj+3OJU5yExlq6GSYGSHk7tPXikynS7ogEvDej/m4="
               :crossorigin "anonymous"}]
     [:script {:src "https://cdn.datatables.net/1.11.5/js/jquery.dataTables.min.js"}]
     [:link {:rel "stylesheet"
             :href "https://cdn.datatables.net/1.11.5/css/jquery.dataTables.min.css"}]
     ;; [:link {:rel "stylesheet"
     ;;         :href "https://unpkg.com/bootstrap-table@1.19.1/dist/bootstrap-table.min.css"}]
     ;; [:script {:src "https://unpkg.com/bootstrap-table@1.19.1/dist/bootstrap-table.min.js"}]
     [:link {:rel "stylesheet"
             :href "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/3.6.0/css/reveal.min.css"}]
     [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/reveal.js/3.6.0/js/reveal.min.js"}]
     [:link {:rel "stylesheet"
             :href "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"}]
     [:title "Clay"]]
    [:body
     [:div (when reveal? {:class :reveal})
      [:div (when reveal? {:class :slides})
       (->> widgets
            count
            range
            (map (fn [i]
                   (cond->> [:div {:id (str "widget" i)}]
                     reveal? (vector :section))))
            (into [:div]))
       [:script {:type "application/x-scittle"}
        (->> (widgets-cljs widgets data)
             (map pr-str)
             (string/join "\n"))]]]
     (when reveal?
       [:script {:type "application/x-scittle"}
        (pr-str '(.initialize js/Reveal (clj->js {:hash true})))])]]))

(def *state
  (atom {:widgets []
         :fns {}}))

(defn routes [{:keys [:body :request-method :uri]
               :as req}]
  (if (:websocket? req)
    (httpkit/as-channel req {:on-open (fn [ch] (swap! *clients conj ch))
                             :on-close (fn [ch _reason] (swap! *clients disj ch))
                             :on-receive (fn [_ch msg])})
    (case [request-method uri]
      [:get "/"] {:body (page @*state)
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
  (httpkit/run-server #'routes {:port port}))

(defn open! []
  (let [url (str "http://localhost:" port "/")]
    (reset! *stop-server! (core-http-server))
    (println "serving" url)
    (browse/browse-url url)))

(defn close! []
  (when-let [s @*stop-server!]
    (s))
  (reset! *stop-server! nil))

(defn show-widget!
  ([widget]
   (show-widget! widget nil))
  ([widget {:keys [fns data]}]
   (swap! *state
          assoc
          :widgets [widget]
          :data data
          :fns fns)
   (broadcast! "refresh")))

(defn reveal! [state]
  (future (Thread/sleep 2000)
          (reset! *state
                  (assoc state :reveal? true))
          (println (pr-str @*state))
          (broadcast! "refresh")))

(defn maybe-apply-viewer
  ([value]
   (maybe-apply-viewer value (kindly/kind value)))
  ([value kind]
   (if-let [viewer (-> kind kindly/kind->behaviour :scittle.viewer)]
     (viewer value)
     value)))

(declare prepare-vector)
(declare prepare-seq)
(declare prepare-map)
(declare prepare-div)

(defn prepare-naive [value]
  [:code (pr-str value)])

(defn div? [v]
  (and (vector? v)
       (-> v first (= :div))))

(defn prepare [value]
  (let [kind (kindly/kind value)]
    (cond (div? value) (prepare-div value)
          kind (maybe-apply-viewer value kind)
          (vector? value) (prepare-vector value)
          (sequential? value) (prepare-seq value)
          (map? value) (prepare-map value)
          :else (prepare-naive value))))

(defn prepare-div [v]
  (let [kind (kindly/kind v)]
    (-> (let [r (rest v)
              fr (first r)]
          (if (map? fr)
            (->> r
                 rest
                 (map (fn [subv]
                        (if (or (kindly/kind subv)
                                (div? subv))
                          (prepare subv)
                          subv)))
                 (into [:div fr]))
            (->> r
                 (map (fn [subv]
                        (if (or (kindly/kind subv)
                                (div? subv))
                          (prepare subv)
                          subv)))
                 (into [:div]))))
        (maybe-apply-viewer kind))))

(defn prepare-vector [value]
  [:div
   [:code "["]
   (->> value
        (map prepare)
        (into [:div
               {:style {:margin-left "10%"}}]))
   [:code "]"]])

(defn prepare-seq [value]
  [:div
   [:code "("]
   (->> value
        (map prepare)
        (into [:div
               {:style {:margin-left "10%"}}]))
   [:code ")"]])

(defn prepare-map [value]
  [:div
   [:code "{"]
   (->> value
        (map (fn [[k v]]
               [:div
                (prepare k)
                (prepare v)]))
        (into [:div
               {:style {:margin-left "10%"}}]))
   [:code "}"]])

(defn show! [value code]
  (-> value
      prepare
      show-widget!))

(def tool
  (reify tool/Tool
    (setup! [this config])
    (open! [this]
      (open!))
    (close! [this]
      (close!))
    (show! [this value code]
      (show! value code))))


(kindly/define-kind-behaviour! :kind/naive
  {:scittle.viewer (fn [v]
                     [:code (pr-str v)])})

(kindly/define-kind-behaviour! :kind/hiccup
  {:scittle.viewer (fn [v] v)})

(kindly/define-kind-behaviour! :kind/table
  {:scittle.viewer (fn [table-spec]
                     ['datatables
                      (-> table-spec
                          table/->table-hiccup)])})

(kindly/define-kind-behaviour! :kind/vega
  {:scittle.viewer (fn [spec]
                     [:div
                      ['vega (list 'quote spec)]])})

(kindly/define-kind-behaviour! :kind/cytoscape
  {})

(kindly/define-kind-behaviour! :kind/echarts
  {:scittle.viewer (fn [option]
                     (if (map? option)
                       ['echarts (list 'quote option)]
                       (->> option
                            (map (fn [op]
                                   (list 'quote op)))
                            (into ['echarts]))))})


;; It is convenient to refresh all clients on re-evaluation.
(broadcast! "refresh")




(comment
  (open!)

  *state

  :bafsd

  *state

  (reveal! {:widgets  [[:div [:code "13"]]
                       [:div [:code "13"]]
                       [:div [:h1 "............"]]]})


  )
