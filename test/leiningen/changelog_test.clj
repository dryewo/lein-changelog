(ns leiningen.changelog-test
  (:require [clojure.test :refer :all]
            [leiningen.changelog :refer :all :as ch])
  (:import (clojure.lang ExceptionInfo)))


(deftest about-tokenize-line
  (are [?line ?res]
    (= ?res (tokenize-line ?line))
    "nothing" nil
    "## [Unreleased]" [::ch/unreleased]
    "  ## [Unreleased]  " [::ch/unreleased]
    "[Unreleased]: foo blah blah /compare/1.2.3...HEAD" [::ch/unreleased-link "1.2.3"]
    "[Unreleased]: foo blah blah /compare/mary had a little lamb...HEAD" [::ch/unreleased-link "mary had a little lamb"]))


(deftest about-extract-owner+repo
  (are [?in ?out]
    (= ?out (extract-owner+repo ?in))
    "git@github.com/foo/bar" "foo/bar"
    "git@github.com/foo/bar.git" "foo/bar"
    "git@github.com/foobar.git" nil))


(deftest works
  (testing "Happy case"
    (= (slurp "test/test-changelog.after.md")
       (release-impl "0.2.0" "1234-56-78" (slurp "test/test-changelog.before.md"))))

  (testing "Errors"
    (are [?in ?error]
      (re-seq ?error
              (binding [leiningen.core.main/*exit-process?* false]
                (try
                  (release-impl "0.2.0" "1234-56-78" ?in)
                  ::not-thrown
                  (catch ExceptionInfo e
                    (str e)))))
      "" #"'## \[Unreleased\]' line not found"
      "## [Unreleased]" #"HEAD' line not found")))
