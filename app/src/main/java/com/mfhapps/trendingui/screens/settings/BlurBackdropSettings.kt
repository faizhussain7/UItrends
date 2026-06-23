package com.mfhapps.trendingui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Grain
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.components.HapticSlider
import com.mfhapps.trendingui.ui.components.modalBackdropHazeEffect
import com.mfhapps.trendingui.ui.platform.supportsBackdropBlur
import com.mfhapps.trendingui.ui.theme.MAX_MODAL_BLUR_RADIUS_DP
import com.mfhapps.trendingui.ui.theme.MAX_MODAL_TINT_ALPHA
import com.mfhapps.trendingui.ui.theme.MIN_MODAL_BLUR_RADIUS_DP
import com.mfhapps.trendingui.ui.theme.MIN_MODAL_TINT_ALPHA
import com.mfhapps.trendingui.ui.theme.ModalBackdropBlurType
import com.mfhapps.trendingui.ui.theme.ModalBackdropStyle
import com.mfhapps.trendingui.ui.theme.resolveTintColor
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.roundToInt

private enum class ModalBackdropTintPreset(val label: String, val hue: Float?) {
    Auto("Auto", null),
    Cool("Cool", 210f),
    Warm("Warm", 28f),
    Violet("Violet", 275f),
    Custom("Custom", null),
}

@Composable
fun BlurBackdropSettings(
    style: ModalBackdropStyle,
    onPreviewChange: (ModalBackdropStyle) -> Unit,
    onCommitChange: (ModalBackdropStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val blurSupported = remember(context) { context.supportsBackdropBlur() }
    val normalized = remember(style) { style.normalized() }
    val tintPreset = remember(normalized) { normalized.tintPreset() }
    val tintColor = normalized.resolveTintColor(
        scheme = scheme,
        isDark = scheme.background.luminance() < 0.45f,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Backdrop blur",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )

        BlurBackdropPreview(
            style = normalized,
            blurSupported = blurSupported,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Blur type",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ModalBackdropBlurType.entries.forEach { type ->
                    FilterChip(
                        selected = normalized.blurType == type,
                        onClick = {
                            val next = normalized.copy(blurType = type)
                            onPreviewChange(next)
                            onCommitChange(next.normalized())
                        },
                        label = { Text(type.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = type.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = scheme.primaryContainer,
                            selectedLabelColor = scheme.onPrimaryContainer,
                        ),
                    )
                }
            }
            Text(
                text = normalized.blurType.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }

        BlurSliderControl(
            label = "Blur strength",
            valueLabel = "${normalized.blurRadiusDp.roundToInt()} dp",
            value = normalized.blurRadiusDp,
            valueRange = MIN_MODAL_BLUR_RADIUS_DP..MAX_MODAL_BLUR_RADIUS_DP,
            onValueChange = { radius ->
                onPreviewChange(normalized.copy(blurRadiusDp = radius))
            },
            onValueCommit = { radius ->
                onCommitChange(normalized.copy(blurRadiusDp = radius).normalized())
            },
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Tint color",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ModalBackdropTintPreset.entries.forEach { preset ->
                    FilterChip(
                        selected = tintPreset == preset,
                        onClick = {
                            val next = normalized.withPreset(preset)
                            onPreviewChange(next)
                            onCommitChange(next.normalized())
                        },
                        label = { Text(preset.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = scheme.primaryContainer,
                            selectedLabelColor = scheme.onPrimaryContainer,
                        ),
                    )
                }
            }
        }

        if (!normalized.autoTint) {
            TintHueControl(
                hue = normalized.tintHue,
                swatchColor = tintColor,
                onHueChange = { hue ->
                    onPreviewChange(
                        normalized.copy(
                            autoTint = false,
                            tintHue = hue,
                        ),
                    )
                },
                onHueCommit = { hue ->
                    onCommitChange(
                        normalized.copy(
                            autoTint = false,
                            tintHue = hue,
                        ).normalized(),
                    )
                },
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Surface tint",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(tintColor)
                        .border(1.dp, scheme.outlineVariant, CircleShape),
                )
            }
        }

        BlurSliderControl(
            label = "Tint opacity",
            valueLabel = "${(normalized.tintAlpha * 100).roundToInt()}%",
            value = normalized.tintAlpha,
            valueRange = MIN_MODAL_TINT_ALPHA..MAX_MODAL_TINT_ALPHA,
            onValueChange = { alpha ->
                onPreviewChange(normalized.copy(tintAlpha = alpha))
            },
            onValueCommit = { alpha ->
                onCommitChange(normalized.copy(tintAlpha = alpha).normalized())
            },
        )

        Text(
            text = "Preview updates live; blur strength and tint save when you release a slider.",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BlurBackdropPreview(
    style: ModalBackdropStyle,
    blurSupported: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val darkTheme = isSystemInDarkTheme()
    val hazeState = rememberHazeState(blurEnabled = blurSupported)
    val sheetHeight = 104.dp

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = scheme.surfaceContainerLowest,
        tonalElevation = 1.dp,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, scheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(212.dp)
                .clip(RoundedCornerShape(20.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState),
            ) {
                PreviewCatalogScene(darkTheme = darkTheme)
            }

            key(
                style.blurType,
                style.blurRadiusDp,
                style.tintAlpha,
                style.tintHue,
                style.autoTint,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = sheetHeight)
                        .modalBackdropHazeEffect(
                            state = hazeState,
                            style = style,
                        ),
                )
            }

            PreviewTypeBadge(
                blurType = style.blurType,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
            )

            PreviewBottomSheet(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(sheetHeight),
            )
        }
    }
}

@Composable
private fun PreviewCatalogScene(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val backdropBrush = remember(scheme, darkTheme) {
        Brush.radialGradient(
            colors = listOf(
                scheme.primary.copy(alpha = if (darkTheme) 0.55f else 0.72f),
                scheme.tertiary.copy(alpha = if (darkTheme) 0.42f else 0.58f),
                scheme.secondary.copy(alpha = if (darkTheme) 0.38f else 0.5f),
                scheme.background.copy(alpha = 0.95f),
            ),
            radius = 720f,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backdropBrush)
            .drawBehind {
                val step = 14.dp.toPx()
                val lineColor = scheme.onPrimary.copy(alpha = if (darkTheme) 0.12f else 0.18f)
                var x = 0f
                while (x < size.width) {
                    drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += step
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += step
                }
            },
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .align(Alignment.TopEnd)
                .offset(x = 18.dp, y = (-12).dp)
                .clip(CircleShape)
                .background(scheme.tertiary.copy(alpha = 0.45f)),
        )
        Box(
            modifier = Modifier
                .size(54.dp)
                .align(Alignment.CenterStart)
                .offset(x = (-10).dp, y = 8.dp)
                .clip(CircleShape)
                .background(scheme.secondary.copy(alpha = 0.5f)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PreviewStatusRow()
            Text(
                text = "UITrends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimary.copy(alpha = 0.92f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PreviewDemoTile(
                    title = "Glass",
                    accent = scheme.primary,
                    modifier = Modifier.weight(1f),
                )
                PreviewDemoTile(
                    title = "Spatial",
                    accent = scheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
            }
            PreviewDemoTile(
                title = "Kinetic typography",
                accent = scheme.secondary,
                modifier = Modifier.fillMaxWidth(0.78f),
            )
        }
    }
}

@Composable
private fun PreviewStatusRow(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "9:41",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onPrimary.copy(alpha = 0.8f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(scheme.onPrimary.copy(alpha = 0.65f)),
                )
            }
        }
    }
}

@Composable
private fun PreviewDemoTile(
    title: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        color = scheme.surface.copy(alpha = 0.9f),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accent,
                                accent.copy(alpha = 0.55f),
                            ),
                        ),
                    ),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                )
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(scheme.onSurfaceVariant.copy(alpha = 0.28f)),
                )
            }
        }
    }
}

@Composable
private fun PreviewTypeBadge(
    blurType: ModalBackdropBlurType,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = scheme.surface.copy(alpha = 0.88f),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = blurType.icon(),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = scheme.primary,
            )
            Text(
                text = blurType.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
        }
    }
}

@Composable
private fun PreviewBottomSheet(
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        color = scheme.surfaceContainerHigh,
        tonalElevation = 4.dp,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(scheme.outlineVariant),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = "Backdrop blur",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PreviewSheetPill(
                        label = "Cancel",
                        filled = false,
                    )
                    PreviewSheetPill(
                        label = "Apply",
                        filled = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewSheetPill(
    label: String,
    filled: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = if (filled) scheme.primary else scheme.surfaceContainerHighest,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (filled) scheme.onPrimary else scheme.onSurface,
            maxLines = 1,
        )
    }
}

@Composable
private fun BlurSliderControl(
    label: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueCommit: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    var pendingValue by remember(value) { mutableFloatStateOf(value) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
            )
        }
        HapticSlider(
            value = value,
            onValueChange = { next ->
                pendingValue = next
                onValueChange(next)
            },
            onValueChangeFinished = { onValueCommit(pendingValue) },
            valueRange = valueRange,
        )
    }
}

@Composable
private fun TintHueControl(
    hue: Float,
    swatchColor: Color,
    onHueChange: (Float) -> Unit,
    onHueCommit: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    var pendingHue by remember(hue) { mutableFloatStateOf(hue) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = scheme.surfaceContainerLow,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Hue",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .border(1.dp, scheme.outlineVariant, CircleShape),
                    )
                    Text(
                        text = "${hue.roundToInt()}°",
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.primary,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(HueSpectrumBrush),
            )
            HapticSlider(
                value = hue,
                onValueChange = {
                    pendingHue = it
                    onHueChange(it)
                },
                onValueChangeFinished = { onHueCommit(pendingHue) },
                valueRange = 0f..360f,
            )
        }
    }
}

private fun ModalBackdropBlurType.icon(): ImageVector = when (this) {
    ModalBackdropBlurType.Default -> Icons.Outlined.BlurOn
    ModalBackdropBlurType.Mosaic -> Icons.Outlined.GridOn
    ModalBackdropBlurType.Progressive -> Icons.Outlined.Gradient
    ModalBackdropBlurType.Frosted -> Icons.Outlined.Grain
}

private val HueSpectrumBrush: Brush
    get() = Brush.horizontalGradient(
        colors = (0..12).map { index -> Color.hsv(index * 30f, 0.78f, 0.88f) },
    )

private fun ModalBackdropStyle.tintPreset(): ModalBackdropTintPreset {
    if (autoTint) return ModalBackdropTintPreset.Auto
    return when (tintHue.roundToInt()) {
        210 -> ModalBackdropTintPreset.Cool
        28 -> ModalBackdropTintPreset.Warm
        275 -> ModalBackdropTintPreset.Violet
        else -> ModalBackdropTintPreset.Custom
    }
}

private fun ModalBackdropStyle.withPreset(preset: ModalBackdropTintPreset): ModalBackdropStyle = when (preset) {
    ModalBackdropTintPreset.Auto -> copy(autoTint = true)
    ModalBackdropTintPreset.Custom -> copy(autoTint = false)
    else -> copy(
        autoTint = false,
        tintHue = preset.hue ?: tintHue,
    )
}
