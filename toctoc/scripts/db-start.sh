#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

docker-compose -f ../ci/docker-compose.yml up -d mysql-db postgres-db
