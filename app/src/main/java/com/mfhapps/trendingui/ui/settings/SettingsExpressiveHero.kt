package com.mfhapps.trendingui.ui.settings

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.components.BrandMark
import com.mfhapps.trendingui.ui.theme.AppFontStyle
import com.mfhapps.trendingui.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsExpressiveHeroCard(
    themeMode: ThemeMode,
    appFontStyle: AppFontStyle,
    dynamicColorEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val heroStops = remember(scheme) {
        listOf(
            scheme.primary,
            lerp(scheme.primary, scheme.tertiary, 0.35f),
            scheme.tertiary,
        )
    }
    val heroBrush = remember(heroStops) {
        Brush.linearGradient(
            colors = heroStops,
            start = Offset.Zero,
            end = Offset(900f, 500f),
        )
    }
    val infiniteTransition = rememberInfiniteTransition(label = "settingsHero")
    val syncPulse by infiniteTransition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "settingsHeroPulse",
    )
    val themeLabel = when (themeMode) {
        ThemeMode.System -> "System theme"
        ThemeMode.Light -> "Light theme"
        ThemeMode.Dark -> "Dark theme"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp),
        shape = MaterialTheme.shapes.extraLargeIncreased,
        color = Color.Transparent,
        tonalElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroBrush)
                .padding(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 24.dp, y = (-28).dp)
                    .size(112.dp)
                    .clip(MaterialShapes.Sunny.toShape())
                    .background(scheme.onPrimary.copy(alpha = 0.1f)),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(
                        shape = MaterialShapes.Clover4Leaf.toShape(),
                        color = scheme.onPrimary.copy(alpha = 0.16f),
                    ) {
                        BrandMark(
                            size = 44.dp,
                            animated = true,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "Appearance studio",
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            color = scheme.onPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "$themeLabel · ${appFontStyle.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onPrimary.copy(alpha = 0.86f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                LinearWavyProgressIndicator(
                    progress = { syncPulse },
                    modifier = Modifier.fillMaxWidth(),
                    color = scheme.onPrimary,
                    trackColor = scheme.onPrimary.copy(alpha = 0.28f),
                )
                Text(
                    text = if (dynamicColorEnabled) {
                        "Dynamic color on · typography & theme in sync"
                    } else {
                        "Brand palette · pick typeface below"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onPrimary.copy(alpha = 0.82f),
                )
            }
        }
    }
}
