#! /bin/sh
# Runs inside a Docker container

# Deploy the jar to Nexus and skip tests
mvn deploy -DskipTests -X -U -s ci/settings.xml
