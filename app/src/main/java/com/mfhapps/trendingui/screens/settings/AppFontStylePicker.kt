package com.mfhapps.trendingui.screens.settings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.IconButton
import com.mfhapps.trendingui.ui.theme.AppFontStyle
import com.mfhapps.trendingui.ui.theme.AppFonts
import com.mfhapps.trendingui.ui.theme.buildExpressiveTypography
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val CarouselCameraDistanceMultiplier = 14f
private const val CarouselMaxTiltDegrees = 36f
private const val CarouselScrollMs = 450

@OptIn(ExperimentalFoundationApi::class)
private fun PagerState.pageOffset(page: Int): Float =
    (currentPage - page) + currentPageOffsetFraction

private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun coverflowTiltDegrees(offset: Float, maxDegrees: Float): Float {
    val clamped = offset.coerceIn(-1f, 1f)
    return sin(clamped * PI.toFloat() * 0.5f) * -maxDegrees
}

internal fun scrubPageForTrackPosition(
    x: Float,
    startX: Float,
    startPage: Float,
    trackWidth: Float,
    lastIndex: Float,
): Float {
    if (lastIndex <= 0f || trackWidth <= 0f) return 0f
    val xInTrack = x.coerceIn(0f, trackWidth)
    val originX = startX.coerceIn(0f, trackWidth)
    val originPage = startPage.coerceIn(0f, lastIndex)

    return if (xInTrack <= originX) {
        val span = originX.coerceAtLeast(1f)
        val t = (xInTrack / span).coerceIn(0f, 1f)
        t * originPage
    } else {
        val span = (trackWidth - originX).coerceAtLeast(1f)
        val t = ((xInTrack - originX) / span).coerceIn(0f, 1f)
        originPage + t * (lastIndex - originPage)
    }
}

@OptIn(ExperimentalFoundationApi::class)
private suspend fun PagerState.scrollToFractionalPage(fractional: Float, pageCount: Int) {
    val lastIndex = (pageCount - 1).coerceAtLeast(0)
    val f = fractional.coerceIn(0f, lastIndex.toFloat())
    val page = floor(f).toInt().coerceIn(0, lastIndex)
    val progress = f - page
    when {
        page >= lastIndex -> scrollToPage(lastIndex, 0f)
        progress <= 0.5f -> scrollToPage(page, progress)
        else -> scrollToPage((page + 1).coerceAtMost(lastIndex), progress - 1f)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppFontStylePicker(
    selected: AppFontStyle,
    onSelected: (AppFontStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val styles = AppFontStyle.entries
    val selectedIndex = styles.indexOf(selected).coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { styles.size },
    )
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    val iconColor = scheme.primary
    val pageAnim = remember {
        tween<Float>(durationMillis = CarouselScrollMs, easing = FastOutSlowInEasing)
    }
    var isScrubbing by remember { mutableStateOf(false) }
    val layoutDirection = LocalLayoutDirection.current
    val onSelectedState by rememberUpdatedState(onSelected)
    val selectedState by rememberUpdatedState(selected)
    val lastIndex = styles.lastIndex

    LaunchedEffect(Unit) {
        styles.forEach { AppFonts.warmUp(it) }
    }

    LaunchedEffect(selected, isScrubbing) {
        if (isScrubbing) return@LaunchedEffect
        val target = styles.indexOf(selected).coerceAtLeast(0)
        if (pagerState.settledPage != target && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(target, animationSpec = pageAnim)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage to isScrubbing }
            .distinctUntilChanged()
            .collect { (page, scrubbing) ->
                if (scrubbing) return@collect
                val style = styles.getOrNull(page) ?: return@collect
                if (style != selectedState) onSelectedState(style)
            }
    }

    LaunchedEffect(pagerState, reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        var lastPage = pagerState.currentPage
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != lastPage) {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                lastPage = page
            }
        }
    }

    fun step(delta: Int) {
        if (isScrubbing) return
        scope.launch {
            val next = (pagerState.currentPage + delta).coerceIn(0, lastIndex)
            pagerState.animateScrollToPage(next, animationSpec = pageAnim)
        }
    }

    fun applyScrubFraction(fractional: Float) {
        scope.launch {
            pagerState.scrollToFractionalPage(fractional, styles.size)
        }
    }

    fun finishScrub() {
        val settle = (
            pagerState.currentPage + pagerState.currentPageOffsetFraction
            ).roundToInt().coerceIn(0, lastIndex)
        scope.launch {
            pagerState.animateScrollToPage(settle, animationSpec = pageAnim)
            isScrubbing = false
        }
    }

    fun trackX(rawX: Float, trackWidth: Float): Float =
        if (layoutDirection == LayoutDirection.Rtl) {
            trackWidth - rawX
        } else {
            rawX
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = { step(-1) },
                enabled = !isScrubbing && pagerState.currentPage > 0,
                colors = IconButtonDefaults.iconButtonColors(contentColor = iconColor),
                modifier = Modifier.semantics {
                    contentDescription = "Previous typeface"
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = iconColor,
                )
            }

            Text(
                text = if (isScrubbing) {
                    styles[pagerState.currentPage.coerceIn(0, lastIndex)].label
                } else {
                    "${pagerState.currentPage + 1} / ${styles.size}"
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (isScrubbing) scheme.primary else scheme.onSurfaceVariant,
                modifier = Modifier.semantics {
                    contentDescription =
                        "Typeface ${pagerState.currentPage + 1} of ${styles.size}"
                },
            )

            IconButton(
                onClick = { step(1) },
                enabled = !isScrubbing && pagerState.currentPage < lastIndex,
                colors = IconButtonDefaults.iconButtonColors(contentColor = iconColor),
                modifier = Modifier.semantics {
                    contentDescription = "Next typeface"
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = iconColor,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp)
                .pointerInput(lastIndex, layoutDirection) {
                    awaitEachGesture {
                        val down = awaitFirstDown(
                            requireUnconsumed = false,
                            pass = PointerEventPass.Initial,
                        )
                        val longPress =
                            awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture

                        val trackWidth = size.width.toFloat().coerceAtLeast(1f)
                        val startX = trackX(longPress.position.x, trackWidth)
                        val startPage =
                            (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                                .coerceIn(0f, lastIndex.toFloat())

                        isScrubbing = true
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        applyScrubFraction(startPage)

                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val change = event.changes.firstOrNull { it.id == down.id }
                                ?: break
                            if (change.changedToUp()) {
                                change.consume()
                                break
                            }
                            if (change.positionChange() != Offset.Zero) {
                                change.consume()
                                val fractional = scrubPageForTrackPosition(
                                    x = trackX(change.position.x, trackWidth),
                                    startX = startX,
                                    startPage = startPage,
                                    trackWidth = trackWidth,
                                    lastIndex = lastIndex.toFloat(),
                                )
                                applyScrubFraction(fractional)
                            }
                        }
                        finishScrub()
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 52.dp),
                pageSpacing = 12.dp,
                beyondViewportPageCount = 2,
                userScrollEnabled = !isScrubbing,
                verticalAlignment = Alignment.CenterVertically,
            ) { page ->
                val style = styles[page]
                val offset = pagerState.pageOffset(page)
                val clamped = offset.coerceIn(-1f, 1f)
                val focus = 1f - clamped.absoluteValue
                val easedFocus = smoothstep(0f, 1f, focus)
                val tilt = if (reduceMotion) {
                    0f
                } else {
                    coverflowTiltDegrees(clamped, CarouselMaxTiltDegrees)
                }
                val scale = if (reduceMotion) {
                    1f
                } else {
                    lerp(0.84f, if (isScrubbing) 1.02f else 1f, easedFocus)
                }
                val alpha = if (reduceMotion) 1f else lerp(0.62f, 1f, easedFocus)
                val arcX = if (reduceMotion) {
                    0f
                } else {
                    with(density) { sin(clamped * PI.toFloat() * 0.5f) * 8.dp.toPx() }
                }
                val shadowStrength =
                    if (reduceMotion) 0.12f else lerp(0.10f, 0.34f, easedFocus)
                val shadowSpread =
                    if (reduceMotion) 0.72f else lerp(0.62f, 0.92f, easedFocus)
                val shadowShiftX = if (reduceMotion) {
                    0f
                } else {
                    with(density) { -clamped * 6.dp.toPx() }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                            translationX = arcX
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .drawBehind {
                                val cx = size.width * 0.5f + shadowShiftX
                                val cy = size.height * 0.82f
                                val radiusX = size.width * 0.48f * shadowSpread
                                val radiusY = size.height * 0.20f * shadowSpread
                                val brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = shadowStrength),
                                        Color.Black.copy(alpha = shadowStrength * 0.32f),
                                        Color.Transparent,
                                    ),
                                    center = Offset(cx, cy),
                                    radius = maxOf(radiusX, radiusY),
                                )
                                drawOval(
                                    brush = brush,
                                    topLeft = Offset(cx - radiusX, cy - radiusY),
                                    size = Size(radiusX * 2f, radiusY * 2f),
                                )
                            },
                    )
                    FontPickerCarouselCard(
                        style = style,
                        focused = easedFocus > 0.88f,
                        scrubbing = isScrubbing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                rotationY = tilt
                                transformOrigin = TransformOrigin.Center
                                cameraDistance =
                                    CarouselCameraDistanceMultiplier * density.density
                                shadowElevation = 0f
                            },
                    )
                }
            }
        }

        Text(
            text = if (isScrubbing) {
                "Slide left or right · release to lock typeface"
            } else {
                "Hold & drag to scrub · swipe or use arrows"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (isScrubbing) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FontPickerCarouselCard(
    style: AppFontStyle,
    focused: Boolean,
    scrubbing: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val previewFamily = remember(style) { AppFonts.previewFont(style) }
    val previewTypography = remember(style, previewFamily) {
        buildExpressiveTypography(
            brandFont = previewFamily,
            bodyFont = previewFamily,
        )
    }

    MaterialTheme(typography = previewTypography) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.largeIncreased,
            color = if (focused) {
                scheme.surfaceContainerHighest
            } else {
                scheme.surfaceContainerHigh
            },
            contentColor = scheme.onSurface,
            tonalElevation = if (focused) 1.dp else 0.dp,
            shadowElevation = 0.dp,
            border = BorderStroke(
                width = if (focused || scrubbing) 1.5.dp else 1.dp,
                color = when {
                    scrubbing && focused -> scheme.primary.copy(alpha = 0.7f)
                    focused -> scheme.primary.copy(alpha = 0.5f)
                    else -> scheme.outlineVariant.copy(alpha = 0.55f)
                },
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = style.label,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    fontFamily = previewFamily,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = style.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = previewFamily,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Aa Bb Cc 123",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontFamily = previewFamily,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
