# lein-changelog
[![Build Status](https://travis-ci.org/dryewo/lein-changelog.svg?branch=master)](https://travis-ci.org/dryewo/lein-changelog)
[![codecov](https://codecov.io/gh/dryewo/lein-changelog/branch/master/graph/badge.svg)](https://codecov.io/gh/dryewo/lein-changelog)
[![Clojars Project](https://img.shields.io/clojars/v/lein-changelog/lein-changelog.svg)](https://clojars.org/lein-changelog/lein-changelog)

A Leiningen plugin to automate changelog tasks as part of `lein release` routine.

In support for [Keep a Changelog] initiative, relies on the changelog format proposed there.

Intended to be used as part of [automated release procedure].


## Usage

First, modify `:plugins` vector of your _project.clj_:

```clj
    :plugins [[lein-changelog "0.3.2"]]
```

Then add `["changelog" "release"]` to `:release-tasks` in your _project.clj_:

```clj
  :release-tasks [... 
                  ["changelog" "release"]
                  ...]
```

If you have no `:release-tasks` key in your _project.clj_, please read more about [custom release tasks] and add it.
Custom `:release-tasks` is necessary to automate changelog work.
  
**IMPORTANT**: the `["changelog" "release"]` has to come after version bumping (`["change" "version" ...]`), because lein-changelog reads the version
from _project.clj_.

Example `:release-tasks` (read more about this specific procedure [here](https://github.com/dryewo/clojure-library-template)):

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

### If you don't have _CHANGELOG.md_

Run this command to generate a dummy _CHANGELOG.md_ file from template:

    $ lein changelog init

Open the freshly generated _CHANGELOG.md_ file and check its contents.
You might want to correct the intro part and add some details to the the last released version section as well
(it was generated from the latest git tag).

As you work on your project, add [notable changes](https://keepachangelog.com/en/1.0.0/#how) to `## [Unreleased]`
section with every commit you make.

### If you already have _CHANGELOG.md_

If you didn't use `lein changelog init` to create it, make sure that it corresponds to the [format](#changelog-format).

When you are ready to release the next version, just run:

    $ lein release :patch
    # or
    $ lein release :minor
    # or
    $ lein release :major

If you have configured `:release-tasks` as described above,
`lein changelog release` will be called automatically to update _CHANGELOG.md_.


## Explanation

When you run

    $ lein release :<segment>

given that you have configured `:release-tasks` as described above, this plugin does the following
(after the version is bumped in _project.clj_):

1. Reads contents of the _CHANGELOG.md_ file.
2. Replaces `## [Unreleased]` line with `## [X.Y.Z] - 2018-18-18`,  
   where `X.Y.Z` is the version from _project.clj_ and `2018-18-18` is today's date.  
3. Adds a new empty section with `## [Unreleased]` title on top of the file.
4. Inserts links to GitHub diff UI to the end of the file:
   * `[X.Y.Z]` to show differences between this released version and previously released one.
   * `[Unreleased]` to show new changes since the latest released version.

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

[0.1.1]: https://github.com/your-name/your-repo/compare/0.1.0...0.1.1
[Unreleased]: https://github.com/your-name/your-repo/compare/0.1.1...HEAD
```

After running `lein release :minor`,
lein-changelog updates _CHANGELOG.md_ like this (new version being released is `0.2.0`):

```
blah-blah

## [Unreleased]

## [0.2.0] - 2018-18-18
new-blah

## [0.1.1] - 2018-06-01
old-blah

## 0.1.0 - 2018-01-01
initial-blah

[0.1.1]: https://github.com/your-name/your-repo/compare/0.1.0...0.1.1
[0.2.0]: https://github.com/your-name/your-repo/compare/0.1.1...0.2.0
[Unreleased]: https://github.com/your-name/your-repo/compare/0.2.0...HEAD
```

### Changelog format

lein-changelog relies on the format described on [Keep a Changelog], but the only required parts here are:

1. The file has to be named "CHANGELOG.md" and located in the root of the repository.
2. `## [Unreleased]` line has be present in _CHANGELOG.md_ exactly.
3. There has to be a line that looks like `[Unreleased]: https://github.com/your-name/your-repo/compare/A.B.C...HEAD`  
   It will be copied and updated to create a diff link to the version currently being released (`X.Y.Z`) as well as
   to the new `[Unreleased]` diff.

If any of these lines are missing, the plugin will fail and exit with a non-zero code. 


## License

Copyright © 2018 Dmitrii Balakhonskii

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[automated release procedure]: https://github.com/technomancy/leiningen/blob/master/doc/DEPLOY.md#releasing-simplified
[custom release tasks]: https://github.com/technomancy/leiningen/blob/master/doc/DEPLOY.md#overriding-the-default-release-tasks
[Keep a Changelog]: https://keepachangelog.com
