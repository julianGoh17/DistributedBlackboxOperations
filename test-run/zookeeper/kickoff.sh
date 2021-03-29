#!/bin/bash

CWD="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

"$CWD/setup.sh"
echo "Waiting 5 seconds to setup servers"
sleep 5
"$CWD/election.sh"