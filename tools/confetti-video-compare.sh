#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="${1:-$REPO_ROOT/artifacts/confetti-video-compare}"
ANDROID_VIDEO="$OUT_DIR/android-confetti.mp4"
WEB_VIDEO="$OUT_DIR/web-confetti.mp4"
REPORT_JSON="$OUT_DIR/comparison.json"
ANDROID_TMP="/sdcard/confetti-android-capture.mp4"

mkdir -p "$OUT_DIR"

if ! command -v adb >/dev/null 2>&1; then
  echo "error: adb not found" >&2
  exit 1
fi

if ! command -v ffmpeg >/dev/null 2>&1; then
  echo "error: ffmpeg not found" >&2
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "error: python3 not found" >&2
  exit 1
fi

echo "==> Checking Android device"
adb wait-for-device
DEVICE_COUNT="$(adb devices | awk 'NR>1 && $2=="device" {count++} END {print count+0}')"
if [ "$DEVICE_COUNT" -lt 1 ]; then
  echo "error: no Android emulator/device connected" >&2
  exit 1
fi

echo "==> Recording Android confetti burst"
(
  (
    cd "$REPO_ROOT/android"
    export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
    export PATH="$JAVA_HOME/bin:$PATH"
    export ANDROID_HOME="/Users/bensmith/Library/Android/sdk"
    export ANDROID_SDK_ROOT="$ANDROID_HOME"
    ./gradlew :app:installDebug >/dev/null
  )
  adb shell am start -n com.confettiprototype.androidsample/.MainActivity >/dev/null
  sleep 1
  adb shell uiautomator dump /sdcard/window_dump.xml >/dev/null
  adb pull /sdcard/window_dump.xml "$OUT_DIR/window_dump.xml" >/dev/null
  TAP_XY="$(
    python3 - "$OUT_DIR/window_dump.xml" <<'PY'
import re
import sys
import xml.etree.ElementTree as ET

path = sys.argv[1]
root = ET.parse(path).getroot()
for node in root.iter("node"):
    text = (node.attrib.get("text") or "").strip().lower()
    if text == "confetti":
        bounds = node.attrib.get("bounds", "")
        match = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
        if match:
            x1, y1, x2, y2 = map(int, match.groups())
            print(f"{(x1 + x2) // 2} {(y1 + y2) // 2}")
            sys.exit(0)
print("672 1359")
PY
  )"
  TAP_X="$(echo "$TAP_XY" | awk '{print $1}')"
  TAP_Y="$(echo "$TAP_XY" | awk '{print $2}')"
  adb shell rm -f "$ANDROID_TMP" >/dev/null 2>&1 || true
  adb shell screenrecord --size 1280x720 --bit-rate 12000000 --time-limit 8 "$ANDROID_TMP" &
  SCREEN_PID=$!
  sleep 1
  adb shell input tap "$TAP_X" "$TAP_Y"
  sleep 6
  wait "$SCREEN_PID" || true
  adb pull "$ANDROID_TMP" "$ANDROID_VIDEO" >/dev/null
  adb shell rm -f "$ANDROID_TMP" >/dev/null 2>&1 || true
)

echo "==> Recording Web confetti burst"
npx -y -p playwright playwright install chromium >/dev/null
node_cmd=(
  npx -y -p playwright node "$REPO_ROOT/tools/record-web-confetti.cjs" "$WEB_VIDEO"
)
"${node_cmd[@]}"

echo "==> Comparing motion speed"
python3 "$REPO_ROOT/tools/compare-confetti-videos.py" "$ANDROID_VIDEO" "$WEB_VIDEO" "$REPORT_JSON"

echo
echo "Artifacts written to: $OUT_DIR"
ls -lh "$OUT_DIR"
