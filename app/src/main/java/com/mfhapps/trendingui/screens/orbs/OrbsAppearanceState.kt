package com.mfhapps.trendingui.screens.orbs

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class OrbsAppearanceState {
    var expressive by mutableStateOf(false)
    var presence by mutableFloatStateOf(1f)
    var randomShapes by mutableStateOf(true)
}

val LocalOrbsAppearanceState = compositionLocalOf<OrbsAppearanceState?> { null }
