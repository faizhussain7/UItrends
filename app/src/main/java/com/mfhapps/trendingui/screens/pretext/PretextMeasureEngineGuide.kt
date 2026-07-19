package com.mfhapps.trendingui.screens.pretext

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import com.mfhapps.trendingui.ui.components.IconButton
import com.mfhapps.trendingui.ui.components.expressiveClickable
import com.mfhapps.trendingui.ui.components.rememberExpressiveMorphPress

enum class CameraSettingsPage {
    Root,
    EngineGuide,
}

data class MeasureEngineGuideSection(
    val title: String,
    val body: String,
    val bullets: List<String> = emptyList(),
)

data class MeasureEngineGuide(
    val mode: PretextMeasureMode,
    val headline: String,
    val tagline: String,
    val icon: ImageVector,
    val pipelineSteps: List<String>,
    val sections: List<MeasureEngineGuideSection>,
)

fun measureEngineGuide(mode: PretextMeasureMode): MeasureEngineGuide = when (mode) {
    PretextMeasureMode.Engine -> MeasureEngineGuide(
        mode = mode,
        headline = "Pretext engine",
        tagline = "Prepare once. Layout in arithmetic. Reflow every frame.",
        icon = Icons.Outlined.Speed,
        pipelineSteps = listOf(
            "prepare() → grapheme advances + break units cached",
            "Obstacle bands → carved text slots (shared geometry)",
            "EngineColumnMeasureSource fills each slot from advances",
            "PositionedTextLayout → PretextPositionedCanvas",
        ),
        sections = listOf(
            MeasureEngineGuideSection(
                title = "What it measures",
                body = "Widths come from a one-time prepare() pass. At reflow time the engine only walks " +
                    "cached unit advances — no Paint.breakText and no TextView.measure per line.",
                bullets = listOf(
                    "Grapheme-aware units (emoji / complex scripts)",
                    "layoutColumn / layoutNewspaper / layoutDynamic share the same prepared text",
                    "Ideal when obstacle shapes change every frame",
                ),
            ),
            MeasureEngineGuideSection(
                title = "Shared architecture",
                body = "Engine and View.measure use the same CameraReflowRequest and slot-carving " +
                    "pipeline. Only the per-slot line filler differs.",
                bullets = listOf(
                    "PretextCameraReflowScheduler.computeLayout(mode, …)",
                    "PretextCameraReflowBenchmark times both backends on the same request",
                    "EMA-smoothed timings drive the sheet segments and camera chip",
                ),
            ),
            MeasureEngineGuideSection(
                title = "When to use it",
                body = "Default choice for production-style reflow demos — especially Newspaper, " +
                    "Magazine, and Dynamic float layouts that lean on prepared text.",
            ),
        ),
    )

    PretextMeasureMode.ViewMeasure -> MeasureEngineGuide(
        mode = mode,
        headline = "View.measure",
        tagline = "Classic Paint.breakText probing on the same carved slots.",
        icon = Icons.Outlined.TextFields,
        pipelineSteps = listOf(
            "Same obstacle bands → carved text slots",
            "ViewPaintColumnMeasureSource probes each slot with Paint.breakText",
            "Word-boundary adjust (last space) when a probe lands mid-word",
            "PositionedTextLayout → same canvas renderer as Engine",
        ),
        sections = listOf(
            MeasureEngineGuideSection(
                title = "What it measures",
                body = "Each slot width is filled the way classic Android text layout probes available " +
                    "width — Paint.breakText — instead of walking precomputed grapheme advances.",
                bullets = listOf(
                    "No prepare() cache required for Column wrap / Uniform",
                    "Fair baseline against TextView / StaticLayout-style measuring",
                    "Newspaper / Magazine / Dynamic still share page geometry with Engine",
                ),
            ),
            MeasureEngineGuideSection(
                title = "Shared architecture",
                body = "View.measure is not a bolt-on TextView. It plugs into the same column pipeline " +
                    "as PretextColumnMeasureSource, so contours, padding, and styles stay identical.",
                bullets = listOf(
                    "ViewPaintColumnMeasureSource : PretextColumnMeasureSource",
                    "layoutColumn(source = …) is the single slot walker",
                    "Benchmark runs Engine + View on one CameraReflowRequest",
                ),
            ),
            MeasureEngineGuideSection(
                title = "When to use it",
                body = "Use it to feel the cost of per-band width probing next to the engine — " +
                    "especially on Column wrap while watching the live fast/slow chip.",
            ),
        ),
    )
}

@Composable
fun PretextMeasureEngineGuideEntry(
    measureMode: PretextMeasureMode,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val morph = rememberExpressiveMorphPress(interactionSource = interaction)
    ListItem(
        headlineContent = { Text("How ${measureMode.shortLabel} works") },
        supportingContent = {
            Text(
                when (measureMode) {
                    PretextMeasureMode.Engine -> "Prepare → slot carve → cached advances"
                    PretextMeasureMode.ViewMeasure -> "Slot carve → Paint.breakText per band"
                },
            )
        },
        leadingContent = {
            DecorativeIcon(
                Icons.Outlined.AccountTree,
                tint = scheme.primary,
                modifier = Modifier.size(22.dp),
            )
        },
        trailingContent = {
            DecorativeIcon(
                Icons.Outlined.Timeline,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .expressiveClickable(
                onClick = onOpen,
                interactionSource = interaction,
                morphPress = morph,
            )
            .padding(bottom = 4.dp),
    )
}

@Composable
fun PretextMeasureEngineGuidePage(
    mode: PretextMeasureMode,
    speed: PretextMeasureSpeed,
    onBack: () -> Unit,
    onModeChange: (PretextMeasureMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val guide = measureEngineGuide(mode)
    val scheme = MaterialTheme.colorScheme
    val tag = speed.speedTag(mode)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back to camera settings",
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "How it works",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    text = guide.headline,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        PretextMeasureSelector(
            selected = mode,
            onSelected = onModeChange,
            speed = speed,
            modifier = Modifier.fillMaxWidth(),
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = scheme.surfaceContainerHigh,
            contentColor = scheme.onSurface,
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = scheme.primaryContainer,
                        contentColor = scheme.onPrimaryContainer,
                    ) {
                        DecorativeIcon(
                            guide.icon,
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(22.dp),
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = guide.tagline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                        if (speed.nanosFor(mode) > 0L) {
                            Text(
                                text = buildString {
                                    append("Live · ")
                                    append(speed.format(mode))
                                    if (tag != null) {
                                        append(" · ")
                                        append(tag)
                                    }
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = when (tag) {
                                    "fast" -> scheme.tertiary
                                    "slow" -> scheme.error
                                    else -> scheme.primary
                                },
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }

                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.5f))

                Text(
                    text = "Pipeline",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                guide.pipelineSteps.forEachIndexed { index, step ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary,
                            modifier = Modifier
                                .background(
                                    scheme.primary.copy(alpha = 0.12f),
                                    RoundedCornerShape(8.dp),
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        guide.sections.forEach { section ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = scheme.surfaceContainerHigh,
                contentColor = scheme.onSurface,
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DecorativeIcon(
                            Icons.Outlined.Bolt,
                            tint = scheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        text = section.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                    if (section.bullets.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            section.bullets.forEach { bullet ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("•", color = scheme.primary)
                                    Text(
                                        text = bullet,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = scheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
