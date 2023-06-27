#! /bin/sh
mvn -version
mvn -Djava.io.tmpdir=/data/jenkins/tmp/builds clean verify -U -s ci/gbs-settings.xml -Dhttp.nonProxyHosts="artifactory.bwinparty.corp|*.ladbrokescoral.com|bitbucket.org" -DproxySet=true -DproxyHost=infrastructure-proxy.gib1.egalacoral.com -DproxyPort=3128

if [ "$codeQuality" == "true" ];then
    echo "Running sonar scan as user selected"
    mkdir test-results
    for s in `find . -iname 'TEST-*.xml'`
    do
       cp $s test-results
    done
    sonar-scanner -Dsonar.projectKey=`echo $JOB_NAME | cut -d "/" -f 1` -Dsonar.language=java -Dsonar.java.binaries=. -Dsonar.exclusions=**/*.xml,**/src/test/**,**/*.sh,**/*.py -Dsonar.test.exclusions=**/*.xml,**/src/test/** -Dsonar.sources=. -Dsonar.projectBaseDir=. -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml -Dsonar.junit.reportPaths=test-results
else
    echo "Sonar scan is not running as user not selected"
fi
