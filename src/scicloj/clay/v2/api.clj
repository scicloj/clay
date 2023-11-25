(ns scicloj.clay.v2.api
  (:require
   [clojure.string :as string]
   [clojure.test]
   [scicloj.clay.v2.config :as config]
   [scicloj.clay.v2.quarto :as quarto]
   [scicloj.clay.v2.server :as server]
   [scicloj.clay.v2.make :as make]
   [scicloj.kindly.v4.api :as kindly]))

(defn stop! []
  (server/close!)
  [:ok])

(defn start! []
  (server/open!)
  (server/update-page! {:page (server/welcome-page)})
  [:ok])

(defn make! [spec]
  (make/make! spec))

(defn browse! []
  (server/browse!))

(defn port []
  (server/port))

(defn url []
  (server/url))

(defn config []
  (config/config))

(defn update-book! [options]
  (quarto/update-book! options))


(comment
  (make! {:format [:html]
          :source-path "notebooks/index.clj"})

  ;; TODO: support a book with multiple chapters
  ;; Quarto: create 1 book (this is just our normal configuration, but Clay can help)
  (make! {:format [:quarto :book]
          ;; Clay: create 3 markdown files from source
          :source-path ["notebooks/index.clj"
                        "notebooks/index1.clj"
                        "notebooks/index2.clj"]
          :show false})

  (make! {:format      [:html]
          :source-path "notebooks/index.clj"
          :single-form '(kind/cytoscape
                          [{:style {:width "300px"
                                    :height "300px"}}
                           cytoscape-example])})

  (make! {:format [:quarto :html]
          :source-path "notebooks/index.clj"})

  (make! {:format [:quarto :html]
          :source-path "notebooks/slides.clj"})

  (make! {:format [:quarto :revealjs]
          :source-path "notebooks/slides.clj"})

  (make! {:format [:quarto :html]
          :source-path "notebooks/index.clj"
          :quarto {:highlight-style :nord}}))
