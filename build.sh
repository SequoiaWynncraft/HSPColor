#!/bin/bash

# Usage: ./build.sh v1.1

VERSION="$1"

if [ -z "$VERSION" ]; then
  echo "Usage: $0 <version>"
  exit 1
fi

# Update gradle.properties
sed -i "s/^mod_version=.*/mod_version=${VERSION}/" gradle.properties
echo "Set mod_version=${VERSION} in gradle.properties"

./gradlew buildAndGather

JAR_NAME="hspcolor-1.21.4-v${VERSION}.jar"
SRC_PATH="build/libs/v${VERSION}/${JAR_NAME}"
DST_PATH="/home/warze/.local/share/multimc/instances/WynnCraft/.minecraft/mods/hspcolorv${VERSION}.jar"
DST_PATHB="/home/warze/.local/share/multimc/instances/1.21.4b/.minecraft/mods/hspcolorv${VERSION}.jar"

echo "${VERSION}" > "/home/warze/.local/share/multimc/instances/WynnCraft/.minecraft/mods/hspcolorlatest.txt"

cp "$SRC_PATH" "$DST_PATH"
cp "$SRC_PATH" "$DST_PATHB"
echo "Copied ${JAR_NAME} to ${DST_PATH}"
