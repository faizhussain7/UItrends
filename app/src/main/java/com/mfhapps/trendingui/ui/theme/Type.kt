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
    return Typography(
        displayLarge = base.displayLarge.withFont(brandFont),
        displayMedium = base.displayMedium.withFont(brandFont),
        displaySmall = base.displaySmall.withFont(brandFont),
        headlineLarge = base.headlineLarge.withFont(brandFont),
        headlineMedium = base.headlineMedium.withFont(brandFont),
        headlineSmall = base.headlineSmall.withFont(brandFont),
        titleLarge = base.titleLarge.withFont(brandFont),
        titleMedium = base.titleMedium.withFont(bodyFont),
        titleSmall = base.titleSmall.withFont(bodyFont),
        bodyLarge = base.bodyLarge.withFont(bodyFont),
        bodyMedium = base.bodyMedium.withFont(bodyFont),
        bodySmall = base.bodySmall.withFont(bodyFont),
        labelLarge = base.labelLarge.withFont(bodyFont),
        labelMedium = base.labelMedium.withFont(bodyFont),
        labelSmall = base.labelSmall.withFont(bodyFont),
        displayLargeEmphasized = base.displayLargeEmphasized.withFont(brandFont),
        displayMediumEmphasized = base.displayMediumEmphasized.withFont(brandFont),
        displaySmallEmphasized = base.displaySmallEmphasized.withFont(brandFont),
        headlineLargeEmphasized = base.headlineLargeEmphasized.withFont(brandFont),
        headlineMediumEmphasized = base.headlineMediumEmphasized.withFont(brandFont),
        headlineSmallEmphasized = base.headlineSmallEmphasized.withFont(brandFont),
        titleLargeEmphasized = base.titleLargeEmphasized.withFont(brandFont),
        titleMediumEmphasized = base.titleMediumEmphasized.withFont(bodyFont),
        titleSmallEmphasized = base.titleSmallEmphasized.withFont(bodyFont),
        bodyLargeEmphasized = base.bodyLargeEmphasized.withFont(bodyFont),
        bodyMediumEmphasized = base.bodyMediumEmphasized.withFont(bodyFont),
        bodySmallEmphasized = base.bodySmallEmphasized.withFont(bodyFont),
        labelLargeEmphasized = base.labelLargeEmphasized.withFont(bodyFont),
        labelMediumEmphasized = base.labelMediumEmphasized.withFont(bodyFont),
        labelSmallEmphasized = base.labelSmallEmphasized.withFont(bodyFont),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val ExpressiveTypography: Typography = buildExpressiveTypography()
