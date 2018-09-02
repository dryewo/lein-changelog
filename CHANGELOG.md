# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).


## [Unreleased]

## [0.3.1] — 2018-09-03
### Changed
- _CHANGELOG.md_ template: `lein changelog init` now generates _CHANGELOG.md_ with latest released version in place.
- Use long dash in section titles.

## [0.3.0] — 2018-09-03
### Added
- New subcommand: `lein changelog init` to create _CHANGELOG.md_ file.
### Fixed
- Exception was thrown by `lein changelog release` when _CHANGELOG.md_ didn't exist.

## [0.2.2] — 2018-08-26
### Fixed
- Added proper changelog :)

## 0.2.0 — 2018-08-26
### Added
- Implemented `release` subtask.


[0.2.2]: https://github.com/dryewo/lein-changelog/compare/0.2.0...0.2.2
[0.3.0]: https://github.com/dryewo/lein-changelog/compare/0.2.2...0.3.0
[0.3.1]: https://github.com/dryewo/lein-changelog/compare/0.3.0...0.3.1
[Unreleased]: https://github.com/dryewo/lein-changelog/compare/0.3.1...HEAD
