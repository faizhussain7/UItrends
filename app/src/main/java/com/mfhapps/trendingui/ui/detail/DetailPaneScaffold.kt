package com.mfhapps.trendingui.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.mfhapps.trendingui.ui.brutal.BrutalChromeIconButton
import com.mfhapps.trendingui.ui.brutal.BrutalShapes
import com.mfhapps.trendingui.ui.components.CollapsedTopAppBarBackdrop
import com.mfhapps.trendingui.ui.components.rememberCollapsedTopAppBarColors
import com.mfhapps.trendingui.ui.neumorphism.NeuChromeIconButton
import com.mfhapps.trendingui.screens.copilot.CopilotLiveStatusChip
import com.mfhapps.trendingui.screens.glass.ProvideGlassDetailEnvironment
import com.mfhapps.trendingui.screens.orbs.ProvideOrbsDetailEnvironment
import com.mfhapps.trendingui.screens.spatial.ProvideSpatialDetailEnvironment
import com.mfhapps.trendingui.screens.copilot.ProvideCopilotDetailEnvironment
import com.mfhapps.trendingui.ui.glass.GlassChromeIconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mfhapps.trendingui.ui.platform.isCompactWindowWidth
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import com.mfhapps.trendingui.ui.theme.CatalogGradientChrome
import com.mfhapps.trendingui.ui.theme.AdaptiveGradientIcon
import com.mfhapps.trendingui.ui.theme.AdaptiveGradientText
import com.mfhapps.trendingui.ui.theme.GradientForegroundRole
import com.mfhapps.trendingui.ui.theme.LocalCatalogAppearance
import com.mfhapps.trendingui.ui.theme.LocalHomeCatalogColors
import com.mfhapps.trendingui.ui.theme.DetailMaterialSystemBars
import com.mfhapps.trendingui.ui.theme.ImmersiveDetailSystemBars
import com.mfhapps.trendingui.ui.theme.MaterialSystemBarsForBackdrop
import com.mfhapps.trendingui.ui.theme.VibrantForegroundStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPaneScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    guide: DemoTrendGuide? = null,
    useGradientTopBar: Boolean = false,
    contentOwnsTitle: Boolean = false,
    hideCompactTopBar: Boolean = false,
    immersiveBackground: Boolean = false,
    glassHazeEnabled: Boolean = true,
    chromeStyle: DetailChromeStyle = DetailChromeStyle.Default,
    content: @Composable () -> Unit,
) {
    val compact = isCompactWindowWidth()
    val appearance = LocalCatalogAppearance.current
    val catalogColors = LocalHomeCatalogColors.current
    val nestedBackDispatcher = remember { NestedBackDispatcher() }
    val brutalChrome = chromeStyle == DetailChromeStyle.NeoBrutal
    val neuChrome = chromeStyle == DetailChromeStyle.Neumorphism
    val glassChrome = chromeStyle == DetailChromeStyle.Glass
    val orbsChrome = chromeStyle == DetailChromeStyle.Orbs
    val spatialChrome = chromeStyle == DetailChromeStyle.Spatial
    val copilotChrome = chromeStyle == DetailChromeStyle.Copilot
    val immersiveChrome = immersiveBackground && !useGradientTopBar
    val transparentTopBar = useGradientTopBar || immersiveChrome
    val contentManagesBackdropBlur = hideCompactTopBar && contentOwnsTitle
    val topBarForeground = when {
        useGradientTopBar -> catalogColors.glassBarTitle
        immersiveChrome -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }

    val immersiveCollapseState = remember { ImmersiveTopBarCollapseState() }
    val immersiveCollapseFraction = if (immersiveChrome) {
        immersiveCollapseState.collapsedFraction
    } else {
        0f
    }

    val scaffoldRoot: @Composable () -> Unit = {
    CompositionLocalProvider(
        LocalDetailPaneActive provides true,
        LocalNestedBackDispatcher provides nestedBackDispatcher,
        LocalDetailChromeStyle provides chromeStyle,
        LocalImmersiveTopBarCollapse provides if (immersiveChrome) immersiveCollapseState else null,
    ) {
        when {
            immersiveChrome -> ImmersiveDetailSystemBars()
            useGradientTopBar -> {
                val gradients = appearance.gradients
                MaterialSystemBarsForBackdrop(gradients.statusBarBackdrop)
            }
            else -> DetailMaterialSystemBars()
        }
        val topBar: @Composable () -> Unit = {
            val topBarColors = if (immersiveChrome) {
                rememberCollapsedTopAppBarColors(
                    collapsedFraction = immersiveCollapseFraction,
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    titleContentColor = topBarForeground,
                    navigationIconContentColor = topBarForeground,
                    actionIconContentColor = topBarForeground,
                )
            } else {
                TopAppBarDefaults.topAppBarColors(
                    containerColor = when {
                        transparentTopBar -> Color.Transparent
                        neuChrome -> MaterialTheme.colorScheme.surfaceContainerLow
                        else -> MaterialTheme.colorScheme.surface
                    },
                    scrolledContainerColor = when {
                        transparentTopBar -> Color.Transparent
                        neuChrome -> MaterialTheme.colorScheme.surfaceContainerLow
                        else -> MaterialTheme.colorScheme.surface
                    },
                    titleContentColor = topBarForeground,
                    navigationIconContentColor = topBarForeground,
                    actionIconContentColor = topBarForeground,
                )
            }

            val topBarContent: @Composable (Modifier) -> Unit = { barModifier ->
                TopAppBar(
                    modifier = barModifier,
                    title = {
                    if (!contentOwnsTitle) {
                        Column {
                            if (useGradientTopBar) {
                                AdaptiveGradientText(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    role = GradientForegroundRole.Title,
                                    vibrantStyle = VibrantForegroundStyle.OnLiquidGlass,
                                )
                                if (subtitle != null) {
                                    AdaptiveGradientText(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        role = GradientForegroundRole.Subtitle,
                                        vibrantStyle = VibrantForegroundStyle.OnLiquidGlass,
                                    )
                                }
                            } else {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = topBarForeground,
                                )
                                if (subtitle != null) {
                                    Text(
                                        text = subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = topBarForeground.copy(alpha = 0.82f),
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    val onBack = {
                        if (nestedBackDispatcher.handler?.invoke() != true) {
                            onNavigateBack()
                        }
                    }
                    when (chromeStyle) {
                        DetailChromeStyle.NeoBrutal -> BrutalChromeIconButton(
                            onClick = onBack,
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to catalog",
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                            fill = MaterialTheme.colorScheme.surfaceContainerHigh,
                            iconTint = MaterialTheme.colorScheme.onSurface,
                        )
                        DetailChromeStyle.Neumorphism -> NeuChromeIconButton(
                            onClick = onBack,
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to catalog",
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                            iconTint = MaterialTheme.colorScheme.primary,
                        )
                        DetailChromeStyle.Glass -> GlassChromeIconButton(
                            onClick = onBack,
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to catalog",
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                        )
                        DetailChromeStyle.Orbs,
                        DetailChromeStyle.Spatial,
                        DetailChromeStyle.Copilot,
                        DetailChromeStyle.Default -> IconButton(onClick = onBack) {
                            if (useGradientTopBar) {
                                AdaptiveGradientIcon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to catalog",
                                    role = GradientForegroundRole.Icon,
                                    vibrantStyle = VibrantForegroundStyle.OnLiquidGlass,
                                )
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to catalog",
                                    tint = topBarForeground,
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (guide != null) {
                        DetailPaneGuideAction(
                            guide = guide,
                            chromeStyle = chromeStyle,
                            iconTint = when {
                                copilotChrome -> MaterialTheme.colorScheme.onSurface
                                immersiveChrome -> catalogColors.headerIcon
                                else -> MaterialTheme.colorScheme.primary
                            },
                            leading = if (copilotChrome) {
                                { CopilotLiveStatusChip() }
                            } else {
                                null
                            },
                            modifier = if (brutalChrome || neuChrome || glassChrome) {
                                Modifier.padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                            } else {
                                Modifier
                            },
                        )
                    }
                },
                colors = topBarColors,
                )
            }

            if (immersiveChrome) {
                CollapsedTopAppBarBackdrop(
                    collapsedFraction = immersiveCollapseFraction,
                    modifier = Modifier.fillMaxWidth(),
                ) { barModifier ->
                    topBarContent(barModifier)
                }
            } else {
                topBarContent(Modifier.fillMaxWidth())
            }
        }

        val scaffoldContent: @Composable () -> Unit = {
            if (compact && immersiveChrome) {
                Box(Modifier.fillMaxSize()) {
                    content()
                    if (!hideCompactTopBar) {
                        topBar()
                    }
                }
            } else if (compact && !hideCompactTopBar) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    topBar = topBar,
                ) { innerPadding ->
                    DetailPaneContentPadding(
                        innerPadding = innerPadding,
                        registerHazeSource = !contentManagesBackdropBlur,
                    ) {
                        content()
                    }
                }
            } else if (compact) {
                DetailPaneContentPadding(
                    innerPadding = PaddingValues(0.dp),
                    registerHazeSource = !contentManagesBackdropBlur,
                ) {
                    content()
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (contentManagesBackdropBlur) {
                                Modifier
                            } else {
                                Modifier.appHazeSource()
                            },
                        ),
                ) {
                    content()
                    if (guide != null) {
                        DetailPaneGuideAction(
                            guide = guide,
                            chromeStyle = chromeStyle,
                            leading = if (copilotChrome) {
                                { CopilotLiveStatusChip() }
                            } else {
                                null
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                        )
                    }
                }
            }
        }

        if (useGradientTopBar) {
            CatalogGradientChrome(
                appearance = appearance,
                modifier = modifier,
                content = { scaffoldContent() },
            )
        } else {
            Box(
                modifier = modifier.fillMaxSize(),
                content = {
                    if (!immersiveChrome) {
                        Box(
                            Modifier
                                .matchParentSize()
                                .background(
                                    if (neuChrome) {
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    },
                                ),
                        )
                    }
                    scaffoldContent()
                },
            )
        }
    }
    }

    val wrappedScaffold: @Composable () -> Unit = {
        when {
            glassChrome -> ProvideGlassDetailEnvironment(hazeEnabled = glassHazeEnabled) {
                scaffoldRoot()
            }
            orbsChrome -> ProvideOrbsDetailEnvironment {
                scaffoldRoot()
            }
            spatialChrome -> ProvideSpatialDetailEnvironment {
                scaffoldRoot()
            }
            copilotChrome -> ProvideCopilotDetailEnvironment {
                scaffoldRoot()
            }
            else -> scaffoldRoot()
        }
    }

    if (brutalChrome) {
        MaterialTheme(shapes = BrutalShapes) {
            wrappedScaffold()
        }
    } else {
        wrappedScaffold()
    }
}

@Composable
private fun DetailPaneContentPadding(
    innerPadding: PaddingValues,
    registerHazeSource: Boolean = true,
    content: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .then(if (registerHazeSource) Modifier.appHazeSource() else Modifier)
            .padding(innerPadding),
    ) {
        content()
    }
}
