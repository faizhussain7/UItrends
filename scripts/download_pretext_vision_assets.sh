#!/usr/bin/env bash
# Downloads TFLite models (LiteRT-compatible) and NCNN YOLOv8n assets for Pretext vision.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ASSETS="$ROOT/app/src/main/assets/vision"
NCNN_DIR="$ROOT/app/src/main/cpp/third_party/ncnn"
NCNN_VERSION="20240820"

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

# Phase B — selfie segmentation (MediaPipe / LiteRT-compatible TFLite)
download \
  "https://storage.googleapis.com/mediapipe-models/image_segmenter/selfie_segmenter/float16/latest/selfie_segmenter.tflite" \
  "$ASSETS/selfie_segmentation.tflite"

download \
  "https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task" \
  "$ASSETS/face_landmarker.task"

# Face — legacy short-range detector (optional fallback asset)
download \
  "https://storage.googleapis.com/mediapipe-assets/face_detection_short_range.tflite" \
  "$ASSETS/face_detection_short_range.tflite"

# Face — raw TFLite fallback decoder (same family, letterbox path)
download \
  "https://storage.googleapis.com/mediapipe-models/face_detector/blaze_face_short_range/float16/latest/blaze_face_short_range.tflite" \
  "$ASSETS/blaze_face_short_range.tflite"

# Object fallback (TFLite SSD) when NCNN is unavailable
download \
  "https://github.com/tensorflow/tflite-support/raw/master/tensorflow_lite_support/metadata/python/tests/testdata/object_detector/ssd_mobilenet_v1.tflite" \
  "$ASSETS/ssd_mobilenet_coco.tflite"

# Phase C — NCNN YOLOv8n (object mode, native C++)
download \
  "https://raw.githubusercontent.com/nihui/ncnn-assets/master/models/yolov8n.ncnn.param" \
  "$ASSETS/yolov8n.param"
download \
  "https://raw.githubusercontent.com/nihui/ncnn-assets/master/models/yolov8n.ncnn.bin" \
  "$ASSETS/yolov8n.bin"

# NCNN prebuilt static libs (per-ABI, CPU)
NCNN_ZIP="$NCNN_DIR/ncnn-${NCNN_VERSION}-android.zip"
if [[ ! -f "$NCNN_DIR/arm64-v8a/lib/libncnn.a" ]]; then
  download \
    "https://github.com/Tencent/ncnn/releases/download/${NCNN_VERSION}/ncnn-${NCNN_VERSION}-android.zip" \
    "$NCNN_ZIP"
  echo "extracting ncnn..."
  unzip -qo "$NCNN_ZIP" -d "$NCNN_DIR"
  if [[ -d "$NCNN_DIR/ncnn-${NCNN_VERSION}-android" ]]; then
    cp -R "$NCNN_DIR/ncnn-${NCNN_VERSION}-android/"* "$NCNN_DIR/"
    rm -rf "$NCNN_DIR/ncnn-${NCNN_VERSION}-android"
  fi
  rm -f "$NCNN_ZIP"
fi

echo "Pretext vision assets ready under $ASSETS"
