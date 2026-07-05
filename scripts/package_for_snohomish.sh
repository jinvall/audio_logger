#!/bin/bash
set -euo pipefail
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD_DIR="$APP_DIR/android"
APK_PATH="$BUILD_DIR/app/build/outputs/apk/debug/app-debug.apk"
EXPORT_DIR="$APP_DIR/telemetry/exports"
mkdir -p "$EXPORT_DIR"
latest_zip=$(find "$EXPORT_DIR" -name "package_*.zip" -type f 2>/dev/null | sort | tail -n 1 || true)
if [[ -z "$latest_zip" ]]; then
  echo "No telemetry package found in $EXPORT_DIR"
  exit 1
fi
ts=$(date +%Y%m%d_%H%M%S)
cp "$latest_zip" "$EXPORT_DIR/snohomish_submission_${ts}.zip"
echo "Packaged: $(basename "$latest_zip") -> snohomish_submission_${ts}.zip"
