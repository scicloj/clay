(ns scicloj.clay.v2.server
  (:require
   [clojure.java.browse :as browse]
   [clojure.java.io :as io]
   [clojure.java.shell :as shell]
   [clojure.string :as string]
   [hiccup.page]
   [org.httpkit.server :as httpkit]
   [scicloj.clay.v2.server.state :as server.state]
   [scicloj.clay.v2.util.path :as path]
   [scicloj.clay.v2.util.time :as time]
   [scicloj.clay.v2.item :as item]
   [scicloj.kindly.v4.api :as kindly]
   [clojure.string :as str]
   [hiccup.core :as hiccup]))

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


(defn communication-script [{:keys [port counter]}]
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

(defn add-communication-script [page state]
  (-> page
      (string/replace #"</body></html>$"
                      (str "\n"
                           (communication-script state)
                           "\n</body></html>"))))

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
       :src "https://raw.githubusercontent.com/scicloj/clay/main/resources/Clay.svg.png"
       :alt "Clay logo"}]
     #_[:big [:big "(Clay)"]]
     [:div {:style {:display "inline-block"
                    :margin "20px"}}
      [:pre {:style {:margin 0}}
       (some->> state
                :full-target-path)]
      [:pre {:style {:margin 0}}
       (time/now)]]]
    #_(:hiccup item/separator)]))

(def avoid-favicon
  ;; avoid favicon.ico request: https://stackoverflow.com/a/38917888
  [:link {:rel "icon" :href "data:,"}])

(defn page
  ([]
   (page @server.state/*state))
  ([state]
   (let [relative-path (some-> state
                               :full-target-path
                               (string/replace (re-pattern (str "^"
                                                                (:base-target-path state)
                                                                "/"))
                                               ""))]
     (some-> state
             :full-target-path
             slurp
             (str/replace #"(<\s*head[^>]*>)"
                          (str "$1"
                               (hiccup/html
                                avoid-favicon)))
             (str/replace #"(<\s*body[^>]*>)"
                          (str "$1"
                               (hiccup/html
                                #_[:style "* {margin: 0; padding: 0; top: 0;}"]
                                [:div {:style {:height "70px"
                                               :background-color "#eee"}}
                                 (header state)])
                               (communication-script state)))))))


(defn routes [{:keys [:body :request-method :uri]
               :as req}]
  (let [state @server.state/*state]
    (if (:websocket? req)
      (httpkit/as-channel req {:on-open (fn [ch] (swap! *clients conj ch))
                               :on-close (fn [ch _reason] (swap! *clients disj ch))
                               :on-receive (fn [_ch msg])})
      (case [request-method uri]
        [:get "/"] {:body (page state)
                    :headers {"Content-Type" "text/html"}
                    :status 200}
        [:get "/counter"] {:body (-> state
                                     :counter
                                     str)
                           :status 200}
        ;; else
        (merge {:body (try (->> uri
                                (str (:base-target-path state))
                                (java.io.FileInputStream.))
                           (catch java.io.FileNotFoundException e
                             ;; Ignoring missing source maps.
                             ;; TODO: Figure this problem out.
                             (if (.endsWith ^String uri ".map")
                               nil
                               (throw e))))
                :status 200}
               (when (.endsWith ^String uri ".js")
                 {:headers {"Content-Type" "text/javascript"}}))))))

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

(defn open! []
  (when-not @*stop-server!
    (let [port (get-free-port)
          server (core-http-server port)]
      (server.state/set-port! port)
      (reset! *stop-server! port)
      (println "serving Clay at" (port->url port))
      (browse!))))

(defn update-page! [{:keys [show
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
  (server.state/reset-full-target-path! full-target-path)
  (when show
    (broadcast! "refresh"))
  [:ok])

(defn close! []
  (when-let [s @*stop-server!]
    (s))
  (reset! *stop-server! nil))
