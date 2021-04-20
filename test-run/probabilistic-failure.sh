#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
SETTINGS_FOLDER="settings"
FAILURE_CHANCE="0.05"

while IFS= read -r PORT
do
    curl -v  "localhost:$PORT/server" -H "Content-Type: application/json" -d "{\"status\": \"probabilistic_failure\", \"failureChance\": $FAILURE_CHANCE}"
done < "$BASEDIR/$SETTINGS_FOLDER/server-ports.txt"