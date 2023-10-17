(ns scicloj.clay.v2.portal
  (:require [portal.api :as portal]
            [clojure.string :as string]))

(defonce dev
  (portal/url
   (portal/start {})))

(def url (let [[host query] (string/split dev #"\?")]
           (str host "/main.js?" query)))
