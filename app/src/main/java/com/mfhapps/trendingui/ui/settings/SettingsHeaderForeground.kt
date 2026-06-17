package com.mfhapps.trendingui.ui.settings

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Immutable
data class SettingsHeaderForeground(
    val title: Color,
    val subtitle: Color,
    val icons: Color,
)

@Composable
fun rememberSettingsHeaderForeground(
    scheme: ColorScheme,
    collapsedFraction: Float,
): SettingsHeaderForeground {
    return remember(scheme, collapsedFraction) {
        SettingsHeaderForeground(
            title = scheme.onSurface,
            subtitle = scheme.onSurfaceVariant,
            icons = scheme.onSurface,
        )
    }
}
