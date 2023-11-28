(ns scicloj.clay.v2.make
  (:require [scicloj.clay.v2.config :as config]
            [scicloj.clay.v2.files :as files]
            [scicloj.clay.v2.util.path :as path]
            [scicloj.clay.v2.read :as read]
            [scicloj.clay.v2.item :as item]
            [scicloj.clay.v2.prepare :as prepare]
            [scicloj.clay.v2.notebook :as notebook]
            [scicloj.clay.v2.page :as page]
            [scicloj.clay.v2.server :as server]
            [scicloj.clay.v2.util.time :as time]
            [clojure.string :as string]
            [clojure.java.shell :as shell]))

(defn spec->full-source-path [{:keys [base-source-path source-path]}]
  (or (some-> base-source-path
              (str "/" source-path))
      source-path))

(defn spec->ns-form [{:keys [full-source-path]}]
  (-> full-source-path
      slurp
      read/read-ns-form))

(defn spec->html-path [{:keys [base-target-path format ns-form]}]
  (path/ns->target-path base-target-path
                        (-> ns-form
                            second
                            name)
                        (str (when (-> format
                                       second
                                       (= :revealjs))
                               "-revealjs")
                             ".html")))

(defn spec->ns-config [{:keys [ns-form]}]
  (-> ns-form
      meta
      :clay))

(defn merge-ns-config [spec]
  (merge spec
         (spec->ns-config spec)))

(defn extract-specs [config spec]
  (let [{:as main-spec :keys [source-path]}
        (merge config spec)] ; prioritize spec over global config
    {:main-spec main-spec
     :single-ns-specs (->> (if (sequential? source-path)
                             (->> source-path
                                  (map (partial assoc main-spec :source-path)))
                             [main-spec])
                           (map (fn [single-ns-spec]
                                  (-> single-ns-spec
                                      (config/add-field :full-source-path spec->full-source-path)
                                      (config/add-field :ns-form spec->ns-form)
                                      merge-ns-config
                                      (merge spec) ; prioritize spec over the ns config
                                      (config/add-field :html-path spec->html-path)))))}))

(defn handle-main-spec! [spec]
  )

(defn handle-single-source-spec! [{:as spec
                                   :keys [format
                                          html-path
                                          run-quarto]}]
  (files/init-target! html-path)
  (let [spec-with-items      (-> spec
                                 (config/add-field :items notebook/notebook-items))]
    (case (first format)
      :html (do (-> spec-with-items
                    (config/add-field :page page/html)
                    server/update-page!)
                [:wrote html-path])
      :quarto (let [qmd-path (-> html-path
                                 (string/replace #"\.html$" ".qmd"))
                    output-file (-> html-path
                                    (string/split #"/")
                                    last)]
                (-> spec-with-items
                    (update-in [:quarto :format]
                               select-keys [(second format)])
                    (update-in [:quarto :format (second format)]
                               assoc :output-file output-file)
                    page/md
                    (->> (spit qmd-path)))
                (println [:wrote qmd-path (time/now)])
                (if run-quarto
                  (do (->> (shell/sh "quarto" "render" qmd-path)
                           ((juxt :err :out))
                           (mapv println))
                      (println [:created html-path (time/now)])
                      (-> spec
                          (merge {:html-path html-path})
                          server/update-page!))
                  ;; else, just show the qmd file
                  (-> spec
                      (merge {:html-path qmd-path})
                      server/update-page!))
                (vec
                 (concat [:wrote qmd-path]
                         (when run-quarto
                           [html-path])))))))



(defn make! [spec]
  (let [{:keys [main-spec single-ns-specs]} (extract-specs (config/config)
                                                           (merge spec))
        {:keys [show]} main-spec]
    (when show
      (-> main-spec
          (merge {:page (page/html
                         {:items [item/loader]})})
          server/update-page!))
    (->> single-ns-specs
         (mapv handle-single-source-spec!))))


(comment
  (make! {:format [:html]
          :source-path "notebooks/index.clj"})

  (make! {:format [:html]
          :source-path "notebooks/index.clj"
          :show false})

  (make! {:format [:html]
          :source-path ["notebooks/slides.clj"
                        "notebooks/index.clj"]
          :show false})

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
          :source-path "notebooks/index.clj"
          :run-quarto false})

  (make! {:format [:quarto :html]
          :source-path "notebooks/slides.clj"})

  (make! {:format [:quarto :revealjs]
          :source-path "notebooks/slides.clj"})

  (make! {:format [:quarto :html]
          :source-path "notebooks/index.clj"
          :quarto {:highlight-style :nord}})

  (make! {:format [:html]
          :base-source-path "notebooks/"
          :source-path "index.clj"}))
