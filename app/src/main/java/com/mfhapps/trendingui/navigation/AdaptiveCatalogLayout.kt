package com.mfhapps.trendingui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import com.mfhapps.trendingui.ui.platform.CatalogPaneRole
import com.mfhapps.trendingui.ui.platform.LocalCatalogPaneRole
import com.mfhapps.trendingui.ui.platform.appBarTopWindowInsets
import com.mfhapps.trendingui.ui.platform.detailPaneHorizontalSafePadding
import com.mfhapps.trendingui.ui.platform.isMediumOrWiderWindow
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.HingePolicy
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.launcher.AppLauncherIcon
import com.mfhapps.trendingui.screens.home.DemoCatalogScreen
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.accessibility.rememberReduceMotion
import com.mfhapps.trendingui.ui.theme.AppFontStyle
import com.mfhapps.trendingui.ui.theme.BrandAccentColor
import com.mfhapps.trendingui.ui.theme.HomeLayoutStyle
import com.mfhapps.trendingui.ui.theme.ModalBackdropStyle
import com.mfhapps.trendingui.ui.theme.ThemeMode
import com.mfhapps.trendingui.ui.theme.ThemePreferences
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun AdaptiveCatalogLayout(
    themePreferences: ThemePreferences,
    launcherIcon: AppLauncherIcon,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onBrandAccentChange: (BrandAccentColor) -> Unit,
    onAppFontStyleChange: (AppFontStyle) -> Unit,
    onHomeLayoutChange: (HomeLayoutStyle) -> Unit,
    onBlurModalBackdropChange: (Boolean) -> Unit,
    onModalBackdropStyleChange: (ModalBackdropStyle) -> Unit,
    onSyncLauncherIconWithThemeChange: (Boolean) -> Unit,
    onLauncherIconChange: (AppLauncherIcon) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReduceMotion()
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val dualPane = isMediumOrWiderWindow()
    val listPaneRole = if (dualPane) CatalogPaneRole.List else CatalogPaneRole.Single
    val detailPaneRole = if (dualPane) CatalogPaneRole.Detail else CatalogPaneRole.Single
    val scaffoldDirective = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(
            windowAdaptiveInfo = windowAdaptiveInfo,
            verticalHingePolicy = HingePolicy.AlwaysAvoid,
        ).copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val navigator = rememberListDetailPaneScaffoldNavigator<String>(
        scaffoldDirective = scaffoldDirective,
    )
    val scope = rememberCoroutineScope()
    var settingsSession by remember { mutableIntStateOf(0) }
    val detailKey = navigator.currentDestination?.contentKey
    val selectedDemoKey = detailKey?.toDemoPaneKeyOrNull()

    CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
        SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
            val sharedScope = this
            NavigableListDetailPaneScaffold(
                navigator = navigator,
                modifier = Modifier.fillMaxSize(),
                listPane = {
                    AnimatedPane {
                        CompositionLocalProvider(
                            LocalSharedTransitionScope provides sharedScope,
                            LocalNavAnimatedVisibilityScope provides this@AnimatedPane,
                            LocalCatalogPaneRole provides listPaneRole,
                        ) {
                            DemoCatalogScreen(
                                modifier = Modifier.fillMaxSize(),
                                launcherIcon = launcherIcon,
                                selectedPaneKey = selectedDemoKey,
                                onOpenDemo = { route ->
                                    val paneKey = route.toDemoPaneKey() ?: return@DemoCatalogScreen
                                    scope.launch {
                                        navigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            paneKey.navKey,
                                        )
                                    }
                                },
                                onOpenSettings = {
                                    settingsSession++
                                    scope.launch {
                                        navigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            SETTINGS_DETAIL_KEY,
                                        )
                                    }
                                },
                            )
                        }
                    }
                },
                detailPane = {
                    AnimatedPane {
                        CompositionLocalProvider(
                            LocalSharedTransitionScope provides sharedScope,
                            LocalNavAnimatedVisibilityScope provides this@AnimatedPane,
                            LocalCatalogPaneRole provides detailPaneRole,
                        ) {
                            when {
                                detailKey == SETTINGS_DETAIL_KEY -> {
                                    key(settingsSession) {
                                        SettingsDetailHost(
                                            preferences = themePreferences,
                                            selectedLauncherIcon = launcherIcon,
                                            onThemeModeChange = onThemeModeChange,
                                            onDynamicColorChange = onDynamicColorChange,
                                            onBrandAccentChange = onBrandAccentChange,
                                            onAppFontStyleChange = onAppFontStyleChange,
                                            onHomeLayoutChange = onHomeLayoutChange,
                                            onBlurModalBackdropChange = onBlurModalBackdropChange,
                                            onModalBackdropStyleChange = onModalBackdropStyleChange,
                                            onSyncLauncherIconWithThemeChange = onSyncLauncherIconWithThemeChange,
                                            onLauncherIconChange = onLauncherIconChange,
                                            onNavigateBack = {
                                                scope.launch {
                                                    navigator.navigateBack(
                                                        backNavigationBehavior =
                                                            BackNavigationBehavior.PopUntilScaffoldValueChange,
                                                    )
                                                }
                                            },
                                        )
                                    }
                                }
                                selectedDemoKey != null -> {
                                    DemoDetailHost(
                                        paneKey = selectedDemoKey,
                                        onNavigateBack = {
                                            scope.launch {
                                                navigator.navigateBack(
                                                    backNavigationBehavior =
                                                        BackNavigationBehavior.PopUntilScaffoldValueChange,
                                                )
                                            }
                                        },
                                    )
                                }
                                else -> CatalogDetailPlaceholder()
                            }
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun CatalogDetailPlaceholder(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxSize(),
        color = scheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .detailPaneHorizontalSafePadding()
                .windowInsetsPadding(appBarTopWindowInsets())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Select a pattern",
                style = MaterialTheme.typography.headlineSmall,
                color = scheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Choose a demo from the catalog to preview it beside the list.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
