#!/bin/sh
set -e

PROJECT_NAME="dbo"
SERVER_IMAGE="${PROJECT_NAME}-server"
IMAGE_TAG="latest"

DEFAULT_HOST="localhost"
DEFAULT_PORT="8888"

if [ -z "$1" ]; then
    echo "Setting server port to default ($DEFAULT_PORT)"
    SERVER_PORT="$DEFAULT_PORT"
else
    echo "Setting server port to custom port ($1)"
    SERVER_PORT="$1"
fi

if [ -z "$2" ]; then
    echo "Setting server host to default ($DEFAULT_HOST)"
    SERVER_HOST="$DEFAULT_HOST"
else
    echo "Setting server port to custom port ($2)"
    SERVER_HOST="$2"
fi

docker run \
    --network="host" \
    -e SERVER_HOST="$SERVER_HOST" \
    -e SERVER_PORT="$SERVER_PORT" \
    "${SERVER_IMAGE}:${IMAGE_TAG}"