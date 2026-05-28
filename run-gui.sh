#!/bin/bash
# ShopEase — run GUI (macOS / Linux / Git Bash on Windows)
set -e
cd "$(dirname "$0")"
[[ -d bin ]] || { echo "Run ./compile.sh first"; exit 1; }
if [[ -x /usr/libexec/java_home ]]; then
  JAVA_HOME="$(/usr/libexec/java_home)"
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi
"$JAVA" -cp "bin:lib/sqlite-jdbc.jar" com.shopease.ui.ShopEaseApp
