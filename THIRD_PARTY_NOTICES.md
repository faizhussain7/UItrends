# Third-party notices

UITrends uses open-source libraries and **on-demand downloaded model assets**. This file is a practical compliance aid — not legal advice.

If you redistribute the app (APK/AAB) or publish a derivative, you must comply with **each component’s license**. Pay special attention to **model weights** (especially YOLOv8).

**Project-owned code** (not listed below): Kotlin/Compose app and all `pretext_*` C++ under `app/src/main/cpp/` — **MIT · Copyright (c) 2026 MFH Apps** — see [LICENSE](LICENSE).

---

## Runtime dependencies

Versions are pinned in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

| Component | Maven coordinates (summary) | License | Notes |
| --- | --- | --- | --- |
| **Android Gradle Plugin** | `com.android.application` 9.2.1 | Apache-2.0 | Build-time only |
| **Kotlin** | `org.jetbrains.kotlin` 2.0.21 | Apache-2.0 | Language + Compose plugin |
| **AndroidX Core** | `androidx.core:core-ktx` 1.18.0 | Apache-2.0 | |
| **Core Splashscreen** | `androidx.core:core-splashscreen` 1.0.1 | Apache-2.0 | |
| **Lifecycle** | `androidx.lifecycle:*` 2.10.0 | Apache-2.0 | Runtime, ViewModel, Compose |
| **Activity Compose** | `androidx.activity:activity-compose` 1.13.0 | Apache-2.0 | |
| **Jetpack Compose BOM** | `androidx.compose:compose-bom` 2026.05.00 | Apache-2.0 | UI, Foundation, Animation, Graphics |
| **Material 3 Expressive** | `androidx.compose.material3:material3` 1.5.0-alpha20 | Apache-2.0 | Pinned above BOM |
| **Material Icons Extended** | `androidx.compose.material:material-icons-extended` | Apache-2.0 | Via BOM |
| **Material 3 Adaptive** | `androidx.compose.material3.adaptive:*` | Apache-2.0 | List–detail scaffolding |
| **Navigation Compose** | `androidx.navigation:navigation-compose` 2.9.8 | Apache-2.0 | |
| **DataStore Preferences** | `androidx.datastore:datastore-preferences` 1.1.7 | Apache-2.0 | Theme / settings |
| **CameraX** | `androidx.camera:*` 1.4.2 | Apache-2.0 | core, camera2, lifecycle, view |
| **Kotlin Coroutines** | `org.jetbrains.kotlinx:kotlinx-coroutines-android` 1.10.2 | Apache-2.0 | |
| **Kotlin Serialization** | `org.jetbrains.kotlinx:kotlinx-serialization-json` 1.8.1 | Apache-2.0 | |
| **Coil** | `io.coil-kt:coil-compose` 2.7.0 | Apache-2.0 | Image loading |
| **Haze** | `dev.chrisbanes.haze:haze` 1.7.2 | MIT | Blur / frosted glass |
| **Haze Materials** | `dev.chrisbanes.haze:haze-materials` 1.7.2 | MIT | Material-tinted haze styles |
| **LiteRT** | `com.google.ai.edge.litert:litert` 1.4.2 | Apache-2.0 | On-device inference (16 KB page-size compatible) |
| **MediaPipe Tasks Vision** | `com.google.mediapipe:tasks-vision` 0.10.26 | Apache-2.0 | Face landmarker API |

Upstream sources: [Google Maven](https://maven.google.com/) · [Maven Central](https://central.sonatype.com/) · [Haze](https://github.com/chrisbanes/haze) · [Coil](https://github.com/coil-kt/coil) · [LiteRT](https://github.com/google-ai-edge/LiteRT) · [MediaPipe](https://github.com/google/mediapipe)

---

## Test & debug dependencies

| Component | Coordinates | License |
| --- | --- | --- |
| JUnit | `junit:junit` 4.13.2 | EPL-2.0 |
| AndroidX JUnit | `androidx.test.ext:junit` 1.3.0 | Apache-2.0 |
| Espresso | `androidx.test.espresso:espresso-core` 3.7.0 | Apache-2.0 |
| Compose UI Test | `androidx.compose.ui:ui-test-*` | Apache-2.0 | Via BOM |
| Compose Tooling | `androidx.compose.ui:ui-tooling` | Apache-2.0 | Debug only |

These are **not shipped** in release APKs unless you add them to `implementation`.

---

## Native libraries (optional NCNN path)

Built into `libpretext_geometry.so` when prebuilt NCNN static libs are present (`app/src/main/cpp/third_party/ncnn/<abi>/`).

| Component | Version / source | License | Role |
| --- | --- | --- | --- |
| **NCNN** | [Tencent/ncnn](https://github.com/Tencent/ncnn) release `20240820` (android zip) | BSD-3-Clause | Inference engine for YOLOv8n |
| **OpenMP** | Linked via `-fopenmp` when NCNN enabled | LLVM / GPL-3.0 with runtime exception (toolchain-dependent) | NCNN parallel ops |

**MFH Apps code** that wraps NCNN (`pretext_ncnn_yolo.cpp`, JNI glue) is **MIT** — the **NCNN binary** is third-party.

NCNN and model assets are downloaded by [`scripts/download_pretext_vision_assets.sh`](scripts/download_pretext_vision_assets.sh) (Gradle task `downloadPretextVisionAssets`, runs before `preBuild`). NCNN libs are gitignored under `third_party/ncnn/`.

---

## Downloaded vision model assets

Fetched into `app/src/main/assets/vision/` on first build. See also [`app/src/main/assets/vision/README.md`](app/src/main/assets/vision/README.md).

| File | Used by | Download source | License (typical) |
| --- | --- | --- | --- |
| `selfie_segmentation.tflite` | Person / Auto | [MediaPipe selfie segmenter](https://storage.googleapis.com/mediapipe-models/image_segmenter/selfie_segmenter/float16/latest/selfie_segmenter.tflite) | Apache-2.0 (MediaPipe) |
| `face_landmarker.task` | Face / Auto | [MediaPipe face landmarker](https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task) | Apache-2.0 (MediaPipe) |
| `blaze_face_short_range.tflite` | Face fallback | [MediaPipe BlazeFace](https://storage.googleapis.com/mediapipe-models/face_detector/blaze_face_short_range/float16/latest/blaze_face_short_range.tflite) | Apache-2.0 (MediaPipe) |
| `face_detection_short_range.tflite` | Downloaded; legacy spare asset | [MediaPipe assets](https://storage.googleapis.com/mediapipe-assets/face_detection_short_range.tflite) | Apache-2.0 (MediaPipe) |
| `ssd_mobilenet_coco.tflite` | Object fallback (no NCNN) | [TensorFlow Lite Support testdata](https://github.com/tensorflow/tflite-support) | Apache-2.0 (inference via LiteRT) |
| `yolov8n.param` / `yolov8n.bin` | Object (NCNN) | [nihui/ncnn-assets](https://github.com/nihui/ncnn-assets) YOLOv8n NCNN export | **See YOLOv8 note below** |

### YOLOv8 weights — important

`yolov8n.param` / `yolov8n.bin` are **YOLOv8n** weights converted for NCNN. Ultralytics YOLOv8 is commonly licensed under **AGPL-3.0**, with a separate [enterprise license](https://www.ultralytics.com/license).

If you ship these weights in a **closed-source commercial app**, review Ultralytics terms and either:

- obtain a suitable license,
- replace the model with one whose license fits your distribution, or
- remove NCNN object detection (LiteRT SSD MobileNet fallback remains, with different accuracy).

The **NCNN conversion scripts/assets** (nihui/ncnn-assets) do not replace the underlying model license.

---

## Pretext stack summary

| Mode | Primary backend | Fallback | Native contour |
| --- | --- | --- | --- |
| Person | LiteRT selfie segmentation (.tflite) | Mask blob → box | `pretext_geometry` (MIT) |
| Face | MediaPipe landmarker | BlazeFace via LiteRT (.tflite) | `pretext_geometry` (MIT) |
| Object | NCNN YOLOv8n | LiteRT SSD MobileNet (.tflite) | `pretext_geometry` (MIT) |

---

## Compliance checklist

- [ ] Include **MIT** notice for MFH Apps code when redistributing source ([LICENSE](LICENSE)).
- [ ] Retain **Apache-2.0** notices for AndroidX, LiteRT, MediaPipe, Coil, etc. (usually via packaged `NOTICE` files from dependencies — verify your release build).
- [ ] Retain **MIT** notice for Haze if required by upstream.
- [ ] Retain **BSD-3-Clause** notice for NCNN if linked in your APK.
- [ ] Review **YOLOv8 / AGPL** before shipping `yolov8n.*` in production.
- [ ] Update this file when adding dependencies or bundled assets.

---

## Updating this file

1. Check new entries in `gradle/libs.versions.toml` and `app/build.gradle.kts`.
2. Check URLs in `scripts/download_pretext_vision_assets.sh`.
3. Record license changes from each project’s `LICENSE` / `NOTICE` in the upstream repo.

*Last reviewed against project dependencies: June 2026.*
