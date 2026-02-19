#!/bin/bash

# POS Application - Run only (no build)
# Run from script directory so it works from anywhere

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR" || exit 1

JAR="target/libs/cicdpos-1.0.0.jar"

if [ -f "$JAR" ]; then
    echo "=== Running POS Application (from $JAR) ==="
    java -jar "$JAR"
else
    echo "=== JAR not found. Running via Gradle (will compile if needed) ==="
    ./gradlew run
fi
