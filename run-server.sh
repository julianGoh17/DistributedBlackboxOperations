#!/bin/sh
set -e

PROJECT_NAME="dbo"
SERVER_IMAGE="${PROJECT_NAME}-server"
IMAGE_TAG="latest"
CWD="$(pwd)"
DEFAULT_JAR_FOLDER="${CWD}/test/target"
DEFAULT_JAR_FILE="test-1.0-SNAPSHOT-jar-with-dependencies.jar"
DEFAULT_PACKAGE_NAME="io.julian.ExampleDistributedAlgorithm"
DEFAULT_HOST="localhost"
DEFAULT_PORT="8888"

echo "Usage: use this script to start up a server after running ./build.sh to build the necessary docker image"
echo "    Arg 1: the folder that contains the JAR of the distributed algorithm you want to load into server  (default: ${DEFAULT_JAR_FOLDER})"
echo "    Arg 2: the name of the JAR of the distributed algorithm (default: ${DEFAULT_JAR_FILE})"
echo "    Arg 3: the name of the package of the distributed algorithm (default: ${DEFAULT_PACKAGE_NAME})"
echo "    Arg 3: the host of the server you want the server to start up on (default: ${DEFAULT_HOST})"
echo "    Arg 4: the port of the server you want the server to start up on (default: ${DEFAULT_PORT})"

JAR_FOLDER="${1:-${DEFAULT_JAR_FOLDER}}"
JAR_NAME="${2:-${DEFAULT_JAR_FILE}}"
PACKAGE_NAME="${3:-${DEFAULT_PACKAGE_NAME}}"
HOST="${4:-${DEFAULT_HOST}}"
PORT="${5:-${DEFAULT_PORT}}"

echo "Set targeted JAR folder to $JAR_FOLDER"
echo "Set targeted JAR name to $JAR_NAME"
echo "Setting package name to $PACKAGE_NAME"
echo "Setting server host to $HOST"
echo "Setting server port to $PORT"

docker run \
    --network="host" \
    -v "$JAR_FOLDER":/resources/jar \
    -e JAR_FILE_PATH="/resources/jar/$JAR_NAME" \
    -e SERVER_HOST="$HOST" \
    -e SERVER_PORT="$PORT" \
    -e PACKAGE_NAME="$PACKAGE_NAME" \
    "${SERVER_IMAGE}:${IMAGE_TAG}"