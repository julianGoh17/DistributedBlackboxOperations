#!/bin/sh
set -xe
BASE_IMAGE="dbo-base"
CLIENT_IMAGE="dbo-client"
IMAGE_TAG="version-1"

#mvn clean package -DskipTests

docker build . -t "${BASE_IMAGE}:${IMAGE_TAG}" -f dockerfiles/Dockerfile.base
docker build . -t "${CLIENT_IMAGE}:${IMAGE_TAG}" -f dockerfiles/Dockerfile.client
