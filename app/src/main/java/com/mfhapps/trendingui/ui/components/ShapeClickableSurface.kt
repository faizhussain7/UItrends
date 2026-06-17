package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class CatalogCardColors(
    val container: Color,
    val onContainer: Color,
    val onContainerVariant: Color,
    val ripple: Color,
)

@Composable
fun rememberCatalogCardColors(selected: Boolean): CatalogCardColors {
    val scheme = MaterialTheme.colorScheme
    return remember(selected, scheme) {
        if (selected) {
            CatalogCardColors(
                container = scheme.primaryContainer,
                onContainer = scheme.onPrimaryContainer,
                onContainerVariant = scheme.onPrimaryContainer.copy(alpha = 0.78f),
                ripple = scheme.primary.copy(alpha = 0.22f),
            )
        } else {
            CatalogCardColors(
                container = scheme.surfaceContainer,
                onContainer = scheme.onSurface,
                onContainerVariant = scheme.onSurfaceVariant,
                ripple = scheme.onSurface.copy(alpha = if (scheme.surface.luminance() > 0.5f) 0.14f else 0.18f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShapeClickableSurface(
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    rippleColor: Color? = null,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val resolvedContent = if (contentColor != Color.Unspecified) {
        contentColor
    } else {
        defaultContentColorForContainer(color, scheme)
    }
    val resolvedRipple = rippleColor ?: resolvedContent.copy(
        alpha = if (scheme.surface.luminance() > 0.5f) 0.14f else 0.2f,
    )
    val rippleConfiguration = remember(resolvedRipple) {
        RippleConfiguration(color = resolvedRipple)
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalRippleConfiguration provides rippleConfiguration,
    ) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            shape = shape,
            color = color,
            contentColor = resolvedContent,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation,
            border = border,
            content = content,
        )
    }
}

private fun defaultContentColorForContainer(
    container: Color,
    scheme: ColorScheme,
): Color {
    if (container == Color.Unspecified) return scheme.onSurface
    if (container == scheme.primaryContainer) return scheme.onPrimaryContainer
    if (container == scheme.surfaceContainer) return scheme.onSurface
    if (container == scheme.surfaceContainerLow) return scheme.onSurface
    if (container == scheme.surfaceContainerHigh) return scheme.onSurface
    return if (container.luminance() > 0.55f) {
        Color(0xFF1A1C1E)
    } else {
        Color(0xFFF5F5F7)
    }
}
