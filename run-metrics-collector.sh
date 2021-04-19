#!/bin/bash

WORKDIR="$(pwd)"
PROJECT_NAME="dbo"
METRICS_COLLECTOR_IMAGE="${PROJECT_NAME}-metrics-collector"
IMAGE_TAG="latest"
DEFAULT_SERVER_PORT="9090"
DEFAULT_SERVER_HOST="localhost"
DOES_USE_MESSAGE="false"
CONTAINER_REPORT_PATH="/resources/report"
REPORT_FOLDER="${WORKDIR}/generated/report"

echo "Usage: use this script to run a metrics after running ./build.sh to build the necessary docker image"
echo "    Arg 1: the port of the server to start on  (default: ${DEFAULT_SERVER_PORT})"
echo "    Arg 2: the host of the server to start on  (default: ${DEFAULT_SERVER_HOST})"

SERVER_PORT=${1:-${DEFAULT_SERVER_PORT}}
echo "Server will start on port ${SERVER_PORT}"
SERVER_HOST=${2:-${DEFAULT_SERVER_HOST}}
echo "Server is hosted at address ${SERVER_HOST}"

docker run -it \
    --network="host" \
    -v "$REPORT_FOLDER":${CONTAINER_REPORT_PATH} \
    -e SERVER_PORT="${SERVER_PORT}" \
    -e SERVER_HOST="${SERVER_HOST}" \
    -e DOES_USE_MESSAGE="$DOES_USE_MESSAGE" \
    "${METRICS_COLLECTOR_IMAGE}:${IMAGE_TAG}"