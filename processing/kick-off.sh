#!/bin/bash

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
TEST_RUN_DIR="$BASEDIR/../test-run"
ENTRY_NAME=${1}
if [ -z "${ENTRY_NAME}" ]; then
    echo "Usage: use this script to kick off all processing scripts"
    echo "    Arg 1: the name of the entry"
    exit 0
fi

"$TEST_RUN_DIR/create-report.sh"
python3 "${BASEDIR}/average-messages.py" "$ENTRY_NAME"
python3 "${BASEDIR}/state-disparity.py" "$ENTRY_NAME"