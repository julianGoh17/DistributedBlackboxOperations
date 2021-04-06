#!/bin/bash
CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
DOCKER_COMPOSE_FILE_NAME="$CWD/docker-compose.yaml"
"$CWD/../cAdvisor/teardown.sh"
docker-compose -f "$DOCKER_COMPOSE_FILE_NAME" down