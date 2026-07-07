@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.mfhapps.trendingui.screens.bento

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.ButtonGroup
import com.mfhapps.trendingui.ui.components.DemoScreenContainer

private enum class BentoType { Hero, Weather, Music, Stats, Calendar, Actions }

private data class BentoItem(
    val id: Int,
    val title: String,
    val type: BentoType,
    val minHeightDp: Int,
    val fullLine: Boolean = false,
)

@Composable
fun BentoGridScreen() {
    var editMode by remember { mutableStateOf(false) }
    var pressedId by remember { mutableIntStateOf(-1) }
    val reduceMotion = LocalReduceMotion.current
    val haptics = LocalHapticFeedback.current

    val items = remember {
        listOf(

            BentoItem(0, "Today", BentoType.Hero, minHeightDp = 200, fullLine = true),
            BentoItem(1, "Weather", BentoType.Weather, minHeightDp = 120),
            BentoItem(2, "Now Playing", BentoType.Music, minHeightDp = 160),
            BentoItem(3, "Steps", BentoType.Stats, minHeightDp = 120),

            BentoItem(4, "Calendar", BentoType.Calendar, minHeightDp = 160, fullLine = true),
            BentoItem(5, "Quick", BentoType.Actions, minHeightDp = 120),
            BentoItem(6, "Focus", BentoType.Stats, minHeightDp = 120),
        )
    }

    DemoScreenContainer(
        title = "Bento Grid",
        subtitle = "Staggered grid · hero anchor · long-press edit",
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
        ) {
            items(
                items = items,
                key = { it.id },
                contentType = { it.type.name },
                span = { item ->
                    if (item.fullLine) StaggeredGridItemSpan.FullLine
                    else StaggeredGridItemSpan.SingleLane
                },
            ) { item ->
                val targetScale = when {
                    pressedId == item.id -> 0.96f
                    editMode -> if (reduceMotion) 1f else 0.97f
                    else -> 1f
                }
                val scale by animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = spring(
                        dampingRatio = 0.85f,
                        stiffness = Spring.StiffnessLow,
                    ),
                    label = "bentoPress",
                )

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .heightIn(min = item.minHeightDp.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .pointerInput(item.id) {
                            detectTapGestures(
                                onLongPress = {
                                    editMode = !editMode
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onPress = {
                                    pressedId = item.id
                                    tryAwaitRelease()
                                    pressedId = -1
                                },
                            )
                        }
                        .semantics(mergeDescendants = true) {
                            role = Role.Button
                            contentDescription = "${item.title} tile. Long press to reorder."
                        },
                    shape = MaterialTheme.shapes.large,

                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = if (editMode) 8.dp else 2.dp,
                    ),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()

                            .padding(20.dp),
                    ) {
                        BentoCardContent(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoCardContent(item: BentoItem) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (item.type) {
            BentoType.Hero -> {
                Text(item.title, style = MaterialTheme.typography.displaySmall)
                Text("Your day at a glance", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("3 meetings · 72°F · 8 432 steps",
                    style = MaterialTheme.typography.bodyLarge)
            }
            BentoType.Weather -> {
                Text(item.title, style = MaterialTheme.typography.titleLargeEmphasized)
                Text("72°F · Sunny", style = MaterialTheme.typography.bodyMedium)
            }
            BentoType.Music -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {


                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                    ),
                                ),
                            ),
                    )
                    Column {
                        Text(item.title, style = MaterialTheme.typography.titleSmall)
                        Text("Lofi · Chillhop", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                @OptIn(ExperimentalMaterial3ExpressiveApi::class)
                LinearWavyProgressIndicator(
                    progress = { 0.42f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape),
                )
                Text("Track 4 of 12", style = MaterialTheme.typography.labelMedium)
            }
            BentoType.Stats -> {
                Text("8 432", style = MaterialTheme.typography.displaySmall)
                Text(item.title, style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            BentoType.Calendar -> {
                Text(item.title, style = MaterialTheme.typography.headlineMediumEmphasized)
                Text("3 events today", style = MaterialTheme.typography.bodyMedium)
                Text("Standup · Design review · Retro",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            BentoType.Actions -> {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                var sel by remember { mutableIntStateOf(0) }
                ButtonGroup {
                    ToggleButton(checked = sel == 0, onCheckedChange = { sel = 0 }) { Text("A") }
                    ToggleButton(checked = sel == 1, onCheckedChange = { sel = 1 }) { Text("B") }
                }
            }
        }
    }
}
