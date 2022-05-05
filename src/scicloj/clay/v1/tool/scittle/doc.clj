(ns scicloj.clay.v1.tool.scittle.doc
  (:require [scicloj.kindly.v2.kind :as kind]
            [nextjournal.clerk :as clerk]
            [nextjournal.markdown.transform]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.clay.v1.view]
            [scicloj.clay.v1.tool.scittle.view :as view]
            [scicloj.clay.v1.tool.scittle.server :as server]
            [scicloj.clay.v1.tool.scittle.widget :as widget]
            [taoensso.nippy :as nippy]
            [clojure.string :as string]))

(defn clerk-eval
  [file]
  (reset! clerk/!last-file file)
  (let [doc (clerk/parse-file file)
        {:keys [blob->result]} @nextjournal.clerk.webserver/!doc]
    (clerk/+eval-results blob->result doc)))

(defn show-doc!
  ([path]
   (show-doc! path nil))
  ([path {:keys [hide-code? hide-nils? hide-vars? hide-toc?
                 title]}]
   (let [kinds (kindly/kinds-set)]
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
                                                    keys
                                                    (filter kinds)
                                                    first
                                                    (view/prepare value))])))))
                      :markdown [(some-> block
                                         :doc
                                         nextjournal.markdown.transform/->hiccup
                                         kind/hiccup
                                         widget/mark-plain-html)])))
          (#(server/show-widgets! % {:title (or (-> path
                                                    (string/split #"/")
                                                    last
                                                    (string/split #"\.")
                                                    first))
                                     :toc? (not hide-toc?)}))))))


(comment
  (show-doc! "notebooks/intro.clj"))
