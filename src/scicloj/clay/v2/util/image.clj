(ns scicloj.clay.v2.util.image
  (:require [clojure.java.io :as io])
  (:import java.awt.image.BufferedImage
           java.util.Base64
           java.io.InputStream
           javax.imageio.ImageIO))

(defn write! [^BufferedImage image
              target-jpg-path]
  (io/make-parents target-jpg-path)
  (ImageIO/write image
                 "jpg"
                 (io/file target-jpg-path)))

(defn buffered-image->byte-array [^BufferedImage image]
  (let [baos (java.io.ByteArrayOutputStream.)]
    (ImageIO/write ^BufferedImage image "png" baos)
    (.toByteArray baos)))

;; copied from dtype-next's tech.v3.libs.buffered-image/load
(defn load-buffered-image [fname-or-stream]
  (with-open [istream (io/input-stream fname-or-stream)]
    (ImageIO/read ^InputStream istream)))

(defn byte-array->data-uri [byte-array]
  (->> byte-array
       (.encodeToString (Base64/getEncoder))
       (str "data:image/png;base64,")))
