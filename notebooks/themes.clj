(ns themes
  (:require [charred.api :as charred]
            [clojure.walk :as walk]
            [clojure.string :as str]
            [clojure.string :as string]))

(defn hex-color? [v]
  (and (string? v)
       (re-matches #"^#?[a-f0-9]{6}" v)))

;; https://gist.github.com/MarcoPolo/4228374
(defn hex->rgb [hex]
  (-> hex
      (subs 1)
      (->> (partition 2)
           (mapv (fn [pair]
                   (-> pair
                       (conj "0x")
                       str/join
                       read-string))))))

(defn rgb->hex [rgb]
  (-> (->> rgb
           (map (partial format "%2h"))
           (apply str "#"))
      (str/replace #" " "0")))

(comment
  (->> "#bf616a"
       hex->rgb
       rgb->hex)
  (->> [54 4 160]
       rgb->hex
       hex->rgb))

(defn darken [x ratio]
  (-> x
      (* ratio)
      int))

(def new-background
  (->> 251
       (repeat 3)
       rgb->hex))

(-> "resources/nord.theme"
    slurp
    charred/read-json
    (->> (walk/postwalk
          (fn [v]
            (cond ;;
              (hex-color? v)
              (->> v
                   hex->rgb
                   (map #(darken % 0.7))
                   rgb->hex)
              ;;
              (and (instance? clojure.lang.MapEntry v)
                   (->> v
                        first
                        (re-find #"ackground")))
              [(first v) new-background]
              ;;
              :else
              v))))
    charred/write-json-str
    (->> (spit "temp/clay.theme")))
