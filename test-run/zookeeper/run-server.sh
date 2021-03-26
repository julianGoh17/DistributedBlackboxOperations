#!/bin/bash
set -e

PROJECT_NAME="dbo"
SERVER_IMAGE="${PROJECT_NAME}-server"
IMAGE_TAG="latest"
CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
BASE_DIR="$CWD/../.."
SETTINGS_FOLDER="${CWD}/settings"
SERVER_CONFIGURATION_LOCATION="settings/server-list.txt"
JAR_FOLDER="${BASE_DIR}/zookeeper/target"
JAR_NAME="zookeeper-1.0-SNAPSHOT-jar-with-dependencies.jar"
PACKAGE_NAME="io.julian.ZookeeperAlgorithm"
HOST="localhost"
PORT="8888"

echo "Set targeted JAR folder to $JAR_FOLDER"
echo "Set targeted JAR name to $JAR_NAME"
echo "Setting package name to $PACKAGE_NAME"
echo "Setting server host to $HOST"
echo "Setting server port to $PORT"

docker run \
    --network="host" \
    -v "$JAR_FOLDER":/resources/jar \
    -v "$SETTINGS_FOLDER":/settings \
    -e JAR_FILE_PATH="/resources/jar/$JAR_NAME" \
    -e SERVER_HOST="$HOST" \
    -e SERVER_PORT="$PORT" \
    -e SERVER_CONFIGURATION_LOCATION="${SERVER_CONFIGURATION_LOCATION}" \
    -e PACKAGE_NAME="$PACKAGE_NAME" \
    -e DOES_PROCESS_REQUEST="false" \
    "${SERVER_IMAGE}:${IMAGE_TAG}"