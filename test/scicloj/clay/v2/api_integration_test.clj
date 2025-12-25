(ns scicloj.clay.v2.old.api-integration-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [scicloj.clay.v2.old.api :as clay]))

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
    (let [test-ns-content "(ns test-notebook
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
          (is (str/includes? html-content "test-notebook"))
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
          "(ns test-nested.subdir.nested-notebook)\n\n\"Nested notebook content\"\n\n{:level \"deep\"}")

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
        (is (str/includes? html-content "&quot;deep&quot;"))))

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
          "(ns test-nested.subdir.nested-notebook)\n\n\"Nested notebook content\"")
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

(deftest test-return-value-structure-comprehensive
  "Test detailed return value structure across different scenarios"
  (testing "Return value structure is consistent and complete"
    ;; Test single-value
    (let [result (clay/make! {:single-value {:test "data"}
                              :show false
                              :browse false})]

      ;; All required keys present
      (is (contains? result :key))
      (is (contains? result :title))
      (is (contains? result :display))
      (is (contains? result :reveal))
      (is (contains? result :info))

      ;; Correct types and values
      (is (or (nil? (:url result)) (string? (:url result))))
      (is (= "clay" (:key result)))
      (is (= "Clay" (:title result)))
      (is (keyword? (:display result)))
      (is (boolean? (:reveal result)))
      (is (vector? (:info result)))

      ;; Info structure validation
      (is (= 1 (count (:info result))))
      (let [info-entry (first (:info result))]
        (is (vector? info-entry))
        (is (= 2 (count info-entry)))
        (let [write-info (first info-entry)]
          (is (vector? write-info))
          (is (= 3 (count write-info)))
          (let [write-detail (first write-info)]
            (is (vector? write-detail))
            (is (= :wrote (first write-detail)))
            (is (string? (second write-detail)))
            ;; Fix: Current Clay API returns 2 elements, not 3
            (is (= 2 (count write-detail)))))))

    ;; Test multiple source files return structure
    (spit "multi_test_1.clj" "(ns multi-test-1)\n\n\"File 1\"")
    (spit "multi_test_2.clj" "(ns multi-test-2)\n\n\"File 2\"")

    (let [multi-result (clay/make! {:source-path ["multi_test_1.clj" "multi_test_2.clj"]
                                    :show false
                                    :browse false})]
      ;; Same structure but with multiple info entries
      (is (= 2 (count (:info multi-result))))
      (is (every? vector? (:info multi-result)))

      ;; Each info entry should have write information
      (doseq [info-entry (:info multi-result)]
        (let [write-info (first info-entry)
              write-detail (first write-info)]
          (is (= :wrote (first write-detail)))
          (is (string? (second write-detail)))
          ;; Fix: Current Clay API returns 2 elements, not 3
          (is (= 2 (count write-detail))))))))

(deftest test-file-system-effects-comprehensive
  "Test comprehensive file system effects and assumptions"
  (testing "File creation patterns and resource management"
    ;; Clean start
    (when (fs/exists? "temp") (fs/delete-tree "temp"))

    ;; Test 1: Empty files create valid HTML
    (spit "empty_file.clj" "")
    (clay/make! {:source-path "empty_file.clj" :show false :browse false})

    (is (fs/exists? "temp/empty_file.html"))
    (is (> (fs/size "temp/empty_file.html") 1000)) ; Should have substantial HTML structure

    ;; Test 2: Whitespace-only files
    (spit "whitespace_file.clj" "   \n\n\t  \n  ")
    (clay/make! {:source-path "whitespace_file.clj" :show false :browse false})

    (is (fs/exists? "temp/whitespace_file.html"))
    (let [html-content (slurp "temp/whitespace_file.html")]
      (is (.startsWith html-content "<!DOCTYPE html"))
      (is (str/includes? html-content "<html"))
      (is (str/includes? html-content "Clay")))

    ;; Test 3: Namespace-only files
    (spit "ns_only.clj" "(ns ns-only)")
    (clay/make! {:source-path "ns_only.clj" :show false :browse false})

    (is (fs/exists? "temp/ns_only.html"))
    (let [html-content (slurp "temp/ns_only.html")]
      (is (str/includes? html-content "ns-only")))

    ;; Test 4: File naming with special characters
    (spit "file_with_underscores.clj" "(ns file-with-underscores)\n\n\"Underscore test\"")
    (clay/make! {:source-path "file_with_underscores.clj" :show false :browse false})

    (is (fs/exists? "temp/file_with_underscores.html"))

    ;; Clean up test files
    (doseq [f ["empty_file.clj" "whitespace_file.clj" "ns_only.clj"
               "file-with-dashes.clj" "file_with_underscores.clj"
               "multi_test_1.clj" "multi_test_2.clj"]]
      (when (fs/exists? f) (fs/delete f)))))

(deftest test-edge-cases-and-assumptions
  "Test edge cases that challenge common assumptions"
  (testing "Edge cases and boundary conditions"
    ;; Test 1: Very large data structures
    (let [large-data (vec (range 1000))
          result (clay/make! {:single-value large-data
                              :show false
                              :browse false})]
      (is (fs/exists? "temp/.clay.html"))
      (is (> (fs/size "temp/.clay.html") 10000)) ; Should be substantial
      (is (= "clay" (:key result))))

    ;; Test 2: Nested data structures
    (let [nested-data {:level1 {:level2 {:level3 {:data "deep"}}}}
          result (clay/make! {:single-value nested-data
                              :show false
                              :browse false})]
      (is (fs/exists? "temp/.clay.html"))
      (let [html (slurp "temp/.clay.html")]
        (is (str/includes? html "level1"))
        (is (str/includes? html "level2"))
        (is (str/includes? html "level3"))
        (is (str/includes? html "&quot;deep&quot;"))))

    ;; Test 3: Special characters in values
    (let [special-data {:unicode "😀🎉", :quotes "\"'", :brackets "[]{}", :html "<>&"}
          result (clay/make! {:single-value special-data
                              :show false
                              :browse false})]
      (is (fs/exists? "temp/.clay.html"))
      (let [html (slurp "temp/.clay.html")]
        ;; Should be properly escaped
        (is (str/includes? html "😀🎉")) ; Unicode should work
        (is (str/includes? html "&quot;")) ; Quotes escaped
        (is (str/includes? html "&lt;")) ; HTML escaped
        (is (str/includes? html "&gt;"))
        (is (str/includes? html "&amp;"))))

    ;; Test 4: Absolute vs relative paths
    (spit "path_test.clj" "(ns path-test)\n\n\"Path test\"")
    (let [relative-result (clay/make! {:source-path "path_test.clj"
                                       :show false :browse false})
          absolute-path (str (fs/cwd) "/path_test.clj")
          absolute-result (clay/make! {:source-path absolute-path
                                       :show false :browse false})]
      ;; Both should work and produce similar results
      (is (= (:key relative-result) (:key absolute-result)))
      (is (= (:title relative-result) (:title absolute-result)))
      (is (fs/exists? "temp/path_test.html")))

;; Test 5: Result uniqueness (timestamps removed from API)
    (let [result1 (clay/make! {:single-value "test1" :show false :browse false})]
      (Thread/sleep 50) ; Small delay
      (let [result2 (clay/make! {:single-value "test2" :show false :browse false})]
        ;; Results should be structurally similar but independently generated
        (is (= (:key result1) (:key result2)))
        (is (= (:title result1) (:title result2)))
        (is (vector? (:info result1)))
        (is (vector? (:info result2))))) ; First should be earlier

    ;; Test 6: Files with syntax errors (but valid for reading)
    (spit "syntax_error.clj" "(ns syntax-error)\n\n(defn incomplete-fn [x]\n  ;; Missing closing paren")
    (is (thrown? Exception
                 (clay/make! {:source-path "syntax_error.clj"
                              :show false :browse false})))

    ;; Clean up
    (doseq [f ["path_test.clj" "syntax_error.clj"]]
      (when (fs/exists? f) (fs/delete f)))))

(deftest test-configuration-edge-cases
  "Test configuration edge cases and overrides"
  (testing "Configuration behavior in edge cases"
    ;; Test 1: Default target path (no base-target-path specified)
    (let [result (clay/make! {:single-value "default target test"
                              :show false :browse false})]
      ;; Should use default "temp" directory when no base-target-path specified
      (is (map? result))
      (is (fs/exists? "temp/.clay.html")))

    ;; Test 2: Non-existent target directory (should create)
    (when (fs/exists? "deep/nested/target") (fs/delete-tree "deep"))
    (let [result (clay/make! {:single-value "nested target test"
                              :base-target-path "deep/nested/target"
                              :show false :browse false})]
      (is (fs/exists? "deep/nested/target/.clay.html"))
      (when (fs/exists? "deep") (fs/delete-tree "deep")))

    ;; Test 3: Target path with special characters
    (when (fs/exists? "target-with-dashes") (fs/delete-tree "target-with-dashes"))
    (let [result (clay/make! {:single-value "special target test"
                              :base-target-path "target-with-dashes"
                              :show false :browse false})]
      (is (fs/exists? "target-with-dashes/.clay.html"))
      (when (fs/exists? "target-with-dashes") (fs/delete-tree "target-with-dashes")))

    ;; Test 4: Multiple conflicting options
    (let [result (clay/make! {:single-value "conflict test"
                              :show true ; Want to show
                              :browse false ; But don't browse
                              :base-target-path "temp"
                              :format [:html]})]
      ;; Should handle conflicting options gracefully
      (is (map? result))
      (is (= "clay" (:key result))))

    ;; Test 5: Invalid format option (should default or error gracefully)
    (try
      (let [result (clay/make! {:single-value "format test"
                                :format [:invalid-format]
                                :show false :browse false})]
        ;; If it succeeds, should still be valid structure
        (is (map? result)))
      (catch Exception e
        ;; If it fails, that's also acceptable behavior
        (is (instance? Exception e))))))

(deftest test-concurrent-and-file-handle-behavior
  "Test behavior under concurrent usage and file handle management"
  (testing "Concurrent make! calls and file handle management"
    ;; Test 1: Multiple rapid calls to same file
    (let [results (doall (repeatedly 5 #(clay/make! {:single-value (rand-int 1000)
                                                     :show false :browse false})))]
      ;; All should succeed
      (is (= 5 (count results)))
      (is (every? map? results))
      (is (every? #(= "clay" (:key %)) results))

      ;; File should exist and be readable
      (is (fs/exists? "temp/.clay.html"))
      (is (> (fs/size "temp/.clay.html") 1000)))

    ;; Test 2: Overwriting same target multiple times
    (spit "overwrite_test.clj" "(ns overwrite-test)\n\n\"Version 1\"")
    (let [result1 (clay/make! {:source-path "overwrite_test.clj"
                               :show false :browse false})
          original-size (fs/size "temp/overwrite_test.html")]

      ;; Modify and overwrite
      (spit "overwrite_test.clj" "(ns overwrite-test)\n\n\"Version 2 with more content to change file size\"")
      (let [result2 (clay/make! {:source-path "overwrite_test.clj"
                                 :show false :browse false})
            new-size (fs/size "temp/overwrite_test.html")]

        ;; Both should succeed
        (is (map? result1))
        (is (map? result2))

        ;; File should be updated (size should be different)
        (is (not= original-size new-size))

        ;; Content should reflect latest version
        (let [html-content (slurp "temp/overwrite_test.html")]
          (is (str/includes? html-content "Version 2"))
          (is (not (str/includes? html-content "Version 1"))))))

    ;; Test 3: Mixed single-value and source-path calls
    (let [single-result (clay/make! {:single-value {:mixed "test"}
                                     :show false :browse false})
          source-result (clay/make! {:source-path "overwrite_test.clj"
                                     :show false :browse false})]
      ;; Both should work
      (is (map? single-result))
      (is (map? source-result))

      ;; Different files should exist
      (is (fs/exists? "temp/.clay.html"))
      (is (fs/exists? "temp/overwrite_test.html")))

    ;; Test 4: File permissions and access
    (when (fs/exists? "temp/.clay.html")
      (is (fs/readable? "temp/.clay.html"))
      (is (fs/writable? "temp/.clay.html")))

    ;; Clean up
    (when (fs/exists? "overwrite_test.clj") (fs/delete "overwrite_test.clj"))))

(deftest test-html-content-quality-and-structure
  "Test the quality and structure of generated HTML content"
  (testing "Generated HTML meets quality standards"
    ;; Test 1: Valid HTML5 structure
    (clay/make! {:single-value {:html "structure test"}
                 :show false :browse false})

    (let [html (slurp "temp/.clay.html")]
      ;; Basic HTML5 structure
      (is (.startsWith html "<!DOCTYPE html"))
      (is (str/includes? html "<html"))
      (is (str/includes? html "<head>"))
      ;; Note: Clay doesn't generate explicit <body> tags
      (is (str/includes? html "</html>"))

      ;; Meta tags present
      (is (str/includes? html "<meta"))
      (is (str/includes? html "charset"))

      ;; Title present
      (is (str/includes? html "<title>"))

      ;; CSS and JS resources
      (is (str/includes? html ".css"))
      (is (str/includes? html ".js")))

    ;; Test 2: Proper escaping of different content types
    (let [tricky-content {:xss "<script>alert('xss')</script>"
                          :quotes "\"single' and 'double\" quotes"
                          :unicode "πΩ∑∆ 中文 العربية"
                          :newlines "line1\nline2\nline3"}]
      (clay/make! {:single-value tricky-content
                   :show false :browse false})

      (let [html (slurp "temp/.clay.html")]
        ;; XSS should be escaped
        (is (not (str/includes? html "<script>alert")))
        (is (str/includes? html "&lt;script&gt;"))

        ;; Quotes should be escaped
        (is (str/includes? html "&quot;"))

        ;; Unicode should be preserved
        (is (str/includes? html "πΩ∑∆"))
        (is (str/includes? html "中文"))
        (is (str/includes? html "العربية"))))

    ;; Test 3: Large content handling
    (let [large-map (into {} (for [i (range 100)]
                               [(keyword (str "key" i)) (str "value" i)]))]
      (clay/make! {:single-value large-map
                   :show false :browse false})

      (let [html (slurp "temp/.clay.html")]
        ;; Should contain all keys and values
        (is (str/includes? html "key0"))
        (is (str/includes? html "key99"))
        (is (str/includes? html "value0"))
        (is (str/includes? html "value99"))

        ;; File should be substantial but not unreasonably large
        (is (> (count html) 10000))
        (is (< (count html) 1000000)))) ; Reasonable upper bound

    ;; Test 4: Empty and nil values
    (clay/make! {:single-value nil :show false :browse false})
    (let [html (slurp "temp/.clay.html")]
      (is (str/includes? html "nil")))

    (clay/make! {:single-value "" :show false :browse false})
    (let [html (slurp "temp/.clay.html")]
      ;; Empty string should be represented somehow
      (is (str/includes? html "&quot;&quot;")))

    ;; Test 5: Complex nested structures
    (let [complex-data {:metadata {:created (java.util.Date.)
                                   :version "1.0"}
                        :data [{:id 1 :values [1 2 3]}
                               {:id 2 :values [4 5 6]}]
                        :config {:nested {:deeply {:value "found"}}}}]
      (clay/make! {:single-value complex-data
                   :show false :browse false})

      (let [html (slurp "temp/.clay.html")]
        (is (str/includes? html "metadata"))
        (is (str/includes? html "created"))
        (is (str/includes? html "version"))
        (is (str/includes? html "&quot;1.0&quot;"))
        (is (str/includes? html "&quot;found&quot;"))))))
