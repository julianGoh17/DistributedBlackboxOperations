#!/bin/bash
SERVER_LIST_FILE="../settings/server-list.txt"
CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
SERVER=$(head -n 1 $CWD/$SERVER_LIST_FILE)
PORT="${SERVER##*:}"

ENDPOINT="http://localhost:$PORT/coordinate/message"
echo $ENDPOINT
curl -X POST -v  "$ENDPOINT" -H "Content-Type: application/json" \
    -d '{"metadata":{"timestamp":"2021-03-29T12:28:14.493818","request":"POST","messageId":null,"type":null},"message":null,"definition":{}}'