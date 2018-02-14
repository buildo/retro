# toctoc

[![drone badge](https://drone.our.buildo.io:4433/api/badges/buildo/toctoc/status.svg)](https://drone.our.buildo.io:4433/buildo/toctoc)

## Usage

Refer to the [web](/web) and [backend](/backend) READMEs.

## Releasing a new verison

`web` and `backend` are two different projects with two different release methods:

* `backend` is release automatically by the CI
* `web` must be release manually as follows:
  1. `npm version X.Y.Z` (to choose a version number, check the latest release tag and follow the standard `breaking` changes guidelines)
  2. npm publish
  3. update the `package.json` with the new version, commit, and add the corresponding tag (`git tag vX.Y.Z)
  4. `git push` and `git push --tags`
