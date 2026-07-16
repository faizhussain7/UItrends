package com.mfhapps.trendingui.screens.spatial

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.HingePolicy
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp as lerpTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.CollapsingBlurTopBarLayout
import com.mfhapps.trendingui.ui.components.IconButton
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.components.collapsingTopBarContentPadding
import com.mfhapps.trendingui.ui.components.rememberCollapsedTopAppBarColors
import com.mfhapps.trendingui.ui.detail.DetailPaneTopBarActions
import com.mfhapps.trendingui.ui.detail.LocalNestedBackDispatcher
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import com.mfhapps.trendingui.ui.motion.ExpressiveMotion
import com.mfhapps.trendingui.ui.motion.expressiveSpatialSpec
import com.mfhapps.trendingui.ui.platform.appBarTopWindowInsets
import com.mfhapps.trendingui.ui.platform.isCompactWindowWidth
import kotlinx.coroutines.launch

private val SpatialCardBorderWidth = 1.5.dp
private val SpatialCardInnerPaddingH = 16.dp
private val SpatialCardInnerPaddingV = 14.dp
private val SpatialListItemSpacing = 8.dp
private const val SpatialScreenTitle = "Spatial depth"

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SpatialDepthScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
) {
    ProvideSpatialChrome {
        val chrome = spatialChromeOrRemember()
        val tilt = rememberSpatialTiltDegrees()
        val haptics = LocalHapticFeedback.current
        val isCompact = isCompactWindowWidth()
        val nestedBackDispatcher = LocalNestedBackDispatcher.current
        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
        val collapsedFraction by remember {
            derivedStateOf { scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f) }
        }

        val layers = remember { defaultSpatialLayers() }
        val sections = remember(layers) { layers.toDepthSections() }
        val layersById = remember(layers) { layers.associateBy { it.id } }
        val windowAdaptiveInfo = currentWindowAdaptiveInfo()
        val scaffoldDirective = remember(windowAdaptiveInfo) {
            calculatePaneScaffoldDirective(
                windowAdaptiveInfo = windowAdaptiveInfo,
                verticalHingePolicy = HingePolicy.AlwaysAvoid,
            ).copy(horizontalPartitionSpacerSize = 0.dp)
        }
        val navigator = rememberListDetailPaneScaffoldNavigator<Int>(
            scaffoldDirective = scaffoldDirective,
        )
        val scope = rememberCoroutineScope()
        val canPopDetail = navigator.canNavigateBack()

        // Navigator contentKey is the single source of truth for selection.
        val selectedId = navigator.currentDestination?.contentKey
        val selectedLayer = selectedId?.let(layersById::get)

        // Expanded layouts always show a detail pane — seed the first layer once.
        LaunchedEffect(isCompact, layers) {
            val firstId = layers.firstOrNull()?.id ?: return@LaunchedEffect
            if (!isCompact && navigator.currentDestination?.contentKey == null) {
                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, firstId)
            }
        }

        val subtitle = when {
            canPopDetail && isCompact -> "Detail open · swipe back to return"
            isCompact -> "Tilt for perspective · tap a layer to open detail"
            else -> "List + detail · gyro parallax on the active pane"
        }

        val popDetail: () -> Unit = remember(scope, navigator, haptics) {
            {
                haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
                scope.launch {
                    navigator.navigateBack(
                        backNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
                    )
                }
            }
        }

        val onLayerSelected: (SpatialLayer) -> Unit = remember(scope, navigator, haptics) {
            { layer ->
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                scope.launch {
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, layer.id)
                }
            }
        }

        DisposableEffect(canPopDetail) {
            nestedBackDispatcher.handler = if (canPopDetail) {
                { popDetail(); true }
            } else {
                null
            }
            onDispose { nestedBackDispatcher.handler = null }
        }

        BackHandler(enabled = canPopDetail) { popDetail() }

        CollapsingBlurTopBarLayout(
            scrollBehavior = scrollBehavior,
            collapsedFraction = collapsedFraction,
            modifier = Modifier.fillMaxSize(),
            topBar = { barModifier ->
                SpatialCollapsingTopBar(
                    scrollBehavior = scrollBehavior,
                    collapsedFraction = collapsedFraction,
                    subtitle = subtitle,
                    onNavigateBack = onNavigateBack,
                    guide = guide,
                    barModifier = barModifier,
                )
            },
        ) {
            NavigableListDetailPaneScaffold(
                navigator = navigator,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                listPane = {
                    AnimatedPane {
                        SpatialLayerList(
                            sections = sections,
                            selectedId = selectedId,
                            tilt = tilt,
                            onLayerSelected = onLayerSelected,
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        when (val layer = selectedLayer) {
                            null -> SpatialDetailPlaceholder(
                                modifier = Modifier.fillMaxSize(),
                            )
                            else -> key(layer.id) {
                                SpatialDetailPane(
                                    layer = layer,
                                    tilt = tilt,
                                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun SpatialLayerList(
    sections: List<SpatialDepthSection>,
    selectedId: Int?,
    tilt: SpatialTiltDegrees,
    onLayerSelected: (SpatialLayer) -> Unit,
) {
    val listState = rememberLazyListState()
    val parallax by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset * 0.18f }
    }
    // Skip scrolling for the first established selection (enter / dual-pane seed).
    var previousSelectedId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(selectedId) {
        val id = selectedId ?: return@LaunchedEffect
        val previous = previousSelectedId
        previousSelectedId = id
        if (previous == null || previous == id) return@LaunchedEffect

        var scrollIndex = 0
        for (section in sections) {
            scrollIndex += 1
            val local = section.layers.indexOfFirst { it.id == id }
            if (local >= 0) {
                scrollIndex += local
                val alreadyVisible = listState.layoutInfo.visibleItemsInfo.any { it.index == scrollIndex }
                if (!alreadyVisible) {
                    listState.animateScrollToItem(scrollIndex)
                }
                return@LaunchedEffect
            }
            scrollIndex += section.layers.size
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .spatialPerspective(tilt, intensity = 0.72f)
            .graphicsLayer { translationY = -parallax * 0.28f },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .appHazeSource(),
            contentPadding = collapsingTopBarContentPadding(
                extra = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
            ),
            verticalArrangement = Arrangement.spacedBy(SpatialListItemSpacing),
        ) {
            sections.forEach { section ->
                stickyHeader(key = "tier-${section.tier}") {
                    SpatialSectionHeader(
                        title = section.title,
                        caption = section.caption,
                    )
                }
                items(
                    items = section.layers,
                    key = { it.id },
                ) { layer ->
                    SpatialDepthCard(
                        layer = layer,
                        selected = layer.id == selectedId,
                        onClick = { onLayerSelected(layer) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .semantics {
                                contentDescription =
                                    "${layer.title}, ${layer.role}, depth tier ${layer.depthTier + 1}, tap to view detail"
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun SpatialSectionHeader(
    title: String,
    caption: String,
) {
    val chrome = spatialChromeOrRemember()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = MaterialTheme.shapes.medium,
        color = chrome.headerSticky,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = chrome.headerStickyLabel,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelMedium,
                color = chrome.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpatialCollapsingTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    collapsedFraction: Float,
    subtitle: String,
    onNavigateBack: () -> Unit,
    guide: DemoTrendGuide?,
    barModifier: Modifier = Modifier,
) {
    val chrome = spatialChromeOrRemember()
    val scheme = MaterialTheme.colorScheme
    val nestedBackDispatcher = LocalNestedBackDispatcher.current
    val iconColor = chrome.topBarIcon
    val titleStyle = lerpTextStyle(
        start = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        stop = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        fraction = collapsedFraction,
    )
    val subtitleAlpha = (1f - collapsedFraction * 1.35f).coerceIn(0f, 1f)

    LargeTopAppBar(
        modifier = barModifier,
        windowInsets = appBarTopWindowInsets(),
        scrollBehavior = scrollBehavior,
        colors = rememberCollapsedTopAppBarColors(
            collapsedFraction = collapsedFraction,
            containerColor = Color.Transparent,
            scrolledContainerColor = scheme.surface,
            navigationIconContentColor = iconColor,
            titleContentColor = chrome.sectionTitle,
            actionIconContentColor = iconColor,
        ),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = SpatialScreenTitle,
                    style = titleStyle,
                    color = chrome.sectionTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitleAlpha > 0.01f) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = chrome.body,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer { alpha = subtitleAlpha },
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    if (nestedBackDispatcher.handler?.invoke() != true) {
                        onNavigateBack()
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(contentColor = iconColor),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to catalog",
                    tint = iconColor,
                )
            }
        },
        actions = {
            DetailPaneTopBarActions(
                guide = guide,
                iconTint = iconColor,
            )
        },
    )
}

@Composable
private fun SpatialDetailPlaceholder(
    modifier: Modifier = Modifier,
) {
    val chrome = spatialChromeOrRemember()
    Box(
        modifier = modifier.padding(
            collapsingTopBarContentPadding(
                extra = PaddingValues(start = 12.dp, end = 12.dp, bottom = 20.dp),
            ),
        ),
        contentAlignment = Alignment.Center,
    ) {
        SpatialLayerSurface(
            selected = false,
            elevation = 0.dp,
            borderColor = chrome.cardBorder,
        ) {
            Text(
                text = "Select a layer",
                style = MaterialTheme.typography.titleMedium,
                color = chrome.sectionTitle,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Choose a depth card from the list to inspect perspective, parallax, and adaptivity details.",
                style = MaterialTheme.typography.bodyLarge,
                color = chrome.body,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SpatialDetailPane(
    layer: SpatialLayer,
    tilt: SpatialTiltDegrees,
    nestedScrollConnection: NestedScrollConnection,
    modifier: Modifier = Modifier,
) {
    val chrome = spatialChromeOrRemember()
    val depthFactor = spatialDepthFactor(layer.id)
    val scrollState = rememberScrollState()
    val detailTilt = if (tilt.enabled) {
        tilt.copy(
            pitch = tilt.pitch * depthFactor * 0.85f,
            roll = tilt.roll * depthFactor * 0.85f,
        )
    } else {
        tilt
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .spatialPerspective(detailTilt, intensity = 0.9f)
            .verticalScroll(scrollState)
            .nestedScroll(nestedScrollConnection)
            .padding(
                collapsingTopBarContentPadding(
                    extra = PaddingValues(start = 12.dp, end = 12.dp, bottom = 20.dp),
                ),
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SpatialLayerSurface(
            selected = true,
            elevation = if (chrome.isDark) 12.dp else 8.dp,
            borderColor = chrome.cardBorderSelected,
        ) {
            SpatialLayerTitleRow(
                title = layer.title,
                subtitle = "${layer.role} · tier ${layer.depthTier + 1}",
                subtitleColor = chrome.accent,
                badgeLabel = "Z${layer.depthTier + 1}",
                titleStyle = MaterialTheme.typography.headlineSmall,
                titleWeight = FontWeight.SemiBold,
            )

            Text(
                text = layer.blurb,
                style = MaterialTheme.typography.bodyLarge,
                color = chrome.body,
                modifier = Modifier.padding(top = 12.dp),
            )

            SpatialDepthMeter(
                activeTier = layer.depthTier,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SpatialSpecChip(
                    label = "Parallax",
                    value = "${"%.2f".format(depthFactor)}×",
                    modifier = Modifier.weight(1f),
                )
                SpatialSpecChip(
                    label = "Lift",
                    value = "${spatialLayerLift(layer.id).value.toInt()}dp",
                    modifier = Modifier.weight(1f),
                )
                SpatialSpecChip(
                    label = "Plane",
                    value = spatialTierTitle(layer.depthTier),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        SpatialLayerSurface(
            selected = false,
            elevation = if (chrome.isDark) 6.dp else 3.dp,
            borderColor = chrome.cardBorder,
        ) {
            Text(
                text = "Perspective",
                style = MaterialTheme.typography.titleSmall,
                color = chrome.sectionTitle,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "This pane shares the same gyro-driven perspective as the list. " +
                    "Tilt your device to feel the card float in Z-space—motion stays subtle and respects reduce motion.",
                style = MaterialTheme.typography.bodyLarge,
                color = chrome.body,
                modifier = Modifier.padding(top = 8.dp),
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = chrome.cardBorder,
            )

            Text(
                text = "Adaptivity",
                style = MaterialTheme.typography.titleSmall,
                color = chrome.sectionTitle,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "On phones the detail pane replaces the list beneath the collapsing header. " +
                    "On wide screens both panes stay visible, with sticky depth sections keeping the list scannable.",
                style = MaterialTheme.typography.bodyLarge,
                color = chrome.body,
                modifier = Modifier.padding(top = 8.dp),
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = chrome.cardBorder,
            )

            Text(
                text = "List management",
                style = MaterialTheme.typography.titleSmall,
                color = chrome.sectionTitle,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Layers are grouped by depth tier with sticky section headers. Selection " +
                    "is owned by the list–detail navigator, so back, fold, and dual-pane stay in sync.",
                style = MaterialTheme.typography.bodyLarge,
                color = chrome.body,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SpatialSpecChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val chrome = spatialChromeOrRemember()
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = chrome.readableSurface,
        border = BorderStroke(1.dp, chrome.cardBorder),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = chrome.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = chrome.sectionTitle,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SpatialLayerSurface(
    selected: Boolean,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val chrome = spatialChromeOrRemember()
    val surfaceColor = if (selected) chrome.cardSurfaceSelected else chrome.readableSurfaceStrong
    val cardShape = MaterialTheme.shapes.medium
    val surfaceModifier = modifier.fillMaxWidth()
    val padding = Modifier.padding(
        horizontal = SpatialCardInnerPaddingH,
        vertical = SpatialCardInnerPaddingV,
    )
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = surfaceModifier,
            shape = cardShape,
            color = surfaceColor,
            border = BorderStroke(SpatialCardBorderWidth, borderColor),
            shadowElevation = elevation,
            tonalElevation = 0.dp,
        ) {
            Column(modifier = padding, content = content)
        }
    } else {
        Surface(
            modifier = surfaceModifier,
            shape = cardShape,
            color = surfaceColor,
            border = BorderStroke(SpatialCardBorderWidth, borderColor),
            shadowElevation = elevation,
            tonalElevation = 0.dp,
        ) {
            Column(modifier = padding, content = content)
        }
    }
}

@Composable
private fun SpatialLayerTitleRow(
    title: String,
    subtitle: String,
    subtitleColor: Color,
    badgeLabel: String,
    titleStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    titleWeight: FontWeight = FontWeight.Medium,
) {
    val chrome = spatialChromeOrRemember()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = titleStyle,
                color = chrome.sectionTitle,
                fontWeight = titleWeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = subtitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        SpatialDepthBadge(label = badgeLabel)
    }
}

@Composable
private fun SpatialDepthMeter(
    activeTier: Int,
    modifier: Modifier = Modifier,
) {
    val chrome = spatialChromeOrRemember()
    val tierCount = 5
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        repeat(tierCount) { tier ->
            val active = tier == activeTier
            val barHeight = (28 + tier * 14).dp
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(barHeight),
                shape = MaterialTheme.shapes.small,
                color = if (active) chrome.meterActive else chrome.meterIdle,
                tonalElevation = 0.dp,
                shadowElevation = if (active) 4.dp else 0.dp,
            ) {}
        }
    }
}

@Composable
private fun SpatialDepthBadge(label: String) {
    val chrome = spatialChromeOrRemember()
    Surface(
        shape = MaterialTheme.shapes.small,
        color = chrome.badgeContainer,
        tonalElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = chrome.badgeLabel,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SpatialDepthCard(
    layer: SpatialLayer,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chrome = spatialChromeOrRemember()
    val density = LocalDensity.current
    val reduceMotion = LocalReduceMotion.current
    val selectionProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = if (reduceMotion) {
            ExpressiveMotion.fastEffects()
        } else {
            expressiveSpatialSpec()
        },
        label = "spatial_layer_selection",
    )
    val depthOffset = spatialLayerLift(layer.id, selected = false)
    val surfaceColor = lerp(
        start = chrome.cardSurface,
        stop = chrome.cardSurfaceSelected,
        fraction = selectionProgress,
    )
    val shadowElevation = lerp(
        start = (2 + layer.depthTier).toFloat(),
        stop = if (chrome.isDark) 16f else 10f,
        fraction = selectionProgress,
    ).dp
    val borderColor = lerp(
        start = chrome.cardBorder,
        stop = chrome.cardBorderSelected,
        fraction = selectionProgress,
    )
    val subtitleColor = lerp(
        start = chrome.muted,
        stop = chrome.accent,
        fraction = selectionProgress,
    )

    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            translationY = with(density) {
                depthOffset.toPx() * (1f - selectionProgress)
            }
        },
        shape = MaterialTheme.shapes.medium,
        color = surfaceColor,
        border = BorderStroke(SpatialCardBorderWidth, borderColor),
        shadowElevation = shadowElevation,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = SpatialCardInnerPaddingH,
                vertical = SpatialCardInnerPaddingV,
            ),
        ) {
            SpatialLayerTitleRow(
                title = layer.title,
                subtitle = if (selected) "Selected · ${layer.role}" else layer.role,
                subtitleColor = subtitleColor,
                badgeLabel = "Z${layer.depthTier + 1}",
                titleWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}
