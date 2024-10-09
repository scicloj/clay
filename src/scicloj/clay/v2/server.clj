(ns scicloj.clay.v2.server
  (:require
   [clojure.java.browse :as browse]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [hiccup.page]
   [org.httpkit.server :as httpkit]
   [scicloj.clay.v2.server.state :as server.state]
   [scicloj.clay.v2.util.time :as time]
   [scicloj.clay.v2.item :as item]
   [clojure.string :as str]
   [hiccup.core :as hiccup])
  (:import (java.net ServerSocket)))

(def default-port 1971)

(defonce *clients (atom #{}))

(defn broadcast! [msg]
  (doseq [ch @*clients]
    (httpkit/send! ch msg)))

(defn get-free-port []
  (loop [port default-port]
    ;; Check if the port is free:
    ;; (https://codereview.stackexchange.com/a/31591)
    (or (try (do (.close (ServerSocket. port))
                 port)
             (catch Exception e nil))
        (recur (inc port)))))


(defn communication-script
  "The communication JS script to init a WebSocket to the server."
  [{:keys [port counter]}]
  (format "
<script type=\"text/javascript\">
  {
    clay_port = %d;
    clay_server_counter = '%d';

    clay_refresh = function() {location.reload();}

    const clay_socket = new WebSocket('ws://localhost:'+clay_port);

    clay_socket.addEventListener('open', (event) => { clay_socket.send('Hello Server!')});

    clay_socket.addEventListener('message', (event)=> {
      if (event.data=='refresh') {
        clay_refresh();
      } else {
        console.log('unknown ws message: ' + event.data);
      }
    });
  }

  async function clay_1 () {
    const response = await fetch('/counter');
    const response_counter = await response.json();
    if (response_counter != clay_server_counter) {
      clay_refresh();
    }
  };
  clay_1();
</script>
"
          port
          counter))

(defn header [state]
  (hiccup.core/html
      [:div
       [:div
        [:img
         {:style {:display "inline-block"
                  :zoom 1
                  :width "40px"
                  :margin-left "20px"}
          ;; { zoom: 1; vertical-align: top; font-size: 12px;}
          :src "/Clay.svg.png"
          :alt "Clay logo"}]
        #_[:big [:big "(Clay)"]]
        [:div {:style {:display "inline-block"
                       :margin "20px"}}
         [:pre {:style {:margin 0}}
          (some->> state
                   :last-rendered-spec
                   :full-target-path)]
         [:pre {:style {:margin 0}}
          (time/now)]]]
       #_(:hiccup item/separator)]))

(defn page
  ([]
   (page @server.state/*state))
  ([state]
   (some-> state
           :last-rendered-spec
           :full-target-path
           slurp)))

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

(defn routes
  "Web server routes."
  [{:keys [:body :request-method :uri]
    :as req}]
  (let [state @server.state/*state]
    (if (:websocket? req)
      (httpkit/as-channel req {:on-open (fn [ch] (swap! *clients conj ch))
                               :on-close (fn [ch _reason] (swap! *clients disj ch))
                               :on-receive (fn [_ch msg])})
      (case [request-method uri]
        [:get "/"] {:body (-> state
                              page
                              (wrap-html state))
                    :headers {"Content-Type" "text/html"}
                    :status 200}
        [:get "/counter"] {:body (-> state
                                     :counter
                                     str)
                           :status 200}

        ;; else
        (let [f (io/file (str (:base-target-path state) uri))]
          (if (.exists f)
            {:body    (if (re-matches #".*\.html$" uri)
                        (-> f
                            slurp
                            (wrap-html state))
                        f)
             :headers (when (str/ends-with? uri ".js")
                        {"Content-Type" "text/javascript"})
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
  (browse/browse-url (url)))

(defn open!
  ([] (open! {}))
  ([{:as opts :keys [port]}]
   (when-not @*stop-server!
     (let [port (or port (get-free-port))
           stop-server (core-http-server port)]
       (server.state/set-port! port)
       (reset! *stop-server! stop-server)
       (println "serving Clay at" (port->url port))
       (browse!)))))

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
    (open!))
  (io/make-parents full-target-path)
  (when page
    (spit full-target-path page))
  (-> spec
      (assoc :full-target-path full-target-path)
      (server.state/reset-last-rendered-spec!))
  (when show
    (broadcast! "refresh"))
  [:ok])

(defn close! []
  (when-let [s @*stop-server!]
    (s))
  (reset! *stop-server! nil))
