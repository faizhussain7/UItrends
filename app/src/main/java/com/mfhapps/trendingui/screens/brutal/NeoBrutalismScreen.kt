package com.mfhapps.trendingui.screens.brutal

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import com.mfhapps.trendingui.ui.demo.DemoAnimatedSection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.detail.demoDetailScrollBottomGap
import com.mfhapps.trendingui.ui.detail.demoDetailScrollInsets
import com.mfhapps.trendingui.ui.brutal.BrutalShapes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NeoBrutalismScreen() {
    val scheme = MaterialTheme.colorScheme
    val ink = scheme.onSurface
    var segment by remember { mutableIntStateOf(0) }

    MaterialTheme(shapes = BrutalShapes) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(scheme.background)
                .demoDetailScrollInsets()
                .verticalScroll(rememberScrollState())
                .demoDetailScrollBottomGap(),
        ) {
            DemoAnimatedSection(index = 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(scheme.tertiaryContainer)
                        .border(3.dp, ink)
                        .padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "NEO\nBRUTAL",
                            style = MaterialTheme.typography.displayMedium,
                            color = scheme.onTertiaryContainer,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = "Hard edges · offset shadows · unapologetic contrast",
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onTertiaryContainer.copy(alpha = 0.9f),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                DemoAnimatedSection(index = 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BrutalSection(
                            title = "Actions",
                            body = "Press moves the face into the shadow — tactile, not soft.",
                        )
                        BrutalButton(
                            label = "Primary action",
                            modifier = Modifier.fillMaxWidth(),
                            ink = scheme.onPrimary,
                            fill = scheme.primary,
                        )
                        BrutalButton(
                            label = "Secondary",
                            modifier = Modifier.fillMaxWidth(),
                            ink = scheme.onSecondaryContainer,
                            fill = scheme.secondaryContainer,
                        )
                        BrutalButton(
                            label = "Destructive",
                            modifier = Modifier.fillMaxWidth(),
                            ink = scheme.onErrorContainer,
                            fill = scheme.errorContainer,
                            shadowInk = scheme.error,
                        )
                    }
                }

                DemoAnimatedSection(index = 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BrutalSection(
                            title = "Icon controls",
                            body = "Square hit targets with the same offset shadow language.",
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BrutalIconButton(
                                icon = Icons.Default.Bolt,
                                contentDescription = "Bolt",
                                fill = scheme.primaryContainer,
                                iconTint = scheme.onPrimaryContainer,
                            )
                            BrutalIconButton(
                                icon = Icons.Default.Star,
                                contentDescription = "Star",
                                fill = scheme.secondaryContainer,
                                iconTint = scheme.onSecondaryContainer,
                            )
                            BrutalIconButton(
                                icon = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                fill = scheme.tertiaryContainer,
                                iconTint = scheme.onTertiaryContainer,
                            )
                        }
                    }
                }

                DemoAnimatedSection(index = 3) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BrutalSection(
                            title = "Segmented control",
                            body = "Zero radius tabs — pick a layout mode.",
                        )
                        BrutalSegmentedRow(
                            labels = listOf("Grid", "List", "Masonry"),
                            selectedIndex = segment,
                            onSelected = { segment = it },
                            ink = ink,
                        )
                    }
                }

                DemoAnimatedSection(index = 4) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BrutalSection(
                            title = "Tags & stats",
                            body = "Chips and metric blocks for dashboards.",
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("UI", "MOTION", "BOLD", "2026").forEach { tag ->
                                BrutalChip(label = tag, ink = ink, fill = scheme.surfaceContainerHigh)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            BrutalStatCard(
                                label = "Views",
                                value = "128K",
                                modifier = Modifier.weight(1f),
                                ink = ink,
                                fill = scheme.primaryContainer,
                                valueColor = scheme.onPrimaryContainer,
                            )
                            BrutalStatCard(
                                label = "Saves",
                                value = "4.2K",
                                modifier = Modifier.weight(1f),
                                ink = ink,
                                fill = scheme.secondaryContainer,
                                valueColor = scheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                DemoAnimatedSection(index = 5) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BrutalSection(
                            title = "Alert banner",
                            body = "High-contrast callout with thick stroke.",
                        )
                        BrutalBanner(
                            title = "Ship it loud",
                            body = "Brutalism rejects subtle gradients. Use flat fills and visible borders.",
                            ink = ink,
                            fill = scheme.errorContainer,
                            titleColor = scheme.onErrorContainer,
                            bodyColor = scheme.onErrorContainer.copy(alpha = 0.85f),
                        )
                    }
                }

                DemoAnimatedSection(index = 6) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BrutalSection(
                            title = "Asymmetric block",
                            body = "Editorial layout with intentional imbalance.",
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(3.dp, ink)
                                .background(scheme.surfaceContainerHighest)
                                .padding(20.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Offset column",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = scheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Pair brutal surfaces with Material 3 dynamic color for modern contrast.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = scheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun BrutalSection(
    title: String,
    body: String,
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = scheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BrutalButton(
    label: String,
    modifier: Modifier = Modifier,
    ink: Color,
    fill: Color,
    shadowInk: Color = ink,
) {
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    val reduceMotion = LocalReduceMotion.current

    val pressAnim by animateFloatAsState(
        targetValue = if (pressed) 4f else 0f,
        animationSpec = if (reduceMotion) snap() else spring(stiffness = Spring.StiffnessHigh),
        label = "brutalPress",
    )

    Box(modifier = modifier.height(56.dp).minimumInteractiveComponentSize()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(5.dp, 5.dp)
                .background(shadowInk),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationX = pressAnim
                    translationY = pressAnim
                }
                .border(3.dp, ink)
                .then(
                    if (focused) Modifier.border(4.dp, MaterialTheme.colorScheme.primary)
                    else Modifier,
                )
                .background(fill)
                .clickable(interactionSource = interaction, indication = null) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                .semantics { role = Role.Button },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = ink,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BrutalIconButton(
    icon: ImageVector,
    contentDescription: String,
    fill: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    val ink = MaterialTheme.colorScheme.onSurface
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val pressAnim by animateFloatAsState(
        targetValue = if (pressed) 3f else 0f,
        animationSpec = if (reduceMotion) snap() else spring(stiffness = Spring.StiffnessHigh),
        label = "brutalIconPress",
    )

    Box(modifier.size(56.dp)) {
        Box(
            Modifier
                .matchParentSize()
                .offset(4.dp, 4.dp)
                .background(ink),
        )
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationX = pressAnim
                    translationY = pressAnim
                }
                .border(3.dp, ink)
                .background(fill)
                .clickable(interactionSource = interaction, indication = null) {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
                .semantics { role = Role.Button },
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription, tint = iconTint)
        }
    }
}

@Composable
private fun BrutalChip(
    label: String,
    ink: Color,
    fill: Color,
) {
    Box(
        modifier = Modifier
            .offset(3.dp, 3.dp)
            .background(ink)
            .padding(end = 3.dp, bottom = 3.dp),
    ) {
        Box(
            modifier = Modifier
                .border(2.dp, ink)
                .background(fill)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = ink,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BrutalStatCard(
    label: String,
    value: String,
    ink: Color,
    fill: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Box(
            Modifier
                .matchParentSize()
                .offset(4.dp, 4.dp)
                .background(ink),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, ink)
                .background(fill)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = valueColor.copy(alpha = 0.85f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = valueColor,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun BrutalBanner(
    title: String,
    body: String,
    ink: Color,
    fill: Color,
    titleColor: Color,
    bodyColor: Color,
) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .matchParentSize()
                .offset(5.dp, 5.dp)
                .background(ink),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, ink)
                .background(fill)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = bodyColor,
            )
        }
    }
}

@Composable
private fun BrutalSegmentedRow(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    ink: Color,
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, ink)
            .background(scheme.surface),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(
                targetValue = if (selected) scheme.primary else scheme.surface,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "brutalSegBg",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(bg)
                    .clickable {
                        onSelected(index)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) scheme.onPrimary else scheme.onSurface,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
            }
            if (index < labels.lastIndex) {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(48.dp)
                        .background(ink),
                )
            }
        }
    }
}
