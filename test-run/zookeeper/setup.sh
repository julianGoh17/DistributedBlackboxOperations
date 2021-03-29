#!/bin/bash

DOCKER_COMPOSE_FILE="version: '2.2'
services:"
COUNTER=1
CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
BASEDIR="$CWD/../.."
JAR_NAME="zookeeper-1.0-SNAPSHOT-jar-with-dependencies.jar"
SERVER_LIST_FILE="settings/server-list.txt"
SERVER_LIST_CONTENTS=""

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
echo -n "$DOCKER_COMPOSE_FILE" > "$CWD/docker-compose.yaml"
echo -n "$SERVER_LIST_CONTENTS" > "$CWD/$SERVER_LIST_FILE"