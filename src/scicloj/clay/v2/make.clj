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

(defn form->context [form]
  {:form form
   :value (eval form)})

(defn make! [options]
  (let [;;
        config1 (config/config)
        ;;
        {:keys [source-path
                single-form]
         :as config2}
        (merge config1 options)]
    (if (sequential? source-path)
      (->> source-path
           (mapv (fn [sp]
                   (-> options
                       (assoc :source-path sp)
                       make!))))
      (let [ns-form (-> source-path
                        slurp
                        read/read-ns-form)
            ns-name (-> ns-form
                        second
                        name)
            ;;
            {:as config3
             :keys [base-source-path
                    base-target-path
                    format
                    html
                    quarto
                    run-quarto
                    show]}
            (merge config1
                   (-> ns-form
                       meta
                       :clay)
                   options)
            ;; target path
            html-path (path/ns->target-path
                       base-target-path
                       ns-name
                       (str (when (-> config3
                                      :format
                                      second
                                      (= :revealjs))
                              "-revealjs")
                            ".html"))
            ;; final configuration
            config (assoc config3
                          :html-path html-path)]
        (when show
          (-> config
              (merge {:page (page/html
                             {:items [item/loader]})})
              server/update-page!))
        (files/init-target! html-path)
        (let [items      (-> source-path
                             (notebook/notebook-items config))]
          (case (first format)
            :html (let [page (page/html {:items items
                                         :config config})]
                    (-> config
                        (merge {:page page
                                :html-path html-path})
                        server/update-page!)
                    [:wrote html-path])
            :quarto (let [qmd-path (-> html-path
                                       (string/replace #"\.html$" ".qmd"))
                          output-file (-> html-path
                                          (string/split #"/")
                                          last)]
                      (->> {:items items
                            :config (-> config
                                        (update-in [:quarto :format]
                                                   select-keys [(second format)])
                                        (update-in [:quarto :format (second format)]
                                                   assoc :output-file output-file))}
                           page/md
                           (spit qmd-path))
                      (println [:wrote qmd-path (time/now)])
                      (if run-quarto
                        (do (->> (shell/sh "quarto" "render" qmd-path)
                                 ((juxt :err :out))
                                 (mapv println))
                            (println [:created html-path (time/now)])
                            (-> config
                                (merge {:html-path html-path})
                                server/update-page!))
                        ;; else, just show the qmd file
                        (-> config
                            (merge {:html-path qmd-path})
                            server/update-page!))
                      (vec
                       (concat [:wrote qmd-path]
                               (when run-quarto
                                 [html-path]))))))))))


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
          :quarto {:highlight-style :nord}}))
