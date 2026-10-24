#!/bin/sh
set -e
cd "$(dirname "$0")"
chrome="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

mkdir -p graphics/iPhone
for page in *.html; do
    "$chrome" --headless=new --virtual-time-budget=10000 \
        --window-size=1242,2688 --screenshot="$PWD/graphics/iPhone/${page%.html}.png" \
        "file://$PWD/$page"
done
