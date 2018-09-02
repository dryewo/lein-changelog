(defproject lein-changelog "0.3.0"
  :description "A Leiningen plugin to automate changelog tasks."
  :url "https://github.com/dryewo/lein-changelog"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies []
  :plugins [[lein-cloverage "1.0.9"]
            [lein-shell "0.5.0"]
            [lein-changelog "0.2.1"]
            [lein-ancient "0.6.15"]]
  :eval-in-leiningen true
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.9.0"]]}}
  :deploy-repositories [["releases" :clojars]]
  :aliases {"update-readme-version" ["shell" "sed" "-i" "s/\\\\[lein-changelog \"[0-9.]*\"\\\\]/[lein-changelog \"${:version}\"]/" "README.md"]}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]])
