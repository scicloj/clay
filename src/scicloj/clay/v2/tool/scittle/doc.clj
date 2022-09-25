(ns scicloj.clay.v2.tool.scittle.doc
  (:require [scicloj.kindly.v3.kind :as kind]
            [nextjournal.markdown.transform]
            [scicloj.kindly.v3.api :as kindly]
            [scicloj.clay.v2.view :as view]
            [scicloj.clay.v2.tool.scittle.view :as scittle.view]
            [scicloj.clay.v2.tool.scittle.server :as scittle.server]
            [scicloj.clay.v2.tool.scittle.widget :as scittle.widget]
            [scicloj.clay.v2.read]
            [taoensso.nippy :as nippy]
            [clojure.string :as string]
            [nextjournal.markdown :as md]
            [nextjournal.markdown.transform :as md.transform]))

(def hidden-form-starters
  #{'ns 'comment 'defn 'def 'defmacro 'defrecord 'defprotocol 'deftype})

(defn show-doc!
  ([path]
   (show-doc! path nil))
  ([path {:keys [hide-code? hide-nils? hide-vars? hide-toc?
                 title toc?]}]
   (-> path
       slurp
       scicloj.clay.v2.read/->safe-notes
       (->> (map (fn [note]
                   (if (:comment? note)
                     note
                     (assoc
                      note
                      :value (-> note
                                 :form
                                 eval
                                 view/deref-if-needed)))))
            (mapcat (fn [{:as note
                          :keys [comment? code form]}]
                      (if comment?
                        [(-> code
                             (string/split #"\n")
                             (->> (map #(string/replace % #"^;*" ""))
                                  (string/join "\n"))
                             md/parse
                             md.transform/->hiccup
                             kind/hiccup
                             #_scittle.widget/mark-plain-html)]
                        [(when-not hide-code?
                           (-> {:value (-> note
                                           :code
                                           vector)
                                :kind :kind/code}
                               scittle.view/prepare))
                         (when-not (or
                                    (and (sequential? form)
                                         (-> form first hidden-form-starters))
                                    (-> note :form meta :kind/hidden))
                           [:div (-> note
                                     (select-keys [:value :code :form])
                                     scittle.view/prepare)])]))))
       doall
       (#(scittle.server/show-widgets! % {:title path
                                          :toc? toc?})))))



#_
(let [path "notebook/intro.clj"
      {:keys [hide-code? hide-nils? hide-vars? hide-toc? title]} nil]
  (->> path
       clerk-eval
       :blocks
       (mapcat (fn [block]
                 (case (:type block)
                   :code (when-not (-> block
                                       :form
                                       meta
                                       :kind/hidden)
                           (-> (concat (when-not hide-code?
                                         [(-> block
                                              :text
                                              vector
                                              kind/code
                                              view/prepare)])
                                       (let [value (-> block
                                                       :result
                                                       :nextjournal/value
                                                       ((fn [v]
                                                          (-> v
                                                              :nextjournal.clerk/var-from-def
                                                              (or v))))
                                                       scicloj.clay.v1.view/deref-if-needed)]
                                         (when-not (or (and hide-nils? (nil? value))
                                                       (and hide-vars? (var? value))
                                                       (:nippy/unthawable value))
                                           [(->> block
                                                 :text
                                                 kindly/code->kind
                                                 (view/prepare value))])))))
                   :markdown [(some-> block
                                      :doc
                                      nextjournal.markdown.transform/->hiccup
                                      kind/hiccup
                                      widget/mark-plain-html)])))))








(comment
  (show-doc! "notebooks/intro.clj"))
