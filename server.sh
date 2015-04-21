#!/usr/bin/env bash
function finish {
    kill ${serverProcess}
    kill ${judgeProcess}
}
trap finish EXIT

node server/server_service.js &
serverProcess=$!

echo "server started"

ant judge &
judgeProcess=$!

echo "judge started"

wait ${judgeProcess}

echo "Finishing"

exit