# This Dockerfile makes the pre-built application in to a container for deployment.
#
# Note: The application should have already been built before using this Dockerfile,
# and its jar file should already be available in the target directory.

#FROM openjdk:11-oracle
#RUN mkdir /app
#WORKDIR /app

#ARG APP=retail-configuration-service
#ARG VERSION=0.0.1
#ADD target/${APP}-${VERSION}-SNAPSHOT-allinone.jar app.jar
#ADD target/${APP}-${VERSION}-SNAPSHOT.jar app.jar
#COPY scripts /app/scripts
#ENV JAVA_OPTS "-Djava.security.egd=file:/dev/./urandom"
#ENV JAVA_XMS "256m"
# Default
#ENV JAVA_XMX "2g"
#CMD java -Xms$JAVA_XMS -Xmx$JAVA_XMX $JAVA_OPTS -jar app.jar

FROM lcgomnia-docker-local.dev.docker.env.works/base-images/maven-service-appd
ARG APP=example
ARG VERSION=0.0.4
ADD target/${APP}-${VERSION}-SNAPSHOT.jar  app.jar
ENV NEW_RELIC_APP_NAME LC-CRE-DO-EXAMPLE-DEV