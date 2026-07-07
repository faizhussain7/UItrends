package com.mfhapps.trendingui.screens.semantic

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.mfhapps.trendingui.ui.detail.demoDetailScrollBottomGap
import com.mfhapps.trendingui.ui.detail.demoDetailScrollInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.mfhapps.trendingui.ui.components.FilledTonalButton
import androidx.compose.material3.Icon
import com.mfhapps.trendingui.ui.components.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.mfhapps.trendingui.ui.components.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.LoadingIndicator
import com.mfhapps.trendingui.ui.demo.DemoAnimatedSection
import com.mfhapps.trendingui.ui.detail.DemoPaneHeader
import com.mfhapps.trendingui.ui.detail.LocalDetailPaneActive
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SemanticMotionScreen() {
    val scheme = MaterialTheme.colorScheme
    val inDetailPane = LocalDetailPaneActive.current
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .demoDetailScrollInsets()
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp)
            .padding(top = if (inDetailPane) 8.dp else 16.dp)
            .demoDetailScrollBottomGap(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DemoPaneHeader(
            title = "Semantic Motion",
            subtitle = "Motion that explains what happened — not decoration.",
        )

        DemoAnimatedSection(index = 0) {
            SemanticSection(
                title = "Cause → effect",
                body = "Animations should answer “what changed?” — download started, item removed, action succeeded.",
            )
        }

        DemoAnimatedSection(index = 1) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SemanticSection(
                    title = "Drag to archive",
                    body = "Drag near the folder — it opens larger, then inhales the download card inside.",
                )
                DragToFolderDemo()
            }
        }

        DemoAnimatedSection(index = 2) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SemanticSection(
                    title = "Swipe to delete",
                    body = "Swipe toward the bin — it grows red, then shakes with a ripple burst when the item is gone.",
                )
                SwipeToDeleteDemo()
            }
        }

        DemoAnimatedSection(index = 3) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SemanticSection(
                    title = "State transition",
                    body = "Idle → loading → success uses cross-fade so the result is obvious.",
                )
                AsyncStateDemo()
            }
        }

        DemoAnimatedSection(index = 4) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SemanticSection(
                    title = "Reveal on demand",
                    body = "Expand and collapse tie height to meaning — details appear only when requested.",
                )
                ExpandableDetailDemo()
            }
        }

        DemoAnimatedSection(index = 5) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SemanticSection(
                    title = "Emphasis feedback",
                    body = "A short spring scale confirms toggle state without leaving the layout.",
                )
                EmphasisToggleDemo()
            }
        }

        DemoAnimatedSection(index = 6) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SemanticSection(
                    title = "Predictive back undo",
                    body = "After delete, pull the system back gesture — the banner tracks progress.",
                )
                PredictiveUndoDemo()
            }
        }

    }
}

@Composable
private fun SemanticSection(title: String, body: String) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.primary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SemanticPlaySurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
    ) {
        Box(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun DragToFolderDemo() {
    val scheme = MaterialTheme.colorScheme
    var phase by remember { mutableStateOf("idle") }
    var dragging by remember { mutableStateOf(false) }
    var folderOpenTarget by remember { mutableFloatStateOf(0f) }
    var archiveLocked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    val density = LocalDensity.current
    val releaseThresholdPx = with(density) { 64.dp.toPx() }
    val cardOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val cardAlpha = remember { Animatable(1f) }
    val cardScale = remember { Animatable(1f) }

    val breatheScale by animateFloatAsState(
        targetValue = if (dragging) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "breathe",
    )
    val folderOpenProgress by animateFloatAsState(
        targetValue = folderOpenTarget,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 340f),
        label = "folderOpenProgress",
    )

    SemanticPlaySurface {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AnimatedContent(
                targetState = phase,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "dragPhase",
            ) { state ->
                when (state) {
                    "moving" -> Row(verticalAlignment = Alignment.CenterVertically) {
                        LoadingIndicator(modifier = Modifier.size(28.dp))
                        Text(
                            text = "Moving into folder…",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurface,
                        )
                    }
                    "archived" -> Row(verticalAlignment = Alignment.CenterVertically) {
                        SemanticIcon(
                            imageVector = Icons.Default.CheckCircle,
                            tint = scheme.primary,
                        )
                        Text(
                            text = "Archived to folder",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurface,
                        )
                    }
                    else -> Text(
                        text = "Drag the download to the folder — it opens to take the file in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            val folderSlot = 72.dp
            val playHeight = 96.dp

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(playHeight)
                    .clip(RoundedCornerShape(12.dp)),
            ) {
                val cardSizePx = with(density) { 64.dp.toPx() }
                val folderSlotPx = with(density) { folderSlot.toPx() }
                val inhaleTargetX = (constraints.maxWidth - folderSlotPx / 2f - cardSizePx / 2f)
                    .coerceIn(releaseThresholdPx, constraints.maxWidth - cardSizePx)

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(folderSlot)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedFolderInhale(
                        openProgress = folderOpenProgress,
                        done = phase == "archived",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(64.dp)
                        .offset {
                            IntOffset(
                                cardOffset.value.x.roundToInt(),
                                cardOffset.value.y.roundToInt(),
                            )
                        }
                        .semantics { contentDescription = "File card. Drag toward folder." }
                        .pointerInput(archiveLocked, inhaleTargetX) {
                            if (archiveLocked) return@pointerInput
                            detectDragGestures(
                                onDragStart = {
                                    phase = "moving"
                                    dragging = true
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    scope.launch {
                                        val next = cardOffset.value + dragAmount
                                        val clampedX = next.x.coerceIn(0f, inhaleTargetX)
                                        cardOffset.snapTo(
                                            Offset(
                                                x = clampedX,
                                                y = next.y.coerceIn(-16f, 16f),
                                            ),
                                        )
                                        val norm = (clampedX / inhaleTargetX.coerceAtLeast(1f))
                                            .coerceIn(0f, 1f)
                                        folderOpenTarget = norm
                                    }
                                },
                                onDragEnd = {
                                    dragging = false
                                    scope.launch {
                                        if (cardOffset.value.x >= releaseThresholdPx) {
                                            archiveLocked = true
                                            phase = "moving"
                                            folderOpenTarget = 1f
                                            haptics.performHapticFeedback(
                                                HapticFeedbackType.TextHandleMove,
                                            )
                                            coroutineScope {
                                                launch {
                                                    cardScale.animateTo(
                                                        targetValue = 0.12f,
                                                        animationSpec = spring(
                                                            dampingRatio = 0.76f,
                                                            stiffness = 360f,
                                                        ),
                                                    )
                                                }
                                                launch {
                                                    cardOffset.animateTo(
                                                        targetValue = Offset(inhaleTargetX, 0f),
                                                        animationSpec = spring(
                                                            dampingRatio = 0.72f,
                                                            stiffness = 320f,
                                                        ),
                                                    )
                                                }
                                            }
                                            cardAlpha.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(220, easing = FastOutSlowInEasing),
                                            )
                                            delay(if (reduceMotion) 120L else 280L)
                                            folderOpenTarget = 0f
                                            delay(if (reduceMotion) 80L else 180L)
                                            phase = "archived"
                                            haptics.performHapticFeedback(
                                                HapticFeedbackType.Confirm,
                                            )
                                            delay(if (reduceMotion) 400L else 1200L)
                                            coroutineScope {
                                                launch {
                                                    cardOffset.animateTo(
                                                        targetValue = Offset.Zero,
                                                        animationSpec = spring(
                                                            dampingRatio = 0.68f,
                                                            stiffness = 300f,
                                                        ),
                                                    )
                                                }
                                                launch {
                                                    cardScale.animateTo(
                                                        targetValue = 1f,
                                                        animationSpec = spring(
                                                            dampingRatio = 0.68f,
                                                            stiffness = 360f,
                                                        ),
                                                    )
                                                }
                                            }
                                            cardAlpha.animateTo(
                                                targetValue = 1f,
                                                animationSpec = spring(
                                                    dampingRatio = 0.68f,
                                                    stiffness = 360f,
                                                ),
                                            )
                                            phase = "idle"
                                            archiveLocked = false
                                        } else {
                                            phase = "idle"
                                            folderOpenTarget = 0f
                                            cardScale.snapTo(1f)
                                            cardOffset.animateTo(
                                                targetValue = Offset.Zero,
                                                animationSpec = spring(
                                                    dampingRatio = 0.65f,
                                                    stiffness = 280f,
                                                ),
                                            )
                                        }
                                    }
                                },
                                onDragCancel = {
                                    dragging = false
                                    phase = "idle"
                                    folderOpenTarget = 0f
                                    scope.launch {
                                        cardScale.snapTo(1f)
                                        cardOffset.animateTo(
                                            targetValue = Offset.Zero,
                                            animationSpec = spring(
                                                dampingRatio = 0.6f,
                                                stiffness = 200f,
                                            ),
                                        )
                                    }
                                },
                            )
                        }
                        .graphicsLayer {
                            val s = breatheScale * cardScale.value
                            scaleX = s
                            scaleY = s
                            alpha = cardAlpha.value
                            transformOrigin = TransformOrigin(0f, 0.5f)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = scheme.surfaceContainerHigh,
                        contentColor = scheme.onSurface,
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        SemanticIcon(
                            imageVector = Icons.Default.Download,
                            tint = scheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedFolderInhale(
    modifier: Modifier = Modifier,
    openProgress: Float,
    done: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val progress = openProgress.coerceIn(0f, 1f)
    val doneScale by animateFloatAsState(
        targetValue = if (done) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 380f),
        label = "folderDoneScale",
    )
    val scale = (1f + progress * 0.34f) * doneScale
    val iconTint = when {
        done -> scheme.primary
        else -> lerp(scheme.onSurfaceVariant, scheme.primary, progress)
    }
    val closedAlpha = if (done) 1f else 1f - progress
    val openAlpha = if (done) 0f else progress

    Box(
        modifier = modifier
            .size(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(1f, 0.5f)
            },
        contentAlignment = Alignment.Center,
    ) {
        SemanticIcon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer { alpha = closedAlpha },
            tint = iconTint,
        )
        SemanticIcon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = "Folder destination",
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer { alpha = openAlpha },
            tint = iconTint,
        )
        AnimatedVisibility(
            visible = done,
            modifier = Modifier.align(Alignment.TopEnd),
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(160)),
        ) {
            SemanticIcon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Archived",
                modifier = Modifier
                    .size(20.dp)
                    .offset(x = 4.dp, y = (-4).dp),
                tint = scheme.primary,
            )
        }
    }
}

@Composable
private fun SwipeToDeleteDemo() {
    val scheme = MaterialTheme.colorScheme
    val deleteProgress = remember { Animatable(0f) }
    var phase by remember { mutableStateOf("idle") }
    var deleteLocked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    val progress = deleteProgress.value

    val trashGrowTarget by animateFloatAsState(
        targetValue = if (phase == "deleted") 1f else progress,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 340f),
        label = "trashGrowTarget",
    )

    SemanticPlaySurface {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AnimatedContent(
                targetState = phase,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "deletePhase",
            ) { state ->
                when (state) {
                    "deleting" -> Row(verticalAlignment = Alignment.CenterVertically) {
                        LoadingIndicator(modifier = Modifier.size(28.dp))
                        Text(
                            text = "Deleting…",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurface,
                        )
                    }
                    "deleted" -> Row(verticalAlignment = Alignment.CenterVertically) {
                        SemanticIcon(
                            imageVector = Icons.Default.Delete,
                            tint = scheme.error,
                        )
                        Text(
                            text = "Deleted — use undo below to restore",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurface,
                        )
                    }
                    else -> Text(
                        text = "Swipe the card right toward the bin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }

            val trashSlot = 72.dp
            val playHeight = 96.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(playHeight)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = Modifier
                        .width(trashSlot)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedTrashDelete(
                        deleteProgress = trashGrowTarget,
                        done = phase == "deleted",
                    )
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = trashSlot)
                        .graphicsLayer {
                            val p = progress.coerceIn(0f, 1f)
                            scaleX = 1f - p * 0.5f
                            scaleY = 1f - p * 0.5f
                            translationX = p * 120f
                            translationY = p * 40f
                            alpha = if (phase == "deleted") 0f else 1f - p * 0.92f
                            transformOrigin = TransformOrigin(0f, 0.5f)
                        }
                        .pointerInput(deleteLocked) {
                            if (deleteLocked) return@pointerInput
                            detectDragGestures(
                                onDragStart = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDrag = { change, delta ->
                                    change.consume()
                                    if (delta.x > 0f) {
                                        scope.launch {
                                            deleteProgress.snapTo(
                                                (deleteProgress.value + (delta.x / 240f) * 1.8f)
                                                    .coerceAtMost(1f),
                                            )
                                        }
                                    }
                                },
                                onDragEnd = {
                                    scope.launch {
                                        if (deleteProgress.value >= 0.88f) {
                                            deleteLocked = true
                                            phase = "deleting"
                                            haptics.performHapticFeedback(
                                                HapticFeedbackType.TextHandleMove,
                                            )
                                            deleteProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = spring(
                                                    dampingRatio = 0.76f,
                                                    stiffness = 380f,
                                                ),
                                            )
                                            delay(if (reduceMotion) 120L else 280L)
                                            phase = "deleted"
                                            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                            delay(if (reduceMotion) 400L else 1200L)
                                            phase = "idle"
                                            deleteProgress.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = 0.68f,
                                                    stiffness = 300f,
                                                ),
                                            )
                                            deleteLocked = false
                                        } else {
                                            phase = "idle"
                                            deleteProgress.animateTo(
                                                targetValue = 0f,
                                                animationSpec = spring(
                                                    dampingRatio = 0.65f,
                                                    stiffness = 300f,
                                                ),
                                            )
                                        }
                                    }
                                },
                                onDragCancel = {
                                    phase = "idle"
                                    scope.launch {
                                        deleteProgress.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = 0.65f,
                                                stiffness = 300f,
                                            ),
                                        )
                                    }
                                },
                            )
                        }
                        .semantics { contentDescription = "Swipe right to delete" },
                    colors = CardDefaults.cardColors(
                        containerColor = scheme.surfaceContainerHighest,
                        contentColor = scheme.onSurface,
                    ),
                ) {
                    Text(
                        text = "Swipe → delete",
                        modifier = Modifier.padding(16.dp),
                        color = scheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedTrashDelete(
    modifier: Modifier = Modifier,
    deleteProgress: Float,
    done: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val progress = deleteProgress.coerceIn(0f, 1f)
    val trashShake = remember { Animatable(0f) }
    val ripple = remember { Animatable(0f) }
    val donePop by animateFloatAsState(
        targetValue = if (done) 1.22f else 1f,
        animationSpec = spring(dampingRatio = 0.48f, stiffness = 520f),
        label = "trashDonePop",
    )
    val scale = (1f + progress * 0.45f) * donePop
    val iconTint = when {
        done -> scheme.error
        progress > 0.35f -> scheme.error
        else -> scheme.onSurfaceVariant
    }

    LaunchedEffect(done) {
        if (done) {
            coroutineScope {
                launch {
                    ripple.snapTo(0f)
                    ripple.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(560, easing = FastOutSlowInEasing),
                    )
                }
                launch {
                    trashShake.snapTo(0f)
                    trashShake.animateTo(-14f, tween(65))
                    trashShake.animateTo(11f, tween(85))
                    trashShake.animateTo(-5f, tween(65))
                    trashShake.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
                    )
                }
            }
        } else {
            ripple.snapTo(0f)
            trashShake.snapTo(0f)
        }
    }

    val rippleProgress = ripple.value
    val ringColor = scheme.error

    Box(
        modifier = modifier
            .size(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(1f, 0.5f)
            },
        contentAlignment = Alignment.Center,
    ) {
        repeat(2) { index ->
            val stagger = index * 0.2f
            val local = ((rippleProgress - stagger) / (1f - stagger)).coerceIn(0f, 1f)
            if (local > 0.01f) {
                Box(
                    modifier = Modifier
                        .size((34 + local * 42).dp)
                        .graphicsLayer { alpha = (1f - local) * 0.7f }
                        .border(
                            width = 2.dp,
                            color = ringColor.copy(alpha = 0.35f + (1f - local) * 0.25f),
                            shape = CircleShape,
                        ),
                )
            }
        }
        SemanticIcon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Trash",
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer { rotationZ = trashShake.value },
            tint = iconTint,
        )
    }
}

private enum class AsyncPhase { Idle, Loading, Success }

@Composable
private fun AsyncStateDemo() {
    val scheme = MaterialTheme.colorScheme
    var phase by remember { mutableStateOf(AsyncPhase.Idle) }
    val scope = rememberCoroutineScope()
    val reduceMotion = LocalReduceMotion.current

    SemanticPlaySurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AnimatedContent(
                targetState = phase,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "asyncPhase",
            ) { state ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when (state) {
                        AsyncPhase.Idle -> SemanticIcon(
                            imageVector = Icons.Default.Download,
                            tint = scheme.onSurfaceVariant,
                        )
                        AsyncPhase.Loading -> LoadingIndicator(modifier = Modifier.size(28.dp))
                        AsyncPhase.Success -> SemanticIcon(
                            imageVector = Icons.Default.CheckCircle,
                            tint = scheme.primary,
                        )
                    }
                    Text(
                        text = when (state) {
                            AsyncPhase.Idle -> "Ready to sync"
                            AsyncPhase.Loading -> "Syncing…"
                            AsyncPhase.Success -> "Synced successfully"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurface,
                    )
                }
            }
            FilledTonalButton(
                onClick = {
                    if (phase != AsyncPhase.Idle) return@FilledTonalButton
                    scope.launch {
                        phase = AsyncPhase.Loading
                        delay(if (reduceMotion) 200L else 1200L)
                        phase = AsyncPhase.Success
                        delay(if (reduceMotion) 300L else 1400L)
                        phase = AsyncPhase.Idle
                    }
                },
                enabled = phase == AsyncPhase.Idle,
            ) {
                Text(
                    text = when (phase) {
                        AsyncPhase.Idle -> "Start sync"
                        AsyncPhase.Loading -> "Working…"
                        AsyncPhase.Success -> "Done"
                    },
                    color = scheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun ExpandableDetailDemo() {
    val scheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    SemanticPlaySurface {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Delivery details",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                )
                IconButton(onClick = { expanded = !expanded }) {
                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) 180f else 0f,
                        animationSpec = spring(dampingRatio = 0.7f),
                        label = "chevron",
                    )
                    SemanticIcon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.graphicsLayer { rotationZ = rotation },
                        tint = scheme.onSurface,
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Text(
                    text = "Motion expands the tray so users connect the chevron rotation with new information appearing.",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmphasisToggleDemo() {
    val scheme = MaterialTheme.colorScheme
    var liked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (liked) 1.18f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 520f),
        label = "likeScale",
    )
    val haptics = LocalHapticFeedback.current

    SemanticPlaySurface {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Save to favorites",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
            )
            IconButton(
                onClick = {
                    liked = !liked
                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                },
            ) {
                SemanticIcon(
                    imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (liked) "Unlike" else "Like",
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                    tint = if (liked) scheme.error else scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PredictiveUndoDemo() {
    var lastDeleted by remember { mutableStateOf<String?>(null) }
    var predictiveProgress by remember { mutableFloatStateOf(0f) }
    val haptics = LocalHapticFeedback.current

    PredictiveBackHandler(enabled = lastDeleted != null) { progress ->
        try {
            progress.collect { event -> predictiveProgress = event.progress }
            lastDeleted = null
            predictiveProgress = 0f
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (e: CancellationException) {
            predictiveProgress = 0f
            throw e
        }
    }

    SemanticPlaySurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Simulate a delete, then use the system back gesture to preview undo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    Icons.Default.Share to "Share",
                    Icons.Default.Download to "Download",
                    Icons.Default.Folder to "Move",
                ).forEach { (icon, label) ->
                    QuickActionChip(
                        icon = icon,
                        label = label,
                        onClick = {
                            lastDeleted = label
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    )
                }
            }
            AnimatedVisibility(visible = lastDeleted != null) {
                UndoBanner(
                    itemLabel = lastDeleted.orEmpty(),
                    progress = predictiveProgress,
                    onUndo = {
                        lastDeleted = null
                        predictiveProgress = 0f
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "chipScale",
    )
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurface,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SemanticIcon(imageVector = icon, tint = scheme.primary, modifier = Modifier.size(22.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurface,
            )
        }
    }
}

@Composable
private fun SemanticIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

@Composable
private fun UndoBanner(
    itemLabel: String,
    progress: Float,
    onUndo: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val highlight = (0.6f + progress * 0.4f).coerceIn(0f, 1f)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = scheme.secondaryContainer,
        contentColor = scheme.onSecondaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = 1f + progress * 0.04f
                scaleY = 1f + progress * 0.04f
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "“$itemLabel” removed. Pull back to undo.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSecondaryContainer.copy(alpha = highlight),
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onUndo) {
                SemanticIcon(
                    imageVector = Icons.AutoMirrored.Outlined.Undo,
                    tint = scheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Undo",
                    color = scheme.onSecondaryContainer,
                )
            }
        }
    }
}
