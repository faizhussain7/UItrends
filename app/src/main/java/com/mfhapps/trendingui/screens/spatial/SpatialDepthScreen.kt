package com.mfhapps.trendingui.screens.spatial

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import com.mfhapps.trendingui.ui.motion.ExpressiveMotion
import com.mfhapps.trendingui.ui.motion.expressiveSpatialSpec
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.adaptive.layout.HingePolicy
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.mfhapps.trendingui.ui.platform.appBarTopWindowInsets
import com.mfhapps.trendingui.ui.platform.isCompactWindowWidth
import kotlinx.coroutines.launch

private val SpatialCardShape = RoundedCornerShape(20.dp)
private val SpatialBadgeShape = RoundedCornerShape(8.dp)
private val SpatialCardBorderWidth = 1.5.dp
private val SpatialCardInnerPaddingH = 16.dp
private val SpatialCardInnerPaddingV = 14.dp
private val SpatialListItemSpacing = 6.dp
private const val SpatialScreenTitle = "Spatial depth"

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SpatialDepthScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val chrome = rememberSpatialChrome()
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
    var selectedId by remember { mutableIntStateOf(layers.first().id) }
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
    val selectedLayer = layers.firstOrNull { it.id == selectedId } ?: layers.first()

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
            selectedId = layer.id
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
        ListDetailPaneScaffold(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                SpatialLayerList(
                    layers = layers,
                    selectedId = selectedId,
                    chrome = chrome,
                    tilt = tilt,
                    onLayerSelected = onLayerSelected,
                )
            },
            detailPane = {
                SpatialDetailPane(
                    layer = selectedLayer,
                    tilt = tilt,
                    chrome = chrome,
                    nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    modifier = Modifier.fillMaxSize(),
                )
            },
        )
    }
}

@Composable
private fun SpatialLayerList(
    layers: List<SpatialLayer>,
    selectedId: Int,
    chrome: SpatialChrome,
    tilt: SpatialTiltDegrees,
    onLayerSelected: (SpatialLayer) -> Unit,
) {
    val listState = rememberLazyListState()
    val parallax by remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset * 0.22f }
    }

    LaunchedEffect(selectedId) {
        val index = layers.indexOfFirst { it.id == selectedId }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .spatialPerspective(tilt)
            .graphicsLayer { translationY = -parallax * 0.35f },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .appHazeSource(),
            contentPadding = collapsingTopBarContentPadding(
                extra = PaddingValues(bottom = 4.dp),
            ),
            verticalArrangement = Arrangement.spacedBy(SpatialListItemSpacing),
        ) {
            items(
                items = layers,
                key = { it.id },
            ) { layer ->
                SpatialDepthCard(
                    layer = layer,
                    selected = layer.id == selectedId,
                    chrome = chrome,
                    onClick = { onLayerSelected(layer) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .semantics {
                            contentDescription =
                                "${layer.title}, depth tier ${layer.depthTier + 1}, tap to view detail"
                        },
                )
            }
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
    val scheme = MaterialTheme.colorScheme
    val nestedBackDispatcher = LocalNestedBackDispatcher.current
    val iconColor = scheme.primary
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
            titleContentColor = scheme.onSurface,
            actionIconContentColor = iconColor,
        ),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = SpatialScreenTitle,
                    style = titleStyle,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitleAlpha > 0.01f) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
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
private fun SpatialDetailPane(
    layer: SpatialLayer,
    tilt: SpatialTiltDegrees,
    chrome: SpatialChrome,
    nestedScrollConnection: NestedScrollConnection,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val depthFactor = spatialDepthFactor(layer.id)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .spatialPerspective(
                if (tilt.enabled) {
                    tilt.copy(
                        pitch = tilt.pitch * depthFactor * 0.85f,
                        roll = tilt.roll * depthFactor * 0.85f,
                    )
                } else {
                    tilt
                },
            )
            .verticalScroll(scrollState)
            .nestedScroll(nestedScrollConnection)
            .padding(
                collapsingTopBarContentPadding(
                    extra = PaddingValues(bottom = 16.dp),
                ),
            ),
    ) {
        SpatialLayerSurface(
            selected = true,
            chrome = chrome,
            elevation = if (chrome.isDark) 10.dp else 6.dp,
            borderColor = scheme.primary.copy(alpha = 0.28f),
        ) {
            SpatialLayerTitleRow(
                title = layer.title,
                subtitle = "Depth tier ${layer.depthTier + 1} · parallax ${"%.2f".format(depthFactor)}×",
                subtitleColor = scheme.primary,
                badgeLabel = "Z${layer.depthTier + 1}",
                chrome = chrome,
                titleStyle = MaterialTheme.typography.headlineSmall,
                titleWeight = FontWeight.SemiBold,
            )

            SpatialDepthMeter(
                activeTier = layer.depthTier,
                chrome = chrome,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )

            Text(
                text = "Perspective",
                style = MaterialTheme.typography.titleSmall,
                color = scheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                text = "This pane shares the same gyro-driven perspective as the list. " +
                    "Tilt your device to feel the card float in Z-space—motion is subtle and respects reduce motion.",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            Text(
                text = "Adaptivity",
                style = MaterialTheme.typography.titleSmall,
                color = scheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                text = "On phones the detail pane replaces the list and stays below the collapsing header. " +
                    "On wide screens both panes stay visible with the same layer card layout.",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun SpatialLayerSurface(
    selected: Boolean,
    chrome: SpatialChrome,
    modifier: Modifier = Modifier,
    elevation: androidx.compose.ui.unit.Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val surfaceColor = if (selected) chrome.cardSurfaceSelected else chrome.cardSurface
    val surfaceModifier = modifier.fillMaxWidth()
    val padding = Modifier.padding(
        horizontal = SpatialCardInnerPaddingH,
        vertical = SpatialCardInnerPaddingV,
    )
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = surfaceModifier,
            shape = SpatialCardShape,
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
            shape = SpatialCardShape,
            color = chrome.readableSurfaceStrong,
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
    chrome: SpatialChrome,
    titleStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    titleWeight: FontWeight = FontWeight.Medium,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = titleStyle,
                color = scheme.onSurface,
                fontWeight = titleWeight,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = subtitleColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        SpatialDepthBadge(
            label = badgeLabel,
            chrome = chrome,
        )
    }
}

@Composable
private fun SpatialDepthMeter(
    activeTier: Int,
    chrome: SpatialChrome,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val tierCount = 5
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        repeat(tierCount) { tier ->
            val active = tier == activeTier
            val barHeight = (32 + tier * 12).dp
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(barHeight),
                shape = RoundedCornerShape(
                    topStart = 8.dp,
                    topEnd = 8.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 4.dp,
                ),
                color = if (active) {
                    scheme.primary.copy(alpha = if (chrome.isDark) 0.85f else 0.72f)
                } else {
                    scheme.surfaceContainerHigh
                },
                tonalElevation = 0.dp,
            ) {}
        }
    }
}

@Composable
private fun SpatialDepthBadge(
    label: String,
    chrome: SpatialChrome,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = SpatialBadgeShape,
        color = scheme.primaryContainer.copy(alpha = if (chrome.isDark) 0.72f else 0.88f),
        tonalElevation = 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SpatialDepthCard(
    layer: SpatialLayer,
    selected: Boolean,
    chrome: SpatialChrome,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
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
        start = (3 + layer.depthTier).toFloat(),
        stop = if (chrome.isDark) 16f else 10f,
        fraction = selectionProgress,
    ).dp
    val borderColor = scheme.primary.copy(alpha = 0.55f * selectionProgress)
    val subtitleColor = lerp(
        start = scheme.onSurfaceVariant,
        stop = scheme.primary,
        fraction = selectionProgress,
    )

    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            translationY = with(density) {
                depthOffset.toPx() * (1f - selectionProgress)
            }
        },
        shape = SpatialCardShape,
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
                subtitle = if (selected) "Selected · elevated" else "Depth card",
                subtitleColor = subtitleColor,
                badgeLabel = "Z${layer.depthTier + 1}",
                chrome = chrome,
                titleWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
        }
    }
}
