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

(defn info-line [absolute-file-path]
  (let [relative-file-path (path/path-relative-to-repo
                            absolute-file-path)
        git-url (some-> (scittle.server/options)
                        :remote-repo
                        (path/file-git-url relative-file-path))]
    (scittle.widget/mark-plain-html
     [:div
      (when relative-file-path
        [:code
         [:small
          [:small
           "source: "
           (if git-url
             [:a {:href git-url} relative-file-path]
             relative-file-path)]]])])))

(comment
  (-> "notebooks/index.clj"
      (gen-doc {})
      (->> (map (juxt meta identity)))))

(def separator
  (scittle.widget/mark-plain-html
   [:div {:style
          {:height "2px"
           :width "100%"
           :background-color "grey"}}]))

(defn gen-doc
  ([path {:keys [hide-info-line?
                 hide-code? hide-nils? hide-vars?
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
                                    ctx)))
                               ((fn [ctx]
                                  (if (-> note :value meta :kindly/kind (= :kind/code))
                                    (-> ctx
                                        (vary-meta
                                         assoc
                                         :clay/original-code
                                         (->> note
                                              :value
                                              (string/join "\n"))))
                                    ctx)))))])))
            (filter some?))
       ((fn [items]
          (if hide-info-line?
            items
            (let [il (info-line path)]
              (concat [il
                       separator]
                      items
                      [separator
                       il])))))
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
          {:title title
           :toc? toc?})))
   :ok))

(defn show-doc-and-write-html!
  [path options]
  (-> options
      (assoc :custom-message [:div
                              [:p "showing document for "
                               [:code (path/path->filename path)]]
                              [:p "and then writing as html file"]
                              [:div.loader]]
             :path path)
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
  (-> options
      (assoc
       :title (or title path)
       :path path)
      (->> (gen-doc path))
      scittle.server/write-quarto!))
