# lein-changelog
[![Build Status](https://travis-ci.org/dryewo/lein-changelog.svg?branch=master)](https://travis-ci.org/dryewo/lein-changelog)
[![codecov](https://codecov.io/gh/dryewo/lein-changelog/branch/master/graph/badge.svg)](https://codecov.io/gh/dryewo/lein-changelog)
[![Clojars Project](https://img.shields.io/clojars/v/lein-changelog/lein-changelog.svg)](https://clojars.org/lein-changelog/lein-changelog)

A Leiningen plugin to automate changelog tasks.
Intended to be used as part of [automated release procedure].

## Usage

Add this into the `:plugins` vector of your _project.clj_:

    [lein-changelog "0.2.0"]

Add this:

```
  :release-tasks [... 
                  ["changelog" "release"]
                  ...]
```
To your _project.clj_. Read more about [custom release tasks].  
**IMPORTANT**: the `["changelog" "release"]` has to come after version bumping, because this plugin reads the version from _project.clj_.

For example (read more about this specific procedure [here](https://github.com/dryewo/clojure-library-template)):

```clj
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]]
```

Release normally:

    $ lein release :minor


## Explanation

When you run

    $ lein changelog release

(or when it's executed as part of `lein release`), this plugin does the following:


1. Reads contents of the _CHANGELOG.md_ file.
2. Replaces `## [Unreleased]` line with `## [X.Y.Z] - 2018-18-18`,  
   where `X.Y.Z` is the version from _project.clj_ and `2018-18-18` is today's date.  
3. Adds a new empty section with `## [Unreleased]` title on top of the file.
4. Inserts links to GitHub diff UI the end of the file:
   * `[X.Y.Z]` to show differences between this released version and previously released one.
   * `[Unreleased]` to show differences between this released version and whatever commits added on top of it but not released yet.

Example:

Given this _CHANGELOG.md_:

```
blah-blah

## [Unreleased]
new-blah

## [0.1.1] - 2018-06-01
old-blah

## 0.1.0 - 2018-01-01
initial-blah

[0.1.1]: https://github.com/your-name/lein-changelog/compare/0.1.0...0.1.1
[Unreleased]: https://github.com/your-name/lein-changelog/compare/0.1.1...HEAD
```

Replaces its contents with this:

```
blah-blah

## [Unreleased]

## [0.2.0] - 2018-18-18
new-blah

## [0.1.1] - 2018-06-01
old-blah

## 0.1.0 - 2018-01-01
initial-blah

[0.1.1]: https://github.com/your-name/lein-changelog/compare/0.1.0...0.1.1
[0.2.0]: https://github.com/your-name/lein-changelog/compare/0.1.1...0.2.0
[Unreleased]: https://github.com/your-name/lein-changelog/compare/0.2.0...HEAD
```

The only required parts here are:

1. `## [Unreleased]` line has to match exactly.
2. There has to be a line that looks like `[Unreleased]: https://github.com/your-name/lein-changelog/compare/0.2.0...HEAD`  
   It will be used to generate a diff link to the version currently being released (`X.Y.Z`) as well as to the new `[Unreleased]` diff.

If any of these lines are missing, the plugin will fail and exit with a non-zero code. 

## License

Copyright © 2018 Dmitrii Balakhonskii

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[automated release procedure]: https://github.com/technomancy/leiningen/blob/master/doc/DEPLOY.md#releasing-simplified
[custom release tasks]: https://github.com/technomancy/leiningen/blob/master/doc/DEPLOY.md#overriding-the-default-release-tasks
