#!/bin/bash
# ShopEase — run all automated tests (macOS / Linux / Git Bash on Windows)
set -e
cd "$(dirname "$0")"

if [[ ! -d bin ]]; then
  echo "Error: bin/ not found. Run ./compile.sh first."
  exit 1
fi

if ! command -v java &>/dev/null; then
  if [[ -x /usr/libexec/java_home ]]; then
    export JAVA_HOME="$(/usr/libexec/java_home)"
    export PATH="$JAVA_HOME/bin:$PATH"
  else
    echo "Error: Java not found. Install JDK 11+ and try again."
    exit 1
  fi
fi

CP="bin:lib/sqlite-jdbc.jar"

echo "========== SmokeTest =========="
java -cp "$CP" com.shopease.SmokeTest
echo ""
echo "========== FeatureTest =========="
java -cp "$CP" com.shopease.FeatureTest
echo ""
echo "========== FullSystemTest =========="
java -cp "$CP" com.shopease.FullSystemTest
echo ""
echo "ALL TEST SUITES PASSED"
