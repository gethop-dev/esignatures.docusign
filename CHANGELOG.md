# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/).

## [Unreleased]

## [0.1.5] - 2022-05-23
## Changed
- Moving the repository to [gethop-dev](https://github.com/gethop-dev) organization
- CI/CD solution switch from [TravisCI](https://travis-ci.org/) to [GitHub Actions](Ihttps://github.com/features/actions)
- `eastwood` dependency bump
- update this changelog's releases tags links

### Added
- Source code linting using [clj-kondo](https://github.com/clj-kondo/clj-kondo)

## [0.1.4] - 2021-11-24
## Changed
- When creating a new envelope, use the signer `id` provided in the
  `envelope` for specifying the `recipientId` instead of creating a
  new incremental integer id.

## [0.1.3] - 2021-11-23

## Fixed
- Fix function for validating the webhook signatures. A step was
  missing so the result of the check was always 'false'.

## Added
- Add 'valid-headers?' helper function for validating all webhook
  signatures at one step. The function will return 'true' if at least
  one matching signature is found.

## [0.1.2] - 2021-11-19

## Added
- `delete-envelope` method for deleting Envelopes.

## Breaking change/fix
- Renamed the `:coop.magnet.esignature/docusign` Integrant key to
  `:coop.magnet.esignatures/docusign` (note the plural of `esignature`
  to match the namespace name.

## Fixes
- Fix exception when invalid Docusign base-url is specified.

## [0.1.1] - 2021-10-26

### Changed
- Fix project to use verified Clojars artifact group name.
- Fix returning keyword name from `:id` to `:url` for `get-envelope-signing-url` method.

## [0.1.0] - 2021-10-26

### Added
- Initial version of the library.


[Unreleased]: https://github.com/gethop-dev/esignatures.docusign/compare/0.1.5...HEAD
[0.1.5]: https://github.com/gethop-dev/esignatures.docusign/releases/tag/v0.1.5
[0.1.4]: https://github.com/gethop-dev/esignatures.docusign/releases/tag/v0.1.4
[0.1.3]: https://github.com/gethop-dev/esignatures.docusign/releases/tag/v0.1.3
[0.1.2]: https://github.com/gethop-dev/esignatures.docusign/releases/tag/v0.1.2
[0.1.1]: https://github.com/gethop-dev/esignatures.docusign/releases/tag/v0.1.1
[0.1.0]: https://github.com/gethop-dev/esignatures.docusign/releases/tag/v0.1.0
