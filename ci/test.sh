#! /bin/sh

## We are using docker compose to build and run the unit tests/integration tests.
## This is because the way maven integration tests run on the pipeline is
## through another docker container and we require a docker container for
## DynamoDB integration testing

# Remove existing images
#docker-compose rm -f &&

# Builds the docker images required for integration tests
#docker-compose build --no-cache &&

# Runs the integration tests
#docker-compose -f docker-compose.yml -f docker-compose.test.yml up --force-recreate --remove-orphans --exit-code-from funds --abort-on-container-exit &&

# Remove the docker container
#docker-compose down -v