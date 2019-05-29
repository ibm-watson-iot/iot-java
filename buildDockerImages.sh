#!/bin/bash

IMAGE_NAME=wiotp/oshi
IMAGE_SRC=samples/oshi

docker build -t ${IMAGE_NAME}:$TRAVIS_BRANCH ${IMAGE_SRC}
if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
	if [ "$TRAVIS_BRANCH" == "master" ]; then
		docker tag ${IMAGE_NAME}:$TRAVIS_BRANCH ${IMAGE_NAME}:latest
		docker push ${IMAGE_NAME}:latest
	else
		docker push ${IMAGE_NAME}:$TRAVIS_BRANCH
	fi
fi
