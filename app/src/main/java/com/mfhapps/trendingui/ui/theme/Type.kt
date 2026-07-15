package com.mfhapps.trendingui.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

private fun TextStyle.withFont(fontFamily: FontFamily): TextStyle =
    copy(fontFamily = fontFamily)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun buildExpressiveTypography(
    brandFont: FontFamily = AppFonts.brand,
    bodyFont: FontFamily = AppFonts.body,
): Typography {
    val base = Typography()
    fun brand(style: TextStyle): TextStyle = style.withFont(brandFont)
    fun plain(style: TextStyle): TextStyle = style.withFont(bodyFont)

    return Typography(
        displayLarge = brand(base.displayLarge),
        displayMedium = brand(base.displayMedium),
        displaySmall = brand(base.displaySmall),
        headlineLarge = brand(base.headlineLarge),
        headlineMedium = brand(base.headlineMedium),
        headlineSmall = brand(base.headlineSmall),
        titleLarge = brand(base.titleLarge),
        titleMedium = brand(base.titleMedium),
        titleSmall = brand(base.titleSmall),
        bodyLarge = plain(base.bodyLarge),
        bodyMedium = plain(base.bodyMedium),
        bodySmall = plain(base.bodySmall),
        labelLarge = plain(base.labelLarge),
        labelMedium = plain(base.labelMedium),
        labelSmall = plain(base.labelSmall),
        displayLargeEmphasized = brand(base.displayLargeEmphasized),
        displayMediumEmphasized = brand(base.displayMediumEmphasized),
        displaySmallEmphasized = brand(base.displaySmallEmphasized),
        headlineLargeEmphasized = brand(base.headlineLargeEmphasized),
        headlineMediumEmphasized = brand(base.headlineMediumEmphasized),
        headlineSmallEmphasized = brand(base.headlineSmallEmphasized),
        titleLargeEmphasized = brand(base.titleLargeEmphasized),
        titleMediumEmphasized = brand(base.titleMediumEmphasized),
        titleSmallEmphasized = brand(base.titleSmallEmphasized),
        bodyLargeEmphasized = plain(base.bodyLargeEmphasized),
        bodyMediumEmphasized = plain(base.bodyMediumEmphasized),
        bodySmallEmphasized = plain(base.bodySmallEmphasized),
        labelLargeEmphasized = plain(base.labelLargeEmphasized),
        labelMediumEmphasized = plain(base.labelMediumEmphasized),
        labelSmallEmphasized = plain(base.labelSmallEmphasized),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val ExpressiveTypography: Typography = buildExpressiveTypography()
