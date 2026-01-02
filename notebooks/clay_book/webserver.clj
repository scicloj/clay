;; # Webserver

;; Clay launches a webserver to serve the HTML pages that it builds.
;; We can also host an API in that webserver as a lightweight webapp backend.
;; This can be useful when prototyping or building interactive experiences.

^:kindly/hide-code
(ns clay-book.webserver
  (:require [clojure.data.json :as json]
            [clojure.pprint :as pprint]
            [clojure.test :refer [deftest is]]
            [hiccup.page :as page]
            [org.httpkit.client :as http]
            [org.httpkit.server :as httpkit]
            [scicloj.clay.v2.server :as server]))

;; Clay serves on `http://localhost:1971/` by default.
;; We can call `server/url` to confirm where Clay is running.

(server/url)

;; ## Servable namespaces

;; When you request a servable namespace,
;; Clay re-evaluates the namespace to return HTML,
;; rather than serving a previously made static file.

^:kind/code
(slurp "notebooks/clay_book/current_time.clj")

;; The `current_time.clj` notebook is evaluated every time we visit the url http://localhost:1971/notebooks/clay_book/current_time.html
;; because the namespace is annotated as `^:kindly/servable`.

;; Servable namespaces are a way to keep a notebook current,
;; rather than serving a static snapshot.
;; So if you have a report, it can always pull the latest data.

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

;; HTTP requests to `/kindly-compute`
;; will be handled by Clay, which will call the function
;; and return the result in the response.
;; The function to call must be a fully qualified symbol that resolves to an annotated function.
;; The function name should be placed in the url path.
;; So to call the `clay-book.webserver/kindly-add` function,
;; we use the URL `http://localhost:1971/kindly-compute/clay-book.webserver/kindly-add`
;; A sequence of arguments `:args` in the params will be applied to the function call.

(def kindly-add-response
  @(http/post (str (server/url) "kindly-compute/clay-book.webserver/kindly-add")
              {:body (json/write-str {:args [2 3]})}))

;; ::: {.callout-note}
;; If you prefer, you can pass the function name as a param called `:func`
;; rather than the URL path.
;; :::

;; This is the request that we made:

(:opts kindly-add-response)

;; And this is the response we got:

(dissoc kindly-add-response :opts)

;; The answer `"5"` is in the `:body`, and the `:content-type` is JSON.

(:body kindly-add-response)

(:content-type (:headers kindly-add-response))

(json/read-str (:body kindly-add-response))

;; Rather than accepting `:args` in `:params`,
;; we can take all the `:params` as a map:

(defn ^:kindly/servable kindly-add-named [{:keys [a b] :as params}]
  (+ a b))

;; When we call this function, we just provide the params.
;; Rather than `{:args [2 3]}` we are now passing `{:a 4 :b 5}`:

(def kindly-add-named-response
  @(http/post (str (server/url) "kindly-compute/clay-book.webserver/kindly-add-named")
              {:body (json/write-str {:a 4 :b 5})}))

kindly-add-named-response

(json/read-str (:body kindly-add-named-response))

;; ::: {.callout-note}
;; Servable functions can be placed in any resolvable namespace,
;; they don't have to be inside a notebook.
;; :::

;; ## Calling `kindly-compute` from a Browser

;; When using Scittle, Clay defines a convenience function `kindly-compute` to make a request.
;; The result will be delivered to a callback handler function.

^:kind/scittle
'(defn handler [result]
   (js/console.log "kindly-compute result:" result))

;; Requesting a function with positional args:

^:kind/scittle
'(kindly-compute 'clay-book.webserver/kindly-add
                 [11 20]
                 handler)

;; Requesting a function with a single params argument:

^:kind/scittle
'(kindly-compute 'clay-book.webserver/kindly-add-named
                 {:a 8, :b 11}
                 handler)

;; If you prefer to write your own clientside code, it might look something like this:

^:kind/hiccup
[:script
 "fetch('/kindly-compute/clay-book.webserver/kindly-add-named', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({a: 99, b: 1})
  })
    .then(resp => resp.json())
    .then(result => console.log('Javascript call to kindly-add-named got', result));"]

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

(json/read-str (:body handle-add-response))

;; ## Params

;; Params may be passed in the query-string, or in the body of the request encoded as form-params, json, edn, or transit.
;; Clay will negotiate the request and response encodings based on content and accept headers where they exist.
;; When no format is specified, Clay will default to using JSON.

;; ## HTML Responses

;; Most endpoint response bodies are encoded base on an "Accept" header if present, or in JSON by default.
;; However when the function name ends in `html`, the endpoint will return `Content-Type: text/html` instead.

(defn ^:kindly/servable greet-html [{:keys [name]}]
  (page/html5 [:h1 (str "Hello " name)]))

;; We can GET the page with params in the query-string `?name=world`:

(def greet-response
  @(http/get (str (server/url) "kindly-compute/clay-book.webserver/greet-html?name=world")))

greet-response

;; We got HTML rather than JSON because the function name was `greet-html`

(:body greet-response)

(:content-type (:headers greet-response))

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
[:script
 "clay_socket.send('can you hear me?');
  clay_socket.addEventListener('message', function(event) {
    console.log('Clay socket:', event.data);
  });"]

;; ## Hosting

;; ### Preparing HTML

;; To build all the notebooks before launching the server:

;; ```sh
;; clojure -M -m scicloj.clay.v2.main --render
;; ```

;; This will produce all the HTML files and exit.

;; ### Serving

;; Consider supplying `:port` in configuration.
;; For example `:port 80` to listen on the HTTP port.
;; If you need to adjust the Ring middleware (sessions, proxy headers, CSRF, etc.),
;; `:ring-defaults` to be deep-merged into Ring's `site-defaults` when Clay starts its server.
;; For example to run behind a proxy you could add `:ring-defaults {:proxy true}`.
;; This configuration can be put in a `clay.edn` file, or passed via the command line interface.

;; To launch Clay as a webserver from the [Command Line Interface](https://scicloj.github.io/clay/#cli):
;;
;; ```sh
;; clojure -M -m scicloj.clay.v2.main -m "{:port 80}"
;; ```

;; ### Serving non-notebook files

;; Clay will serve any files in the `:base-target-path` (`docs` is the default).
;; As part of making a notebook, any non source files are copied from the `:subdirs-to-sync` to the `:base-target-path`.
;; (the default is `["src" "notebooks"]`).
;; The server will also serve any files found in `resources/public`
;; (because of [`:ring-defaults`](https://github.com/ring-clojure/ring-defaults)).

;; Normally Clay uses the root route `/` to show the last made file.
;; But when launched as a server, it will show "index.html" from the `:base-target-path` if found.
;; Alternatively you can install a custom handler to handle `/`.

;; ## Conclusion

;; Clay can act as a webserver for prototyping interactive dashboards,
;; live analysis, or a web app.
;; It's easy to create endpoints is by annotating functions,
;; and call them from the rendered notebook.
