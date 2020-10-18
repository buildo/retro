#! /bin/sh

set -e

apk add --no-cache curl

sbt -batch ';+tapiroCore/test ;sbt-tapiro/scripted'
