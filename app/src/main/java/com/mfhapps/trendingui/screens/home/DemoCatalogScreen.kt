@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.mfhapps.trendingui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import com.mfhapps.trendingui.launcher.AppLauncherIcon
import com.mfhapps.trendingui.navigation.DemoCatalogEntry
import com.mfhapps.trendingui.navigation.DemoCategory
import com.mfhapps.trendingui.navigation.DemoPaneKey
import com.mfhapps.trendingui.navigation.DemoSharedIcon
import com.mfhapps.trendingui.navigation.demoCatalogEntries
import com.mfhapps.trendingui.navigation.demoSharedContentKey
import com.mfhapps.trendingui.navigation.toDemoPaneKey
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import com.mfhapps.trendingui.ui.components.BrandMark
import com.mfhapps.trendingui.ui.components.CollapsedHeaderBackdrop
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.components.CatalogMorphShapes
import com.mfhapps.trendingui.ui.components.ShapeClickableSurface
import com.mfhapps.trendingui.ui.components.rememberCatalogCardColors
import com.mfhapps.trendingui.ui.platform.catalogAdaptiveMinTileWidth
import com.mfhapps.trendingui.ui.platform.catalogCollapsedBarWindowInsets
import com.mfhapps.trendingui.ui.platform.catalogListContentWindowInsets
import com.mfhapps.trendingui.ui.platform.catalogScaffoldContentPadding
import com.mfhapps.trendingui.ui.platform.isExpandedWindowWidth
import com.mfhapps.trendingui.ui.platform.isMediumOrWiderWindow
import com.mfhapps.trendingui.ui.theme.CatalogColorMath
import com.mfhapps.trendingui.ui.accessibility.SectionChangeHapticEffect
import com.mfhapps.trendingui.ui.theme.CatalogHomeSystemBars
import com.mfhapps.trendingui.ui.theme.HomeLayoutStyle
import com.mfhapps.trendingui.ui.theme.LocalHomeLayoutStyle

private const val CONTENT_TYPE_HEADER = "header"
private const val CONTENT_TYPE_HERO = "hero"
private const val CONTENT_TYPE_SECTION = "section"
private const val CONTENT_TYPE_DEMO = "demo"
private const val CATALOG_PREFIX_ITEM_COUNT = 3
private val CatalogPortraitGutter = 12.dp
private val CatalogLandscapeGutter = 8.dp
private val ListCardShape = RoundedCornerShape(20.dp)
private val HeroCardShape = RoundedCornerShape(28.dp)
private val CompactCardShape = RoundedCornerShape(18.dp)

private fun scrolledPastBrandHeader(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    brandHeaderHeightPx: Int,
): Boolean {
    if (firstVisibleItemIndex > 0) return true
    if (brandHeaderHeightPx == 0) return false
    return firstVisibleItemScrollOffset >= (brandHeaderHeightPx * 1.15f).toInt()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DemoCatalogScreen(
    onOpenDemo: (Any) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    launcherIcon: AppLauncherIcon = AppLauncherIcon.Default,
    selectedPaneKey: DemoPaneKey? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val layoutStyle = LocalHomeLayoutStyle.current
    val scheme = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val bentoGridState = rememberLazyStaggeredGridState()
    val compactListState = rememberLazyListState()
    var brandHeaderHeightPx by remember(layoutStyle) { mutableIntStateOf(0) }
    val compactBarVisible by remember(layoutStyle, brandHeaderHeightPx) {
        derivedStateOf {
            when (layoutStyle) {
                HomeLayoutStyle.FeaturedList -> scrolledPastBrandHeader(
                    listState.firstVisibleItemIndex,
                    listState.firstVisibleItemScrollOffset,
                    brandHeaderHeightPx,
                )
                HomeLayoutStyle.BentoGrid -> {
                    bentoGridState.layoutInfo
                    scrolledPastBrandHeader(
                        bentoGridState.firstVisibleItemIndex,
                        bentoGridState.firstVisibleItemScrollOffset,
                        brandHeaderHeightPx,
                    )
                }
                HomeLayoutStyle.CompactTiles -> scrolledPastBrandHeader(
                    compactListState.firstVisibleItemIndex,
                    compactListState.firstVisibleItemScrollOffset,
                    brandHeaderHeightPx,
                )
            }
        }
    }
    val demoCountLabel = "${demoCatalogEntries.size} interactive demos"
    var activeCategory: DemoCategory? by remember { mutableStateOf(null) }
    val visibleDemos = remember(activeCategory) {
        if (activeCategory == null) demoCatalogEntries
        else demoCatalogEntries.filter { it.category == activeCategory }
    }
    val featuredDemo = remember {
        demoCatalogEntries.firstOrNull { it.title == "Pretext Engine" }
            ?: demoCatalogEntries.first()
    }
    val grouped = remember(visibleDemos) {
        visibleDemos.groupBy { it.category }
            .toSortedMap(compareBy { it.ordinal })
    }
    CatalogHomeSystemBars(compactToolbarVisible = compactBarVisible)

    val activeCatalogSection by remember(layoutStyle, grouped, compactBarVisible) {
        derivedStateOf {
            if (!compactBarVisible) return@derivedStateOf null
            when (layoutStyle) {
                HomeLayoutStyle.FeaturedList -> catalogSectionForScrollIndex(
                    listIndex = listState.firstVisibleItemIndex,
                    grouped = grouped,
                )
                HomeLayoutStyle.BentoGrid -> {
                    val layoutInfo = bentoGridState.layoutInfo
                    val anchorIndex = layoutInfo.visibleItemsInfo
                        .firstOrNull()
                        ?.index
                        ?: bentoGridState.firstVisibleItemIndex
                    catalogSectionForScrollIndex(
                        listIndex = anchorIndex,
                        grouped = grouped,
                    )
                }
                HomeLayoutStyle.CompactTiles -> catalogSectionForScrollIndex(
                    listIndex = compactListState.firstVisibleItemIndex,
                    grouped = grouped,
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = catalogListContentWindowInsets(),
        ) { innerPadding ->
            val landscape = LocalConfiguration.current.orientation ==
                Configuration.ORIENTATION_LANDSCAPE
            val listContentPadding = catalogScaffoldContentPadding(
                innerPadding = innerPadding,
                horizontalGutter = if (landscape) CatalogLandscapeGutter else CatalogPortraitGutter,
            )
            when (layoutStyle) {
                HomeLayoutStyle.FeaturedList -> FeaturedCatalogList(
                    listState = listState,
                    contentPadding = listContentPadding,
                    launcherIcon = launcherIcon,
                    demoCountLabel = demoCountLabel,
                    featuredDemo = featuredDemo,
                    grouped = grouped,
                    activeCategory = activeCategory,
                    onCategoryChange = { activeCategory = it },
                    selectedPaneKey = selectedPaneKey,
                    onOpenDemo = onOpenDemo,
                    onOpenSettings = onOpenSettings,
                    onBrandHeaderSized = { brandHeaderHeightPx = it },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                HomeLayoutStyle.BentoGrid -> BentoCatalogGrid(
                    gridState = bentoGridState,
                    contentPadding = listContentPadding,
                    launcherIcon = launcherIcon,
                    demoCountLabel = demoCountLabel,
                    featuredDemo = featuredDemo,
                    grouped = grouped,
                    activeCategory = activeCategory,
                    onCategoryChange = { activeCategory = it },
                    selectedPaneKey = selectedPaneKey,
                    onOpenDemo = onOpenDemo,
                    onOpenSettings = onOpenSettings,
                    onBrandHeaderSized = { brandHeaderHeightPx = it },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                HomeLayoutStyle.CompactTiles -> CompactCatalogGrid(
                    listState = compactListState,
                    contentPadding = listContentPadding,
                    launcherIcon = launcherIcon,
                    demoCountLabel = demoCountLabel,
                    featuredDemo = featuredDemo,
                    visibleDemos = visibleDemos,
                    activeCategory = activeCategory,
                    onCategoryChange = { activeCategory = it },
                    selectedPaneKey = selectedPaneKey,
                    onOpenDemo = onOpenDemo,
                    onOpenSettings = onOpenSettings,
                    onBrandHeaderSized = { brandHeaderHeightPx = it },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        }
        AnimatedVisibility(
            visible = compactBarVisible,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f),
        ) {
            CatalogCollapsedTopBar(
                activeSection = activeCatalogSection,
                launcherIcon = launcherIcon,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeaturedCatalogList(
    listState: androidx.compose.foundation.lazy.LazyListState,
    contentPadding: PaddingValues,
    launcherIcon: AppLauncherIcon,
    demoCountLabel: String,
    featuredDemo: DemoCatalogEntry,
    grouped: Map<DemoCategory, List<DemoCatalogEntry>>,
    activeCategory: DemoCategory?,
    onCategoryChange: (DemoCategory?) -> Unit,
    selectedPaneKey: DemoPaneKey?,
    onOpenDemo: (Any) -> Unit,
    onOpenSettings: () -> Unit,
    onBrandHeaderSized: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .appHazeSource(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "brand_header", contentType = CONTENT_TYPE_HEADER) {
            CatalogBrandHeader(
                demoCountLabel = demoCountLabel,
                onOpenSettings = onOpenSettings,
                launcherIcon = launcherIcon,
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { onBrandHeaderSized(it.height) }
                    .padding(bottom = 4.dp),
            )
        }
        item(key = "featured_hero", contentType = CONTENT_TYPE_HERO) {
            FeaturedHeroCard(
                demo = featuredDemo,
                onClick = { onOpenDemo(featuredDemo.route) },
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                modifier = Modifier.animateItem(),
            )
        }
        item(key = "catalog_filters", contentType = CONTENT_TYPE_HEADER) {
            CatalogFilters(
                activeCategory = activeCategory,
                onCategoryChange = onCategoryChange,
                modifier = Modifier.animateItem(),
            )
        }
        grouped.forEach { (category, demos) ->
            item(key = "section_$category", contentType = CONTENT_TYPE_SECTION) {
                CategoryHeader(category)
            }
            items(
                items = demos,
                key = { demo -> "demo_${demo.route::class.simpleName}" },
                contentType = { CONTENT_TYPE_DEMO },
            ) { demo ->
                DemoListRow(
                    demo = demo,
                    selected = demo.route.toDemoPaneKey() == selectedPaneKey,
                    onClick = { onOpenDemo(demo.route) },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BentoCatalogGrid(
    gridState: androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState,
    contentPadding: PaddingValues,
    launcherIcon: AppLauncherIcon,
    demoCountLabel: String,
    featuredDemo: DemoCatalogEntry,
    grouped: Map<DemoCategory, List<DemoCatalogEntry>>,
    activeCategory: DemoCategory?,
    onCategoryChange: (DemoCategory?) -> Unit,
    selectedPaneKey: DemoPaneKey?,
    onOpenDemo: (Any) -> Unit,
    onOpenSettings: () -> Unit,
    onBrandHeaderSized: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    val minTileWidth = catalogAdaptiveMinTileWidth()
    LazyVerticalStaggeredGrid(
        state = gridState,
        columns = StaggeredGridCells.Adaptive(minTileWidth),
        modifier = Modifier
            .fillMaxSize()
            .appHazeSource(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            CatalogBrandHeader(
                demoCountLabel = demoCountLabel,
                onOpenSettings = onOpenSettings,
                launcherIcon = launcherIcon,
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { onBrandHeaderSized(it.height) }
                    .padding(bottom = 4.dp),
            )
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            FeaturedHeroCard(
                demo = featuredDemo,
                onClick = { onOpenDemo(featuredDemo.route) },
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            CatalogFilters(
                activeCategory = activeCategory,
                onCategoryChange = onCategoryChange,
            )
        }
        grouped.forEach { (category, demos) ->
            item(span = StaggeredGridItemSpan.FullLine, key = "bento_hdr_$category") {
                CategoryHeader(category)
            }
            itemsIndexed(
                items = demos,
                key = { _, demo -> "bento_${demo.route::class.simpleName}" },
            ) { index, demo ->
                BentoDemoTile(
                    demo = demo,
                    tall = index % 3 != 1,
                    selected = demo.route.toDemoPaneKey() == selectedPaneKey,
                    onClick = { onOpenDemo(demo.route) },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        }
    }
}

@Composable
private fun CompactCatalogGrid(
    listState: androidx.compose.foundation.lazy.LazyListState,
    contentPadding: PaddingValues,
    launcherIcon: AppLauncherIcon,
    demoCountLabel: String,
    featuredDemo: DemoCatalogEntry,
    visibleDemos: List<DemoCatalogEntry>,
    activeCategory: DemoCategory?,
    onCategoryChange: (DemoCategory?) -> Unit,
    selectedPaneKey: DemoPaneKey?,
    onOpenDemo: (Any) -> Unit,
    onOpenSettings: () -> Unit,
    onBrandHeaderSized: (Int) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
    val tileHeight = rememberCompactTileMinHeight()
    val rowGap = 10.dp
    val columnCount = when {
        isExpandedWindowWidth() -> 3
        isMediumOrWiderWindow() -> 3
        else -> 2
    }
    val gridHeight = remember(visibleDemos.size, tileHeight, columnCount) {
        val rows = if (visibleDemos.isEmpty()) {
            0
        } else {
            (visibleDemos.size + columnCount - 1) / columnCount
        }
        if (rows == 0) 0.dp else tileHeight * rows + rowGap * (rows - 1).coerceAtLeast(0)
    }
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .appHazeSource(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CatalogBrandHeader(
                demoCountLabel = demoCountLabel,
                onOpenSettings = onOpenSettings,
                launcherIcon = launcherIcon,
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { onBrandHeaderSized(it.height) }
                    .padding(bottom = 4.dp),
            )
        }
        item {
            FeaturedHeroCard(
                demo = featuredDemo,
                onClick = { onOpenDemo(featuredDemo.route) },
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
        item {
            CatalogFilters(
                activeCategory = activeCategory,
                onCategoryChange = onCategoryChange,
            )
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                userScrollEnabled = false,
            ) {
                items(
                    items = visibleDemos,
                    key = { demo -> "grid_${demo.route::class.simpleName}" },
                ) { demo ->
                    CompactDemoTile(
                        demo = demo,
                        selected = demo.route.toDemoPaneKey() == selectedPaneKey,
                        onClick = { onOpenDemo(demo.route) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FeaturedHeroCard(
    demo: DemoCatalogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val heroStops = remember(scheme) {
        listOf(
            scheme.primary,
            lerp(scheme.primary, scheme.tertiary, 0.2f),
            lerp(scheme.primary, scheme.tertiary, 0.45f),
            lerp(scheme.secondary, scheme.tertiary, 0.65f),
            scheme.tertiary,
        )
    }
    val heroForeground = remember(heroStops, scheme) {
        CatalogColorMath.resolveHeroCardForeground(heroStops, scheme)
    }
    val heroBrush = remember(heroStops) {
        Brush.linearGradient(
            colors = heroStops,
            start = Offset(0f, 0f),
            end = Offset(900f, 700f),
        )
    }
    val heroGlow = remember(heroStops) {
        Brush.radialGradient(
            colors = listOf(
                heroStops.first().copy(alpha = 0.55f),
                Color.Transparent,
            ),
            center = Offset(0.85f, 0.12f),
            radius = 520f,
        )
    }
    val heroMorph = CatalogMorphShapes.heroMorph
    val iconShape = CatalogMorphShapes.iconForDemo(demo.route).toShape()
    val accentShape = MaterialShapes.Sunny.toShape()
    ShapeClickableSurface(
        onClick = onClick,
        shape = HeroCardShape,
        morphRest = heroMorph.rest,
        morphPressed = heroMorph.pressed,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Featured demo: ${demo.title}. ${demo.subtitle}."
            },
        color = Color.Transparent,
        contentColor = heroForeground.title,
        rippleColor = heroForeground.title.copy(alpha = 0.28f),
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, heroForeground.title.copy(alpha = 0.22f)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(heroBrush)
                .drawBehind { drawRect(brush = heroGlow) },
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 28.dp, y = (-18).dp)
                    .size(132.dp)
                    .graphicsLayer { alpha = 0.2f }
                    .clip(accentShape)
                    .background(heroForeground.title),
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.largeIncreased,
                        color = heroForeground.badgeFill,
                        shadowElevation = 2.dp,
                    ) {
                        Text(
                            text = "Spotlight",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = heroForeground.badgeText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        )
                    }
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = heroForeground.title.copy(alpha = 0.14f),
                    ) {
                        Text(
                            text = demo.category.displayName(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = heroForeground.section,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = demo.title,
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        color = heroForeground.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = demo.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = heroForeground.subtitle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DemoSharedIcon(
                        contentKey = demo.route.demoSharedContentKey(),
                        icon = demo.icon,
                        shape = iconShape,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = MaterialTheme.shapes.largeIncreased,
                        color = heroForeground.badgeFill,
                        shadowElevation = 4.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "Open demo",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = heroForeground.badgeText,
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = heroForeground.badgeText,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogBrandHeader(
    demoCountLabel: String,
    onOpenSettings: () -> Unit,
    launcherIcon: AppLauncherIcon,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BrandMark(
            size = 48.dp,
            animated = true,
            launcherIcon = launcherIcon,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "UITrends",
                style = MaterialTheme.typography.headlineMediumEmphasized,
                color = scheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Material 3 pattern library · $demoCountLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FilledTonalIconButton(
            onClick = onOpenSettings,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = scheme.secondaryContainer,
                contentColor = scheme.onSecondaryContainer,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
            )
        }
    }
}

@Composable
private fun CatalogCollapsedTopBar(
    activeSection: DemoCategory?,
    launcherIcon: AppLauncherIcon,
) {
    SectionChangeHapticEffect(sectionKey = activeSection)
    val scheme = MaterialTheme.colorScheme
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    CollapsedHeaderBackdrop(
        modifier = Modifier.fillMaxWidth(),
        collapsedFraction = 1f,
        surfaceColor = scheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(catalogCollapsedBarWindowInsets())
                .padding(
                    horizontal = if (landscape) CatalogLandscapeGutter else CatalogPortraitGutter,
                    vertical = 10.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrandMark(
                size = 32.dp,
                animated = false,
                launcherIcon = launcherIcon,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "UITrends",
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AnimatedContent(
                    targetState = activeSection,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically { it / 3 }) togetherWith
                            (fadeOut(tween(160)) + slideOutVertically { -it / 3 })
                    },
                    label = "collapsedSectionTitle",
                ) { section ->
                    if (section != null) {
                        Text(
                            text = section.displayName(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: DemoCategory) {
    Text(
        text = category.displayName(),
        style = MaterialTheme.typography.titleSmallEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .semantics { role = Role.Tab },
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DemoListRow(
    demo: DemoCatalogEntry,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val cardColors = rememberCatalogCardColors(selected)
    val morphPair = CatalogMorphShapes.forDemo(demo.route)
    val iconShape = morphPair.rest.toShape()
    val scheme = MaterialTheme.colorScheme
    ShapeClickableSurface(
        onClick = onClick,
        shape = ListCardShape,
        morphRest = morphPair.rest,
        morphPressed = morphPair.pressed,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "${demo.title}. ${demo.subtitle}."
            },
        color = cardColors.container,
        contentColor = cardColors.onContainer,
        rippleColor = cardColors.ripple,
        tonalElevation = if (selected) 3.dp else 1.dp,
        shadowElevation = if (selected) 2.dp else 0.dp,
        border = if (selected) {
            BorderStroke(1.5.dp, scheme.primary.copy(alpha = 0.55f))
        } else {
            null
        },
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            headlineContent = {
                Text(
                    demo.title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = cardColors.onContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Text(
                    demo.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cardColors.onContainerVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingContent = {
                DemoSharedIcon(
                    contentKey = demo.route.demoSharedContentKey(),
                    icon = demo.icon,
                    shape = iconShape,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            },
            trailingContent = {
                DecorativeIcon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    tint = cardColors.onContainerVariant,
                    modifier = Modifier.size(22.dp),
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
                headlineColor = cardColors.onContainer,
                supportingColor = cardColors.onContainerVariant,
            ),
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun BentoDemoTile(
    demo: DemoCatalogEntry,
    tall: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val cardColors = rememberCatalogCardColors(selected)
    val morphPair = CatalogMorphShapes.forDemo(demo.route)
    val iconShape = morphPair.rest.toShape()
    val tileMinHeight = rememberBentoTileMinHeight(tall = tall)
    ShapeClickableSurface(
        onClick = onClick,
        shape = ListCardShape,
        morphRest = morphPair.rest,
        morphPressed = morphPair.pressed,
        modifier = modifier
            .fillMaxWidth()
            .height(tileMinHeight),
        color = cardColors.container,
        contentColor = cardColors.onContainer,
        rippleColor = cardColors.ripple,
        tonalElevation = if (selected) 3.dp else 1.dp,
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            DemoSharedIcon(
                contentKey = demo.route.demoSharedContentKey(),
                icon = demo.icon,
                shape = iconShape,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    demo.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = cardColors.onContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    demo.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = cardColors.onContainerVariant,
                    maxLines = if (tall) 3 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CompactDemoTile(
    demo: DemoCatalogEntry,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val cardColors = rememberCatalogCardColors(selected)
    val morphPair = CatalogMorphShapes.forDemo(demo.route)
    val iconShape = morphPair.rest.toShape()
    val tileMinHeight = rememberCompactTileMinHeight()
    val scheme = MaterialTheme.colorScheme
    ShapeClickableSurface(
        onClick = onClick,
        shape = CompactCardShape,
        morphRest = morphPair.rest,
        morphPressed = morphPair.pressed,
        modifier = modifier
            .fillMaxWidth()
            .height(tileMinHeight),
        color = cardColors.container,
        contentColor = cardColors.onContainer,
        rippleColor = cardColors.ripple,
        tonalElevation = if (selected) 2.dp else 1.dp,
        shadowElevation = if (selected) 1.dp else 0.dp,
        border = if (selected) {
            BorderStroke(1.5.dp, scheme.primary.copy(alpha = 0.55f))
        } else {
            null
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DemoSharedIcon(
                contentKey = demo.route.demoSharedContentKey(),
                icon = demo.icon,
                shape = iconShape,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
            )
            Text(
                demo.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = cardColors.onContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                demo.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = cardColors.onContainerVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CatalogFilters(
    activeCategory: DemoCategory?,
    onCategoryChange: (DemoCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val chips = remember {
        listOf(
            "All" to null,
            "Layout" to DemoCategory.Layout,
            "Motion" to DemoCategory.Motion,
            "Surfaces" to DemoCategory.Surfaces,
            "AI" to DemoCategory.AiReading,
            "3D" to DemoCategory.Sensors3d,
        )
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = scheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            chips.forEach { (label, category) ->
                val selected = activeCategory == category
                FilterChip(
                    selected = selected,
                    onClick = { onCategoryChange(category) },
                    label = {
                        Text(
                            text = label,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.Transparent,
                        labelColor = scheme.onSurfaceVariant,
                        selectedContainerColor = scheme.primaryContainer,
                        selectedLabelColor = scheme.onPrimaryContainer,
                    ),
                    border = null,
                )
            }
        }
    }
}

private fun catalogSectionForScrollIndex(
    listIndex: Int,
    grouped: Map<DemoCategory, List<DemoCatalogEntry>>,
): DemoCategory? {
    if (listIndex < CATALOG_PREFIX_ITEM_COUNT) return null
    var index = CATALOG_PREFIX_ITEM_COUNT
    var lastSection: DemoCategory? = null
    for ((category, demos) in grouped) {
        lastSection = category
        if (listIndex <= index) return category
        index += 1
        if (listIndex < index + demos.size) return category
        index += demos.size
    }
    return lastSection
}

private fun DemoCategory.displayName(): String = when (this) {
    DemoCategory.Layout -> "Layout"
    DemoCategory.Motion -> "Motion"
    DemoCategory.Surfaces -> "Surfaces & style"
    DemoCategory.AiReading -> "AI & reading"
    DemoCategory.Sensors3d -> "Sensors & 3D"
}
