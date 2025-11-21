(ns scicloj.clay.v2.server
  (:require [babashka.fs :as fs]
            [clojure.java.browse :as browse]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [hiccup.page]
            [org.httpkit.server :as httpkit]
            [ring.util.mime-type :as mime-type]
            [scicloj.clay.v2.server.state :as server.state]
            [scicloj.clay.v2.util.time :as time]
            [clojure.string :as str]
            [cognitect.transit :as transit]
            [hiccup.core :as hiccup])
  (:import (java.net ServerSocket)))

(def default-port 1971)

(defonce *clients (atom #{}))

(defn broadcast! [msg]
  (doseq [ch @*clients]
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

    const clay_socket = new WebSocket('ws://localhost:'+clay_port);

    clay_socket.addEventListener('open', (event) => { clay_socket.send('Hello Server!')});

    clay_socket.addEventListener('message', (event)=> {
      if (event.data=='refresh') {
        clay_refresh();
      } else if (event.data=='loading') {
        document.body.style.opacity = 0.5;
        document.body.prepend(document.createElement('div', {class: 'loader'}));
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
      } else {
        console.log('unknown ws message: ' + event.data);
      }
    });

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
   (let [{:keys [last-rendered-spec live-reload]} state
         path (some-> last-rendered-spec :full-target-path)]
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

       (fs/exists? path)
       (slurp path)

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


(defn compute
  [input]
  (let [{:keys [func args]} input]
    (if-let [func-var (resolve func)]
      (if (-> func-var meta :kindly/servable)
        (apply func-var args)
        (throw (Exception. (str "Function is not safe to serve: "
                                func))))
      (throw (Exception. (str "Symbol not found: "
                              func))))))

(defn routes
  "Web server routes."
  [{:keys [body request-method uri]
    :as req}]
  (let [state @server.state/*state]
    (if (:websocket? req)
      (httpkit/as-channel req {:on-open (fn [ch]
                                          (swap! *clients conj ch)
                                          (when (:loading state)
                                            (httpkit/send! ch "loading")))
                               :on-close (fn [ch _reason] (swap! *clients disj ch))
                               :on-receive (fn [_ch msg])})
      (case [request-method uri]
        [:get "/"] {:body (-> state
                              page
                              (wrap-base-url state)
                              (wrap-html state))
                    :headers {"Content-Type" "text/html"}
                    :status 200}
        [:get "/counter"] {:body (-> state
                                     :counter
                                     str)
                           :status 200}
        [:post "/kindly-compute"] (let [input (-> body
                                                  (transit/reader :json)
                                                  transit/read
                                                  read-string)
                                        output (compute input)]
                                    {:body (pr-str output)
                                     :status 200})
        ;; else
        (let [f (io/file (str (:base-target-path state) uri))]
          (if (.exists f)
            {:body    (if (re-matches #".*\.html$" uri)
                        (-> f
                            slurp
                            (wrap-html state))
                        f)
             :headers (when-let [t (mime-type/ext-mime-type uri {"cljs" "text/plain"})]
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

(defonce *stop-server! (atom nil))

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
     (let [port (or port (get-free-port))
           stop-server (core-http-server port)]
       (server.state/set-port! port)
       (reset! *stop-server! stop-server)
       (println "Clay serving at" (port->url port))
       ;; browse can be :browser to prefer using a browser always
       (when (or (= browse :browser)
                 ;; clay default is browse true,
                 ;; ide flag causes a flare to request a webview in the ide
                 ;; so if ide is true we do not show the browser, even when browse is true
                 (and browse (not ide)))
         (browse!))))))

(defn update-page! [{:as spec
                     :keys [show
                            base-target-path
                            page
                            full-target-path]
                     :or   {full-target-path (str base-target-path
                                                  "/"
                                                  ".clay.html")}}]
  (server.state/set-base-target-path! base-target-path)
  (when show
    (open! spec))
  (io/make-parents full-target-path)
  (when page
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
