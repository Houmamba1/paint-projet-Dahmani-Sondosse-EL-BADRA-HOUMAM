#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes dist
javac --release 17 -encoding UTF-8 -d build/classes src/paint/*.java
cp resources/*.properties build/classes/
jar --create --file dist/PaintInternational.jar --main-class paint.Main -C build/classes .
echo 'JAR créé : dist/PaintInternational.jar'
