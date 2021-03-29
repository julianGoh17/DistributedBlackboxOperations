#!/bin/sh
set -xe
PROJECT_NAME="dbo"
BASE_IMAGE="${PROJECT_NAME}-base"
CLIENT_IMAGE="${PROJECT_NAME}-client"
SERVER_IMAGE="${PROJECT_NAME}-server"
IMAGE_TAG="latest"

CLIENT_JAR_NAME="client-1.0-SNAPSHOT-jar-with-dependencies.jar"
CLIENT_GENERATED_FOLDER="client/src/main/resources/generated"

SERVER_JAR_NAME="server-1.0-SNAPSHOT-jar-with-dependencies.jar"
SERVER_OPENAPI_YAML="server/src/main/resources/ServerEndpoints.yaml"
STANDARD_LOG4J2_FILE="logger/standard-log4j2.xml"
FILE_LOG4J2_FILE="logger/file-out-log4j2.xml"
mvn clean package -DskipTests

docker build . -t "${BASE_IMAGE}:${IMAGE_TAG}" -f dockerfiles/Dockerfile.base
docker build . -t "${CLIENT_IMAGE}:${IMAGE_TAG}" \
    --build-arg BASE_IMAGE="${BASE_IMAGE}"  \
    --build-arg JAR_NAME="${CLIENT_JAR_NAME}" \
    --build-arg CLIENT_GENERATED_FOLDER="${CLIENT_GENERATED_FOLDER}" \
    --build-arg LOG4J2_FILE="${FILE_LOG4J2_FILE}" \
    -f dockerfiles/Dockerfile.client

docker build . -t "${SERVER_IMAGE}:${IMAGE_TAG}" \
    --build-arg BASE_IMAGE="${BASE_IMAGE}"  \
    --build-arg JAR_NAME="${SERVER_JAR_NAME}" \
    --build-arg SERVER_OPENAPI_YAML="${SERVER_OPENAPI_YAML}" \
    --build-arg LOG4J2_FILE="${STANDARD_LOG4J2_FILE}" \
    -f dockerfiles/Dockerfile.server
