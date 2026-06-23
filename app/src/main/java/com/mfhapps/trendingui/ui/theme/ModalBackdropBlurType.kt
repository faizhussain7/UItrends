package com.mfhapps.trendingui.ui.theme

enum class ModalBackdropBlurType(
    val label: String,
    val subtitle: String,
) {
    Default(
        label = "Default",
        subtitle = "Clean Gaussian blur with a light surface tint",
    ),
    Mosaic(
        label = "Mosaic",
        subtitle = "Chunky pixel blocks from downscaled sampling",
    ),
    Progressive(
        label = "Progressive",
        subtitle = "Heavy blur at the top that fades toward the sheet",
    ),
    Frosted(
        label = "Frosted",
        subtitle = "Thick frosted glass with layered tint and grain",
    ),
}
