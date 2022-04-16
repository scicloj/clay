(ns scicloj.clay.v1.hiccups
  (:require [hiccup.page :as page]))

(defn in-iframe [html]
  (format
   "<iframe sandbox='' frameBorder='0' width='100%%' height='500px' srcdoc='%s'></iframe>"
   (page/html5 html)))

(defn js-src [url]
  [:script {:src url}])

(defn css-link [url]
  [:link {:rel "stylesheet"
          :href url}])
