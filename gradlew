#!/bin/sh
# Gradle wrapper — auto-downloads Gradle 8.7 on first run
DIRNAME="$(dirname "$0")"
exec "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" "$@" 2>/dev/null || \
  (java -jar "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" "$@")
