#!/bin/bash

PORT=8000

echo "Checking port $PORT..."

PIDS=$(lsof -ti TCP:$PORT -sTCP:LISTEN)

if [ -n "$PIDS" ]; then
    echo "Stopping existing server(s): $PIDS"
    kill $PIDS 2>/dev/null
    sleep 2
fi

PIDS=$(lsof -ti TCP:$PORT -sTCP:LISTEN)

if [ -n "$PIDS" ]; then
    echo "Force stopping: $PIDS"
    kill -9 $PIDS 2>/dev/null
    sleep 1
fi

echo "Starting SIH AI Smart Catalog API..."

exec uvicorn api.main:app --host 0.0.0.0 --port "$PORT"
