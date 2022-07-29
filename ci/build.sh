#! /bin/sh

#mvn clean package -U -s ci/settings.xml

# Scan code with sonar
#mvn -U clean verify sonar:sonar -s ci/settings.xml \
#-Dsonar.host.url=https://sonar.retail.infra.aws.ladbrokescoral.com \
#-Dsonar.login=${SONARTOKEN}

docker login -u ${ARTIFACTORY_USR} -p ${ARTIFACTORY_PSW} lcgomnia-docker-local.dev.docker.env.works
#mvn clean verify -U -s ci/gbs-settings.xml
mvn -version
mvn -Djava.io.tmpdir=/data/jenkins/tmp/builds clean deploy -U -s ci/gbs-settings.xml -Dhttp.nonProxyHosts="artifactory.bwinparty.corp|*.ladbrokescoral.com" -DproxySet=true -DproxyHost=infrastructure-proxy -DproxyPort=3128
