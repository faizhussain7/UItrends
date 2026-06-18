package com.mfhapps.trendingui.screens.settings

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.mfhapps.trendingui.BuildConfig
import com.mfhapps.trendingui.launcher.AppLauncherIcon
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import com.mfhapps.trendingui.ui.settings.SettingsAboutPanel
import com.mfhapps.trendingui.ui.settings.SettingsAppInfoOverscrollHaptics
import com.mfhapps.trendingui.ui.settings.SettingsCardContent
import com.mfhapps.trendingui.ui.settings.SettingsChipCollapseOnScrollEffect
import com.mfhapps.trendingui.ui.components.CollapsingBlurTopBarLayout
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.components.collapsingTopBarContentPadding
import com.mfhapps.trendingui.ui.components.rememberCollapsedTopAppBarColors
import com.mfhapps.trendingui.ui.settings.SettingsCollapsingTopBar
import com.mfhapps.trendingui.ui.settings.SettingsExpressiveDefaults
import com.mfhapps.trendingui.ui.settings.SettingsScreenBackground
import com.mfhapps.trendingui.ui.settings.SettingsSectionCard
import com.mfhapps.trendingui.ui.settings.SettingsSectionDivider
import com.mfhapps.trendingui.ui.settings.SettingsSectionTitle
import com.mfhapps.trendingui.ui.settings.SettingsSubsectionLabel
import com.mfhapps.trendingui.ui.settings.SettingsSwitchRow
import com.mfhapps.trendingui.ui.settings.SettingsThemeModePicker
import com.mfhapps.trendingui.ui.settings.rememberSettingsListNestedScroll
import com.mfhapps.trendingui.ui.settings.rememberSettingsHeaderForeground
import com.mfhapps.trendingui.ui.settings.rememberSettingsChipCollapseOnScroll
import com.mfhapps.trendingui.ui.settings.scrollSettingsToTop
import androidx.compose.ui.platform.LocalContext
import com.mfhapps.trendingui.ui.platform.supportsBackdropBlur
import com.mfhapps.trendingui.ui.theme.BrandAccentColor
import com.mfhapps.trendingui.ui.theme.HomeLayoutStyle
import com.mfhapps.trendingui.ui.theme.SettingsCollapsingSystemBars
import com.mfhapps.trendingui.ui.theme.ThemeMode
import com.mfhapps.trendingui.ui.theme.ThemePreferences
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: ThemePreferences,
    selectedLauncherIcon: AppLauncherIcon,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onBrandAccentChange: (BrandAccentColor) -> Unit,
    onHomeLayoutChange: (HomeLayoutStyle) -> Unit,
    onBlurModalBackdropChange: (Boolean) -> Unit,
    onSyncLauncherIconWithThemeChange: (Boolean) -> Unit,
    onLauncherIconChange: (AppLauncherIcon) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    guide: DemoTrendGuide? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val backdropBlurSupported = remember(context) { context.supportsBackdropBlur() }
    val brandAccentEnabled = !preferences.useDynamicColor || !dynamicColorSupported
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    var appInfoExpanded by rememberSaveable { mutableStateOf(false) }
    val collapsedFraction by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f) }
    }
    val topOverscroll = rememberSettingsListNestedScroll(
        listState = listState,
        scrollBehavior = scrollBehavior,
    )
    val stretchFraction by topOverscroll.stretchFraction
    val followOverscrollFinger by topOverscroll.followFinger
    val chipCollapseOnScroll = rememberSettingsChipCollapseOnScroll(
        chipTapExpanded = appInfoExpanded,
        onCollapseChip = { appInfoExpanded = false },
    )
    val appInfoRevealProgress = if (appInfoExpanded) 1f else stretchFraction
    val headerForeground = rememberSettingsHeaderForeground(scheme, collapsedFraction)
    val systemDarkTheme = isSystemInDarkTheme()
    SettingsCollapsingSystemBars(collapsedFraction = collapsedFraction)
    val topBarColors = rememberCollapsedTopAppBarColors(
        collapsedFraction = collapsedFraction,
        containerColor = Color.Transparent,
        scrolledContainerColor = scheme.surface,
        navigationIconContentColor = headerForeground.icons,
        titleContentColor = headerForeground.title,
        actionIconContentColor = headerForeground.icons,
    )

    SettingsChipCollapseOnScrollEffect(
        listState = listState,
        chipTapExpanded = appInfoExpanded,
        onCollapseChip = { appInfoExpanded = false },
    )
    SettingsScreenBackground(modifier = modifier) {
        SettingsAppInfoOverscrollHaptics(stretchFraction = stretchFraction)
        val bottomInset = WindowInsets.navigationBars.asPaddingValues()
        CollapsingBlurTopBarLayout(
            scrollBehavior = scrollBehavior,
            collapsedFraction = collapsedFraction,
            modifier = Modifier.fillMaxSize(),
            topBar = { barModifier ->
                SettingsCollapsingTopBar(
                    scrollBehavior = scrollBehavior,
                    collapsedFraction = collapsedFraction,
                    headerForeground = headerForeground,
                    onNavigateBack = onNavigateBack,
                    appInfoRevealProgress = appInfoRevealProgress,
                    followOverscrollFinger = followOverscrollFinger,
                    onAppInfoClick = {
                        if (appInfoExpanded) {
                            appInfoExpanded = false
                        } else {
                            appInfoExpanded = true
                            scope.launch {
                                scrollSettingsToTop(
                                    listState = listState,
                                    topAppBarState = topAppBarState,
                                )
                            }
                        }
                    },
                    guide = guide,
                    colors = topBarColors,
                    barModifier = barModifier,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .appHazeSource()
                    .nestedScroll(chipCollapseOnScroll)
                    .nestedScroll(topOverscroll.pullNestedScrollConnection),
                state = listState,
                contentPadding = collapsingTopBarContentPadding(
                    extra = PaddingValues(
                        bottom = bottomInset.calculateBottomPadding() +
                            SettingsExpressiveDefaults.sectionSpacing,
                    ),
                ),
                verticalArrangement = Arrangement.spacedBy(SettingsExpressiveDefaults.sectionSpacing),
            ) {
                item(key = "appearance-title") {
                    SettingsSectionTitle(
                        title = "Appearance",
                        subtitle = "Theme and accent colors.",
                    )
                }

                item(key = "appearance-card") {
                    SettingsSectionCard {
                        SettingsSubsectionLabel(
                            title = "Theme",
                            subtitle = "Splash, launcher, and status bar icons",
                        )
                        SettingsCardContent {
                            SettingsThemeModePicker(
                                selected = preferences.themeMode,
                                systemDarkTheme = systemDarkTheme,
                                onSelected = onThemeModeChange,
                            )
                        }

                        SettingsSectionDivider()

                        SettingsSwitchRow(
                            checked = preferences.useDynamicColor && dynamicColorSupported,
                            onCheckedChange = onDynamicColorChange,
                            enabled = dynamicColorSupported,
                            icon = Icons.Outlined.Palette,
                            title = "Dynamic color",
                            subtitle = if (dynamicColorSupported) {
                                "Wallpaper palette on Android 12+"
                            } else {
                                "Requires Android 12 or newer"
                            },
                        )

                        SettingsSectionDivider()

                        SettingsSwitchRow(
                            checked = preferences.blurModalBackdrop && backdropBlurSupported,
                            onCheckedChange = onBlurModalBackdropChange,
                            enabled = backdropBlurSupported,
                            icon = Icons.Outlined.BlurOn,
                            title = "Blur sheet & dialog backdrops",
                            subtitle = if (backdropBlurSupported) {
                                "Blur behind sheets, dialogs, and collapsed headers"
                            } else {
                                "Requires Android 12+ and sufficient device memory"
                            },
                        )

                        SettingsSectionDivider()

                        SettingsSubsectionLabel(
                            title = "Accent color",
                            subtitle = if (brandAccentEnabled) {
                                "Buttons, chips, and accents"
                            } else {
                                "Turn off dynamic color to choose an accent"
                            },
                        )
                        SettingsCardContent {
                            BrandAccentColorPicker(
                                selected = preferences.brandAccentColor,
                                onSelected = onBrandAccentChange,
                                enabled = brandAccentEnabled,
                            )
                        }
                    }
                }

                item(key = "home-settings") {
                    HomeAppearanceSettings(
                        preferences = preferences,
                        selectedLauncherIcon = selectedLauncherIcon,
                        onLayoutChange = onHomeLayoutChange,
                        onSyncLauncherIconWithThemeChange = onSyncLauncherIconWithThemeChange,
                        onLauncherIconChange = onLauncherIconChange,
                    )
                }

                item(key = "about-title") {
                    SettingsSectionTitle(title = "About")
                }

                item(key = "about-card") {
                    SettingsAboutPanel(
                        versionName = BuildConfig.VERSION_NAME,
                        versionCode = BuildConfig.VERSION_CODE,
                    )
                }
            }
        }
    }
}
