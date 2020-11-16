#!/bin/sh
set -e

WORKDIR="$(pwd)"
PROJECT_NAME="dbo"
CLIENT_IMAGE="${PROJECT_NAME}-client"
IMAGE_TAG="latest"
CONTAINER_REPORT_PATH="/resources/report"
REPORT_FOLDER="${WORKDIR}/generated/report"

docker run -it \
    --network="host" \
    -v "$REPORT_FOLDER":${CONTAINER_REPORT_PATH} \
    "${CLIENT_IMAGE}:${IMAGE_TAG}"