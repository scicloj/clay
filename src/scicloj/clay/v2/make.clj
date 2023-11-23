(ns scicloj.clay.v2.make
  (:require [scicloj.clay.v2.config :as config]
            [scicloj.clay.v2.files :as files]
            [scicloj.clay.v2.util.path :as path]
            [scicloj.clay.v2.read :as read]
            [scicloj.clay.v2.prepare :as prepare]
            [scicloj.clay.v2.notebook :as notebook]
            [scicloj.clay.v2.page :as page]
            [scicloj.clay.v2.server :as server]))

(defn form->context [form]
  {:form form
   :value (eval form)})

(defn make [{:keys [source-path
                    single-form]
             :as options}]
  (let [ns-form (-> source-path
                    slurp
                    read/read-ns-form)
        ns-name (-> ns-form
                    second
                    name)
        ;; merge configurations
        {:as pre-config
         :keys [base-target-path
                format
                html
                quarto]}
        (merge (config/config)
               (-> ns-form
                   meta
                   :clay)
               options)
        ;; target path
        target-path (path/ns->target-path
                     base-target-path
                     ns-name
                     ".html")
        config (assoc pre-config
                      :target-path target-path)]
    (server/update-page! {:page (page/html
                                 {:items [item/loader]
                                  :config config})})
    (files/init-target! target-path)
    (let [items      (-> source-path
                         (notebook/notebook-items config))]
      (case format
        :html (let [page (page/html {:items items
                                     :config config})]
                (server/update-page!
                 (merge {:page page
                         :html-path target-path}
                        (select-keys config [:show]))))))))


(comment
  (make {:format :html
         :source-path "notebooks/index.clj"})

  (make {:format :html
         :source-path "notebooks/index.clj"
         :show false})

  (make {:format :html
         :source-path "notebooks/index.clj"
         :single-form '(kind/cytoscape
                        [{:style {:width "100px"
                                  :height "100px"}}
                         cytoscape-example])})

  )
