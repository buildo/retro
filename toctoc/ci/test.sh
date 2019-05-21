#! /bin/sh

set -e

source /docker-lib.sh
echo "Starting docker..."
start_docker
echo "Docker started!"

# Use images cached by concourse
docker load -i postgres/image
docker tag "$(cat postgres/image-id)" "$(cat postgres/repository):$(cat postgres/tag)"

docker load -i mysql/image
docker tag "$(cat mysql/image-id)" "$(cat mysql/repository):$(cat mysql/tag)"

docker load -i scala-sbt/image
docker tag "$(cat scala-sbt/image-id)" "$(cat scala-sbt/repository):$(cat scala-sbt/tag)"

# Run the tests container and its dependencies
export PROJECT_PATH="$PWD/backend"
export IVY_PATH="$PWD/.ivy2"
docker-compose -f backend/toctoc/ci/docker-compose.yml up -d

# Run the tests inside the tests container
docker exec -it tests bash -c 'cd /project && sbt ";toctocSlickPostgreSql/test; toctocSlickMySql/test"'

# Cleanup
docker-compose -f backend/toctoc/ci/docker-compose.yml down
docker volume rm $(docker volume ls -q)

