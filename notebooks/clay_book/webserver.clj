;; # Webserver

;; Clay launches a webserver to serve the HTML pages that it builds.
;; We can also host an API in that webserver as a lightweight webapp backend.
;; This can be useful when prototyping or building interactive experiences.

(ns clay-book.webserver
  (:require
   [clojure.data.json :as json]
   [clojure.pprint :as pprint]
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [cognitect.transit :as t]
   [hiccup.page :as page]
   [org.httpkit.client :as http]
   [org.httpkit.server :as httpkit]
   [scicloj.clay.v2.server :as server]))


;; ## Servable namespaces

;; When you request a servable namespace,
;; Clay re-evaluates the namespace to return HTML,
;; rather than serving a previously made static file.

^:kind/code
(slurp "notebooks/clay_book/current_time.clj")

;; The "current_time.clj" notebook is evaluated every time we visit the url `http://localhost:1971/notebooks/clay_book/current_time.html`
;; because the namespace has `^:kindly/servable` annotated in metadata.
;; Servable namespaces are a way to keep a report current, rather than serving a static snapshot.

;; ## Servable functions

;; Functions may be annotated as endpoints that can be called by a HTTP request.

(defn ^:kindly/servable kindly-add [a b]
  (+ a b))

;; ::: {.callout-note}
;; Unlike other Kindly annotations, the servable function metadata must be placed on the var.
;;
;; Wrong
;; : `^:kindly/servable (defn add [a b] (+ a b))`
;;
;; Correct
;; : `(defn ^:kindly/servable add [a b] (+ a b))`
;; :::

;; Clay listens on `http://localhost:1971/` by default.
;; We can call `server/url` to confirm where Clay is running.

(server/url)

;; HTTP requests to `/kindly-compute` will call the function and return the result in the response.
;; The function to call must be a fully qualified symbol that resolves to an annotated function.
;; The function can be in the request params as `:func`.
;; A sequence of arguments `:args` in the params will be applied to the function call.

(def kindly-add-response
  @(http/post (str (server/url) "kindly-compute")
              {:body (json/write-str {:func "example.webserver/kindly-add"
                                      :args [2 3]})}))

;; This is the request that we made:

(:opts kindly-add-response)

;; And this is the response we got:

(dissoc kindly-add-response :opts)

;; The answer `"5"` is in the `:body`, and the `:content-type` is JSON.

(deftest kindly-add-response-value
  (is (= "5" (:body kindly-add-response)))
  (is (str/starts-with? (:content-type (:headers kindly-add-response)) "application/json"))
  (is (= 5 (json/read-str (:body kindly-add-response)))))

;; Rather than accepting `:args` in `:params`, we can instead simply take all the `:params`:

(defn ^:kindly/servable kindly-add-named [{:keys [a b]}]
  (+ a b))

;; Rather than passing the function name in `:func` of params,
;; we can instead use the fully qualified symbol as a route in the URL.

(def kindly-add-named-response
  @(http/post (str (server/url) "kindly-compute/clay-book.webserver/kindly-add-named")
              {:headers {"Content-Type" "application/json"}
               :body (json/write-str {:a 4 :b 5})}))

kindly-add-named-response

(deftest kindly-add-named-response-value
  (is (= 9 (json/read-str (:body kindly-add-named-response)))))

;; The reason for supporting multiple ways of encoding the request is to allow the
;; client side code to be flexible in how it constructs a request.

;; ## Calling kindly-compute from a Browser

;; When using Scittle, Clay defines a convenience function `kindly-compute` to make a request:

^:kind/scittle
'(kindly-compute
  {:func 'example.webserver/kindly-add
   :args [11 20]}
  (fn [result]
    (js/console.log "kindly-compute result:" result)))

;; If you prefer to write your own client code, it might look something like this:

;; ```
;; (-> (js/fetch "/kindly-compute"
;;               (clj->js {:method "POST"
;;                         :headers {"Content-Type" "application/edn"
;;                                   "Accept" "application/edn"}
;;                         :body (pr-str input)}))
;;     (.then (fn [response] (.text response)))
;;     (.then (fn [text] (clojure.edn/read-string text)))
;;     (.then callback)
;;     (.catch (fn [e]
;;               (js/console.log "kindly-compute error:" e))))
;;```

;; ## Handler endpoints

;; Sometimes a little more control over the request/response handling is required.
;; A handler is a function that takes a request like `{:uri "/about.html"}`
;; and returns a response like `{:body "hello"}`.

(defn ^:kindly/handler handle-add [req]
  (let [{:keys [params request-method]} req
        {:keys [a b]} params]
    (println "Request handle-add" request-method)
    (pprint/pprint req)
    {:body (+ a b)}))

;; The `handle-add` function prints out the request so we can see that the handler has access to the entire request handling context.

(def handle-add-response
  @(http/post (str (server/url) "kindly-compute/clay-book.webserver/handle-add")
              {:body (json/write-str {:a 5, :b 9})}))

;; The request was printed as a side-effect of calling the endpoint.

handle-add-response

(deftest handle-add-response-value
  (is (= 14 (json/read-str (:body handle-add-response)))))

;; ## Params

;; Params may be passed in the query-string, or in the body of the request encoded as form-params, json, edn, or transit.
;; Clay will negotiate the request and response encodings based on content and accept headers where they exist.
;; When no format is specified, Clay will default to using JSON.

;; Here is an example where the request sends transit+msgpack and accepts transit+msgpack:

(def transit-add-response
  (-> @(http/post (str (server/url) "kindly-compute/clay-book.webserver/kindly-add-named")
                  {:headers {"Content-Type" "application/transit+msgpack"
                             "Accept" "application/transit+msgpack"}
                   :body (let [baos (java.io.ByteArrayOutputStream.)
                               writer (t/writer baos :msgpack)]
                           (t/write writer {:a 7, :b 13})
                           (.toByteArray baos))})
      (update :body (fn [body]
                      (t/read (t/reader body :msgpack))))))

transit-add-response

(deftest transit-add-response-value
  (is (= 20 (:body transit-add-response))))

;; ## HTML Responses

;; Endpoint response bodies are encoded either in a requested format or in JSON.
;; However when the function name ends in `.html`, the endpoint will return content-type `text/html` instead.

(defn ^:kindly/servable greet.html [{:keys [name]}]
  (page/html5 [:h1 (str "Hello " name)]))

;; In this example we do a GET with params in the query-string `?name=world`:

(def greet-response
  @(http/get (str (server/url) "kindly-compute/clay-book.webserver/greet.html?name=world")))

greet-response

;; We got HTML rather than JSON because the function name was `greet.html`

(deftest greet-response-is-html
  (is (= "<!DOCTYPE html>\n<html><h1>Hello world</h1></html>"
         (:body greet-response))
      (is (= "text/html; charset=utf-8" (:content-type (:headers greet-response))))))

;; ## Dynamic handlers

;; Handlers are more generic than endpoints.
;; Not all handlers are associated with a route, or maybe we just prefer to use a non-annotated handler.
;; Clay supports these by allowing you to add handlers to the Clay server stack.

;; You can add a handler with `install-handler!`

(defn my-handler [{:keys [request-method uri]}]
  (case [request-method uri]
    [:get "/clicken"] {:status 200
                       :headers {"Content-Type" "text/plain; charset=utf-8"}
                       :body "bock bock bock"}
    nil))

(server/install-handler! #'my-handler)

;; `/clicken` is a top level route (not namespaced).
;; We could handle `/` which would be necessary for a complete website.
;; Handling `/` prevents Clay interactively showing the last made namespace,
;; so only do that when deploying.

(def clicken-response
  @(http/get (str (server/url) "clicken")))

clicken-response

(deftest clicken-response-value
  (is (= "bock bock bock" (:body clicken-response))))

;; Unlike normal server handlers, clay handlers may return `nil`,
;; which allows your handler to ignore a request, leaving it to other handlers to handle.
;; You can install multiple handlers side-by-side in this way.

;; Clay requires a handler to be a var holding a function because
;; that allows you to replace the definition of the handler conveniently,
;; and for the handlers to be tracked by their var.
;; Vars have identity, functions do not.

;; ## Clay Websocket handler

;; Similar to dynamic handlers, you may also install a websocket handler:

(defn my-websocket-receive [ch msg]
  (println "Received" msg)
  (httpkit/send! ch "got it"))

(server/install-websocket-handler! :on-receive #'my-websocket-receive)

;; On the client you can access `clay_socket` to send and receive messages to the server:

^:kind/hiccup
[:script "clay_socket.send('can you hear me?');
          clay_socket.addEventListener('message', function(event) {
            console.log(event);
          });"]

;; ## Hosting

;; You can choose a different port via `:port` (for example `:port 80` to listen on the HTTP port).

;; If you need to adjust the Ring middleware (sessions, proxy headers, CSRF, etc.),
;; set `:ring-defaults` in your Clay config (`clay.edn` or the `make!` call).
;; It is deep-merged into Ring's `site-defaults` when Clay starts its server.
;; For example to run behind a proxy you could add `:ring-defaults {:proxy true}`.

;; ## Conclusion

;; Clay can act as a webserver for building data-driven web applications.
;; Clay can be used for prototyping an interactive dashboard,
;; sharing a live analysis, or building a lightweight web app.
;; The main way to create endpoints is through annotation.
