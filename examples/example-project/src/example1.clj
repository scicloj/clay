(ns example1
  (:require [scicloj.clay.v1.api :as clay]
            [scicloj.clay.v1.tools :as tools]
            [scicloj.kindly.v2.api :as kindly]
            [scicloj.kindly.v2.kind :as kind]
            [clojure.tools.deps.alpha.repl :as repl]
            [nextjournal.clerk :as clerk])
  (:import javax.imageio.ImageIO
           java.net.URL))

(clay/start! {:tools [tools/clerk
                      tools/portal]})

(+ 1 2)

(defonce clay-image
  (-> "https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg"
      (URL.)
      (ImageIO/read)))

clay-image

(delay
  (Thread/sleep 500)
  (+ 1 2))

(-> 2
    (+ 3)
    (clay/check = 4))

(-> 2
    (+ 3)
    (clay/check = 5))

(import java.awt.image.BufferedImage
        java.awt.Color
        sun.java2d.SunGraphics2D)

(let [n (rand-int 400)]
  (let [bi (BufferedImage. n n BufferedImage/TYPE_INT_RGB)
        g  (-> (.createGraphics ^BufferedImage bi))]
    (dotimes [t 100]
      (->> #(rand-int n)
           (repeatedly 4)
           (apply #(.drawLine ^SunGraphics2D g %1 %2 %3 %4))))
    bi))


(repl/add-libs '{scicloj/tablecloth {:mvn/version "6.051"}
                 aerial.hanami/aerial.hanami {:mvn/version "0.17.0"}
                 org.scicloj/viz.clj {:mvn/version "0.1.2"}})

(require '[tablecloth.api :as tc])
