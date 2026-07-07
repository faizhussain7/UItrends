package com.mfhapps.trendingui.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
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
import com.mfhapps.trendingui.ui.components.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.theme.AppFontStyle
import com.mfhapps.trendingui.ui.theme.AppFonts
import com.mfhapps.trendingui.ui.theme.buildExpressiveTypography
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

private val PickerCardShape @Composable get() = MaterialTheme.shapes.largeIncreased
private const val CarouselCameraDistanceMultiplier = 10f

@OptIn(ExperimentalFoundationApi::class)
private fun PagerState.pageOffset(page: Int): Float =
    (currentPage - page) + currentPageOffsetFraction

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

    LaunchedEffect(selected) {
        val target = styles.indexOf(selected).coerceAtLeast(0)
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(target)
        }
    }

    LaunchedEffect(pagerState, reduceMotion) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            styles.getOrNull(page)?.let { style ->
                if (style != selected) onSelected(style)
            }
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

    LaunchedEffect(pagerState, reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        var wasScrolling = false
        snapshotFlow { pagerState.isScrollInProgress }.collect { scrolling ->
            if (scrolling && !wasScrolling) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            wasScrolling = scrolling
        }
    }

    fun step(delta: Int) {
        scope.launch {
            val next = (pagerState.currentPage + delta).coerceIn(0, styles.lastIndex)
            pagerState.animateScrollToPage(next)
        }
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
                enabled = pagerState.currentPage > 0,
                modifier = Modifier.semantics {
                    contentDescription = "Previous typeface"
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = null,
                )
            }

            Text(
                text = "${pagerState.currentPage + 1} / ${styles.size}",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.semantics {
                    contentDescription = "Typeface ${pagerState.currentPage + 1} of ${styles.size}"
                },
            )

            IconButton(
                onClick = { step(1) },
                enabled = pagerState.currentPage < styles.lastIndex,
                modifier = Modifier.semantics {
                    contentDescription = "Next typeface"
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp),
            contentAlignment = Alignment.Center,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 52.dp),
                pageSpacing = 14.dp,
                beyondViewportPageCount = 2,
                verticalAlignment = Alignment.CenterVertically,
            ) { page ->
                val style = styles[page]
                val offset = pagerState.pageOffset(page)
                val focus = (1f - offset.absoluteValue.coerceIn(0f, 1f))
                val scale = lerp(0.84f, 1f, focus)
                val alpha = lerp(0.58f, 1f, focus)
                val rotationY = if (reduceMotion) 0f else offset * -46f
                val elevation = (2f + focus * 10f).dp

                FontPickerCarouselCard(
                    style = style,
                    focus = focus,
                    elevation = elevation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                            this.rotationY = rotationY
                            cameraDistance = CarouselCameraDistanceMultiplier * density.density
                            shadowElevation = with(density) { elevation.toPx() }
                        }
                        .shadow(
                            elevation = elevation,
                            shape = PickerCardShape,
                            clip = false,
                        ),
                )
            }
        }

        Text(
            text = "Hold and drag to browse · ticks on each typeface",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FontPickerCarouselCard(
    style: AppFontStyle,
    focus: Float,
    elevation: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val previewFamily = AppFonts.previewFont(style)
    val previewTypography = remember(style) {
        buildExpressiveTypography(
            brandFont = previewFamily,
            bodyFont = AppFonts.bodyFont(style),
        )
    }
    val isCentered = focus > 0.92f

    Surface(
        modifier = modifier,
        shape = PickerCardShape,
        color = if (isCentered) {
            scheme.surfaceContainerHighest
        } else {
            scheme.surfaceContainerHigh.copy(alpha = 0.92f + focus * 0.08f)
        },
        contentColor = scheme.onSurface,
        tonalElevation = elevation,
        shadowElevation = elevation,
        border = BorderStroke(
            width = if (isCentered) 1.5.dp else 1.dp,
            color = if (isCentered) {
                scheme.primary.copy(alpha = 0.45f)
            } else {
                scheme.outlineVariant.copy(alpha = lerp(0.35f, 0.65f, 1f - focus))
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
                style = previewTypography.headlineSmallEmphasized,
                fontFamily = previewFamily,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = style.description,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant.copy(alpha = lerp(0.7f, 1f, focus)),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Aa Bb Cc 123",
                style = previewTypography.titleMediumEmphasized,
                fontFamily = previewFamily,
                textAlign = TextAlign.Center,
            )
        }
    }
}
