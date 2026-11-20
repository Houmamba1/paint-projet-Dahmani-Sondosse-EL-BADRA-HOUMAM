#!/usr/bin/env sh
cd "$(dirname "$0")"
exec java -Xmx512m -jar dist/PaintInternational.jar
