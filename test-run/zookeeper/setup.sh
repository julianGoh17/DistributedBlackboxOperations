#!/bin/bash

DOCKER_COMPOSE_FILE="version: '2.2'
services:"
COUNTER=1
CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
BASEDIR="$CWD/../.."
JAR_NAME="zookeeper-1.0-SNAPSHOT-jar-with-dependencies.jar"
SERVER_LIST_FILE="settings/server-list.txt"
SERVER_LIST_CONTENTS=""

GREP_FOR_CADVISOR_HOST=$(cat "$CWD/settings/server-ports.txt" | grep 8080)

if [ -n "$GREP_FOR_CADVISOR_HOST" ]; then
  echo "Server Ports can't contain 8080 as cAdvisor is created on that port"
  exit 1
fi

while IFS= read -r PORT
do
  DOCKER_COMPOSE_FILE="$DOCKER_COMPOSE_FILE
  server$COUNTER:
    image: dbo-server:latest
    networks:
      - distributed-server
    ports: [$PORT:$PORT]
    environment:
      - JAR_FILE_PATH=/resources/jar/$JAR_NAME
      - SERVER_HOST=server$COUNTER
      - SERVER_PORT=$PORT
      - SERVER_CONFIGURATION_LOCATION=$SERVER_LIST_FILE
      - PACKAGE_NAME=io.julian.ZookeeperAlgorithm
      - DOES_PROCESS_REQUEST=false
    volumes: 
      - $BASEDIR/zookeeper/target:/resources/jar 
      - $CWD/settings:/settings"
  SERVER_LIST_CONTENTS="${SERVER_LIST_CONTENTS}server$COUNTER:$PORT
"
  COUNTER=$((COUNTER+1))
done < "$CWD/settings/server-ports.txt"

DOCKER_COMPOSE_FILE="$DOCKER_COMPOSE_FILE
networks:
  distributed-server:
    driver: bridge
"
DOCKER_COMPOSE_FILE_NAME="$CWD/docker-compose.yaml"
echo -n "$DOCKER_COMPOSE_FILE" > "$DOCKER_COMPOSE_FILE_NAME"
echo -n "$SERVER_LIST_CONTENTS" > "$CWD/$SERVER_LIST_FILE"
docker-compose -f "$DOCKER_COMPOSE_FILE_NAME" up -d 