#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ASSETS="$ROOT/app/src/main/assets/vision"
NCNN_DIR="$ROOT/app/src/main/cpp/third_party/ncnn"
NCNN_VERSION="20260526"

mkdir -p "$ASSETS" "$NCNN_DIR"

download() {
  local url="$1"
  local dest="$2"
  if [[ -f "$dest" ]] && [[ -s "$dest" ]]; then
    echo "skip (exists): $(basename "$dest")"
    return 0
  fi
  echo "download: $url"
  curl -fsSL --retry 3 --connect-timeout 60 -o "$dest" "$url"
}

download \
  "https://storage.googleapis.com/mediapipe-models/image_segmenter/selfie_segmenter/float16/latest/selfie_segmenter.tflite" \
  "$ASSETS/selfie_segmentation.tflite"

download \
  "https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task" \
  "$ASSETS/face_landmarker.task"

download \
  "https://storage.googleapis.com/mediapipe-models/face_detector/blaze_face_short_range/float16/latest/blaze_face_short_range.tflite" \
  "$ASSETS/blaze_face_short_range.tflite"

download \
  "https://raw.githubusercontent.com/nihui/ncnn-assets/master/models/yolo11n_seg.ncnn.param" \
  "$ASSETS/yolo11n_seg.param"
download \
  "https://raw.githubusercontent.com/nihui/ncnn-assets/master/models/yolo11n_seg.ncnn.bin" \
  "$ASSETS/yolo11n_seg.bin"
download \
  "https://raw.githubusercontent.com/nihui/ncnn-assets/master/models/yolov8n_seg.ncnn.param" \
  "$ASSETS/yolov8n_seg.param"
download \
  "https://raw.githubusercontent.com/nihui/ncnn-assets/master/models/yolov8n_seg.ncnn.bin" \
  "$ASSETS/yolov8n_seg.bin"

NCNN_STAMP="$NCNN_DIR/.ncnn_version"
NCNN_ZIP="$NCNN_DIR/ncnn-${NCNN_VERSION}-android.zip"
needs_ncnn=false
if [[ ! -f "$NCNN_DIR/arm64-v8a/lib/libncnn.a" ]]; then
  needs_ncnn=true
elif [[ ! -f "$NCNN_STAMP" ]] || [[ "$(cat "$NCNN_STAMP")" != "$NCNN_VERSION" ]]; then
  echo "ncnn version bump: $(cat "$NCNN_STAMP" 2>/dev/null || echo none) -> $NCNN_VERSION"
  rm -rf "$NCNN_DIR"/arm64-v8a "$NCNN_DIR"/armeabi-v7a "$NCNN_DIR"/x86 "$NCNN_DIR"/x86_64
  needs_ncnn=true
fi
if [[ "$needs_ncnn" == true ]]; then
  download \
    "https://github.com/Tencent/ncnn/releases/download/${NCNN_VERSION}/ncnn-${NCNN_VERSION}-android.zip" \
    "$NCNN_ZIP"
  echo "extracting ncnn ${NCNN_VERSION}..."
  unzip -qo "$NCNN_ZIP" -d "$NCNN_DIR"
  if [[ -d "$NCNN_DIR/ncnn-${NCNN_VERSION}-android" ]]; then
    cp -R "$NCNN_DIR/ncnn-${NCNN_VERSION}-android/"* "$NCNN_DIR/"
    rm -rf "$NCNN_DIR/ncnn-${NCNN_VERSION}-android"
  fi
  rm -f "$NCNN_ZIP"
  echo "$NCNN_VERSION" > "$NCNN_STAMP"
fi

echo "Pretext vision assets ready under $ASSETS"
