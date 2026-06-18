package com.mfhapps.trendingui.screens.masonry

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mfhapps.trendingui.navigation.demoSharedElement
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.ZoomableImage
import com.mfhapps.trendingui.ui.detail.LocalImmersiveTopBarCollapse
import com.mfhapps.trendingui.ui.detail.LocalNestedBackDispatcher
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.theme.AdaptiveGradientText
import com.mfhapps.trendingui.ui.theme.GradientForegroundRole
import com.mfhapps.trendingui.ui.theme.LocalHomeCatalogColors

private data class MasonryItem(
    val id: Int,
    val title: String,
    val body: String,
    val imageHeightDp: Int,
    val category: String,
)

private sealed interface MasonryFeedRow {
    val item: MasonryItem

    data class Tile(override val item: MasonryItem) : MasonryFeedRow
    data class Featured(override val item: MasonryItem) : MasonryFeedRow
}

private val HeroBodyHeight = 240.dp
private val TopBarHeight = 56.dp
private val GridHorizontalPadding = 14.dp
private val HeroSectionPadding = 20.dp
private const val MasonryImageFadeStart = 0.68f


private fun Modifier.masonryImageBottomFade(blendColor: Color): Modifier =
    imageDissolveInto(
        blendColor = blendColor,
        topScrimAlpha = 0f,
        fadeStart = MasonryImageFadeStart,
    )

private fun Modifier.horizontalBleed(bleed: Dp): Modifier = layout { measurable, constraints ->
    val bleedPx = bleed.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(maxWidth = constraints.maxWidth + bleedPx * 2),
    )
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(-bleedPx, 0)
    }
}

private fun masonryImageKey(itemId: Int): String = "masonry-image-$itemId"

private fun masonryImageUrl(item: MasonryItem): String =
    "https://picsum.photos/seed/masonry-tile-${item.id}/800/1000"

private val FilterCategories = listOf("All", "Trending", "Nature", "Architecture", "Minimal")


private fun Modifier.imageDissolveInto(
    blendColor: Color,
    topScrimAlpha: Float = 0.28f,
    fadeStart: Float = 0.52f,
): Modifier = background(
    Brush.verticalGradient(
        colorStops = arrayOf(
            0f to blendColor.copy(alpha = topScrimAlpha),
            0.16f to Color.Transparent,
            fadeStart to Color.Transparent,
            (fadeStart + (1f - fadeStart) * 0.42f) to blendColor.copy(alpha = 0.38f),
            (fadeStart + (1f - fadeStart) * 0.72f) to blendColor.copy(alpha = 0.76f),
            0.94f to blendColor.copy(alpha = 0.96f),
            1f to blendColor,
        ),
    ),
)


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ImmersiveScrollScreen() {
    val haptics = LocalHapticFeedback.current
    val nestedBackDispatcher = LocalNestedBackDispatcher.current
    val gridState = rememberLazyStaggeredGridState()
    var activeFilter by remember { mutableIntStateOf(0) }

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        var selectedItem by remember { mutableStateOf<MasonryItem?>(null) }

        val closeDetail = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedItem = null
        }

        DisposableEffect(selectedItem != null) {
            nestedBackDispatcher.handler = if (selectedItem != null) {
                { closeDetail(); true }
            } else {
                null
            }
            onDispose { nestedBackDispatcher.handler = null }
        }

        BackHandler(enabled = selectedItem != null) { closeDetail() }

        AnimatedContent(
            targetState = selectedItem,
            transitionSpec = {
                fadeIn(animationSpec = tween(240)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "masonry-feed-detail",
        ) { item ->
            if (item == null) {
                MasonryFeedGrid(
                    gridState = gridState,
                    activeFilter = activeFilter,
                    onActiveFilterChange = { index ->
                        activeFilter = index
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onItemClick = { clicked ->
                        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        selectedItem = clicked
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                )
            } else {
                MasonryItemDetail(
                    item = item,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MasonryFeedGrid(
    gridState: LazyStaggeredGridState,
    activeFilter: Int,
    onActiveFilterChange: (Int) -> Unit,
    onItemClick: (MasonryItem) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val reduceMotion = LocalReduceMotion.current
    val heroState = rememberMasonryHeroLoopState(
        colorScheme = scheme,
        reduceMotion = reduceMotion,
    )
    val backgroundLayers = heroState.background
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomSafe = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val heroImageHeight = HeroBodyHeight + statusBarTop + TopBarHeight
    val heroFadePx = with(density) { (heroImageHeight * 0.85f).toPx() }

    val scrollCollapse by remember(gridState, heroFadePx) {
        derivedStateOf {
            when {
                gridState.firstVisibleItemIndex > 0 -> 1f
                else -> (gridState.firstVisibleItemScrollOffset / heroFadePx).coerceIn(0f, 1f)
            }
        }
    }

    val immersiveTopBarCollapse = LocalImmersiveTopBarCollapse.current
    SideEffect(scrollCollapse) {
        immersiveTopBarCollapse?.collapsedFraction = scrollCollapse
    }

    val feedProgress by remember(gridState) {
        derivedStateOf {
            val info = gridState.layoutInfo
            if (info.totalItemsCount <= 1) return@derivedStateOf 0f
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            (lastVisible.toFloat() / (info.totalItemsCount - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
        }
    }

    val heroTitleAlpha by remember(scrollCollapse) {
        derivedStateOf { (1f - scrollCollapse * 1.25f).coerceIn(0f, 1f) }
    }

    val exploreAlpha by remember(scrollCollapse) {
        derivedStateOf { (1f - scrollCollapse * 0.85f).coerceIn(0f, 1f) }
    }

    val heroParallaxPx by remember(gridState, reduceMotion) {
        derivedStateOf {
            if (reduceMotion || gridState.firstVisibleItemIndex > 0) 0f
            else gridState.firstVisibleItemScrollOffset.toFloat()
        }
    }

    val heroImageScale by remember(scrollCollapse, reduceMotion) {
        derivedStateOf {
            if (reduceMotion) 1f else 1.05f + scrollCollapse * 0.1f
        }
    }

    val items = remember {
        val heights = listOf(132, 176, 148, 204, 156, 220, 140, 188, 164, 196, 152, 184, 128, 212, 160, 172)
        val categories = listOf("Trending", "Nature", "Architecture", "Minimal", "Trending", "Nature")
        (0 until 16).map { i ->
            MasonryItem(
                id = i,
                title = "Discovery ${i + 1}",
                body = "Variable-height tile in an immersive, scroll-linked masonry feed.",
                imageHeightDp = heights[i],
                category = categories[i % categories.size],
            )
        }
    }

    val feedRows = remember(items, activeFilter) {
        val filtered = if (activeFilter == 0) items else items.filter { it.category == FilterCategories[activeFilter] }
        filtered.flatMapIndexed { index, item ->
            if (index % 5 == 4) listOf(MasonryFeedRow.Featured(item)) else listOf(MasonryFeedRow.Tile(item))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appHazeSource()
            .drawBehind {
                drawRect(brush = backgroundLayers.outgoing)
                drawRect(
                    brush = backgroundLayers.incoming,
                    alpha = backgroundLayers.progress,
                )
                val blend = (scrollCollapse * 0.55f + feedProgress * 0.45f).coerceIn(0f, 1f)
                if (blend > 0f) {
                    drawRect(color = scheme.background.copy(alpha = blend * 0.42f))
                }
            },
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = GridHorizontalPadding,
                end = GridHorizontalPadding,
                bottom = bottomSafe + 32.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 12.dp,
        ) {
            item(span = StaggeredGridItemSpan.FullLine, key = "hero") {
                ImmersiveMasonryHero(
                    imageHeight = heroImageHeight,
                    heroState = heroState,
                    activeFilter = activeFilter,
                    onFilterChange = onActiveFilterChange,
                    titleAlpha = heroTitleAlpha,
                    exploreAlpha = exploreAlpha,
                    parallaxOffset = heroParallaxPx,
                    imageScale = heroImageScale,
                    horizontalBleed = GridHorizontalPadding,
                )
            }

            items(
                items = feedRows,
                key = { row ->
                    when (row) {
                        is MasonryFeedRow.Featured -> "featured-${row.item.id}"
                        is MasonryFeedRow.Tile -> "tile-${row.item.id}"
                    }
                },
                span = { row ->
                    when (row) {
                        is MasonryFeedRow.Featured -> StaggeredGridItemSpan.FullLine
                        is MasonryFeedRow.Tile -> StaggeredGridItemSpan.SingleLane
                    }
                },
            ) { row ->
                val item = row.item
                when (row) {
                    is MasonryFeedRow.Featured -> FeaturedMasonryCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        modifier = Modifier.animateItem(),
                    )

                    is MasonryFeedRow.Tile -> MasonryTileCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ImmersiveMasonryHero(
    imageHeight: Dp,
    heroState: MasonryHeroLoopState,
    activeFilter: Int,
    onFilterChange: (Int) -> Unit,
    titleAlpha: Float,
    exploreAlpha: Float,
    parallaxOffset: Float,
    imageScale: Float,
    horizontalBleed: Dp,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.horizontalBleed(horizontalBleed),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight),
        ) {
            MasonryHeroSlideshow(
                state = heroState,
                parallaxOffset = parallaxOffset,
                scrollImageScale = imageScale,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = HeroSectionPadding,
                    end = HeroSectionPadding,
                    top = HeroSectionPadding,
                    bottom = HeroSectionPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier.graphicsLayer { alpha = titleAlpha },
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Immersive Masonry",
                    style = MaterialTheme.typography.displaySmall,
                    color = scheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Scroll to blend hero into the feed palette",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier.graphicsLayer { alpha = exploreAlpha },
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Explore",
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterCategories.forEachIndexed { index, label ->
                        FilterChip(
                            selected = activeFilter == index,
                            onClick = { onFilterChange(index) },
                            label = {
                                Text(
                                    text = label,
                                    color = if (activeFilter == index) {
                                        scheme.onPrimaryContainer
                                    } else {
                                        scheme.onSurfaceVariant
                                    },
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = scheme.surfaceContainerHigh,
                                labelColor = scheme.onSurfaceVariant,
                                selectedContainerColor = scheme.primaryContainer,
                                selectedLabelColor = scheme.onPrimaryContainer,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = activeFilter == index,
                                borderColor = scheme.outlineVariant,
                                selectedBorderColor = scheme.primary,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun FeaturedMasonryCard(
    item: MasonryItem,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val catalogColors = LocalHomeCatalogColors.current
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerHigh,
            contentColor = catalogColors.headerTitle,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        MasonryCardImageBlock(
            item = item,
            imageBlockHeight = 220.dp,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            content = {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                ) {
                    AdaptiveGradientText(
                        text = "Featured",
                        style = MaterialTheme.typography.labelLarge,
                        role = GradientForegroundRole.Subtitle,
                    )
                    AdaptiveGradientText(
                        text = item.title,
                        style = MaterialTheme.typography.titleLarge,
                        role = GradientForegroundRole.Title,
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun MasonryTileCard(
    item: MasonryItem,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = scheme.surfaceContainerHigh,
            contentColor = scheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            MasonryCardImageBlock(
                item = item,
                imageBlockHeight = item.imageHeightDp.dp,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                bottomBlendColor = scheme.surfaceContainerHigh,
            )
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MasonryCardImageBlock(
    item: MasonryItem,
    imageBlockHeight: Dp,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    bottomBlendColor: Color? = null,
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(imageBlockHeight),
    ) {
        MasonrySharedImage(
            item = item,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            modifier = Modifier.fillMaxSize(),
        )
        if (bottomBlendColor != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .masonryImageBottomFade(bottomBlendColor),
            )
        } else if (content != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                scheme.primary.copy(alpha = 0.72f),
                            ),
                        ),
                    ),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                scheme.primary.copy(alpha = 0.22f),
                            ),
                        ),
                    ),
            )
        }
        content?.invoke(this)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MasonrySharedImage(
    item: MasonryItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = masonryImageUrl(item),
        contentDescription = item.title,
        modifier = modifier
            .demoSharedElement(
                sharedTransitionScope = sharedTransitionScope,
                key = masonryImageKey(item.id),
                animatedVisibilityScope = animatedVisibilityScope,
            )
            .clip(MaterialTheme.shapes.medium),
        contentScale = ContentScale.Crop,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MasonryItemDetail(
    item: MasonryItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    val backgroundLayers = rememberMasonryDetailBackground(
        imageSeed = "masonry-tile-${item.id}",
        colorScheme = scheme,
        reduceMotion = reduceMotion,
    )
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + TopBarHeight
    val imageHeight = 340.dp
    var isZoomed by remember { mutableStateOf(false) }
    val immersiveTopBarCollapse = LocalImmersiveTopBarCollapse.current
    SideEffect(Unit) {
        immersiveTopBarCollapse?.collapsedFraction = 1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appHazeSource()
            .drawBehind {
                drawRect(brush = backgroundLayers.outgoing)
                drawRect(
                    brush = backgroundLayers.incoming,
                    alpha = backgroundLayers.progress,
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState(), enabled = !isZoomed),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topInset)
                    .height(imageHeight),
            ) {
                ZoomableImage(
                    modifier = Modifier.fillMaxSize(),
                    onZoomChanged = { zoomed ->
                        if (zoomed != isZoomed) {
                            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        }
                        isZoomed = zoomed
                    },
                ) {
                    AsyncImage(
                        model = masonryImageUrl(item),
                        contentDescription = item.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .demoSharedElement(
                                sharedTransitionScope = sharedTransitionScope,
                                key = masonryImageKey(item.id),
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            CompositionLocalProvider(LocalContentColor provides scheme.onBackground) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.primary,
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = scheme.onBackground,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = item.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Pinch or double-tap the image to zoom",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}
