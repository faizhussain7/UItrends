package com.mfhapps.trendingui.screens.glass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

val LocalGlassBackdrop = compositionLocalOf<GlassBackdropState?> { null }

@Composable
fun requireGlassBackdrop(): GlassBackdropState =
    LocalGlassBackdrop.current
        ?: error("Glass backdrop missing — wrap detail pane in ProvideGlassDetailEnvironment")

@Composable
fun ProvideGlassDetailEnvironment(
    hazeEnabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val backdrop = rememberGlassBackdropState(hazeEnabled = hazeEnabled)
    CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
        Box(modifier.fillMaxSize()) {
            GlassDemoBackdrop(
                state = backdrop,
                modifier = Modifier.matchParentSize(),
            )
            content()
        }
    }
}

@Stable
data class GlassBackdropState(
    val hazeState: HazeState,
    val hazeEnabled: Boolean,
    val specularPhase: Float,
)

@Composable
fun rememberGlassBackdropState(hazeEnabled: Boolean): GlassBackdropState {
    val reduceMotion = LocalReduceMotion.current
    val hazeState = rememberHazeState(blurEnabled = hazeEnabled)
    val infinite = rememberInfiniteTransition(label = "glassSpecular")
    val specularPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "specular",
    )
    return GlassBackdropState(
        hazeState = hazeState,
        hazeEnabled = hazeEnabled,
        specularPhase = specularPhase,
    )
}

@Composable
fun GlassDemoBackdrop(
    state: GlassBackdropState,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val reduceMotion = LocalReduceMotion.current
    val infinite = rememberInfiniteTransition(label = "glassDrift")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )

    val bgBrush = remember(scheme) {
        Brush.verticalGradient(
            colors = listOf(
                scheme.primaryContainer.copy(alpha = 0.95f),
                scheme.background,
                scheme.secondaryContainer.copy(alpha = 0.88f),
                scheme.tertiaryContainer.copy(alpha = 0.75f),
            ),
        )
    }

    val orbs = remember(scheme) {
        listOf(
            scheme.primary.copy(alpha = 0.38f),
            scheme.secondary.copy(alpha = 0.34f),
            scheme.tertiary.copy(alpha = 0.32f),
            scheme.primary.copy(alpha = 0.26f),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
            .then(
                if (!state.hazeEnabled) Modifier
                else Modifier.hazeSource(state = state.hazeState),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centers = listOf(
                Offset(w * (0.22f + drift * 0.06f), h * 0.20f),
                Offset(w * (0.80f - drift * 0.05f), h * 0.34f),
                Offset(w * 0.48f, h * (0.76f + drift * 0.04f)),
                Offset(w * (0.64f + drift * 0.05f), h * 0.52f),
            )
            val radii = listOf(w * 0.36f, w * 0.28f, w * 0.32f, w * 0.24f)
            orbs.forEachIndexed { i, color ->
                drawCircle(color = color, radius = radii[i], center = centers[i])
            }
        }
    }
}

enum class GlassVariant {
    Standard,
    Thin,
    Thick,
    Tinted,
}

@Composable
private fun rememberGlassStyle(
    variant: GlassVariant,
    hazeEnabled: Boolean,
): GlassStyle {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.45f
    val (tintAlpha, surfaceAlpha, blurExtra) = when (variant) {
        GlassVariant.Thin -> Triple(0.08f, 0.10f, 0f)
        GlassVariant.Thick -> Triple(0.22f, 0.28f, 4f)
        GlassVariant.Tinted -> Triple(0.18f, 0.14f, 0f)
        GlassVariant.Standard -> Triple(0.14f, 0.16f, 0f)
    }
    val rimHighlight = scheme.onSurface.copy(alpha = if (isDark) 0.22f else 0.35f)
    val rimShadow = scheme.onSurface.copy(alpha = if (isDark) 0.45f else 0.12f)
    return GlassStyle(
        tintColor = when (variant) {
            GlassVariant.Tinted -> scheme.primaryContainer
            else -> scheme.surface
        },
        tintAlpha = if (hazeEnabled) tintAlpha else tintAlpha + 0.25f,
        surfaceAlpha = if (hazeEnabled) surfaceAlpha else surfaceAlpha + 0.35f,
        rimBrush = Brush.linearGradient(
            colors = listOf(rimHighlight, rimShadow),
            start = Offset(0f, 0f),
            end = Offset(400f, 600f),
        ),
        specularWash = scheme.onSurface.copy(alpha = if (isDark) 0.06f else 0.10f),
        blurRadius = (if (hazeEnabled) 28f else 0f) + blurExtra,
        textScrimAlpha = when (variant) {
            GlassVariant.Thick -> 0.42f
            GlassVariant.Thin -> 0f
            else -> 0.18f
        },
    )
}

private data class GlassStyle(
    val tintColor: Color,
    val tintAlpha: Float,
    val surfaceAlpha: Float,
    val rimBrush: Brush,
    val specularWash: Color,
    val blurRadius: Float,
    val textScrimAlpha: Float,
)

@Composable
fun GlassSurface(
    backdrop: GlassBackdropState,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    variant: GlassVariant = GlassVariant.Standard,
    content: @Composable BoxScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val style = rememberGlassStyle(variant, backdrop.hazeEnabled)
    val fillColor = scheme.surface.copy(alpha = style.surfaceAlpha)

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (!backdrop.hazeEnabled) Modifier
                else Modifier.hazeEffect(
                    state = backdrop.hazeState,
                    style = HazeDefaults.style(
                        backgroundColor = scheme.background,
                        blurRadius = style.blurRadius.dp,
                        tint = HazeDefaults.tint(style.tintColor.copy(alpha = style.tintAlpha)),
                        noiseFactor = 0.07f,
                    ),
                ),
            )
            .background(fillColor)
            .drawBehind {
                val cornerPx = when {
                    shape === CircleShape -> minOf(size.width, size.height) / 2f
                    shape is RoundedCornerShape ->
                        shape.topStart.toPx(shapeSize = size, density = this)
                    else -> 28.dp.toPx()
                }
                if (style.textScrimAlpha > 0f) {
                    drawRoundRect(
                        color = scheme.surfaceContainerHighest.copy(alpha = style.textScrimAlpha),
                        cornerRadius = CornerRadius(cornerPx, cornerPx),
                    )
                }
                val w = size.width
                val h = size.height
                val streak = w * (-0.15f + backdrop.specularPhase * 0.45f)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            style.specularWash,
                            Color.Transparent,
                        ),
                        start = Offset(streak, 0f),
                        end = Offset(streak + w * 0.35f, h * 0.5f),
                    ),
                    size = size,
                )
                drawRoundRect(
                    brush = style.rimBrush,
                    cornerRadius = CornerRadius(cornerPx, cornerPx),
                    style = Stroke(width = 1.dp.toPx()),
                )
            },
        content = content,
    )
}

@Composable
fun GlassSection(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = scheme.onBackground,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun resolveBackdrop(backdrop: GlassBackdropState?): GlassBackdropState =
    backdrop ?: requireGlassBackdrop()

@Composable
fun GlassPanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdropState? = null,
    variant: GlassVariant = GlassVariant.Standard,
) {
    val resolved = resolveBackdrop(backdrop)

    GlassSurface(
        backdrop = resolved,
        modifier = modifier.fillMaxWidth(),
        variant = variant,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun GlassDock(
    modifier: Modifier = Modifier,
    backdrop: GlassBackdropState? = null,
) {
    val resolved = resolveBackdrop(backdrop)
    val scheme = MaterialTheme.colorScheme
    val items = listOf(
        Icons.Outlined.Home to "Home",
        Icons.Outlined.Search to "Search",
        Icons.Outlined.Person to "Profile",
        Icons.Outlined.Settings to "Settings",
    )

    GlassSurface(
        backdrop = resolved,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { (icon, label) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(icon, contentDescription = label, tint = scheme.onSurface)
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun GlassMediaCard(
    modifier: Modifier = Modifier,
    backdrop: GlassBackdropState? = null,
) {
    val resolved = resolveBackdrop(backdrop)
    val scheme = MaterialTheme.colorScheme
    val artShape = MaterialTheme.shapes.extraLarge

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(artShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        scheme.primary,
                        scheme.tertiary,
                    ),
                ),
            ),
    ) {
        GlassSurface(
            backdrop = resolved,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            shape = MaterialTheme.shapes.large,
            variant = GlassVariant.Thick,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Midnight Run",
                        style = MaterialTheme.typography.titleSmall,
                        color = scheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Theme-colored glass transport",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.SkipPrevious, null, tint = scheme.onSurface)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.PlayArrow, null, tint = scheme.primary, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Pause, null, tint = scheme.onSurface)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.SkipNext, null, tint = scheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun GlassMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdropState? = null,
) {
    val resolved = resolveBackdrop(backdrop)
    val scheme = MaterialTheme.colorScheme
    GlassSurface(
        backdrop = resolved,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        variant = GlassVariant.Thin,
    ) {
        Column(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GlassNotificationRow(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    backdrop: GlassBackdropState? = null,
    dotColor: Color = MaterialTheme.colorScheme.primary,
) {
    val resolved = resolveBackdrop(backdrop)
    val scheme = MaterialTheme.colorScheme
    GlassSurface(
        backdrop = resolved,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        variant = GlassVariant.Standard,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
        }
    }
}
