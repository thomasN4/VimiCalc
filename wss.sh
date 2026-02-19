#!/bin/sh
if [ -n "$1" ]; then
    ./gradlew run --args="$1"
else
    ./gradlew run
fi
