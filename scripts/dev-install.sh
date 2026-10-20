#!/bin/sh
# build + cài + giữ lại Trợ năng (HyperOS tắt nó sau mỗi lần install) + mở lại app
set -e
D=${1:-5d6c1a88}; P=com.huyi.recap; S=$P/$P.feature.capture.ShotAccessibilityService
./gradlew :app:assembleDebug -q
adb -s $D install -r app/build/outputs/apk/debug/app-debug.apk
cur=$(adb -s $D shell settings get secure enabled_accessibility_services | tr -d '\r')
case "$cur" in
  *$S*) ;;
  null|"") adb -s $D shell settings put secure enabled_accessibility_services "$S" ;;
  *) adb -s $D shell settings put secure enabled_accessibility_services "$cur:$S" ;;
esac
adb -s $D shell settings put secure accessibility_enabled 1
adb -s $D shell am start -n $P/.app.MainActivity >/dev/null
