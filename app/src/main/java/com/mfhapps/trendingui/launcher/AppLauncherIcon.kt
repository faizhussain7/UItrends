package com.mfhapps.trendingui.launcher

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mfhapps.trendingui.R

enum class AppLauncherIcon(
    val design: Int,
    @param:StringRes val labelRes: Int,
    val lightAliasClassName: String,
    val darkAliasClassName: String,
    @param:DrawableRes val lightIconMipmap: Int,
    @param:DrawableRes val darkIconMipmap: Int,
    @param:DrawableRes val lightRoundIconMipmap: Int,
    @param:DrawableRes val darkRoundIconMipmap: Int,
    @param:DrawableRes val launcherForegroundRes: Int,
    @param:DrawableRes val brandLogoRes: Int,
    @param:DrawableRes val brandMonochromeRes: Int,
) {
    Bijapur(
        design = 1,
        labelRes = R.string.launcher_icon_bijapur,
        lightAliasClassName = "com.mfhapps.trendingui.LauncherIconBijapurLight",
        darkAliasClassName = "com.mfhapps.trendingui.LauncherIconBijapurDark",
        lightIconMipmap = R.mipmap.ic_launcher,
        darkIconMipmap = R.mipmap.ic_launcher_dark,
        lightRoundIconMipmap = R.mipmap.ic_launcher_round,
        darkRoundIconMipmap = R.mipmap.ic_launcher_round_dark,
        launcherForegroundRes = R.drawable.ic_launcher_foreground,
        brandLogoRes = R.drawable.ic_brand_logo,
        brandMonochromeRes = R.drawable.ic_brand_logo_monochrome,
    ),
    Golconda(
        design = 2,
        labelRes = R.string.launcher_icon_golconda,
        lightAliasClassName = "com.mfhapps.trendingui.LauncherIconGolcondaLight",
        darkAliasClassName = "com.mfhapps.trendingui.LauncherIconGolcondaDark",
        lightIconMipmap = R.mipmap.ic_launcher_2,
        darkIconMipmap = R.mipmap.ic_launcher_2_dark,
        lightRoundIconMipmap = R.mipmap.ic_launcher_round_2,
        darkRoundIconMipmap = R.mipmap.ic_launcher_round_2_dark,
        launcherForegroundRes = R.drawable.ic_launcher_foreground_2,
        brandLogoRes = R.drawable.ic_brand_logo_2,
        brandMonochromeRes = R.drawable.ic_brand_logo_monochrome_2,
    ),
    Rauza(
        design = 3,
        labelRes = R.string.launcher_icon_rauza,
        lightAliasClassName = "com.mfhapps.trendingui.LauncherIconRauzaLight",
        darkAliasClassName = "com.mfhapps.trendingui.LauncherIconRauzaDark",
        lightIconMipmap = R.mipmap.ic_launcher_3,
        darkIconMipmap = R.mipmap.ic_launcher_3_dark,
        lightRoundIconMipmap = R.mipmap.ic_launcher_round_3,
        darkRoundIconMipmap = R.mipmap.ic_launcher_round_3_dark,
        launcherForegroundRes = R.drawable.ic_launcher_foreground_3,
        brandLogoRes = R.drawable.ic_brand_logo_3,
        brandMonochromeRes = R.drawable.ic_brand_logo_monochrome_3,
    ),
    Hyderabad(
        design = 4,
        labelRes = R.string.launcher_icon_hyderabad,
        lightAliasClassName = "com.mfhapps.trendingui.LauncherIconHyderabadLight",
        darkAliasClassName = "com.mfhapps.trendingui.LauncherIconHyderabadDark",
        lightIconMipmap = R.mipmap.ic_launcher_4,
        darkIconMipmap = R.mipmap.ic_launcher_4_dark,
        lightRoundIconMipmap = R.mipmap.ic_launcher_round_4,
        darkRoundIconMipmap = R.mipmap.ic_launcher_round_4_dark,
        launcherForegroundRes = R.drawable.ic_launcher_foreground_4,
        brandLogoRes = R.drawable.ic_brand_logo_4,
        brandMonochromeRes = R.drawable.ic_brand_logo_monochrome_4,
    ),
    ;

    fun aliasClassName(darkTheme: Boolean): String =
        if (darkTheme) darkAliasClassName else lightAliasClassName

    fun iconMipmap(darkTheme: Boolean): Int =
        if (darkTheme) darkIconMipmap else lightIconMipmap

    fun roundIconMipmap(darkTheme: Boolean): Int =
        if (darkTheme) darkRoundIconMipmap else lightRoundIconMipmap

    companion object {
        val Default: AppLauncherIcon = Bijapur

        fun fromDesign(design: Int): AppLauncherIcon =
            entries.firstOrNull { it.design == design } ?: Default

        fun fromStoredName(name: String?): AppLauncherIcon =
            entries.firstOrNull { it.name == name } ?: Default

        fun fromAliasClassName(className: String): AppLauncherIcon? =
            entries.firstOrNull { entry ->
                entry.lightAliasClassName == className ||
                    entry.darkAliasClassName == className
            }
    }
}
