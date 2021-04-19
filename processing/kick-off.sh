#!/bin/sh


ENTRY_NAME=${1}
if [ -z "${ENTRY_NAME}" ]; then
    echo "Usage: use this script to kick off all processing scripts"
    echo "    Arg 1: the name of the entry"
    exit 0
fi

python3 average-messages.py "$ENTRY_NAME"
python3 state-disparity.py "$ENTRY_NAME"