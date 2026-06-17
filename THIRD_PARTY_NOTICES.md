# Third‑party notices

This file lists notable third‑party components used by **UITrends**. It is not necessarily exhaustive.

If you redistribute the app (APK/AAB) or publish it, you are responsible for complying with all applicable licenses.

## Libraries

- **AndroidX / Jetpack (Compose, Navigation, DataStore, CameraX, etc.)**
  - Common licenses: **Apache-2.0**
  - Sources: Google Maven artifacts (see `gradle/libs.versions.toml`)

- **KotlinX Coroutines / Serialization**
  - License: **Apache-2.0**
  - Sources: `org.jetbrains.kotlinx:*`

- **Coil (Compose image loading)**
  - License: **Apache-2.0**
  - Source: `io.coil-kt:coil-compose`

- **Haze (blur/frosted glass)**
  - License: MIT (per upstream)
  - Source: `dev.chrisbanes.haze:*`

- **TensorFlow Lite**
  - License: **Apache-2.0**
  - Source: `org.tensorflow:tensorflow-lite`

- **MediaPipe Tasks Vision**
  - License: **Apache-2.0**
  - Source: `com.google.mediapipe:tasks-vision`

## Bundled / downloaded model assets (Pretext)

See `app/src/main/assets/vision/README.md` for the local file list and intended stack per mode.

### YOLOv8 (NCNN export)

This repo contains `app/src/main/assets/vision/yolov8n.param`.

**Important**: Ultralytics’ YOLOv8 models are commonly distributed under **AGPL-3.0** (with an alternative paid enterprise license). If you ship Ultralytics YOLO model artifacts with your app, ensure you comply with the applicable license terms.

References (for license understanding):
- `https://www.ultralytics.com/license`

### NCNN

The project can optionally use **NCNN** (static libraries under `app/src/main/cpp/third_party/ncnn/` when present).

- License: **BSD 3‑Clause** (per upstream `Tencent/ncnn`)
- Upstream: `https://github.com/Tencent/ncnn`

## Notes

- This notice file is meant to be a practical compliance aid. If you add/remove dependencies or bundled assets, update it accordingly.

