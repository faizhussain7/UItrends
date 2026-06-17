package com.mfhapps.trendingui.ui.theme

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle

enum class GradientForegroundRole {
    Title,
    Subtitle,
    Section,
    Icon,
}

@Composable
fun AdaptiveGradientText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle,
    role: GradientForegroundRole = GradientForegroundRole.Title,
    vibrantStyle: VibrantForegroundStyle = VibrantForegroundStyle.OnGradient,
    color: Color? = null,
) {
    val catalogColors = LocalHomeCatalogColors.current
    val foreground = catalogColors.foregroundFor(role, vibrantStyle)
    val textStyle = if (vibrantStyle == VibrantForegroundStyle.OnLiquidGlass) {
        style.copy(
            shadow = Shadow(
                color = catalogColors.glassBarShadow,
                offset = androidx.compose.ui.geometry.Offset(0f, 1.5f),
                blurRadius = 6f,
            ),
        )
    } else {
        style
    }
    Text(
        text = text,
        style = textStyle,
        color = color ?: foreground,
        modifier = modifier,
    )
}

@Composable
fun AdaptiveGradientIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String?,
    role: GradientForegroundRole = GradientForegroundRole.Icon,
    vibrantStyle: VibrantForegroundStyle = VibrantForegroundStyle.OnGradient,
    tint: Color? = null,
) {
    val catalogColors = LocalHomeCatalogColors.current
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint ?: catalogColors.foregroundFor(role, vibrantStyle),
        modifier = modifier,
    )
}
