package com.mfhapps.trendingui.screens.pretext

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import kotlin.math.roundToInt

@Composable
fun BoxScope.PretextContourInstanceBadges(
    shapes: List<ViewShape>,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    if (alpha <= 0.01f || shapes.isEmpty()) return
    val density = LocalDensity.current
    shapes.forEach { shape ->
        val anchor = badgeAnchor(shape)
        val badgeWidthPx = with(density) { 96.dp.toPx() }
        val xPx = (anchor.first - badgeWidthPx * 0.5f).roundToInt()
        val yPx = (anchor.second - with(density) { 28.dp.toPx() }).roundToInt()
        Box(
            modifier = modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(xPx, yPx) },
        ) {
            ContourInstanceBadge(shape = shape, alpha = alpha)
        }
    }
}

@Composable
private fun ContourInstanceBadge(
    shape: ViewShape,
    alpha: Float,
) {
    val scheme = MaterialTheme.colorScheme
    val (icon, title) = badgeContent(shape)
    val container = when (shape.source) {
        VisionSource.Face -> scheme.tertiaryContainer
        VisionSource.Person -> scheme.primaryContainer
        VisionSource.Object -> scheme.secondaryContainer
        VisionSource.Manual -> scheme.surfaceContainerHighest
        VisionSource.Idle -> scheme.surfaceContainerHigh
    }
    val content = when (shape.source) {
        VisionSource.Face -> scheme.onTertiaryContainer
        VisionSource.Person -> scheme.onPrimaryContainer
        VisionSource.Object -> scheme.onSecondaryContainer
        VisionSource.Manual -> scheme.onSurface
        VisionSource.Idle -> scheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = container.copy(alpha = 0.94f * alpha),
        contentColor = content.copy(alpha = alpha),
        shadowElevation = 2.dp,
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp),
        ) {
            DecorativeIcon(icon, tint = content.copy(alpha = alpha), modifier = Modifier.padding(0.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun badgeContent(shape: ViewShape): Pair<ImageVector, String> = when (shape.source) {
    VisionSource.Face -> Icons.Outlined.Face to "Face"
    VisionSource.Person -> Icons.Outlined.Person to "Person"
    VisionSource.Object -> Icons.Outlined.Category to formatObjectLabel(shape.label)
    VisionSource.Manual -> Icons.Outlined.TouchApp to "Manual"
    VisionSource.Idle -> Icons.Outlined.Category to "Scanning"
}

private fun formatObjectLabel(label: String?): String {
    val raw = label?.trim().orEmpty()
    if (raw.isEmpty()) return "Object"
    return raw.split(' ', '-', '_')
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase() else ch.toString() }
        }
}

private fun badgeAnchor(shape: ViewShape): Pair<Float, Float> {
    val poly = shape.polygonPx?.points
    if (!poly.isNullOrEmpty()) {
        val minY = poly.minOf { it.second }
        val topPts = poly.filter { it.second <= minY + 2f }
        val cx = if (topPts.isNotEmpty()) topPts.map { it.first }.average().toFloat() else shape.boundsPx.centerX()
        return cx to minY
    }
    return shape.boundsPx.centerX() to shape.boundsPx.top
}
