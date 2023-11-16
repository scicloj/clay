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
      [:pre (some->> state
                     :html-path)]
      [:pre (time/now)]]]
    #_(:hiccup item/separator)]))

(def avoid-favicon
  ;; avoid favicon.ico request: https://stackoverflow.com/a/38917888
  [:link {:rel "icon" :href "data:,"}])

(defn page
  ([]
   (page @server.state/*state))
  ([state]
   (hiccup.page/html5
    [:head avoid-favicon]
    [:body {:style {:overflow-x "hidden"}}
     [:style "* {margin: 0; padding: 0; top: 0;}"]
     [:div {:style {:left "0px"
                    :top "0px"
                    :height "70px"
                    :background-color "#ddd"}}
      (header state)]
     ;; https://makersaid.com/make-iframe-fit-100-of-remaining-height/
     [:iframe {:style {:height "calc(100vh - 100px)"
                       :width "100%"
                       :border "none"}
               :srcdoc (some-> state
                               :html-path
                               slurp)}]
     (communication-script state)])))



(def debug )

(defn routes [{:keys [:body :request-method :uri]
               :as req}]
  (if (:websocket? req)
    (httpkit/as-channel req {:on-open (fn [ch] (swap! *clients conj ch))
                             :on-close (fn [ch _reason] (swap! *clients disj ch))
                             :on-receive (fn [_ch msg])})
    (case [request-method uri]
      [:get "/"] {:body (page @server.state/*state)
                  :status 200}
      [:get "/counter"] {:body (-> @server.state/*state
                                   :counter
                                   str)
                         :status 200}
      ;; else
      {:body (let [base-path (or (some-> @server.state/*state
                                         :html-path
                                         path/path->parent)
                                 "docs")]
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
  (-> @server.state/*state
      :port))

(defn url []
  (some-> @server.state/*state
          :port
          port->url))

(defn browse! []
  (browse/browse-url (url)))

(defn welcome-page []
  (hiccup.page/html5
   [:head
    avoid-favicon]
   [:body
    [:div
     [:p [:pre (str (java.util.Date.))]]
     [:p [:pre [:a {:href "https://scicloj.github.io/clay/"}
                "Clay"]
          " is ready, waiting for interaction."]]]]))

(defn update-page! [{:keys [page
                            html-path
                            show]
                     :or {html-path ".clay.html"
                          show true}}]
  (io/make-parents html-path)
  (when page
    (spit html-path page))
  (server.state/reset-html-path! html-path)
  (shell/sh "rsync" "-avu" "src/" "docs/src")
  (when show
    (broadcast! "refresh"))
  [:ok])

(defn open! []
  (when-not @*stop-server!
    (let [port (get-free-port)
          server (core-http-server port)]
      (server.state/set-port! port)
      (reset! *stop-server! port)
      (println "serving scittle at " (port->url port))
      (update-page! {:page (welcome-page)})
      (browse!))))

(defn close! []
  (when-let [s @*stop-server!]
    (s))
  (reset! *stop-server! nil))
