package com.mfhapps.trendingui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mfhapps.trendingui.launcher.AppLauncherIcon
import com.mfhapps.trendingui.navigation.AdaptiveCatalogLayout
import com.mfhapps.trendingui.ui.components.ProvideAppModalBackdrop
import com.mfhapps.trendingui.ui.theme.AppFontStyle
import com.mfhapps.trendingui.ui.theme.BrandAccentColor
import com.mfhapps.trendingui.ui.theme.HomeLayoutStyle
import com.mfhapps.trendingui.ui.theme.ModalBackdropStyle
import com.mfhapps.trendingui.ui.theme.ThemeMode
import com.mfhapps.trendingui.ui.theme.ThemePreferences

@Composable
fun UITrendsApp(
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
    openDestination: String? = null,
    onOpenDestinationConsumed: () -> Unit = {},
) {
    ProvideAppModalBackdrop(
        blurEnabled = themePreferences.blurModalBackdrop,
        backdropStyle = themePreferences.modalBackdropStyle,
    ) {
        AdaptiveCatalogLayout(
            modifier = modifier,
            themePreferences = themePreferences,
            launcherIcon = launcherIcon,
            openDestination = openDestination,
            onOpenDestinationConsumed = onOpenDestinationConsumed,
            onThemeModeChange = onThemeModeChange,
            onDynamicColorChange = onDynamicColorChange,
            onBrandAccentChange = onBrandAccentChange,
            onAppFontStyleChange = onAppFontStyleChange,
            onHomeLayoutChange = onHomeLayoutChange,
            onBlurModalBackdropChange = onBlurModalBackdropChange,
            onModalBackdropStyleChange = onModalBackdropStyleChange,
            onSyncLauncherIconWithThemeChange = onSyncLauncherIconWithThemeChange,
            onLauncherIconChange = onLauncherIconChange,
        )
    }
}
