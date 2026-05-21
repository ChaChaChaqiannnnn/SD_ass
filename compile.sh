#!/bin/bash
# ShopEase — compile script (Group 5)
set -e
cd "$(dirname "$0")"

if ! command -v javac &>/dev/null; then
  if [[ -x /usr/libexec/java_home ]]; then
    export JAVA_HOME="$(/usr/libexec/java_home)"
    export PATH="$JAVA_HOME/bin:$PATH"
  else
    echo "Error: Java JDK not found. Install JDK 11+ and try again."
    exit 1
  fi
fi

if [[ ! -f lib/sqlite-jdbc.jar ]] || ! jar tf lib/sqlite-jdbc.jar &>/dev/null; then
  echo "Error: lib/sqlite-jdbc.jar is missing or corrupted."
  echo "Download from: https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/"
  echo "Or copy from Maven cache: ~/.m2/repository/org/xerial/sqlite-jdbc/"
  exit 1
fi

rm -rf bin
mkdir -p bin
find code -name "*.java" > sources.txt

echo "Compiling $(wc -l < sources.txt | tr -d ' ') source files..."
javac -encoding UTF-8 -d bin -cp "lib/sqlite-jdbc.jar" @sources.txt
echo "Build OK. Classes in bin/"
