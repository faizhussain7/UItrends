package com.mfhapps.trendingui.screens.masonry

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import com.mfhapps.trendingui.navigation.nestedPopTransform
import com.mfhapps.trendingui.navigation.nestedPushTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.TouchApp
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalConfiguration
import com.mfhapps.trendingui.ui.components.CatalogMorphShapes
import com.mfhapps.trendingui.ui.components.Button
import com.mfhapps.trendingui.ui.components.ExpressivePolygonIcon
import com.mfhapps.trendingui.ui.components.FilledTonalButton
import com.mfhapps.trendingui.ui.components.ShapeClickableSurface
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

private val HeroBodyHeight = 272.dp
private val TopBarHeight = 64.dp
private val GridHorizontalPadding = 14.dp
private val HeroDeckOverlap = 28.dp
private val MasonryTileShape = RoundedCornerShape(24.dp)
private val MasonryImageShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
private val MasonryFeaturedShape = RoundedCornerShape(28.dp)
private val MasonryHeroDeckShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
private val MasonryDetailDeckOverlap = 32.dp
private const val MasonryImageFadeStart = 0.62f

private data class MasonryDetailMeta(
    val views: String,
    val readTime: String,
    val saves: String,
    val note: String,
    val tags: List<String>,
)

private fun MasonryItem.detailMeta(): MasonryDetailMeta {
    val tags = listOf(category, "Immersive", "Masonry", "Expressive").distinct()
    return MasonryDetailMeta(
        views = "${180 + id * 23} views",
        readTime = "${3 + id % 6} min read",
        saves = "${24 + id * 3}% saved",
        note = "Palette-matched scrims and scroll-linked tiles keep photography legible while the feed morphs between collections.",
        tags = tags,
    )
}


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

private fun masonryMorphPair(itemId: Int) = CatalogMorphShapes.forIndex(itemId)

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
    val reduceMotion = LocalReduceMotion.current
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
                if (reduceMotion) {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(180))
                } else if (targetState != null) {
                    nestedPushTransform()
                } else {
                    nestedPopTransform()
                }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val accentShape = MaterialShapes.Gem.toShape()

    Column(
        modifier = modifier
            .horizontalBleed(horizontalBleed)
            .padding(bottom = 8.dp),
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.42f to Color.Transparent,
                                0.72f to scheme.scrim.copy(alpha = 0.35f),
                                1f to scheme.scrim.copy(alpha = 0.82f),
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 24.dp, y = 12.dp)
                    .size(88.dp)
                    .graphicsLayer { alpha = titleAlpha * 0.22f }
                    .clip(accentShape)
                    .background(scheme.primaryContainer),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .graphicsLayer { alpha = titleAlpha },
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = scheme.primaryContainer.copy(alpha = 0.92f),
                ) {
                    Text(
                        text = "Immersive feed",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                }
                Text(
                    text = "Immersive Masonry",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Scroll-linked hero, palette shifts, and expressive tiles",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .offset(y = -HeroDeckOverlap)
                .graphicsLayer { alpha = exploreAlpha },
            shape = MasonryHeroDeckShape,
            color = scheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 10.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Explore collections",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (activeFilter == index) {
                                        scheme.onPrimaryContainer
                                    } else {
                                        scheme.onSurfaceVariant
                                    },
                                )
                            },
                            shape = MaterialTheme.shapes.largeIncreased,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val morphPair = masonryMorphPair(item.id)
    val badgeShape = MaterialShapes.Cookie4Sided.toShape()
    ShapeClickableSurface(
        onClick = onClick,
        shape = MasonryFeaturedShape,
        morphRest = morphPair.rest,
        morphPressed = morphPair.pressed,
        modifier = modifier
            .fillMaxWidth()
            .height(236.dp),
        color = scheme.surfaceContainerHigh,
        contentColor = catalogColors.headerTitle,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
    ) {
        MasonryCardImageBlock(
            item = item,
            imageBlockHeight = 236.dp,
            imageShape = MasonryFeaturedShape,
            sharedTransitionScope = sharedTransitionScope,
            animatedVisibilityScope = animatedVisibilityScope,
            content = {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = badgeShape,
                        color = scheme.primaryContainer.copy(alpha = 0.94f),
                    ) {
                        Text(
                            text = "Featured",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        )
                    }
                    AdaptiveGradientText(
                        text = item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        role = GradientForegroundRole.Title,
                    )
                    AdaptiveGradientText(
                        text = item.category,
                        style = MaterialTheme.typography.labelLarge,
                        role = GradientForegroundRole.Subtitle,
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MasonryTileCard(
    item: MasonryItem,
    onClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val morphPair = masonryMorphPair(item.id)

    ShapeClickableSurface(
        onClick = onClick,
        shape = MasonryTileShape,
        morphRest = morphPair.rest,
        morphPressed = morphPair.pressed,
        modifier = modifier.fillMaxWidth(),
        color = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurface,
        shadowElevation = 3.dp,
        tonalElevation = 1.dp,
    ) {
        Column {
            MasonryCardImageBlock(
                item = item,
                imageBlockHeight = item.imageHeightDp.dp,
                imageShape = MasonryImageShape,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                bottomBlendColor = scheme.surfaceContainerHigh,
            )
            Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    modifier = Modifier.padding(top = 6.dp),
                    shape = MaterialTheme.shapes.largeIncreased,
                    color = scheme.tertiaryContainer.copy(alpha = 0.72f),
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onTertiaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MasonryCardImageBlock(
    item: MasonryItem,
    imageBlockHeight: Dp,
    imageShape: androidx.compose.ui.graphics.Shape,
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
            .height(imageBlockHeight)
            .clip(imageShape),
    ) {
        MasonrySharedImage(
            item = item,
            imageShape = imageShape,
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
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.45f to Color.Transparent,
                                1f to scheme.scrim.copy(alpha = 0.72f),
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
    imageShape: androidx.compose.ui.graphics.Shape,
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
            .clip(imageShape),
        contentScale = ContentScale.Crop,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MasonryItemDetail(
    item: MasonryItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    val configuration = LocalConfiguration.current
    val backgroundLayers = rememberMasonryDetailBackground(
        imageSeed = "masonry-tile-${item.id}",
        colorScheme = scheme,
        reduceMotion = reduceMotion,
    )
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + TopBarHeight + 4.dp
    val heroHeight = (configuration.screenHeightDp * 0.56f).dp.coerceIn(380.dp, 520.dp)
    val meta = remember(item.id) { item.detailMeta() }
    val morphPair = remember(item.id) { masonryMorphPair(item.id) }
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
                    .height(heroHeight + topInset),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(heroHeight),
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heroHeight * 0.35f)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0f to Color.Transparent,
                                        1f to scheme.scrim.copy(alpha = 0.45f),
                                    ),
                                ),
                            ),
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 16.dp),
                        shape = MaterialTheme.shapes.largeIncreased,
                        color = scheme.primaryContainer.copy(alpha = 0.94f),
                        shadowElevation = 4.dp,
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = -MasonryDetailDeckOverlap),
                shape = MasonryHeroDeckShape,
                color = scheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 10.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ExpressivePolygonIcon(
                            icon = Icons.Outlined.AutoAwesome,
                            polygon = morphPair.rest,
                            modifier = Modifier.size(44.dp),
                            containerColor = scheme.primaryContainer,
                            contentColor = scheme.onPrimaryContainer,
                        )
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = scheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MasonryDetailMetric(label = "Views", value = meta.views, modifier = Modifier.weight(1f))
                        MasonryDetailMetric(label = "Read", value = meta.readTime, modifier = Modifier.weight(1f))
                        MasonryDetailMetric(label = "Saves", value = meta.saves, modifier = Modifier.weight(1f))
                    }

                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.45f))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "About this tile",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = scheme.onSurface,
                        )
                        Text(
                            text = item.body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = scheme.onSurfaceVariant,
                        )
                    }

                    MasonryDetailSectionCard(
                        title = "Gallery notes",
                        body = meta.note,
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.titleSmallEmphasized,
                            color = scheme.onSurface,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            meta.tags.forEach { tag ->
                                Surface(
                                    shape = MaterialTheme.shapes.largeIncreased,
                                    color = scheme.secondaryContainer.copy(alpha = 0.82f),
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = scheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Save")
                        }
                        FilledTonalButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Share")
                        }
                    }

                    Surface(
                        shape = MaterialTheme.shapes.largeIncreased,
                        color = scheme.surfaceContainerHigh,
                        tonalElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            ExpressivePolygonIcon(
                                icon = Icons.Outlined.TouchApp,
                                polygon = MaterialShapes.Sunny,
                                modifier = Modifier.size(36.dp),
                                containerColor = scheme.tertiaryContainer,
                                contentColor = scheme.onTertiaryContainer,
                                iconSize = 18.dp,
                            )
                            Text(
                                text = "Pinch or double-tap the hero image to zoom in full screen.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MasonryDetailMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.largeIncreased,
        color = scheme.surfaceContainerHigh,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MasonryDetailSectionCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLargeIncreased,
        color = scheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = scheme.onSurface,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}
