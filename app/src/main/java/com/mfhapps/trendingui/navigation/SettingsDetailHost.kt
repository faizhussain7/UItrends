package com.mfhapps.trendingui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mfhapps.trendingui.screens.settings.SettingsScreen
import com.mfhapps.trendingui.ui.detail.DetailPaneScaffold
import com.mfhapps.trendingui.ui.guide.settingsTrendGuide
import com.mfhapps.trendingui.ui.theme.BrandAccentColor
import com.mfhapps.trendingui.ui.theme.HomeLayoutStyle
import com.mfhapps.trendingui.launcher.AppLauncherIcon
import com.mfhapps.trendingui.ui.theme.ThemeMode
import com.mfhapps.trendingui.ui.theme.ThemePreferences

@Composable
fun SettingsDetailHost(
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
) {
    val guide = settingsTrendGuide()
    DetailPaneScaffold(
        modifier = modifier,
        title = "Settings",
        subtitle = "Theme & appearance",
        guide = guide,
        useGradientTopBar = false,
        contentOwnsTitle = true,
        hideCompactTopBar = true,
        onNavigateBack = onNavigateBack,
    ) {
        SettingsScreen(
            preferences = preferences,
            selectedLauncherIcon = selectedLauncherIcon,
            onThemeModeChange = onThemeModeChange,
            onDynamicColorChange = onDynamicColorChange,
            onBrandAccentChange = onBrandAccentChange,
            onHomeLayoutChange = onHomeLayoutChange,
            onBlurModalBackdropChange = onBlurModalBackdropChange,
            onSyncLauncherIconWithThemeChange = onSyncLauncherIconWithThemeChange,
            onLauncherIconChange = onLauncherIconChange,
            onNavigateBack = onNavigateBack,
            guide = guide,
        )
    }
}
