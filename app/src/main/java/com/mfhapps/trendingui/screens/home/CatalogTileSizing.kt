package com.mfhapps.trendingui.screens.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

internal val CatalogDemoIconSize = 42.dp

@Composable
internal fun rememberBentoTileMinHeight(tall: Boolean): Dp {
    val density = LocalDensity.current
    val titleStyle = MaterialTheme.typography.titleSmall
    val subtitleStyle = MaterialTheme.typography.labelSmall
    val subtitleLines = if (tall) 3 else 2
    return remember(tall, density.fontScale, titleStyle, subtitleStyle) {
        catalogTextBlockMinHeight(
            density = density,
            contentPadding = 14.dp,
            iconSize = CatalogDemoIconSize,
            titleLineHeight = titleStyle.lineHeight,
            subtitleLineHeight = subtitleStyle.lineHeight,
            titleMaxLines = 2,
            subtitleMaxLines = subtitleLines,
            iconToTextSpacing = 8.dp,
            textLineSpacing = 4.dp,
            stackedItemSpacing = 0.dp,
        )
    }
}

@Composable
internal fun rememberCompactTileMinHeight(): Dp {
    val density = LocalDensity.current
    val titleStyle = MaterialTheme.typography.labelLarge
    val subtitleStyle = MaterialTheme.typography.labelSmall
    return remember(density.fontScale, titleStyle, subtitleStyle) {
        catalogTextBlockMinHeight(
            density = density,
            contentPadding = 12.dp,
            iconSize = CatalogDemoIconSize,
            titleLineHeight = titleStyle.lineHeight,
            subtitleLineHeight = subtitleStyle.lineHeight,
            titleMaxLines = 2,
            subtitleMaxLines = 2,
            iconToTextSpacing = 0.dp,
            textLineSpacing = 0.dp,
            stackedItemSpacing = 8.dp,
        )
    }
}

@Composable
internal fun rememberFeaturedHeroMinHeight(): Dp {
    val density = LocalDensity.current
    val badgeStyle = MaterialTheme.typography.labelMedium
    val categoryStyle = MaterialTheme.typography.labelSmall
    val titleStyle = MaterialTheme.typography.headlineLargeEmphasized
    val subtitleStyle = MaterialTheme.typography.bodyMedium
    val ctaStyle = MaterialTheme.typography.labelLarge
    return remember(
        density.fontScale,
        badgeStyle,
        categoryStyle,
        titleStyle,
        subtitleStyle,
        ctaStyle,
    ) {
        with(density) {
            val outerPadding = 20.dp * 2
            val topRowHeight = badgeStyle.lineHeight.toDp() + 10.dp
            val textColumnHeight =
                categoryStyle.lineHeight.toDp() +
                    6.dp +
                    titleStyle.lineHeight.toDp() +
                    6.dp +
                    subtitleStyle.lineHeight.toDp() * 2
            val ctaRowHeight = maxOf(52.dp, ctaStyle.lineHeight.toDp() + 20.dp)
            val sectionGaps = 14.dp * 2
            outerPadding + topRowHeight + textColumnHeight + ctaRowHeight + sectionGaps
        }
    }
}

private fun catalogTextBlockMinHeight(
    density: Density,
    contentPadding: Dp,
    iconSize: Dp,
    titleLineHeight: TextUnit,
    subtitleLineHeight: TextUnit,
    titleMaxLines: Int,
    subtitleMaxLines: Int,
    iconToTextSpacing: Dp,
    textLineSpacing: Dp,
    stackedItemSpacing: Dp,
): Dp = with(density) {
    val verticalPadding = contentPadding * 2
    val titleHeight = titleLineHeight.toDp() * titleMaxLines
    val subtitleHeight = subtitleLineHeight.toDp() * subtitleMaxLines
    val textBlockHeight = titleHeight +
        if (subtitleMaxLines > 0) textLineSpacing + subtitleHeight else 0.dp
    val stackedGaps = if (stackedItemSpacing > 0.dp) stackedItemSpacing * 2 else 0.dp
    verticalPadding + iconSize + iconToTextSpacing + textBlockHeight + stackedGaps
}
