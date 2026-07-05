#!/bin/bash
set -euo pipefail
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BASE="$APP_DIR/telemetry"
LOG_DIR="$BASE/logs"
CSV_DIR="$BASE/csv"
mkdir -p "$LOG_DIR" "$CSV_DIR"
find "$BASE" -type f \( -name "*.csv" -o -name "*.pcm" -o -name "*.jpg" \) -print0 | while IFS= read -r -d '' f; do
  rel="${f#$APP_DIR/}"
  ts=$(date -r "$f" +%Y%m%d_%H%M%S 2>/dev/null || date +%Y%m%d_%H%M%S)
  cp "$f" "$LOG_DIR/${ts}_$(basename "$f")"
done
cp "$LOG_DIR"/*.csv "$CSV_DIR/" 2>/dev/null || true
echo "Exported telemetry to $LOG_DIR and $CSV_DIR"
