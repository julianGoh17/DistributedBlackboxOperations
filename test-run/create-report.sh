#!/bin/bash
METRICS_COLLECTOR_PORT="9090"

ENDPOINT="http://localhost:$METRICS_COLLECTOR_PORT/report?filterName=generic"
echo $ENDPOINT
curl -X POST -v  "$ENDPOINT" -H "Content-Type: application/json"