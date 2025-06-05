(ns scicloj.clay.v2.util.meta
  (:require [clojure.string :as str]
            [clojure.walk :as walk]))

(defn pr-str-with-meta [value]
  (binding [*print-meta* true]
    (pr-str value)))

(defn- toggle-pluraity
  "Given `:authors`, returns `:author`. Given `:author` returns `:authors`."
  [k]
  (keyword
    (let [s (str k)]
      (if (str/ends-with? s "s")
        (subs s 1 (dec (count s)))
        (str (subs s 1) "s")))))

(defn- apply-xf
  "Replace keyword, or keyword(s) in a sequence with (xf v) if found, or v if not"
  [v xf]
  (cond
    (sequential? v) (mapv (fn [x] (apply-xf x xf)) v)
    (keyword? v) (or (xf v) v)
    :else v))

(defn- expand-map
  "Expands value(s) whose key has an expansion defined.
  To expand is to replace a keyword with a transformation (usually a map lookup)."
  [node expansions]
  (reduce
    (fn [acc [k xf]]
      (let [k' (toggle-pluraity k)]
        (cond
          (contains? acc k) (update acc k apply-xf xf)
          (contains? acc k') (update acc k' apply-xf xf)
          :else acc)))
    node
    expansions))

(defn expand
  "Applies map expansions on any map found in a tree.
  Useful for updating document metadata from a central definition of authors for example.
  Expansions will apply to matching keys, and their plural dual."
  [m expansions]
  (walk/prewalk (fn [node]
                  (if (map? node)
                    (expand-map node expansions)
                    node))
                m))

(comment
  (def expansions {:author      {:timothypratley {:name        "Timothy Pratley"
                                                  :url         "https://timothypratley.blogspot.com"
                                                  :affiliation [:hummi]}}
                   :affiliation {:hummi {:name "Hummi"
                                         :url  "https://hummi.app"}}})
  (expand {:author [:timothypratley]} expansions)
  ;;=> {:author [{:name "Timothy Pratley",
  ;               :url "https://timothypratley.blogspot.com",
  ;               :affiliation [{:name "Hummi", :url "https://hummi.app"}]}]}
  ;
  (expand {:authors [:timothypratley]} expansions)
  ;;=> {:authors [{:name "Timothy Pratley",
  ;                :url "https://timothypratley.blogspot.com",
  ;                :affiliation [{:name "Hummi", :url "https://hummi.app"}]}]}

  :-)
