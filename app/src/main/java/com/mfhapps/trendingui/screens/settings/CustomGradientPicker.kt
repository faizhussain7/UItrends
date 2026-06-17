package com.mfhapps.trendingui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import com.mfhapps.trendingui.ui.components.HapticSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.theme.CustomGradientConfig
import com.mfhapps.trendingui.ui.theme.HomeGradientStyle
import com.mfhapps.trendingui.ui.theme.previewBrush
import kotlin.math.roundToInt

@Composable
fun CustomGradientPicker(
    config: CustomGradientConfig,
    onPreviewChange: (CustomGradientConfig) -> Unit,
    onCommitChange: (CustomGradientConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val darkTheme = isSystemInDarkTheme()
    val previewBrush = HomeGradientStyle.Custom.previewBrush(
        colorScheme = scheme,
        customGradient = config,
    )
    val startColor = config.startColor(darkTheme)
    val endColor = config.endColor(darkTheme)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Custom gradient",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, scheme.outlineVariant, RoundedCornerShape(16.dp))
                .background(previewBrush),
        )

        GradientStopControl(
            label = "Start color",
            hue = config.startHue,
            swatchColor = startColor,
            onHueChange = { onPreviewChange(config.copy(startHue = it)) },
            onHueCommit = { onCommitChange(config.copy(startHue = it)) },
        )

        GradientStopControl(
            label = "End color",
            hue = config.endHue,
            swatchColor = endColor,
            onHueChange = { onPreviewChange(config.copy(endHue = it)) },
            onHueCommit = { onCommitChange(config.copy(endHue = it)) },
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Saturation",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${(config.saturation * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.primary,
                )
            }
            HapticSlider(
                value = config.saturation,
                onValueChange = { onPreviewChange(config.copy(saturation = it)) },
                onValueChangeFinished = { onCommitChange(config) },
                valueRange = 0.22f..0.78f,
            )
        }

        Text(
            text = "Preview updates live; changes are saved when you release a slider.",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GradientStopControl(
    label: String,
    hue: Float,
    swatchColor: Color,
    onHueChange: (Float) -> Unit,
    onHueCommit: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
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
                    text = label,
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
                onValueChange = onHueChange,
                onValueChangeFinished = { onHueCommit(hue) },
                valueRange = 0f..360f,
            )
        }
    }
}

private val HueSpectrumBrush: Brush
    get() = Brush.horizontalGradient(
        colors = (0..12).map { i -> Color.hsv(i * 30f, 0.78f, 0.88f) },
    )
