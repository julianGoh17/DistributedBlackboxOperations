#!/bin/bash

DOCKER_COMPOSE_FILE="version: '2.2'
services:"
COUNTER=1
CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
BASEDIR="$CWD/../.."
REPORT_FOLDER="$BASEDIR/generated/report"
JAR_NAME="zookeeper-1.0-SNAPSHOT-jar-with-dependencies.jar"
SERVER_LIST_FILE="settings/server-list.txt"
SERVER_LIST_CONTENTS=""
METRICS_COLLECTOR_PORT="9090"

GREP_FOR_CADVISOR_PORT=$(cat "$CWD/settings/server-ports.txt" | grep 8080)
GREP_FOR_METRICS_COLLECTOR_PORT=$(cat "$CWD/settings/server-ports.txt" | grep $METRICS_COLLECTOR_PORT)

if [ -n "$GREP_FOR_CADVISOR_PORT" ]; then
  echo "Server ports can't contain 8080 as cAdvisor is created on that port"
  exit 1
fi

if [ -n "$GREP_FOR_METRICS_COLLECTOR_PORT" ]; then
  echo "Server ports can't contain $METRICS_COLLECTOR_PORT as metrics collector is created on that port"
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
  metrics-collector:
    image: dbo-metrics-collector:latest
    networks:
      - distributed-server  
    ports: [$METRICS_COLLECTOR_PORT:$METRICS_COLLECTOR_PORT]
    environment:
      - SERVER_HOST=metrics-collector
      - SERVER_PORT=$METRICS_COLLECTOR_PORT
    volumes:
      - $REPORT_FOLDER:/resources/report"

DOCKER_COMPOSE_FILE="$DOCKER_COMPOSE_FILE
networks:
  distributed-server:
    driver: bridge
"
DOCKER_COMPOSE_FILE_NAME="$CWD/docker-compose.yaml"
echo -n "$DOCKER_COMPOSE_FILE" > "$DOCKER_COMPOSE_FILE_NAME"
echo -n "$SERVER_LIST_CONTENTS" > "$CWD/$SERVER_LIST_FILE"
docker-compose -f "$DOCKER_COMPOSE_FILE_NAME" up -d 