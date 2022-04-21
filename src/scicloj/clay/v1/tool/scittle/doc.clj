(ns scicloj.clay.v1.tool.scittle.doc
  (:require [scicloj.kindly.v2.kind :as kind]
            [nextjournal.clerk :as clerk]
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
  ([path {:keys [hide-code? hide-nils? hide-vars?
                 title]}]
   (cond->> path
     true clerk-eval
     true :blocks
     true (mapcat (fn [block]
                    (case (:type block)
                      :code [(when-not hide-code?
                               (-> block
                                   :text
                                   vector
                                   kind/code))
                             (-> block
                                 :result
                                 :nextjournal/value
                                 ((fn [v]
                                    (-> v
                                        :nextjournal.clerk/var-from-def
                                        (or v))))
                                 scicloj.clay.v1.view/deref-if-needed)]
                      :markdown [(-> block
                                     :text
                                     vector
                                     kind/md)])))
     hide-nils? (filter some?)
     hide-vars? (filter (complement var?))
     true (filter (complement :nippy/unthawable))
     true (map view/prepare)
     true (#(server/show-widgets! % {:title (or (-> path
                                                    (string/split #"/")
                                                    last
                                                    (string/split #"\.")
                                                    first))})))))


(comment
  (show-doc! "notebooks/intro.clj")
  )

(+ 1 2)
