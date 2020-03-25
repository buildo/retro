#! /bin/sh

set -e

apk add --no-cache curl

sbt -batch ';tapiroCore/compile ;sbt-tapiro/scripted' # TODO(claudio): compile to test once we have them
