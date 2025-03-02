(ns scicloj.clay.v2.live-reload-test
  (:require [scicloj.clay.v2.live-reload :as lr]
            [babashka.fs :as fs]
            [clojure.test :refer [deftest is]]))

(deftest subdir-paths-test
  (fs/with-temp-dir [test-dir]
    (is (= #{(fs/path test-dir "foo" "a")
             (fs/path test-dir "foo" "a" "b")
             (fs/path test-dir "bar" "a")}
           (lr/subdirs [(fs/path test-dir "foo" "a")
                             (fs/path test-dir "foo" "a" "b")
                             (fs/path test-dir "bar" "a")
                             (fs/path test-dir "baz" "a")]
                       [(fs/path test-dir "foo")
                             (fs/path test-dir "bar")])))))

(deftest exclude-subdir-paths-test
  (fs/with-temp-dir [test-dir]
    (is (= #{(fs/path test-dir "foo")
             (fs/path test-dir "bar")}
           (lr/roots [(fs/path test-dir "foo")
                                     (fs/path test-dir "bar")])))
    (is (= #{(fs/path test-dir "foo")
             (fs/path test-dir "bar")}
           (lr/roots [(fs/path test-dir "foo")
                                     (fs/path test-dir "foo" "a")
                                     (fs/path test-dir "bar")
                                     (fs/path test-dir "bar" "b")])))))

(deftest dirs-to-watch-test
  (fs/with-temp-dir [test-dir]
    (is (= #{(fs/path test-dir "bar")}
           (lr/dirs-to-watch #{(fs/path test-dir "foo")}
                             #{(fs/path test-dir "foo" "a.clj")
                               (fs/path test-dir "foo" "b" "a.clj")
                               (fs/path test-dir "bar" "a.clj")
                               (fs/path test-dir "bar" "b" "a.clj")})))))

(defn mock-make-fn [spec]
  (println "mock-make-fn: " spec))

(defn create-dirs [dirs]
  (doseq [dir dirs]
    (fs/create-dirs dir)))

(deftest start-stop-watching-dirs!-test
  (fs/with-temp-dir [test-dir]
    (let [dirs [(fs/path test-dir "foo")
                (fs/path test-dir "foo")
                (fs/path test-dir "bar")]]
      (create-dirs dirs)
      (lr/watch-dirs! dirs mock-make-fn)
      (is (= #{(fs/path test-dir "foo")
               (fs/path test-dir "bar")}
             (lr/watched-dirs))
          "watch a dir exactly once")
      (lr/stop-watching-dirs! dirs)
      (is (= #{}
             (lr/watched-dirs))
          "all being stopped"))))

(deftest start!-stop!-test
  (fs/with-temp-dir [test-dir]
                    (let [file1 (fs/path test-dir "foo" "bar" "a.clj")]
                      (fs/create-dirs (fs/parent file1))
                      (lr/start! mock-make-fn {:live-reload true
                               :source-path (str file1)})
                      (is (= #{file1}
                             (lr/watched-files))
          "watch first file")
                      (is (= #{(fs/parent file1)}
                             (lr/watched-dirs))
          "watch first dir")
                      (lr/stop!)
                      (is (= #{}
                             (lr/watched-files)))
                      (is (= #{}
                             (lr/watched-dirs)))
                      ;; do it again
                      (lr/start! mock-make-fn {:live-reload true
                               :source-path (str file1)})
                      (is (= #{(fs/parent file1)}
                             (lr/watched-dirs))
          "watch first dir")
                      ;; make other files
                      (let [files [(fs/path test-dir "foo" "a.clj")
                   (fs/path test-dir "foo" "baz" "a.clj")
                   (fs/path test-dir "bar" "a.clj")]]
                        (create-dirs (map fs/parent files))
                        (lr/start! mock-make-fn {:live-reload true
                                 :source-path (into [] (map str files))})
                        (is (= (set (apply vector file1 files))
                               (lr/watched-files))
            "watch files twice")
                        (is (= #{(fs/path test-dir "foo")
                 (fs/path test-dir "bar")}
                               (lr/watched-dirs))
            "watch dirs twice")
                        (lr/stop!)
                        (is (= #{}
                               (lr/watched-files)))
                        (is (= #{}
                               (lr/watched-dirs)))))))
