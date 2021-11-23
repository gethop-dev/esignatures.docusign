# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/).

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
