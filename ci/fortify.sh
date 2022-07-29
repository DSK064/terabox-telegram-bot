#!/bin/sh -x

PATH=/opt/Fortify/Fortify_SCA_and_Apps_20.1.0/bin/:$PATH
JOB=`echo "$JOB_NAME" | cut -d "/" -f 1`

echo we use $JOB

if [[ "$BRANCH_NAME" == "rc"* ]] || [[ "$BRANCH_NAME" == "RC"* ]];then
 app=lcg.lcgomnia."$JOB"_master
 echo $app
#elif [[ "$BRANCH_NAME" == *"master"* ]];then
#  app=lcg.lcgomnia."$JOB"_master
#  echo $app
else
 app=lcg.lcgomnia."$JOB"_develop
 echo $app
fi


token=c1bd1578-96c9-4cc8-bc1e-549035c4a29a
#sourceanalyzer -Xmx14745M -Xms400M -Xss24M -b $JOB -clean
#sourceanalyzer -Xmx14745M -Xms400M -Xss24M -b $JOB_NAME -exclude ci/*.sh -exclude **/src/test/**/*.java -exclude **/*.xml -exclude **/*.json -exclude **/*.txt
#sourceanalyzer -Xmx14745M -Xms400M -Xss24M -b $JOB  -source 1.8 mvn -f ./pom.xml com.fortify.sca.plugins.maven:sca-maven-plugin:translate -DskipTests -s ci/omnia-settings.xml -Dhttp.nonProxyHosts="artifactory.bwinparty.corp" -DproxySet=true -DproxyHost=infrastructure-proxy -DproxyPort=3128
#sourceanalyzer -Xmx14745M -Xms400M -Xss24M -b $JOB -scan -f $JOB-$BRANCH_NAME.fpr
#fortifyclient uploadFPR -url https://fortify.bwin.com/ssc/ -f $JOB-$BRANCH_NAME.fpr  -project $app -version $app -authtoken $token

#token=e3e441c4-dccd-40d3-aa4b-25767b48b2b9
sh /opt/Fortify/Fortify_SCA_and_Apps_20.1.0/bin/fortifyclient uploadFPR -url https://fortify.bwin.com/ssc/ -f ./$JOB_NAME.fpr  -project $app -version $app -authtoken $token    