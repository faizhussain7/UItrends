package com.mfhapps.trendingui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mfhapps.trendingui.launcher.AppLauncherIcon
import com.mfhapps.trendingui.navigation.AdaptiveCatalogLayout
import com.mfhapps.trendingui.ui.components.ProvideAppModalBackdrop
import com.mfhapps.trendingui.ui.theme.BrandAccentColor
import com.mfhapps.trendingui.ui.theme.HomeLayoutStyle
import com.mfhapps.trendingui.ui.theme.ThemeMode
import com.mfhapps.trendingui.ui.theme.ThemePreferences

@Composable
fun UITrendsApp(
    themePreferences: ThemePreferences,
    launcherIcon: AppLauncherIcon,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onBrandAccentChange: (BrandAccentColor) -> Unit,
    onHomeLayoutChange: (HomeLayoutStyle) -> Unit,
    onBlurModalBackdropChange: (Boolean) -> Unit,
    onLauncherIconChange: (AppLauncherIcon) -> Unit,
    modifier: Modifier = Modifier,
) {
    ProvideAppModalBackdrop(blurEnabled = themePreferences.blurModalBackdrop) {
        AdaptiveCatalogLayout(
            modifier = modifier,
            themePreferences = themePreferences,
            launcherIcon = launcherIcon,
            onThemeModeChange = onThemeModeChange,
            onDynamicColorChange = onDynamicColorChange,
            onBrandAccentChange = onBrandAccentChange,
            onHomeLayoutChange = onHomeLayoutChange,
            onBlurModalBackdropChange = onBlurModalBackdropChange,
            onLauncherIconChange = onLauncherIconChange,
        )
    }
}
