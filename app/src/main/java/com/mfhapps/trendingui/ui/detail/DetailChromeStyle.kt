package com.mfhapps.trendingui.ui.detail

import androidx.compose.runtime.staticCompositionLocalOf

enum class DetailChromeStyle {
    Default,
    NeoBrutal,
    Neumorphism,
    Glass,
    Orbs,
    Spatial,
    Copilot,
}

val LocalDetailChromeStyle = staticCompositionLocalOf { DetailChromeStyle.Default }
