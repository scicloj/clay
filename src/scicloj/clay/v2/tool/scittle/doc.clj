(ns scicloj.clay.v2.tool.scittle.doc
  (:require [scicloj.kindly.v3.kind :as kind]
            [nextjournal.markdown.transform]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.clay.v2.view :as view]
            [scicloj.clay.v2.tool.scittle.view :as scittle.view]
            [scicloj.clay.v2.tool.scittle.server :as scittle.server]
            [scicloj.clay.v2.tool.scittle.widget :as scittle.widget]
            [scicloj.clay.v2.tool.scittle.page :as scittle.page]
            [scicloj.clay.v2.read]
            [scicloj.clay.v2.path :as path]
            [clojure.string :as string]
            [nextjournal.markdown :as md]
            [nextjournal.markdown.transform :as md.transform]
            [hiccup.core :as hiccup]
            [clojure.walk :as walk]))

(def hidden-form-starters
  #{'ns 'comment 'defn 'def 'defmacro 'defrecord 'defprotocol 'deftype})

(defn gen-doc
  ([path {:keys [hide-code? hide-nils? hide-vars?
                 title toc?]}]
   (-> path
       slurp
       scicloj.clay.v2.read/->safe-notes
       (->> (map (fn [{:as note
                       :keys [comment? code form]}]
                   (if comment?
                     note
                     (assoc
                      note
                      :value (if form
                               (-> form
                                   eval
                                   view/deref-if-needed)
                               (-> code
                                   read-string
                                   eval
                                   view/deref-if-needed))))))
            (mapcat (fn [{:as note
                          :keys [comment? code form value]}]
                      (if comment?
                        [(let [markdown
                               (-> code
                                   (string/split #"\n")
                                   (->> (map #(-> %
                                                  (string/replace
                                                   #"^;+\s*" "")
                                                  (string/replace
                                                   #"^#" "\n#")))
                                        (string/join "\n")))]
                           (->> markdown
                                md/parse
                                md.transform/->hiccup
                                ;; TODO: How to handle these better?
                                ;; (:<> does not work in plain hiccup)
                                (walk/postwalk-replace {:<> :div})
                                scittle.widget/mark-plain-html
                                (#(vary-meta
                                   %
                                   assoc
                                   :clay/original-markdown markdown))))]
                        [(-> {:value (-> note
                                         :code
                                         vector)
                              :kind :kind/code}
                             scittle.view/prepare
                             (#(vary-meta
                                %
                                assoc
                                :clay/original-code (-> note
                                                        :code)
                                :clay/hide-code? (or hide-code?
                                                     (-> form meta :kindly/hide-code?)
                                                     (-> value meta :kindly/hide-code?)))))
                         (when-not (or
                                    (and (sequential? form)
                                         (-> form first hidden-form-starters))
                                    (-> note :form meta :kind/hidden)
                                    (and hide-nils? (nil? value))
                                    (and hide-vars? (var? value)))
                           (-> note
                               (select-keys [:value :code :form])
                               (update :value view/deref-if-needed)
                               scittle.view/prepare
                               scittle.widget/in-div ; TODO: was this needed?
                               ((fn [ctx]
                                  (if (-> note :value meta :kindly/kind (= :kind/md))
                                    (-> ctx
                                        (vary-meta
                                         assoc
                                         :clay/original-markdown
                                         (->> note
                                              :value
                                              (string/join "\n"))))
                                    ctx)))))]))))
       doall)))

(defn show-doc!
  ([path]
   (show-doc! path nil))
  ([path {:keys [title toc? custom-message]
          :as options}]
   (scittle.server/show-message!
    (or custom-message
        [:div
         [:p "showing document for "
          [:code (path/path->filename path)]]
         [:div.loader]]))
   (let [doc (gen-doc path options)]
     (-> doc
         (scittle.server/show-widgets!
          {:title (or title path)
           :toc? toc?})))
   :ok))

(defn show-doc-and-write-html!
  [path options]
  (-> options
      (assoc :custom-message
             [:div
              [:p "showing document for "
               [:code (path/path->filename path)]]
              [:p "and then writing as html file"]
              [:div.loader]])
      (->> (show-doc! path)))
  (scittle.server/write-html!))

(defn gen-doc-and-write-quarto!
  [path {:keys [title]
         :as options}]
  (scittle.server/show-message!
   [:div
    [:p "generating Quarto document for "
     [:code (path/path->filename path)]]
    [:div.loader]])
  (->> options
       (merge {:title (or title path)})
       (gen-doc path)
       (scittle.server/write-quarto!)))
