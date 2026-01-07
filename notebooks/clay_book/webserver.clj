^:kindly/hide-code
(ns clay-book.webserver
  (:require [clojure.data.json :as json]
            [clojure.pprint :as pprint]
            [hiccup.page :as page]
            [org.httpkit.client :as http]
            [org.httpkit.server :as httpkit]
            [scicloj.clay.v2.server :as server]))

;; # Web Server

;; Clay launches a web server to serve the HTML pages made from Clojure namespaces.
;; We can also host an API in that web server as a lightweight web app backend.
;; This can be useful when prototyping or building interactive experiences.

;; **Key features:**
;;
;; * **Servable namespaces** - Notebooks that evaluate when you view them
;; * **Annotated endpoints** — Mark functions with metadata to create HTTP endpoints
;; * **Live reloading** — Update handlers without restarting
;; * **Multiple formats** — Negotiate JSON, EDN, Transit, or HTML responses
;; * **Browser integration** — Call endpoints from Scittle or plain JavaScript
;; * **Lightweight deployment** — Launch from the command line

;; Clay serves on http://localhost:1971/ by default.
;; We can call `server/url` to confirm where Clay is running.

(require '[scicloj.clay.v2.server :as server])
(server/url)

;; While the Clay server is running, it shows the last made namespace at `/`.
;; To view other pages, you can navigate to a particular page,
;; for example, this page is at `/clay_book.webserver.html`,
;; so if you run Clay locally and make this notebook, you can navigate to it at
;; http://localhost:1971/clay_book.webserver.html

;; ## Servable namespaces

;; When you request a servable namespace,
;; Clay evaluates the namespace to return HTML,
;; rather than serving a static file.

^:kind/code
(slurp "notebooks/clay_book/current_time.clj")

;; The `current_time.clj` notebook is evaluated when we visit the URL http://localhost:1971/notebooks/clay_book/current_time.html
;; because the namespace is annotated as `^:kindly/servable`.

;; Servable namespaces are a way to keep a notebook current,
;; rather than serving a static snapshot.
;; So if you have a report, it can always pull the latest data.

;; ## Servable functions

;; Functions behave as endpoints that can be called by an HTTP request
;; when they have the metadata flag `^:kindly/servable`.

;; ### Accepting positional `args`

(defn ^:kindly/servable kindly-add [a b]
  (+ a b))

;; ::: {.callout-note}
;; Unlike other Kindly annotations, the servable function metadata must be placed on the Var.
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
;; The function name should be placed in the URL path.
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

;; ### Accepting a single map of `params`

(defn ^:kindly/servable kindly-add-named [{:keys [a b]}]
  (+ a b))

;; When we call this function, we provide params.
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

;; ## Calling `kindly-compute` from a browser

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

;; If you prefer to write your own client-side code, it might look something like this:

^:kind/hiccup
[:script
 "fetch('/kindly-compute/clay-book.webserver/kindly-add-named', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({a: 99, b: 1})
  })
    .then(resp => resp.json())
    .then(result => console.log('Javascript call to kindly-add-named got', result));"]

;; The Clay server must be running for browser requests to succeed.

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
;; Handlers have access to the session, cookies, and everything about the request.

handle-add-response

(json/read-str (:body handle-add-response))

;; ## Params

;; Params may be placed in the query-string, or in the body of the request.
;; Clay will negotiate the request and response encodings based on content and accept headers where they exist.
;; When no format is specified, Clay will default to using JSON.
;; Requests with body-params should set a "Content-Type" header to let Clay know how to read the body,
;; and an "Accept" header to let Clay know what format the response should be in.
;; JavaScript `fetch` sets "Content Type" to `text/plain` by default,
;; which will not be decoded.
;; Available formats are form-params, json, edn, or transit.

;; ## HTML Responses

;; Endpoint response bodies are encoded based on an "Accept" header if present,
;; or in JSON by default.
;; However when a servable function name ends in `html`,
;; then the response will be `Content-Type: text/html` instead.
;; This differentiates between endpoints that return data,
;; and endpoints that return HTML pages.

(defn ^:kindly/servable greet-html [{:keys [name]}]
  (page/html5 [:h1 (str "Hello " name)]))

;; We can GET the page with params in the query string `?name=world`:

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
    [:get "/chicken"] {:status 200
                       :headers {"Content-Type" "text/plain; charset=utf-8"}
                       :body "bock bock bock"}
    nil))

(server/install-handler! #'my-handler)

;; `/chicken` is a top-level route (not namespaced).
;; We could handle `/` which would be necessary for a complete website.
;; Handling `/` prevents Clay interactively showing the last made namespace,
;; so only do that when deploying.

(def chicken-response
  @(http/get (str (server/url) "chicken")))

(:body chicken-response)

;; Unlike most [Ring](https://github.com/ring-clojure/ring) handlers,
;; Clay handlers may return `nil` to ignore a request,
;; leaving it to other handlers or the site-defaults handler.
;; You can install multiple handlers side-by-side in this way.

;; Clay requires a handler to be a **var holding a function** because
;; that allows you to replace the definition of the handler conveniently,
;; and for the handlers to be tracked by their var.
;; Vars have identity, functions do not.

;; Because handlers and servable functions are Vars,
;; they can be updated dynamically without restarting the server.
;; If you need to remove an unwanted handler,
;;`server/clear-handlers!` will clear all current handlers.

;; ## Clay WebSocket handler

;; Similar to dynamic handlers, you may also install a websocket handler:

(defn my-websocket-receive [ch msg]
  (println "Received" msg)
  (httpkit/send! ch "got it"))

(server/install-websocket-handler! :on-receive #'my-websocket-receive)

;; On the client, you can access `clay_socket` to send and receive messages to the server:

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

;; Configure Clay with `:port 80` to listen on the default HTTP port.
;; If you need to adjust the Ring middleware (sessions, proxy headers, CSRF, etc.),
;; `:ring-defaults` to be deep-merged into Ring's `site-defaults` when Clay starts its server.
;; For example to run behind a proxy you could add `:ring-defaults {:proxy true}`.
;; Configuration can be put in a `clay.edn` file, or passed via the command line interface.

;; To launch Clay as a web server from the [Command Line Interface](https://scicloj.github.io/clay/#cli):
;;
;; ```sh
;; clojure -M -m scicloj.clay.v2.main -m "{:port 80}"
;; ```

;; You may find it convenient to package your project as an
;; [uberjar](https://clojure.org/guides/tools_build#_compiled_uberjar_application_build)
;; for deployment to a hosting service.

;; ### Static files

;; Clay will serve any files in the `:base-target-path` (`"docs"` is the default).
;; When making notebooks,
;; any non-source files are copied from the `:subdirs-to-sync`
;; (`["src" "notebooks"]` by default) to the `:base-target-path` (`"docs"` by default).

;; Clay will also serve any files found in `resources/public`,
;; as provided by Ring's site-defaults.
;; This can be changed by configuring
;; [`:ring-defaults`](https://github.com/ring-clojure/ring-defaults)

;; ### Site index file

;; Normally Clay uses the root route `/` to show the last made file.
;; But when first launched as a server,
;; it will show "index.html" from the `:base-target-path` if found.
;; Alternatively you can install a custom handler to handle `/`.

;; ## Glossary

;; URL
;; : `https://clojurecivitas.github.io/about.html`
;;   scheme + host + uri

;; URI
;; : `/about.html` the path at the end of a URL

;; request-method
;; : **GET**, **POST**, PUT, PATCH, DELETE, OPTIONS, CONNECT, TRACE

;; handler
;; : A function that takes a request like `{:uri "/about.html"}`
;;   and returns a response like `{:body "ClojureCivitas is a shared blog space"}`.

;; routing
;; : Matching a `request-method` and a `uri` to determine a sub-handler that should process the request.
;;   Handlers may perform routing, and may call other handlers.

;; endpoint
;; : A servable function that can be called from the frontend via HTTP.

;; params
;; : Data extracted from an HTTP request, which can come from three sources:
;;   * URL params: query string after `?`, e.g., `/some/path?a=1` yields `{:a "1"}`
;;   * Form params: form-encoded body (usually from HTML form submission)
;;   * Body params: JSON or other format in the request body

;; middleware
;; : A function that wraps a handler to modify the request before it reaches the handler,
;;   and modify the response after the handler returns.
;;   For example adding authentication or logging.
;;   The result of wrapping a handler is a handler.

;; frontend
;; : Application code that runs in the Browser (JavaScript or ClojureScript).

;; backend
;; : Application code that runs on the server (in Clojure).

;; ## Conclusion

;; Clay can act as a web server for prototyping interactive dashboards,
;; live analysis, or a web app.
;; It's easy to create endpoints by annotating functions
;; and call them from the rendered notebook.
