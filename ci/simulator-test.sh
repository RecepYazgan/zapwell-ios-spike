#!/bin/bash
# Installs the app on an iPad simulator and plays (1) a live MPEG-TS stream made by ffmpeg on this machine, like an
# IPTV channel, and (2) Apple's public HLS test stream. Saves screenshots, a video and the app's status lines.
set -u
APP="$1"; OUT="$2"; mkdir -p "$OUT"
ID=net.webotion.zapspike

DEV=$(xcrun simctl list devices available -j | python3 -c '
import json,sys
d=json.load(sys.stdin)["devices"]
c=[x for r,l in d.items() if "iOS" in r for x in l if x["name"].startswith("iPad Pro")]
c=c or [x for r,l in d.items() if "iOS" in r for x in l if x["name"].startswith("iPad")]
print(c[-1]["udid"])')
echo "simulator: $DEV"
xcrun simctl boot "$DEV"; xcrun simctl bootstatus "$DEV" -b
xcrun simctl install "$DEV" "$APP"

# Live TS over http: test pattern + tone, 720p H.264/AAC, served to one client (restarted for each run).
serve() {
  ffmpeg -hide_banner -loglevel error -re -f lavfi -i testsrc2=size=1280x720:rate=25 -f lavfi -i sine=frequency=440 \
    -c:v libx264 -preset veryfast -tune zerolatency -g 50 -c:a aac -f mpegts -listen 1 http://127.0.0.1:8090/live.ts &
  FF=$!
}

run() { # name url seconds
  xcrun simctl terminate "$DEV" $ID 2>/dev/null
  xcrun simctl launch --terminate-running-process --stdout="$OUT/$1.log" --stderr="$OUT/$1.err" "$DEV" $ID -autoplay "$2"
  sleep "$3"
  xcrun simctl io "$DEV" screenshot "$OUT/$1.png"
}

serve; sleep 2
xcrun simctl io "$DEV" recordVideo --codec=h264 "$OUT/ts-live.mp4" & REC=$!
run ts-live http://127.0.0.1:8090/live.ts 25
kill -INT $REC; wait $REC 2>/dev/null
kill $FF 2>/dev/null

run hls-apple "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8" 25

xcrun simctl terminate "$DEV" $ID
for f in "$OUT"/*.log; do echo "== $f"; cat "$f"; done
for f in "$OUT"/*.err; do echo "== $f"; tail -n 30 "$f"; done
exit 0
