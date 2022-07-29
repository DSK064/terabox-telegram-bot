#!/bin/bash -ex

# default values
export region="eu-west-2"
export git_version="`git describe --tags || echo 0`"
export version="${git_version}-`date +%Y%m%d.%H%M%S`-$GIT_COMMIT"

# read manifest.json
manifest="$WORKSPACE/manifest.json"

#check if jq is available
if ! which jq ; then echo missing jq ; exit 2 ; fi

#login to docker repo
#`aws ecr get-login --no-include-email --region $region`



# get names of publishable apps out
for publishable in `jq -r '.deploy[].name' $manifest` ; do
  ./ci/publish.sh $publishable
  # container tags gets written by publish.sh above
  container="`cat container_tag`"
  ci/update-manifest.py $manifest $publishable $container
done

curl -u ${ARTIFACTORY_USR}:${ARTIFACTORY_PSW} -X PUT "https://artifactory.bwinparty.corp/artifactory/lcggbs-rpm-local/apps-manifests/services/$JOB_NAME/$version.json" -T manifest.json



#aws s3 cp manifest.json s3://lcg-$TENANT-app-manifests/omnia/$JOB_NAME/$version.json

echo +_+_+_+_+_+_+_+_+_+_+_+_+_+_+__+_+_+_+_+_+_+_+_
echo Version=$version
echo $version > param_ver.txt
basename -s .git `git config --get remote.origin.url`  > repo_name.txt
echo Branch=$BRANCH_NAME
echo +_+_+_+_+_+_+_+_+_+_+_+_+_+_+__+_+_+_+_+_+_+_+_