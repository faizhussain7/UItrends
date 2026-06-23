package com.mfhapps.trendingui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
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
import com.mfhapps.trendingui.screens.home.DemoCatalogScreen
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.accessibility.rememberReduceMotion
import com.mfhapps.trendingui.ui.theme.BrandAccentColor
import com.mfhapps.trendingui.ui.theme.HomeLayoutStyle
import com.mfhapps.trendingui.launcher.AppLauncherIcon
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
    onHomeLayoutChange: (HomeLayoutStyle) -> Unit,
    onBlurModalBackdropChange: (Boolean) -> Unit,
    onModalBackdropStyleChange: (ModalBackdropStyle) -> Unit,
    onSyncLauncherIconWithThemeChange: (Boolean) -> Unit,
    onLauncherIconChange: (AppLauncherIcon) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduceMotion = rememberReduceMotion()
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()
    var settingsSession by remember { mutableIntStateOf(0) }
    val detailKey = navigator.currentDestination?.contentKey
    val selectedDemoKey = detailKey?.toDemoPaneKeyOrNull()

    CompositionLocalProvider(LocalReduceMotion provides reduceMotion) {
        SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
            val sharedScope = this
            NavigableListDetailPaneScaffold(
                navigator = navigator,
                listPane = {
                    AnimatedPane {
                        CompositionLocalProvider(
                            LocalSharedTransitionScope provides sharedScope,
                            LocalNavAnimatedVisibilityScope provides this@AnimatedPane,
                        ) {
                            DemoCatalogScreen(
                                modifier = Modifier.fillMaxSize(),
                                brandDesign = launcherIcon.design,
                                selectedPaneKey = selectedDemoKey,
                                onOpenDemo = { route ->
                                    val key = route.toDemoPaneKey() ?: return@DemoCatalogScreen
                                    scope.launch {
                                        navigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            key.navKey,
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Select a pattern",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Choose a demo from the catalog to preview it here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
