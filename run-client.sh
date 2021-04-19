#!/bin/sh
set -e

WORKDIR="$(pwd)"
PROJECT_NAME="dbo"
CLIENT_IMAGE="${PROJECT_NAME}-client"
IMAGE_TAG="latest"
CONTAINER_REPORT_PATH="/resources/report"
REPORT_FOLDER="${WORKDIR}/generated/report"
CONTAINER_LOGS_FOLDER="/resources/logs"
LOGS_FOLDER="${WORKDIR}/generated/logs"
SERVERS_PATH="/resources/servers"
SERVERS_FOLDER="${WORKDIR}/test-run/settings"

DEFAULT_SERVER_PORT="8888"

echo "Usage: use this script to run a client after running ./build.sh to build the necessary docker image"
echo "    Arg 1: the port of the server you want to connect to  (default: ${DEFAULT_SERVER_PORT})"

SERVER_PORT=${1:-${DEFAULT_SERVER_PORT}}
echo "Client will connect to server on port ${SERVER_PORT}"

docker run -it \
    --network="host" \
    -v "$REPORT_FOLDER":${CONTAINER_REPORT_PATH} \
    -v "$LOGS_FOLDER":${CONTAINER_LOGS_FOLDER} \
    -v "$SERVERS_FOLDER":${SERVERS_PATH} \
    --env SERVER_HOSTS_FILE_PATH="$SERVERS_PATH/server-list.txt" \
    --env SERVER_PORT="${SERVER_PORT}" \
    "${CLIENT_IMAGE}:${IMAGE_TAG}"