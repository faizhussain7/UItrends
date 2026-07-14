package com.mfhapps.trendingui.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.mfhapps.trendingui.ui.platform.appBarTopWindowInsets
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp as lerpTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.brutal.BrutalChromeIconButton
import com.mfhapps.trendingui.ui.components.CollapsingBlurTopBarLayout
import com.mfhapps.trendingui.ui.components.IconButton
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.components.collapsingTopBarContentPadding
import com.mfhapps.trendingui.ui.components.rememberCollapsedTopAppBarColors
import com.mfhapps.trendingui.ui.glass.GlassChromeIconButton
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import com.mfhapps.trendingui.ui.neumorphism.NeuChromeIconButton

enum class DemoCollapsingHeaderMode {

    LargeCollapsing,

    HeroTitle,
}

private val CollapsedTopBarHeight = 76.dp
private val CompactTopBarHeight = 72.dp
private val HeroTitleFadeDistance = 56.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoCollapsingScrollScaffold(
    title: String,
    subtitle: String,
    chromeStyle: DetailChromeStyle,
    onNavigateBack: () -> Unit,
    guide: DemoTrendGuide?,
    modifier: Modifier = Modifier,
    headerMode: DemoCollapsingHeaderMode = DemoCollapsingHeaderMode.LargeCollapsing,
    heroTitleThresholdDp: Dp = 200.dp,
    bottomPadding: Dp = 28.dp,
    verticalSpacing: Dp = 28.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = when (headerMode) {
        DemoCollapsingHeaderMode.LargeCollapsing ->
            TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
        DemoCollapsingHeaderMode.HeroTitle ->
            TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    }
    val listState = rememberLazyListState()
    val heroThresholdPx = remember(density, heroTitleThresholdDp) {
        with(density) { heroTitleThresholdDp.roundToPx() }
    }
    val heroFadePx = remember(density) {
        with(density) { HeroTitleFadeDistance.roundToPx() }
    }
    val collapsedFraction by remember(
        headerMode,
        scrollBehavior,
        listState,
        heroThresholdPx,
        heroFadePx,
    ) {
        derivedStateOf {
            when (headerMode) {
                DemoCollapsingHeaderMode.LargeCollapsing ->
                    scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f)
                DemoCollapsingHeaderMode.HeroTitle -> {
                    val scrollY = listState.firstVisibleItemScrollOffset +
                        listState.firstVisibleItemIndex * heroThresholdPx
                    if (scrollY <= heroThresholdPx) {
                        0f
                    } else {
                        ((scrollY - heroThresholdPx).toFloat() / heroFadePx).coerceIn(0f, 1f)
                    }
                }
            }
        }
    }

    CollapsingBlurTopBarLayout(
        scrollBehavior = scrollBehavior,
        collapsedFraction = collapsedFraction,
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        topBar = { barModifier ->
            when (headerMode) {
                DemoCollapsingHeaderMode.LargeCollapsing -> DemoCollapsingLargeTopBar(
                    title = title,
                    subtitle = subtitle,
                    chromeStyle = chromeStyle,
                    scrollBehavior = scrollBehavior,
                    collapsedFraction = collapsedFraction,
                    onNavigateBack = onNavigateBack,
                    guide = guide,
                    modifier = barModifier,
                )
                DemoCollapsingHeaderMode.HeroTitle -> DemoHeroTitleTopBar(
                    title = title,
                    chromeStyle = chromeStyle,
                    onNavigateBack = onNavigateBack,
                    guide = guide,
                    collapsedFraction = collapsedFraction,
                    modifier = barModifier,
                )
            }
        },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .appHazeSource(),
            contentPadding = collapsingTopBarContentPadding(
                extra = PaddingValues(bottom = bottomPadding),
            ),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        ) {
            item(key = "demo-content") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                    content = content,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoCollapsingLargeTopBar(
    title: String,
    subtitle: String,
    chromeStyle: DetailChromeStyle,
    scrollBehavior: TopAppBarScrollBehavior,
    collapsedFraction: Float,
    onNavigateBack: () -> Unit,
    guide: DemoTrendGuide?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val titleStyle = lerpTextStyle(
        start = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        stop = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        fraction = collapsedFraction,
    )
    val subtitleAlpha = (1f - collapsedFraction * 1.35f).coerceIn(0f, 1f)
    val scrolledContainer = when (chromeStyle) {
        DetailChromeStyle.NeoBrutal -> scheme.surface
        DetailChromeStyle.Neumorphism -> scheme.surface
        DetailChromeStyle.Glass -> Color.Transparent
        else -> scheme.surface
    }

    LargeTopAppBar(
        modifier = modifier,
        collapsedHeight = CollapsedTopBarHeight,
        windowInsets = appBarTopWindowInsets(),
        scrollBehavior = scrollBehavior,
        colors = rememberCollapsedTopAppBarColors(
            collapsedFraction = collapsedFraction,
            containerColor = Color.Transparent,
            scrolledContainerColor = scrolledContainer,
            titleContentColor = scheme.onSurface,
            navigationIconContentColor = scheme.onSurface,
            actionIconContentColor = scheme.onSurface,
        ),
        title = {
            Column(
                modifier = Modifier.padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = titleStyle,
                    color = scheme.onSurface,
                    maxLines = if (collapsedFraction > 0.85f) 1 else 2,
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
            DemoCollapsingNavIcon(
                chromeStyle = chromeStyle,
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
            )
        },
        actions = {
            DemoCollapsingTopBarActions(
                guide = guide,
                chromeStyle = chromeStyle,
                iconTint = scheme.onSurface,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoHeroTitleTopBar(
    title: String,
    chromeStyle: DetailChromeStyle,
    onNavigateBack: () -> Unit,
    guide: DemoTrendGuide?,
    collapsedFraction: Float,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val scrolledContainer = when (chromeStyle) {
        DetailChromeStyle.NeoBrutal -> scheme.surface
        DetailChromeStyle.Neumorphism -> scheme.surface
        DetailChromeStyle.Glass -> Color.Transparent
        else -> scheme.surface
    }

    TopAppBar(
        modifier = modifier.heightIn(min = CompactTopBarHeight),
        windowInsets = appBarTopWindowInsets(),
        colors = rememberCollapsedTopAppBarColors(
            collapsedFraction = collapsedFraction,
            containerColor = Color.Transparent,
            scrolledContainerColor = scrolledContainer,
            titleContentColor = scheme.onSurface,
            navigationIconContentColor = scheme.onSurface,
            actionIconContentColor = scheme.onSurface,
        ),
        title = {
            if (collapsedFraction > 0.01f) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .graphicsLayer { alpha = collapsedFraction },
                )
            }
        },
        navigationIcon = {
            DemoCollapsingNavIcon(
                chromeStyle = chromeStyle,
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
            )
        },
        actions = {
            DemoCollapsingTopBarActions(
                guide = guide,
                chromeStyle = chromeStyle,
                iconTint = scheme.onSurface,
            )
        },
    )
}

@Composable
private fun DemoCollapsingNavIcon(
    chromeStyle: DetailChromeStyle,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val nestedBackDispatcher = LocalNestedBackDispatcher.current
    val onBack = {
        if (nestedBackDispatcher.handler?.invoke() != true) {
            onNavigateBack()
        }
    }
    Box(modifier = modifier) {
        when (chromeStyle) {
            DetailChromeStyle.NeoBrutal -> BrutalChromeIconButton(
                onClick = onBack,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to catalog",
                fill = scheme.surfaceContainerHigh,
                iconTint = scheme.onSurface,
            )
            DetailChromeStyle.Neumorphism -> NeuChromeIconButton(
                onClick = onBack,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to catalog",
                iconTint = scheme.primary,
            )
            DetailChromeStyle.Glass -> GlassChromeIconButton(
                onClick = onBack,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to catalog",
            )
            else -> IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to catalog",
                    tint = scheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun DemoCollapsingTopBarActions(
    guide: DemoTrendGuide?,
    chromeStyle: DetailChromeStyle,
    iconTint: Color,
) {
    DetailPaneTopBarActions(
        guide = guide,
        chromeStyle = chromeStyle,
        iconTint = iconTint,
        modifier = when (chromeStyle) {
            DetailChromeStyle.NeoBrutal,
            DetailChromeStyle.Neumorphism,
            DetailChromeStyle.Glass,
            -> Modifier.padding(end = 12.dp, top = 4.dp, bottom = 4.dp)
            else -> Modifier
        },
    )
}
