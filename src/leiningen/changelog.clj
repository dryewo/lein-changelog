(ns leiningen.changelog
  (:require [clojure.string :as str]
            [leiningen.core.main :as main])
  (:import (java.text SimpleDateFormat)
           (java.util Date)))


(def unreleased-line-pattern "## [Unreleased]")
(def unreleased-link-line-pattern #"\[Unreleased\]: .*/compare/([^/]+)...HEAD")


(defn tokenize-line [line]
  (let [line (str/trim line)]
    (if (= unreleased-line-pattern line)
      [::unreleased]
      (if-let [[[_ version]] (re-seq unreleased-link-line-pattern line)]
        [::unreleased-link version]
        nil))))


(defn process-line [acc line]
  (let [{:keys [found-unreleased-title]} acc
        [token value] (tokenize-line line)
        append-to-key (if found-unreleased-title
                        :after-unreleased
                        :before-unreleased)]
    (case token
      ::unreleased (assoc acc :found-unreleased-title line)
      ::unreleased-link (-> acc
                            (assoc :last-released-version value)
                            (assoc :unreleased-link line))
      (update acc append-to-key (fnil conj []) line))))


(defn parse-changelog [changelog-str]
  (let [lines (str/split-lines changelog-str)]
    (reduce process-line
            {}
            lines)))


(defn render-changelog [changelog-data next-version today-date]
  (let [{:keys [before-unreleased found-unreleased-title after-unreleased unreleased-link last-released-version]} changelog-data
        next-version-title  (format "## [%s] - %s" next-version today-date)
        next-version-link   (-> unreleased-link
                                (str/replace "[Unreleased]" (str "[" next-version "]"))
                                (str/replace "...HEAD" (str "..." next-version)))
        new-unreleased-link (-> unreleased-link
                                (str/replace last-released-version next-version))]
    (str/join "\n"
              (concat before-unreleased
                      [found-unreleased-title
                       ""
                       next-version-title]
                      after-unreleased
                      [next-version-link
                       new-unreleased-link
                       ""]))))


(defn release [next-version today-date changelog-str]
  (let [changelog-data (parse-changelog changelog-str)]
    (if-not (:found-unreleased-title changelog-data)
      (main/exit 1 (format "'%s' line not found, cannot figure out which section contains next version's feature descriptions." unreleased-line-pattern))
      (if-not (:last-released-version changelog-data)
        (main/exit 1 (format "'%s' line not found, cannot figure out which version was released previously and how a comparison link between it and the latest commit would look like." unreleased-link-line-pattern))
        (render-changelog changelog-data next-version today-date)))))


(def changelog-filename "CHANGELOG.md")


(defn changelog
  {:subtasks [#'release]}
  [project & [subtask]]
  (case subtask
    "release" (let [changelog-str (slurp changelog-filename)
                    today-date    (.format (SimpleDateFormat. "yyyy-MM-dd") (Date.))]
                (-> (release (:version project) today-date changelog-str)
                    (spit changelog-filename)))
    nil :not-implemented-yet
    (leiningen.core.main/warn "Unknown task.")))


(comment
  (release "0.2.0"
           "2018-18-18"
           "blah-blah\n\n## [Unreleased]\nnew-blah\n\n## [0.1.1] - 2018-06-01\nold-blah\n\n## 0.1.0 - 2018-01-01\ninitial-blah\n\n[Unreleased]: https://github.com/your-name/lein-changelog/compare/0.1.1...HEAD\n[0.1.1]: https://github.com/your-name/lein-changelog/compare/0.1.0...0.1.1\n"
           ))
