#!/bin/sh
set -e
cd "$(dirname "$0")"
chrome="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

render() {
    device=$1
    window_size=$2
    mkdir -p "graphics/$device"
    for page in *.html; do
        "$chrome" --headless=new --virtual-time-budget=10000 \
            --window-size="$window_size" --screenshot="$PWD/graphics/$device/${page%.html}.png" \
            "file://$PWD/$page"
    done
}

render iPhone 1242,2688
render Android 1080,1920
