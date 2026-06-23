package com.mfhapps.trendingui.legal

enum class LicenseFamily(val label: String) {
    All("All"),
    Mit("MIT"),
    Apache("Apache-2.0"),
    Bsd("BSD"),
    Other("Other"),
}

data class OpenSourceComponent(
    val name: String,
    val license: String,
    val family: LicenseFamily,
    val role: String,
    val url: String? = null,
)

val appOwnedLicense = OpenSourceComponent(
    name = "UITrends (MFH Apps)",
    license = "MIT",
    family = LicenseFamily.Mit,
    role = "App UI, navigation, Pretext JNI glue, and project C++",
    url = AppLinks.LICENSE_FILE,
)

val openSourceComponents = listOf(
    appOwnedLicense,
    OpenSourceComponent(
        name = "Jetpack Compose & Material 3",
        license = "Apache-2.0",
        family = LicenseFamily.Apache,
        role = "UI toolkit, adaptive layouts, navigation",
        url = "https://developer.android.com/jetpack/compose",
    ),
    OpenSourceComponent(
        name = "AndroidX CameraX",
        license = "Apache-2.0",
        family = LicenseFamily.Apache,
        role = "Camera preview & analysis (Pretext)",
        url = "https://developer.android.com/media/camera/camerax",
    ),
    OpenSourceComponent(
        name = "Coil",
        license = "Apache-2.0",
        family = LicenseFamily.Apache,
        role = "Image loading in masonry & catalog",
        url = "https://github.com/coil-kt/coil",
    ),
    OpenSourceComponent(
        name = "Haze",
        license = "MIT",
        family = LicenseFamily.Mit,
        role = "Backdrop blur & glass surfaces",
        url = "https://github.com/chrisbanes/haze",
    ),
    OpenSourceComponent(
        name = "LiteRT",
        license = "Apache-2.0",
        family = LicenseFamily.Apache,
        role = "On-device .tflite inference (com.google.ai.edge.litert:litert 1.4.2; successor to TensorFlow Lite)",
        url = "https://github.com/google-ai-edge/LiteRT",
    ),
    OpenSourceComponent(
        name = "MediaPipe Tasks Vision",
        license = "Apache-2.0",
        family = LicenseFamily.Apache,
        role = "Face landmarker & segmentation models",
        url = "https://github.com/google/mediapipe",
    ),
    OpenSourceComponent(
        name = "Kotlin & Coroutines",
        license = "Apache-2.0",
        family = LicenseFamily.Apache,
        role = "Language runtime & async",
        url = "https://kotlinlang.org/",
    ),
    OpenSourceComponent(
        name = "DataStore Preferences",
        license = "Apache-2.0",
        family = LicenseFamily.Apache,
        role = "Theme & settings persistence",
    ),
    OpenSourceComponent(
        name = "NCNN (optional)",
        license = "BSD-3-Clause",
        family = LicenseFamily.Bsd,
        role = "Optional YOLOv8 inference backend",
        url = "https://github.com/Tencent/ncnn",
    ),
    OpenSourceComponent(
        name = "MediaPipe model weights",
        license = "Apache-2.0",
        family = LicenseFamily.Apache,
        role = "Downloaded .tflite / .task bundles (LiteRT & MediaPipe runtimes)",
    ),
    OpenSourceComponent(
        name = "YOLOv8n weights (optional)",
        license = "AGPL-3.0*",
        family = LicenseFamily.Other,
        role = "NCNN object detection—review before commercial ship",
        url = "https://www.ultralytics.com/license",
    ),
)

fun openSourceComponentsForFamily(family: LicenseFamily): List<OpenSourceComponent> =
    when (family) {
        LicenseFamily.All -> openSourceComponents
        else -> openSourceComponents.filter { it.family == family }
    }
