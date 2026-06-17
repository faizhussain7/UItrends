# UITrends (Android)

UITrends is a Jetpack Compose showcase of **modern, high‑polish Android UI patterns**:

- blurred backdrops and glass surfaces
- expressive Material 3 top bars and sheets
- sticky headers that “attach” with haptics
- adaptive hero layouts and motion

Everything is implemented with **first‑class Compose APIs**, real navigation, and production‑grade state management, so you can copy pieces into your own apps.

---

## 1. Feature tour

### 1.1 Demo catalog

- **Home catalog screen**
  - Scrollable list of UI demos (Bento, Glassmorphism, Neo‑brutalism, Kinetic typography, etc.)
  - **Featured hero card** with background‑adaptive badge + text colors
  - Category **filter chips** that adapt their foreground/background to the gradient behind them
- **Detail panes**
  - Two‑pane layout on large screens (list + detail)
  - Shared element transitions and per‑demo chrome

### 1.2 Pretext playground

The **Pretext** screen is the most advanced demo; it exercises layout, input, native vision, and blur:

- **Sticky “text to measure” field**
  - One `LazyColumn` with a pinned header
  - Field **attaches** to the top with a smooth height/offset animation
  - Hard haptic feedback when it first pins
- **Progressive sticky stack**
  - As you scroll:
    - Text field pins first
    - Slider controls attach below the field
    - “Run benchmark” attaches below the controls when you reach the benchmark section
  - Reverse scroll un‑attaches sections in the opposite order without glitches or duplicates
- **Focus and keyboard behavior**
  - Scroll and IME hide both **clear focus** on the field (same pattern as Zero UI)
  - Tap overlay opt‑in for focus instead of auto‑focus when the screen opens
- **Header camera action + tooltip**
  - Camera icon fades/scales into the top app bar when you scroll into the playground
  - Tooltip:
    - Anchored below the icon (never under the status bar)
    - Can auto‑show for a few seconds when the icon first appears
    - Haptics on show (auto + long press)
    - Optionally blurred backdrop, controlled by a **per‑page toggle** in the camera settings sheet

### 1.3 Blur and glass

- **Global blur toggle**
  - Settings → Appearance → *Blur sheet & dialog backdrops*
  - When enabled, applies a **Haze‑based frosted scrim** behind:
    - Bottom sheets
    - App dialogs
    - Some collapsing headers and tooltips that opt into the same backdrop system
- **Haze integration**
  - Uses `dev.chrisbanes.haze` and `haze-materials`
  - Shared utilities coordinate:
    - Back‑press handling for modals
    - Registration of blur regions and scrims
    - Respecting device capabilities (`supportsBackdropBlur()`) and user settings

### 1.4 Other demos (high level)

- **Glassmorphism**: layered translucent cards with blur, vibrant gradients, and depth
- **Neo‑brutalism**: bold, flat surfaces with heavy contrast and intentional “clunky” motion
- **Kinetic typography**: animated type and text motion
- **Spatial depth**: card selection with improved padding, elevation, and aligned layouts
- **Zero UI**: smooth scroll/focus/IME behavior used as a reference for Pretext

---

## 2. Architecture overview

### 2.1 Modules and entry points

- **Root project**
  - `settings.gradle.kts` – standard single‑module Android project (`:app`)
  - `build.gradle.kts` – plugin version catalog wiring
  - `gradle/libs.versions.toml` – central dependency versions
- **App module (`app/`)**
  - `MainActivity` – hosts the Compose content
  - `UITrendsApp` – top‑level composable: theme, navigation graph, global providers
  - `navigation/` – routes, animated transitions, adaptive list/detail scaffolding
  - `ui/theme/` – color, typography, gradient math, adaptive surfaces
  - `ui/components/` – reusable widgets (blur backdrops, sticky attach chrome, sliders, etc.)
  - `screens/*/` – one directory per demo or screen (e.g. `pretext`, `orbs`, `spatial`, `settings`)

### 2.2 State and navigation

- **Navigation**
  - Uses `androidx.navigation.compose` with a navigation graph (`UITrendsNavGraph`)
  - Detail panes coordinate with a `DemoPaneKey` and shared transition helpers
- **State**
  - Uses `ViewModel` and `DataStore` where persistent state is needed:
    - Theme + appearance preferences (`ThemeViewModel`, `ThemePreferencesRepository`)
    - Global blur toggle (“Blur sheet & dialog backdrops”)
  - Screen‑local state is usually `remember{}` / `rememberSaveable{}` at the composable level

### 2.3 Native / vision pipeline

Native code implements performance‑sensitive pieces for the Pretext playground:

- Location: `app/src/main/cpp/`
- Build: `app/src/main/cpp/CMakeLists.txt` (shared lib: `pretext_geometry`)
- Responsibilities:
  - **Geometry / contours**
    - `pretext_geometry.*`, `pretext_contour_*.*` – contour extraction, simplification, smoothing
  - **Vision orchestration**
    - `pretext_vision_orchestrator.*` – routes RGB frames through detection and contour machinery
    - `pretext_vision_jni.cpp` – JNI bridge to Kotlin surface (`PretextNativeVision`)
  - **YUV → RGB**
    - `pretext_yuv_rgb.*` – YUV420 rotation and RGB conversion for camera frames
  - **Optional NCNN object detector**
    - `pretext_ncnn_yolo.*` – YOLOv8‑style detection on top of NCNN (if the static libs are present)
  - **Thread safety**
    - `pretext_native_lock.*` – simple global mutex guard (`NativeLock`) around shared native state

All native sources carry an **MIT SPDX identifier** and build with additional hardening flags (see below).

---

## 3. Tech stack

### 3.1 Core

- **Language**: Kotlin (with some C++ for vision)
- **UI**: Jetpack Compose
  - Compose BOM `2026.05.00`
  - Material 3 + **Material 3 Expressive** + adaptive layouts/navigation
- **Navigation**: `androidx.navigation.compose`
- **Async**: Kotlin coroutines
- **Persistence**: `androidx.datastore.preferences`

### 3.2 Visual & UX

- **Haze** (`dev.chrisbanes.haze` + `haze-materials`) for blur and glass effects
- **CameraX** for live camera pipeline in Pretext
- **Coil** (`coil-compose`) for image loading

### 3.3 ML / Vision

- **TensorFlow Lite** for some model backends
- **MediaPipe Tasks Vision** for face landmarks and segmentation
- **NCNN** (optional) for YOLO‑style object detection
- On‑device assets under `app/src/main/assets/vision/` (see its `README.md`)

---

## 4. Build, run, and scripts

### 4.1 Requirements

- **Android Studio**: recent stable (Arctic Fox or later is recommended)
- **JDK**: 11 (project sets Java 11 compatibility)
- **Android SDK**:
  - `minSdk` **28**
  - `compileSdk` **37**
  - `targetSdk` **37**

### 4.2 Cloning and opening

1. Clone the repo
2. Open the root folder in Android Studio
3. Let Gradle sync

### 4.3 Build and run

From Android Studio:

1. Select the `app` run configuration
2. Choose a device/emulator (API 28+)
3. Run or Debug

From command line:

```bash
./gradlew :app:assembleDebug
```

### 4.4 Vision asset download

Large vision model files are downloaded on demand:

- Gradle task: `downloadPretextVisionAssets`
  - Defined in `app/build.gradle.kts`
  - Runs automatically before `preBuild`
- It fetches:
  - MediaPipe / TFLite assets (segmenter, face landmarks, etc.)
  - NCNN static libs for the configured ABI (into `app/src/main/cpp/third_party/ncnn/`)

If you prefer to manage assets manually, you can inspect what the task does in `scripts/` (when present in your working copy).

---

## 5. Native code and security‑oriented flags

### 5.1 C++ sources

- All core C++ files are in `app/src/main/cpp/`
- They are **project‑owned** and carry:
  - `SPDX-License-Identifier: MIT`
  - `Copyright (c) 2026 MFH Apps`

Headers use `#pragma once` and separate implementation files keep logic organized by concern (geometry, contour math, JNI, NCNN integration, etc.).

### 5.2 CMake configuration

`app/src/main/cpp/CMakeLists.txt`:

- Enables C++17
- Adds hardening flags to `pretext_geometry`:
  - `-fstack-protector-strong`
  - `-D_FORTIFY_SOURCE=2`
  - `-Wformat -Wformat-security`
  - `-fvisibility=hidden`
- Links with:
  - Android `log` library
  - Optional NCNN and OpenMP when `PRETEXT_USE_NCNN` is enabled
- Adds linker hardening:
  - `-Wl,-z,relro`
  - `-Wl,-z,now`

These flags are chosen to tighten native call sites without getting in the way of development builds.

---

## 6. Blur and modal backdrop system

The app centralizes blur handling so that **sheets, dialogs, tooltips, and some headers** can share the same backdrop behavior.

- **Global setting**
  - `Blur sheet & dialog backdrops` (in Settings)
  - Persists in `ThemePreferences` via DataStore
  - Controls:
    - Haze‑based scrims behind modal surfaces
    - Whether collapsing headers use blur or a solid surface
- **Implementation**
  - Core logic in `ui/components/ModalBackdrop.kt`
  - Reusable composables for:
    - Modal bottom sheets
    - Alert dialogs
    - Tooltips that want blurred backdrops
  - Collapsing headers coordinate with a backdrop controller so the blur sits **behind** the header content, not over it.

If you are only interested in the blur architecture, start by reading `ModalBackdrop.kt` and then the usages in `SettingsExpressive.kt`, `DemoCatalogScreen.kt`, and the Pretext tooltip integration.

---

## 7. Pretext details for contributors

If you plan to modify or reuse the **Pretext** screen:

- Look at:
  - `screens/pretext/PretextScreen.kt` – entry point and mode switching
  - `screens/pretext/PretextPlaygroundList.kt` – sticky stack, list structure, attach animations
  - `ui/components/DismissFocusOnScrollAndImeEffect.kt` – shared focus/IME effect
  - `ui/components/ModalBackdrop.kt` – blur for sheets/tooltips used in camera settings
- Native side:
  - `native/PretextNativeVision.kt` and JNI glue (`pretext_vision_jni.cpp`)
  - Contour math and smoothing in `pretext_contour_*` + `pretext_geometry.*`

The code is structured to favor **reusable effects and components** instead of one‑off hacks, so it’s safe to copy pieces into other apps with the same dependencies.

---

## 8. Contributing / customizing

This repo is primarily a **reference app**, but you can:

- Fork it and:
  - Add your own demo screens following the existing patterns
  - Swap out vision models or UI treatments
- Use individual components:
  - Blur/modals from `ui/components/ModalBackdrop.kt`
  - Sticky attach chrome + haptics from the Pretext list
  - Adaptive hero/chip color math from `ui/theme/CatalogColorMath.kt`

If you publish a fork, please review the **model and library licenses** in `THIRD_PARTY_NOTICES.md` and in the upstream projects.

---

## 9. Licensing

- **App code (this repo)**: **MIT License** – see `LICENSE`.
- **Third‑party libraries and model assets**:
  - See `THIRD_PARTY_NOTICES.md` and each upstream project’s LICENSE.
  - Pay special attention to:
    - **NCNN** (BSD‑3-Clause)
    - **Ultralytics YOLOv8** model exports (commonly AGPL‑3.0 / Enterprise)

If you use this project as the base for a **closed‑source commercial app**, double‑check your obligations around any bundled model weights or NCNN/YOLO integration and adjust assets or licenses as needed.

