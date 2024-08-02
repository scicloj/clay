^{:clay {:kindly/options {:kinds-that-hide-code #{:kind/hiccup :kind/image}}}}
(ns dummy1
  (:require [scicloj.kindly.v4.kind :as kind]))

(-> "https://upload.wikimedia.org/wikipedia/commons/2/2c/Clay-ss-2005.jpg"
    (java.net.URL.)
    (javax.imageio.ImageIO/read)
    kind/image)
