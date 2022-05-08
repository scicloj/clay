(ns scicloj.clay.v1.view
  (:require [scicloj.clay.v1.tool :as tool]
            [scicloj.clay.v1.view.image]
            [scicloj.kindly.v2.api :as kindly]))

(defn open! [tools]
  (doseq [tool tools]
    (tool/open! tool)))

(defn close! [tools]
  (doseq [tool tools]
    (tool/close! tool)))

(defn deref-if-needed [v]
  (if (delay? v)
    (let [_ (println "deref ...")
          dv @v
          _ (println "done.")]
      dv)
    v))

(kindly/define-kind! :kind/hidden)

(def hidden-form-starters
  #{'ns 'comment 'defn 'def 'defmacro 'defrecord 'defprotocol 'deftype
    'nextjournal.clerk/show! 'clerk/show!})

(defn show! [value form tools]
  (let [kind-override (or (->> form
                               meta
                               keys
                               (filter (kindly/kinds-set))
                               first)
                          (when (and (list? form)
                                     (-> form
                                         first
                                         hidden-form-starters))
                            :kind/hidden))]
    (when-not (-> kind-override
                  (or (kindly/kind value))
                  (= :kind/hidden))
      (let [value-to-show (deref-if-needed value)]
        (doseq [tool tools]
          (try
            (tool/show! tool
                        value-to-show
                        kind-override)
            (catch Exception e
              (println ["Exception while trying to show value:"
                        e]))))))))

(defn setup-extension! [extension]
  (try (require (:ns extension))
       (catch Exception e
         (throw (ex-info
                 "Failed to setup extension. Have you included the necessary dependencies in your project?"
                 {:extension extension})))))

(defn setup! [{:keys [tools extensions events-source]
               :as config}]
  (doseq [tool tools]
    (tool/setup! tool config))
  (doseq [ex extensions]
    (setup-extension! ex)))
