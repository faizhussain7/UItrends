# Pretext vision models

Bundled automatically on first build via `scripts/download_pretext_vision_assets.sh`.

| File | Stack | Mode |
|------|--------|------|
| `selfie_segmentation.tflite` | TFLite | Person / Auto |
| `face_landmarker.task` | MediaPipe Face Landmarker | Face |
| `blaze_face_short_range.tflite` | BlazeFace TFLite fallback | Face |
| `yolo11n_seg.param` / `yolo11n_seg.bin` | NCNN | Object (primary) |
| `yolov8n_seg.param` / `yolov8n_seg.bin` | NCNN | Object (fallback) |

NCNN static libs (20260526+) live under `app/src/main/cpp/third_party/ncnn/`.
