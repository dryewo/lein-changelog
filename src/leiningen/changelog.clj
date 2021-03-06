(ns leiningen.changelog
  (:require [clojure.string :as str]
            [leiningen.core.main :as main]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh])
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
        next-version-title  (format "## [%s] — %s" next-version today-date)
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


(defn release-impl [next-version today-date changelog-str]
  (let [changelog-data (parse-changelog changelog-str)]
    (if-not (:found-unreleased-title changelog-data)
      (main/exit 1 (format "'%s' line not found, cannot figure out which section contains next version's feature descriptions." unreleased-line-pattern))
      (if-not (:last-released-version changelog-data)
        (main/exit 1 (format "'%s' line not found, cannot figure out which version was released previously and how a comparison link between it and the latest commit would look like." unreleased-link-line-pattern))
        (render-changelog changelog-data next-version today-date)))))


(def changelog-filename "CHANGELOG.md")


(defn prompt-overwrite []
  (if-not (.exists (io/file changelog-filename))
    true
    (do
      (print (str changelog-filename " file found. Do you want to overwrite it? [y/N] "))
      (flush)
      (let [response (str/trim (read-line))]
        (#{"y" "Y"} response)))))


(def git-repo-regex #"github.com(?:/|:)([^/]+/[^/]+)")


(defn extract-owner+repo [git-remote-url]
  (let [trimmed   (str/trim git-remote-url)
        truncated (str/replace trimmed #".git$" "")
        [[_ owner+repo]] (re-seq git-repo-regex truncated)]
    (when-not owner+repo
      (main/info "Cannot figure out owner and repo from remote URL:" trimmed))
    owner+repo))


(defn get-owner+repo []
  (let [{:keys [exit out]} (sh/sh "git" "remote" "get-url" "origin")]
    (if (zero? exit)
      (extract-owner+repo out)
      (main/info "Git repository not found in the current directory."))))


(defn generate-changelog-str [template-str owner+repo last-version last-version-date]
  (when-not owner+repo
    (main/info "Using \"OWNER/REPO\" in the generated changelog file. You should replace it later."))
  (-> template-str
      (str/replace "{{owner+repo}}" (or owner+repo "OWNER/REPO"))
      (str/replace "{{last-version}}" last-version)
      (str/replace "{{last-version-date}}" last-version-date)))


(defn get-today-date []
  (.format (SimpleDateFormat. "yyyy-MM-dd") (Date.)))


(defn get-latest-tag []
  (let [{:keys [exit out err]} (sh/sh "git" "describe" "--tags" "--abbrev=0")]
    (if (zero? exit)
      (str/trim out)
      (main/info "No tags found in current repo:" err))))


(defn get-tag-date [tag]
  (let [{:keys [exit out err]} (sh/sh "git" "--no-pager" "log" "-1" "--format=%ad" "--date=short" tag)]
    (if (zero? exit)
      (str/trim out)
      (main/info "Cannot get tag date:" err))))


(defn init [{:keys [version]}]
  (if-not (prompt-overwrite)
    (main/exit 1)
    (let [template-str    (slurp (io/resource "templates/CHANGELOG.md"))
          latest-tag      (get-latest-tag)
          latest-tag-date (when latest-tag
                            (get-tag-date latest-tag))
          today-date      (get-today-date)
          owner+repo      (get-owner+repo)
          changelog-str   (generate-changelog-str template-str owner+repo (or latest-tag version) (or latest-tag-date today-date))]
      (main/info "Wrote" changelog-filename)
      (spit changelog-filename changelog-str))))


(defn release [{:keys [version]}]
  (if-not (.exists (io/file changelog-filename))
    (main/warn changelog-filename "not found, use `lein changelog init` to create one.")
    (let [changelog-str (slurp changelog-filename)
          today-date    (get-today-date)]
      (->> (release-impl version today-date changelog-str)
           (spit changelog-filename)))))


(defn changelog
  {:subtasks [#'init #'release]}
  [project & [subtask]]
  (case subtask
    "init" (init project)
    "release" (release project)
    nil :not-implemented-yet
    (leiningen.core.main/warn "Unknown task.")))


(comment
  (release-impl "0.2.0"
                "2018-18-18"
                (slurp "test/test-changelog.before.md")
                ))
