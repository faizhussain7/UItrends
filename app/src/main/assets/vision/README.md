# Pretext vision models

Bundled automatically on first build via `scripts/download_pretext_vision_assets.sh`.

| File | Stack | Mode |
|------|--------|------|
| `selfie_segmentation.tflite` | TFLite (LiteRT-compatible) | Person / Auto |
| `face_landmarker.task` | MediaPipe Face Landmarker (478-pt mesh → jawline oval) | Face |
| `blaze_face_short_range.tflite` | BlazeFace TFLite fallback (box only) | Face |
| `ssd_mobilenet_coco.tflite` | TFLite fallback | Object |
| `yolov8n.param` / `yolov8n.bin` | NCNN | Object (preferred) |

NCNN static libs live under `app/src/main/cpp/third_party/ncnn/` (also downloaded by the script).
