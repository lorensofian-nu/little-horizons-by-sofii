#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
test_output="${TMPDIR:-/tmp}/little-horizons-test-classes"

rm -rf "$test_output"
mkdir -p "$test_output"
javac --release 17 -encoding UTF-8 \
  -cp "$project_root/lib/mysql-connector-j.jar" \
  -d "$test_output" \
  $(find "$project_root/src" -name '*.java' -print) \
  "$project_root/tests/UnitTest.java"

java -cp "$test_output:$project_root/lib/mysql-connector-j.jar" \
  com.littlehorizons.erp.UnitTest
