package com.mfhapps.trendingui.ui.theme

enum class ThemeMode {
    System,
    Light,
    Dark,
}

data class ThemePreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val useDynamicColor: Boolean = true,
    val brandAccentColor: BrandAccentColor = BrandAccentColor.Rose,
    val homeGradientStyle: HomeGradientStyle = HomeGradientStyle.RoseBloom,
    val homeLayoutStyle: HomeLayoutStyle = HomeLayoutStyle.FeaturedList,
    val customGradient: CustomGradientConfig = CustomGradientConfig.Default,
    val blurModalBackdrop: Boolean = true,
    val modalBackdropStyle: ModalBackdropStyle = ModalBackdropStyle.Default,
    val syncLauncherIconWithTheme: Boolean = false,
    val appFontStyle: AppFontStyle = AppFontStyle.Raleway,
)
