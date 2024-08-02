(ns scratch2
  (:require [scicloj.kindly.v4.kind :as kind]
            [scicloj.kindly.v4.api :as kindly]
            [tablecloth.api :as tc]
            [clojure.string :as str]))


(-> [:img {:src "https://scicloj.github.io/sci-cloj-logo-transparent.png"
           :alt "SciCloj logo"
           :width "100"
           :align "right"}]
    kind/hiccup
    kindly/hide-code)

;; The Clojure Data Scrapbook is a collection of
;; community-contributed examples for the emerging Clojure data stack.
;;
;; You can interact with the examples by cloning this project and starting a REPL.
;;
;; This project is part of the [SciCloj community](https://scicloj.github.io/docs/community/about/).

;; ## Tutorials

(-> {:row-vectors (->> [["2023-12-16"
                         "Clay Calva integration - datavis demo"
                         "projects/visual-tools/clay-calva-demo-20231216/index.html"
                         "projects/visual-tools/clay-calva-demo-20231216"
                         "X_SsjhmG5Ok"
                         [:visual-tools :clay :calva :noj
                          :datavis :hanami :tablecloth]]
                        ["2023-12-17"
                         "Clay CIDER integration - image processing demo"
                         "projects/visual-tools/clay-cider-demo-20231217/index.html"
                         "projects/visual-tools/clay-cider-demo-20231217"
                         "fd4kjlws6Ts"
                         [:visual-tools :clay :cider :noj
                          :image-processing :dtype-next :tensors]]
                        ["2023-12-31"
                         "Reading HDF files"
                         "projects/data-formats/hdf/index.html"
                         "projects/data-formats/hdf/"
                         nil
                         [:data-formats :hdf :dtype-next :tensors]]
                        ["2024-01-11"
                         "Machine learning - DRAFT"
                         "projects/noj/ml.html"
                         "projects/noj/notebooks/ml.clj"
                         nil
                         [:noj :ml :scicloj.ml :draft]]
                        ["2024-01-25"
                         "Wolfram Lanauge interop with Wolframite"
                         "projects/math/wolframite/index.html"
                         "projects/math/wolframite/"
                         nil
                         [:math :wolframite :interop :draft]]
                        ["2024-02-06"
                         "Exploring ggplot - DRAFT"
                         "projects/noj/ggplot.html"
                         "projects/noj/notebooks/ggplot.clj"
                         nil
                         [:noj :r :clojisr :interop :ggplot :datavis :draft]]]
                       (map (fn [[date title url source-path youtube-id tags]]
                              [date
                               (kind/hiccup
                                [:div
                                 [:pre
                                  [:p [:a {:href url}
                                       title]]
                                  [:p [:a {:style {:background-color "#fdf6e3"}
                                           :href (str "https://github.com/scicloj/clojure-data-scrapbook/tree/main/"
                                                      source-path)}
                                       "(source)"]]]])
                               (when youtube-id
                                 (kind/video {:youtube-id youtube-id}))
                               (->> tags
                                    (map name)
                                    (str/join ", "))])))
     :column-names ["date"
                    "title"
                    "video"
                    "tags"]}
    (kind/table {:datatables {:paging false}})
    kindly/hide-code)

;; ## Contributing

;; (coming soon)
