(ns scicloj.clay.v2.server
  (:require [babashka.fs :as fs]
            [clojure.java.browse :as browse]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [hiccup.core :as hiccup]
            [muuntaja.core :as mc]
            [muuntaja.middleware :as mm]
            [org.httpkit.server :as httpkit]
            [ring.middleware.defaults :as rmd]
            [ring.util.mime-type :as mime-type]
            [scicloj.clay.v2.server.state :as server.state]
            [scicloj.clay.v2.util.time :as time]
            [scicloj.kindly.v4.api :as kindly])
  (:import (java.net ServerSocket)))

(def default-port 1971)

(defn broadcast! [msg]
  (doseq [ch @server.state/*clients]
    (httpkit/send! ch msg)))

(defn scittle-eval-string!
  "Send ClojureScript code to be evaluated on the Clay page.
  The code will be executed directly using scittle.core.eval_string."
  [code]
  (broadcast! (str "scittle-eval-string " code)))

(defn get-free-port []
  (loop [port default-port]
    ;; Check if the port is free:
    ;; (https://codereview.stackexchange.com/a/31591)
    (or (try (.close (ServerSocket. port))
             port
             (catch Exception e nil))
        (recur (inc port)))))

(defn communication-script
  "The communication JS script to init a WebSocket to the server."
  [{:keys [port counter]}]
  (let [reload-regexp ".*/(#[a-zA-Z\\-]+)?\\$"
        ;; We use this regexp to recognize when to used
        ;; page reload rather than revert to the original URL,
        ;; see below.
        ]
    (->> [port counter reload-regexp]
         (apply format "
<script type=\"text/javascript\">
  clay_port = %d;
  clay_server_counter = '%d';
  reload_regexp = new RegExp('%s');

  clay_refresh = function() {
    // Check whether we are still in the main page
    // (but possibly in an anchor (#...) inside it):
    if(reload_regexp.test(window.location.href)) {
      // Just reload, keeping the current position:
      location.reload();
    } else {
        // We might be in a different book to the chapter.
        // So, reload and force returning to the main page.
        location.assign('http://localhost:'+clay_port);
    }
  }

  let clay_socket;
  let clay_reconnect_timeout;

  function clay_connect() {
    clay_socket = new WebSocket('ws://localhost:'+clay_port);

    clay_socket.addEventListener('open', (event) => {
      clay_socket.send('Hello Server!')
    });

    clay_socket.addEventListener('close', (event) => {
      clearTimeout(clay_reconnect_timeout);
      clay_reconnect_timeout = setTimeout(clay_connect, 2000);
    });

    clay_socket.addEventListener('message', (event)=> {
      if (event.data=='refresh') {
        clay_refresh();
      } else if (event.data=='loading') {
        document.body.style.opacity = 0.5;
        document.body.prepend(document.createElement('div', {class: 'loader'}));
      } else if (event.data.startsWith('eval-js ')) {
        const code = event.data.substring('eval-js '.length);
        try {
          const result = eval(code);
          console.log('Clay eval-js result:', result);
        } catch (e) {
          console.error('Clay eval-js error:', e);
        }
      } else if (event.data.startsWith('scittle-eval-string ')) {
        // Evaluate ClojureScript code directly
        const code = event.data.substring('scittle-eval-string '.length);
        if (window.scittle && window.scittle.core && window.scittle.core.eval_string) {
          try {
            const result = window.scittle.core.eval_string(code);
            console.log('Clay eval result:', result);
          } catch (e) {
            console.error('Clay eval error:', e);
          }
        } else {
          console.warn('Scittle not available for eval-string');
        }
      }
    });
  }

  clay_connect();


  async function clay_1 () {
    const response = await fetch('/counter');
    const response_counter = await response.json();
    if (response_counter != clay_server_counter) {
      clay_refresh();
    }
  };
  clay_1();
</script>"))))

(defn header [state]
  (hiccup/html
   [:div
    [:div
     [:img
      {:style {:display "inline-block"
               :zoom 1
               :width "40px"
               :margin-left "20px"},
       ;; { zoom: 1; vertical-align: top; font-size: 12px;}
       :src "/Clay.svg.png"
       :alt "Clay logo"}]
     [:div {:style {:display "inline-block"
                    :margin "20px"}}
      [:pre {:style {:margin 0}}
       (some->> state
                :last-rendered-spec
                :full-target-path)]
      [:pre {:style {:margin 0}}
       (time/now)]]]]))

(defn page
  ([]
   (page @server.state/*state))
  ([state]
   (let [{:keys [last-rendered-spec]} state
         {:keys [page]} last-rendered-spec
         path (some-> last-rendered-spec :full-target-path)
         index (fs/file (:base-target-path last-rendered-spec) "index.html")]
     (cond
       (and path (str/ends-with? path ".pdf"))
       (hiccup/html
        [:html
         [:head [:title "PDF Viewer"]]
         [:body
          [:embed {:src (str "/" (fs/unixify (fs/relativize (:base-target-path last-rendered-spec) path)))
                   :type "application/pdf"
                   :width "100%"
                   :height "900px"}]]])

       (and path (fs/exists? path))
       (slurp path)

       page page

       (fs/exists? index)
       (slurp index)

       :else
       (hiccup/html
        [:html
         [:head [:title "Clay Server State"]]
         [:body
          [:h2 "No file to display"]
          [:p "Create or edit source files"]
          [:details [:pre (with-out-str (pprint/pprint state))]]]])))))

(defn wrap-base-url [html {:as state
                           {:keys [flatten-targets
                                   full-target-path
                                   base-target-path]} :last-rendered-spec}]
  (if (and (false? flatten-targets)
           base-target-path
           full-target-path)
    (str/replace html #"(<\s*head[^>]*>)"
                 (str "$1"
                      "<base href=\"/"
                      (fs/unixify (fs/relativize base-target-path full-target-path))
                      "\" />\n"))
    html))

(defn wrap-html [html state]
  (-> html
      (str/replace #"(<\s*body[^>]*>)"
                   (str "$1"
                        (when-not (-> state
                                      :last-rendered-spec
                                      :hide-ui-header)
                          (hiccup/html
                           #_[:style "* {margin: 0; padding: 0; top: 0;}"]
                           [:div {:style {:height "70px"
                                          :background-color "#eee"}}
                            (header state)]))
                        (communication-script state)))))

(defn resolve-servable-var [func]
  (if (str/blank? func)
    (throw (ex-info "Func missing"
                    {:id ::func-missing}))
    (let [func-var (try (resolve (symbol func)) (catch Exception _ex))
          {:kindly/keys [servable rpc handler]} (meta func-var)]
      (cond (not func-var)
            (throw (ex-info (str "Func not resolved: " func)
                            {:id ::not-resolved
                             :func func}))
            (or (not (var? func-var)) (not (fn? @func-var)))
            (throw (ex-info (str "Not a function var: " func)
                            {:id ::not-a-function
                             :func func}))
            (not (or servable rpc handler))
            (throw (ex-info (str "Function is not safe to serve: " func)
                            {:id ::not-safe-to-serve
                             :func func}))
            :else
            func-var))))

(defn uri->filename [uri]
  (some-> uri
          (clojure.string/replace #"\." "/")
          (clojure.string/replace #"-" "_")
          (str/replace #".html$" ".clj")))

;; delay is to avoid cyclic dependency
(def *make-html-page
  (delay (resolve 'scicloj.clay.v2.make/make-html-page)))

(defn call-annotated-endpoint
  "Processes a servable or handler request.
   Will apply `args` from `params` of the request if present,
   or call the function with `params` as an argument.
   Response will be HTML if the result is a string and the function name ends in the `html` suffix."
  [req func]
  (when-let [v (resolve-servable-var (str func))]
    (let [m (meta v)
          {:keys [kindly/handler]} m
          {:keys [params]} req
          {:keys [args]} params
          result (if handler
                   (v req)
                   (if (sequential? args)
                     (apply v args)
                     (v params)))
          resp (if handler
                 result
                 {:status 200
                  :body result})
          resp (cond-> resp
                 (and (string? result)
                      (str/ends-with? func "html"))
                 (assoc-in [:headers "Content-Type"] "text/html; charset=utf-8"))]
      resp)))

(defn annotated-routes
  "When uri resolves to a servable function to call.
   Negotiates body based on content-type and accept headers when present,
   defaults to JSON otherwise, or HTML when ends with .html or :html present in metadata."
  [{:keys [body request-method uri] :as req}]
  (when (or (= uri "/kindly-compute")
            (str/starts-with? uri "/kindly-compute/"))
    (let [req (cond-> req
                (and body (not (get (:headers req) "content-type")))
                (assoc-in [:headers "content-type"] "application/json"))
          {:keys [query-params form-params body-params]} req
          req (update req :params merge query-params form-params body-params)
          {:keys [params]} req
          func (if (= uri "/kindly-compute")
                 (:func params "")
                 (str/replace-first uri "/kindly-compute/" ""))]
      (call-annotated-endpoint req func))))

(defn live-namespace-routes
  "When the uri resolves to a servable namespace"
  [{:keys [uri]}]
  (when (str/ends-with? uri ".html")
   (when-let [html (try (some-> (uri->filename (subs uri 1))
                                (@*make-html-page)
                                :page)
                        (catch Exception _ex))]
     {:status 200
      :headers {"Content-Type" "text/html; charset=utf-8"}
      :body html})))

(def websocket-handler
  {:on-open (fn [ch]
              (swap! server.state/*clients conj ch)
              (when (:loading @server.state/*state)
                (httpkit/send! ch "loading"))
              (doseq [on-open (:on-open @server.state/*websocket-handlers)]
                (on-open ch)))
   :on-close (fn [ch reason]
               (swap! server.state/*clients disj ch)
               (doseq [on-close (:on-close @server.state/*websocket-handlers)]
                 (on-close ch reason)))
   :on-receive (fn [ch msg]
                 (doseq [on-receive (:on-receive @server.state/*websocket-handlers)]
                   (on-receive ch msg)))})

(defn clay-routes
  "Clay's built in web server routes."
  [{:keys [body request-method uri]
    :as req}]
  (let [state @server.state/*state]
    (if (:websocket? req)
      (httpkit/as-channel req websocket-handler)
      (case [request-method uri]
        [:get "/"] {:body (-> state
                              page
                              (wrap-base-url state)
                              (wrap-html state))
                    :headers {"Content-Type" "text/html; charset=utf-8"}
                    :status 200}
        [:get "/counter"] {:body (-> state
                                     :counter
                                     str)
                           :headers {"Content-Type" "application/json"}
                           :status 200}
        ;; else
        (let [f (io/file (str (:base-target-path state) uri))]
          (if (.exists f)
            {:body    (if (re-matches #".*\.html$" uri)
                        (-> f
                            slurp
                            (wrap-html state))
                        f)
             :headers (when-let [t (mime-type/ext-mime-type uri {"cljs" "text/plain; charset=utf-8"})]
                        {"Content-Type" t})
             :status  200}
            (case [request-method uri]
              ;; user files have priority, otherwise serve the default from resources
              [:get "/favicon.ico"] {:body   (io/input-stream (io/resource "favicon.ico"))
                                     :status 200}
              ;; this image is for the header above the page during interactive mode
              [:get "/Clay.svg.png"] {:body   (io/input-stream (io/resource "Clay.svg.png"))
                                      :status 200}
              {:body   "not found"
               :status 404})))))))

(defn all-routes [req]
  (or (some (fn [handler]
              (handler req))
            @server.state/*handlers)
      (annotated-routes req)
      (live-namespace-routes req)
      (clay-routes req)))

(defn encode? [_ response]
  ((some-fn coll? number? string? keyword? boolean? nil?) (:body response)))

(defn clay-handler [{:keys [site-defaults]}]
  (-> all-routes
      (mm/wrap-format (assoc-in mc/default-options [:http :encode-response-body?] encode?))
      (rmd/wrap-defaults (kindly/deep-merge rmd/site-defaults
                                            {:security {:anti-forgery false}}
                                            site-defaults))))
(comment
  (alter-var-root #'routes (constantly (clay-handler {}))))

(defonce *stop-server! (atom nil))

(defonce ^{:doc "`routes` is a handler, the name is kept for backwards compatibility.
                 Will be bound when open is first called, as it requires configuration."}
  routes nil)

(defn core-http-server [port]
  (httpkit/run-server #'routes {:port port}))

(defn port->url [port]
  (str "http://localhost:" port "/"))

(defn port []
  (-> @server.state/*state
      :port))

(defn url []
  (some-> @server.state/*state
          :port
          port->url))

(defn browse! []
  (let [u (url)]
    (try
      (browse/browse-url u)
      (catch Exception e
        (println "Clay could not open the browser for" u)))))

(defn open!
  ([] (open! {}))
  ([{:as opts :keys [port browse ide]}]
   (when-not @*stop-server!
     (alter-var-root #'routes (constantly (clay-handler opts)))
     (let [port (or port (get-free-port))
           stop-server (core-http-server port)]
       (server.state/set-port! port)
       (reset! *stop-server! stop-server)
       (println "Clay serving at" (port->url port))))
   ;; browse can be :browser to prefer using a browser always
   (when (and (or (= browse :browser)
                  ;; clay default is browse true,
                  ;; ide flag causes a flare to request a webview in the ide
                  ;; so if ide is true we do not show the browser, even when browse is true
                  (and browse (not ide)))
              ;; always and only browse when there are no connected clients
              (empty? @server.state/*clients))
     (browse!))))

(defn update-page! [{:as spec
                     :keys [show
                            base-target-path
                            page
                            full-target-path
                            in-memory]
                     :or   {full-target-path (str base-target-path
                                                  "/"
                                                  ".clay.html")}}]
  (server.state/set-base-target-path! base-target-path)
  (when (and page (not in-memory))
    (io/make-parents full-target-path)
    (spit full-target-path page))
  (-> spec
      (assoc :full-target-path full-target-path)
      (server.state/reset-last-rendered-spec!))
  (when show
    (swap! server.state/*state dissoc :loading)
    (broadcast! "refresh"))
  [:ok])

(defn loading! []
  (swap! server.state/*state assoc :loading true)
  (broadcast! "loading"))

(defn close! []
  (when-let [s @*stop-server!]
    (s))
  (reset! *stop-server! nil))

(defn install-handler!
  "Adds a ring request handler to Clay's built in server.
   Handlers are functions that take a request and return a response, or nil if not handled.
   `handler-var` should be a var that derefs to a handler.
   Using a var makes installation idempotent and dynamic."
  [handler-var]
  {:pre [(var? handler-var)]}
  (swap! server.state/*handlers conj handler-var))

(defn clear-handlers! []
  (reset! server.state/*handlers #{}))

(defn install-websocket-handler!
  "Adds a httpkit websocket handler to Clay's built in server.
   `event-type` :on-receive should be a var that derefs to a function taking channel and message.
   Using a var makes installation idempotent and dynamic."
  [event-type handler-var]
  {:pre [(#{:on-open :on-close :on-receive} event-type)
         (var? handler-var)]}
  (swap! server.state/*websocket-handlers update event-type (fnil conj #{}) handler-var))

(defn clear-websocket-handlers! []
  (reset! server.state/*websocket-handlers {}))
