(ns scicloj.clay.v2.live-reload-test
  (:require [scicloj.clay.v2.live-reload :as lr]
            [babashka.fs :as fs]
            [clojure.test :refer [deftest is]]))

(defn path-str [& args]
  (str (apply (comp fs/canonicalize fs/path) args)))

(deftest subdir-paths-test
  (fs/with-temp-dir [test-dir]
    (is (= #{(path-str test-dir "foo" "a")
             (path-str test-dir "foo" "a" "b")
             (path-str test-dir "bar" "a")}
           (lr/subdirs [(path-str test-dir "foo" "a")
                        (path-str test-dir "foo" "a" "b")
                        (path-str test-dir "bar" "a")
                        (path-str test-dir "baz" "a")]
                       [(path-str test-dir "foo")
                        (path-str test-dir "bar")])))))

(deftest exclude-subdir-paths-test
  (fs/with-temp-dir [test-dir]
    (is (= #{(path-str test-dir "foo")
             (path-str test-dir "bar")}
           (lr/roots #{(path-str test-dir "foo")
                       (path-str test-dir "bar")})))
    (is (= #{(path-str test-dir "foo")
             (path-str test-dir "bar")}
           (lr/roots #{(path-str test-dir "foo")
                       (path-str test-dir "foo" "a")
                       (path-str test-dir "bar")
                       (path-str test-dir "bar" "b")})))))

(deftest dirs-to-watch-test
  (fs/with-temp-dir [test-dir]
    (is (= #{(path-str test-dir "bar")}
           (lr/dirs-to-watch #{(path-str test-dir "foo")}
                             #{}
                             #{(path-str test-dir "foo" "a.clj")
                               (path-str test-dir "foo" "b" "a.clj")
                               (path-str test-dir "bar" "a.clj")
                               (path-str test-dir "bar" "b" "a.clj")})))))

(defn mock-make-fn [spec]
  (println "mock-make-fn: " spec))

(defn create-dirs [dirs]
  (doseq [dir dirs]
    (fs/create-dirs dir)))

(deftest start-stop-watching-dirs!-test
  (fs/with-temp-dir [test-dir]
    (let [dirs [(path-str test-dir "foo")
                (path-str test-dir "foo")
                (path-str test-dir "bar")]]
      (create-dirs dirs)
      (lr/watch-dirs! dirs mock-make-fn {})
      (is (= #{(path-str test-dir "foo")
               (path-str test-dir "bar")}
             (lr/watched-dirs))
          "watch a dir exactly once")
      (lr/stop-watching-dirs! dirs)
      (is (= #{}
             (lr/watched-dirs))
          "all being stopped"))))

(deftest start!-stop!-test
  (fs/with-temp-dir [test-dir]
    (let [file1 (path-str test-dir "foo" "bar" "a.clj")]
      (fs/create-dirs (fs/parent file1))
      (lr/start! mock-make-fn
                 {:live-reload true
                  :source-path file1}
                 [file1]
                 [])
      (is (= #{file1}
             (lr/watched-files))
          "watch first file")
      (is (= #{(str (fs/parent file1))}
             (lr/watched-dirs))
          "watch first dir")
      (lr/stop!)
      (is (= #{}
             (lr/watched-files)))
      (is (= #{}
             (lr/watched-dirs)))
      ;; do it again
      (lr/start! mock-make-fn
                 {:live-reload true
                  :source-path file1}
                 [file1]
                 [])
      (is (= #{(str (fs/parent file1))}
             (lr/watched-dirs))
          "watch first dir")
      ;; make other files
      (let [files [(path-str test-dir "foo" "a.clj")
                   (path-str test-dir "foo" "baz" "a.clj")
                   (path-str test-dir "bar" "a.clj")]]
        (create-dirs (map fs/parent files))
        (lr/start! mock-make-fn
                   {:live-reload true
                    :source-path files}
                   files
                   [])
        (is (= (set (apply vector file1 files))
               (lr/watched-files))
            "watch files twice")
        (is (= #{(path-str test-dir "foo")
                 (path-str test-dir "bar")}
               (lr/watched-dirs))
            "watch dirs twice")
        (lr/stop!)
        (is (= #{}
               (lr/watched-files)))
        (is (= #{}
               (lr/watched-dirs)))))))
