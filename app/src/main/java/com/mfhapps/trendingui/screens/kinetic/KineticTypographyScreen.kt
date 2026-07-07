@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.mfhapps.trendingui.screens.kinetic

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.theme.LocalHomeCatalogColors
import com.mfhapps.trendingui.ui.theme.launcherIconGradientBackground
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin

private val DetailTopBarHeight = 56.dp
private const val HeroImageHeightDp = 300

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KineticTypographyScreen() {
    val scheme = MaterialTheme.colorScheme
    val catalogColors = LocalHomeCatalogColors.current
    val scroll = rememberScrollState()
    val reduceMotion = LocalReduceMotion.current
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + DetailTopBarHeight
    val heroHeight = HeroImageHeightDp.dp + topInset

    var prevScroll by remember { mutableIntStateOf(0) }
    var lastTickMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var velocityNorm by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        snapshotFlow { scroll.value }.collect { current ->
            val now = System.currentTimeMillis()
            val dt = (now - lastTickMs).coerceAtLeast(1L)
            val delta = abs(current - prevScroll).toFloat()
            velocityNorm = (delta / dt / 3f).coerceIn(0f, 1f)
            prevScroll = current
            lastTickMs = now
        }
    }

    val animatedT = remember { Animatable(0f) }
    val scrollT by remember {
        derivedStateOf { (scroll.value / 600f).coerceIn(0f, 1f) }
    }
    LaunchedEffect(scrollT, reduceMotion) {
        if (reduceMotion) {
            animatedT.snapTo(0.5f)
        } else {
            animatedT.animateTo(scrollT, tween(160))
        }
    }

    val t = if (reduceMotion) 0.5f else animatedT.value
    val letterSpacing = (6f - t * 6f).coerceIn(0f, 6f)
    val weight = FontWeight((300 + t * 500).toInt().coerceIn(300, 800))
    val widthScale = 0.92f + t * 0.14f

    val boldEmphasis by animateFloatAsState(
        targetValue = if (reduceMotion) 0.5f else if (velocityNorm > 0.2f) 1f else 0.35f,
        label = "weightCrossfade",
    )

    val headline = "KINETIC TYPE"
    var visibleCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            visibleCount = headline.length
        } else {
            headline.forEachIndexed { index, _ ->
                delay(index * 35L)
                visibleCount = index + 1
            }
        }
    }

    val waveWord = "WAVE"
    val expandWord = "EXPAND"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .appHazeSource()
            .background(scheme.background)
            .navigationBarsPadding()
            .verticalScroll(scroll),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .launcherIconGradientBackground()
                    .graphicsLayer {
                        alpha = (1f - scroll.value / 500f).coerceIn(0.2f, 1f)
                    },
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to scheme.scrim.copy(alpha = 0.35f),
                                0.45f to Color.Transparent,
                                0.72f to scheme.scrim.copy(alpha = 0.5f),
                                1f to scheme.background.copy(alpha = 0.92f),
                            ),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Kinetic Type",
                    style = MaterialTheme.typography.displaySmall,
                    color = catalogColors.headerTitle,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Scroll-driven typography",
                    style = MaterialTheme.typography.titleMedium,
                    color = catalogColors.headerSubtitle,
                )
                Text(
                    text = "Header merges with detail chrome",
                    style = MaterialTheme.typography.labelLarge,
                    color = catalogColors.headerSubtitle.copy(alpha = 0.85f),
                    modifier = Modifier.graphicsLayer {
                        alpha = (scroll.value / 280f).coerceIn(0f, 1f)
                    },
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            KineticSectionLabel(
                title = "Stagger reveal",
                body = "Letters appear in sequence on load; respects reduce motion.",
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                headline.forEachIndexed { index, ch ->
                    if (index < visibleCount) {
                        Text(
                            text = ch.toString(),
                            style = MaterialTheme.typography.displayMedium.copy(
                                textMotion = TextMotion.Animated,
                            ),
                            color = scheme.onBackground,
                            fontWeight = weight,
                            letterSpacing = letterSpacing.sp,
                            modifier = Modifier.graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 0.5f)
                                scaleX = widthScale
                            },
                        )
                    }
                }
            }

            KineticSectionLabel(
                title = "Scroll axes",
                body = "Weight, tracking, and width interpolate as you scroll this page.",
            )
            Text(
                text = "Variable axis crossfade",
                color = scheme.onBackground,
                style = TextStyle(
                    fontSize = 22.sp,
                    textMotion = TextMotion.Animated,
                ),
                fontWeight = FontWeight((300 + boldEmphasis * 500).toInt()),
                modifier = Modifier.graphicsLayer {
                    scaleX = 0.85f + boldEmphasis * 0.18f
                    scaleY = 0.85f + boldEmphasis * 0.18f
                },
            )

            KineticSectionLabel(
                title = "Velocity emphasis",
                body = "Fling speed temporarily pushes weight toward 800.",
            )
            Text(
                text = "Speed ${(velocityNorm * 100).toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                color = lerp(scheme.onSurfaceVariant, scheme.primary, velocityNorm),
                fontWeight = FontWeight((400 + velocityNorm * 400).toInt()),
            )

            KineticSectionLabel(
                title = "Color drift",
                body = "Primary → tertiary blend tied to scroll progress.",
            )
            Text(
                text = "CHROMATIC SHIFT",
                style = MaterialTheme.typography.headlineMediumEmphasized,
                color = lerp(scheme.primary, scheme.tertiary, t),
                letterSpacing = ((1f - t) * 4f).sp,
                fontWeight = FontWeight.Bold,
            )

            KineticSectionLabel(
                title = "Per-glyph wave",
                body = "Vertical offset follows scroll index for a wave motion.",
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                waveWord.forEachIndexed { index, ch ->
                    Text(
                        text = ch.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = scheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.graphicsLayer {
                            val phase = scroll.value / 40f + index * 0.9f
                            translationY = sin(phase) * 10f
                        },
                    )
                }
            }

            KineticSectionLabel(
                title = "Scale pulse",
                body = "Each character scales with scroll offset.",
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                expandWord.forEachIndexed { index, ch ->
                    Text(
                        text = ch.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = scheme.secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.graphicsLayer {
                            val localT = ((scroll.value / 80f) + index * 0.15f) % 1f
                            val scale = 0.88f + localT * 0.28f
                            scaleX = scale
                            scaleY = scale
                        },
                    )
                }
            }

            KineticSectionLabel(
                title = "Opacity stack",
                body = "Layered copies at different alphas for depth.",
            )
            Box {
                listOf(0.18f, 0.38f, 1f).forEachIndexed { layer, alpha ->
                    Text(
                        text = "DEPTH",
                        style = MaterialTheme.typography.displayLarge,
                        color = scheme.onBackground.copy(alpha = alpha),
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.graphicsLayer {
                            translationX = layer * 6f
                            translationY = layer * 4f
                        },
                    )
                }
            }

            Text(
                text = "On Android 12+, variable fonts interpolate weight and width. " +
                    "Disable motion in system settings to see static fallbacks.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun KineticSectionLabel(
    title: String,
    body: String,
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = scheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}
