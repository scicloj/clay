(ns scicloj.clay.v2.api-integration-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [scicloj.clay.v2.api :as clay]))

;; Test fixtures for cleanup
(defn cleanup-test-files [f]
  "Clean up test directories before and after tests"
  ;; Clean up before test
  (doseq [dir ["test-output" "temp" "test_notebook.clj" "test_index.clj" "test_nested"]]
    (when (fs/exists? dir)
      (if (fs/directory? dir)
        (fs/delete-tree dir)
        (fs/delete dir))))

  ;; Run the test
  (f)

  ;; Clean up after test
  (doseq [dir ["test-output" "temp" "test_notebook.clj" "test_index.clj" "test_nested"]]
    (when (fs/exists? dir)
      (if (fs/directory? dir)
        (fs/delete-tree dir)
        (fs/delete dir)))))

(use-fixtures :each cleanup-test-files)

(deftest test-simple-value-rendering
  "Test rendering a simple value - most reliable baseline test"
  (testing "Clay can render simple values to HTML"
    (let [test-content "Hello from Clay test!"
          result (clay/make! {:single-value test-content
                              :show false
                              :browse false})]

      ;; Verify detailed return value structure
      (is (map? result))
      (is (= "clay" (:key result)))
      (is (= "Clay" (:title result)))
      (is (= :editor (:display result)))
      (is (= false (:reveal result)))
      (is (nil? (:url result))) ; nil when :show false

      ;; Verify info field structure
      (is (vector? (:info result)))
      (is (= 1 (count (:info result)))) ; one processing result
      (let [info-entry (first (:info result))]
        (is (vector? info-entry))
        (is (= :wrote (first (first (first info-entry))))))

      ;; Verify HTML file was created at expected location
      (is (fs/exists? "temp/.clay.html"))

      ;; Verify content was rendered correctly with detailed checks
      (let [html-content (slurp "temp/.clay.html")]
        (is (.startsWith html-content "<!DOCTYPE html"))
        (is (str/includes? html-content "<html"))
        (is (str/includes? html-content test-content))
        (is (str/includes? html-content "Clay")) ; title
        (is (> (count html-content) 1000)) ; substantial HTML content
        ))))

(deftest test-target-directory-control
  "Test that we can control where files are written"
  (testing "Clay respects :base-target-path configuration"
    ;; Clean up any existing files
    (when (fs/exists? "test-output") (fs/delete-tree "test-output"))
    (when (fs/exists? "temp/.clay.html") (fs/delete "temp/.clay.html"))

    (let [test-dir "test-output"
          result (clay/make! {:single-value "test content"
                              :base-target-path test-dir
                              :show false
                              :browse false})]

      ;; Verify file was created in specified directory
      (is (fs/exists? (str test-dir "/.clay.html")))
      ;; Verify no file was created in temp (since we specified different target)
      (is (not (fs/exists? "temp/.clay.html")))

      ;; Verify content is correct
      (let [html-content (slurp (str test-dir "/.clay.html"))]
        (is (str/includes? html-content "test content"))))))

(deftest test-config-defaults
  "Verify configuration defaults match expectations"
  (testing "Clay config has expected default values"
    (let [config (clay/config)]
      (is (= "temp" (:base-target-path config)))
      (is (= [:html] (:format config)))
      (is (:flatten-targets config))
      (is (:show config))
      (is (:browse config))
      (is (= ["src" "notebooks"] (:subdirs-to-sync config))))))

(deftest test-single-form-evaluation
  "Test rendering a single Clojure form"
  (testing "Clay can evaluate and render Clojure forms"
    (let [test-form '(+ 1 2 3)
          result (clay/make! {:single-form test-form
                              :show false
                              :browse false})]

      ;; Verify return structure matches expected pattern
      (is (map? result))
      (is (= "clay" (:key result)))
      (is (= "Clay" (:title result)))

      ;; Single forms also create .clay.html (same as single-value)
      (is (fs/exists? "temp/.clay.html"))

      (let [html-content (slurp "temp/.clay.html")]
        ;; Should contain the evaluated result
        (is (str/includes? html-content "6")) ; result of (+ 1 2 3)
        (is (.startsWith html-content "<!DOCTYPE html"))
        (is (str/includes? html-content "<html"))))))

(deftest test-source-file-with-simple-content
  "Test processing a simple Clojure source file"
  (testing "Clay can process simple Clojure namespace files"
    ;; Create a minimal test notebook file
    (let [test-ns-content "(ns simple-test-notebook
  \"A simple test notebook for Clay\")

;; A simple calculation
(def result (+ 1 2 3))

;; A string value  
\"This is a test string\"

;; A data structure
{:test true :value 42}"]

      (spit "test_notebook.clj" test-ns-content)

      (let [result (clay/make! {:source-path "test_notebook.clj"
                                :show false
                                :browse false})]

        ;; Verify result structure
        (is (map? result))
        (is (= "clay" (:key result)))

        ;; Verify HTML file was created with flattened name
        (is (fs/exists? "temp/test_notebook.html"))

        ;; Verify content was processed correctly
        (let [html-content (slurp "temp/test_notebook.html")]
          (is (str/includes? html-content "simple-test-notebook"))
          (is (str/includes? html-content "A simple calculation"))
          (is (str/includes? html-content "This is a test string"))
          (is (str/includes? html-content "{:test true"))
          (is (str/includes? html-content "6")) ;; result of (+ 1 2 3)
          )))))

(deftest test-missing-source-file-error
  "Test error handling for missing source files"
  (testing "Clay handles missing source files gracefully"
    (is (thrown? Exception
                 (clay/make! {:source-path "nonexistent_file.clj"
                              :show false
                              :browse false})))))

(deftest test-different-formats
  "Test different output formats"
  (testing "Clay can render to different formats"
    ;; Test HTML format (default)
    (let [html-result (clay/make! {:single-value "format test"
                                   :format [:html]
                                   :show false
                                   :browse false})]
      (is (fs/exists? "temp/.clay.html")))

    ;; Test Hiccup format returns a vector structure
    (let [hiccup-result (clay/make-hiccup {:single-value "hiccup test"})]
      (is (vector? hiccup-result))
      ;; Just verify it's a proper hiccup structure (starts with vector)
      (is (sequential? hiccup-result)))))

(deftest test-path-flattening-behavior
  "Test the default path flattening behavior"
  (testing "Clay flattens source paths by default"
    ;; Clean up any existing files
    (when (fs/exists? "test_nested") (fs/delete-tree "test_nested"))
    (when (fs/exists? "temp/test_nested.subdir.nested_notebook.html")
      (fs/delete "temp/test_nested.subdir.nested_notebook.html"))
    (when (fs/exists? "temp/test_nested") (fs/delete-tree "temp/test_nested"))

    ;; Create a nested directory structure
    (fs/create-dirs "test_nested/subdir")
    (spit "test_nested/subdir/nested_notebook.clj"
          "(ns nested-test)\n\n\"Nested notebook content\"\n\n{:level \"deep\"}")

    (let [result (clay/make! {:source-path "test_nested/subdir/nested_notebook.clj"
                              :show false
                              :browse false})]

      ;; Verify return value structure
      (is (map? result))
      (is (= "clay" (:key result)))

      ;; Verify info shows flattened file path
      (let [info-entry (first (:info result))
            written-path (second (first (first info-entry)))]
        (is (str/includes? written-path "test_nested.subdir.nested_notebook.html")))

      ;; With flattening (default), should create flattened filename
      (is (fs/exists? "temp/test_nested.subdir.nested_notebook.html"))
      (is (not (fs/exists? "temp/test_nested/subdir/nested_notebook.html")))

      ;; Verify content is correct (note: HTML escapes quotes as &quot;)
      (let [html-content (slurp "temp/test_nested.subdir.nested_notebook.html")]
        (is (str/includes? html-content "Nested notebook content"))
        (is (str/includes? html-content ":level"))
        (is (str/includes? html-content "&quot;deep&quot;")))

      ;; Also check that resource directory is created with flattened name
      (is (fs/exists? "temp/test_nested.subdir.nested_notebook_files")))

    ;; Clean up
    (fs/delete-tree "test_nested")
    (when (fs/exists? "temp/test_nested.subdir.nested_notebook.html")
      (fs/delete "temp/test_nested.subdir.nested_notebook.html"))
    (when (fs/exists? "temp/test_nested") (fs/delete-tree "temp/test_nested"))))

(deftest test-flatten-targets-disabled
  "Test behavior when path flattening is disabled"
  (testing "Clay preserves directory structure when :flatten-targets is false"
    ;; Clean up any existing files
    (when (fs/exists? "test_nested") (fs/delete-tree "test_nested"))
    (when (fs/exists? "temp/test_nested.subdir.nested_notebook.html")
      (fs/delete "temp/test_nested.subdir.nested_notebook.html"))
    (when (fs/exists? "temp/test_nested") (fs/delete-tree "temp/test_nested"))

    ;; Create a nested directory structure
    (fs/create-dirs "test_nested/subdir")
    (spit "test_nested/subdir/nested_notebook.clj"
          "(ns nested-test)\n\n\"Nested notebook content\"")

    (let [result (clay/make! {:source-path "test_nested/subdir/nested_notebook.clj"
                              :flatten-targets false
                              :show false
                              :browse false})]

      ;; With flattening disabled, should preserve directory structure
      (is (fs/exists? "temp/test_nested/subdir/nested_notebook.html"))
      (is (not (fs/exists? "temp/test_nested.subdir.nested_notebook.html"))))

    ;; Clean up
    (fs/delete-tree "test_nested")
    (when (fs/exists? "temp/test_nested.subdir.nested_notebook.html")
      (fs/delete "temp/test_nested.subdir.nested_notebook.html"))
    (when (fs/exists? "temp/test_nested") (fs/delete-tree "temp/test_nested"))))

;; Integration test that demonstrates the specific API example from index.clj
(deftest test-api-example-equivalent
  "Test equivalent to the API example: (clay/make! {:source-path \"notebooks/index.clj\"})"
  (testing "Equivalent test to the original API example but with controlled dependencies"
    ;; Create a self-contained version of what index.clj might contain
    (let [simple-notebook-content
          "(ns test-index
  \"A simple version of index.clj for testing\")

;; # Test Documentation

;; This is a simple test notebook that demonstrates Clay functionality
;; without external dependencies.

;; ## Basic values
\"Welcome to Clay!\"

;; ## Simple calculations
(+ 1 2 3)

;; ## Data structures
{:clay \"awesome\" 
 :test true
 :numbers [1 2 3 4 5]}

;; ## Collections
(range 10)"]

      (spit "test_index.clj" simple-notebook-content)

      ;; This mimics the original example but with controlled content
      (let [result (clay/make! {:source-path "test_index.clj"
                                :show false
                                :browse false})]

        ;; Verify the result matches expectations from our REPL testing
        (is (map? result))
        (is (= "clay" (:key result)))

        ;; File should be created with flattened name in temp (not docs)
        (is (fs/exists? "temp/test_index.html"))

        ;; Verify content structure
        (let [html-content (slurp "temp/test_index.html")]
          (is (str/includes? html-content "Test Documentation"))
          (is (str/includes? html-content "Welcome to Clay!"))
          (is (str/includes? html-content "6")) ;; result of (+ 1 2 3)
          (is (str/includes? html-content "awesome"))
          (is (str/includes? html-content "(0 1 2 3 4 5 6 7 8 9)")) ;; result of (range 10)
          ))

      ;; Clean up
      (fs/delete "test_index.clj"))))

(deftest test-multiple-source-files
  "Test processing multiple source files in a single call"
  (testing "Clay can process multiple source files and track results"
    ;; Create multiple test files
    (spit "test_multi_a.clj" "(ns test-multi-a)\n\n\"Content from file A\"\n\n(+ 10 20)")
    (spit "test_multi_b.clj" "(ns test-multi-b)\n\n\"Content from file B\"\n\n(* 3 4)")

    (let [result (clay/make! {:source-path ["test_multi_a.clj" "test_multi_b.clj"]
                              :show false
                              :browse false})]

      ;; Verify return structure includes both files
      (is (map? result))
      (is (= "clay" (:key result)))

      ;; Info field should have two entries (one per source file)
      (is (vector? (:info result)))
      (is (= 2 (count (:info result))))

      ;; Both HTML files should be created with source file names
      (is (fs/exists? "temp/test_multi_a.html"))
      (is (fs/exists? "temp/test_multi_b.html"))

      ;; Verify content in each file
      (let [html-a (slurp "temp/test_multi_a.html")
            html-b (slurp "temp/test_multi_b.html")]
        (is (str/includes? html-a "Content from file A"))
        (is (str/includes? html-a "30")) ; result of (+ 10 20)
        (is (str/includes? html-b "Content from file B"))
        (is (str/includes? html-b "12")) ; result of (* 3 4)
        ))))

(deftest test-resource-directory-creation
  "Test that Clay creates associated _files directories for resources"
  (testing "Clay creates resource directories alongside HTML files"
    (spit "test_resources.clj" "(ns test-resources)\n\n\"Testing resource creation\"")

    (clay/make! {:source-path "test_resources.clj"
                 :show false
                 :browse false})

    ;; Should create both HTML file and corresponding _files directory
    (is (fs/exists? "temp/test_resources.html"))
    (is (fs/exists? "temp/test_resources_files"))
    (is (fs/directory? "temp/test_resources_files"))))

(deftest test-hiccup-format-differences
  "Test differences between make! and make-hiccup functions"
  (testing "make-hiccup returns hiccup structure without creating files"
    ;; Clean up any existing .clay.html first
    (when (fs/exists? "temp/.clay.html") (fs/delete "temp/.clay.html"))

    (let [hiccup-result (clay/make-hiccup {:single-value "Hiccup test data"})]

      ;; Hiccup result should be a vector (hiccup structure)
      (is (vector? hiccup-result))
      (is (sequential? hiccup-result))
      (is (> (count hiccup-result) 0))

      ;; Should NOT create any files
      (is (not (fs/exists? "temp/.clay.html")))

      ;; Compare with make! which DOES create files
      (let [make-result (clay/make! {:single-value "Regular test data"
                                     :show false
                                     :browse false})]
        (is (map? make-result)) ; make! returns map
        (is (fs/exists? "temp/.clay.html"))) ; make! creates files
      )))

(deftest test-detailed-config-behavior
  "Test detailed configuration behavior and defaults"
  (testing "Configuration defaults and overrides work as expected"
    ;; Test that config returns expected defaults
    (let [config (clay/config)]
      (is (= "temp" (:base-target-path config)))
      (is (= [:html] (:format config)))
      (is (= true (:show config)))
      (is (= true (:browse config)))
      (is (= true (:flatten-targets config))))

    ;; Clean up any existing temp/.clay.html to avoid test interference
    (when (fs/exists? "temp/.clay.html") (fs/delete "temp/.clay.html"))

    ;; Test that runtime config overrides defaults
    (let [result (clay/make! {:single-value "Config test"
                              :base-target-path "test-config-dir"
                              :show false
                              :browse false})]

      ;; File should be created in custom directory
      (is (fs/exists? "test-config-dir/.clay.html"))
      ;; No file should be created in temp (since we specified different target)
      (is (not (fs/exists? "temp/.clay.html")))

      ;; Clean up
      (when (fs/exists? "test-config-dir") (fs/delete-tree "test-config-dir")))))

(deftest test-error-handling-comprehensive
  "Test comprehensive error handling scenarios"
  (testing "Clay handles various error conditions gracefully"
    ;; Test missing source file
    (is (thrown? Exception
                 (clay/make! {:source-path "completely_nonexistent_file.clj"
                              :show false
                              :browse false})))

    ;; Test missing directory in source path
    (is (thrown? Exception
                 (clay/make! {:source-path "nonexistent/directory/file.clj"
                              :show false
                              :browse false})))

    ;; Test malformed Clojure in source file
    (spit "test_malformed.clj" "(ns test-malformed\n\n(+ 1 2 ; missing closing paren")
    (is (thrown? Exception
                 (clay/make! {:source-path "test_malformed.clj"
                              :show false
                              :browse false})))

    ;; Clean up
    (when (fs/exists? "test_malformed.clj") (fs/delete "test_malformed.clj"))))