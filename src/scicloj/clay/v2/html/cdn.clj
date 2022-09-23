(ns scicloj.clay.v2.html.cdn
  (:require [hiccup.core :as hiccup]
            [hiccup.page :as page]))

(def scripts
  {:datatables (hiccup/html (page/include-css "https://cdn.datatables.net/1.11.5/css/jquery.dataTables.min.css")
                            (page/include-js "https://code.jquery.com/jquery-3.6.0.min.js")
                            (page/include-js "https://cdn.datatables.net/1.11.5/js/jquery.dataTables.min.js"))})
