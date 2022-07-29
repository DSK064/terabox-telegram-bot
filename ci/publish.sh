#! /bin/sh -ex
#
# publish.sh
# Copyright (C) 2017 Mateusz Pawlowski <mateusz@generik.co.uk>
#
# Distributed under terms of the MIT license.
#

alias errcho='>&2 echo'

project=$1
# we are adding a branch name if it's not "publish"
if [ "$BRANCH_NAME" != "publish" ];then
 repo_name="$project/`echo $BRANCH_NAME | tr [:upper:] [:lower:]`"
else
 repo_name=$project
fi

repourl="lcgomnia-docker-local.dev.docker.env.works/gbs/$repo_name"

#this is a full docker image name
tag=$repourl:$version

docker build --build-arg APP=$project -t $tag .

docker login -u ${ARTIFACTORY_USR} -p ${ARTIFACTORY_PSW} lcgomnia-docker-local.dev.docker.env.works

docker push $tag
docker rmi $tag

# communicate the container_tag back to adapter
echo $tag > container_tag


