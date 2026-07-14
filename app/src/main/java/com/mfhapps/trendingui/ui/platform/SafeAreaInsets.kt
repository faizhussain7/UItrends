package com.mfhapps.trendingui.ui.platform

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CatalogPaneRole {
    Single,
    List,
    Detail,
}

val LocalCatalogPaneRole = staticCompositionLocalOf { CatalogPaneRole.Single }

@Composable
fun appBarTopWindowInsets(): WindowInsets =
    TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Top)

@Composable
fun appBarWindowInsets(): WindowInsets = TopAppBarDefaults.windowInsets

@Composable
fun catalogListContentWindowInsets(): WindowInsets {
    val dualPane = LocalCatalogPaneRole.current == CatalogPaneRole.List
    return if (dualPane) {
        WindowInsets.safeDrawing.only(
            WindowInsetsSides.Start + WindowInsetsSides.Top + WindowInsetsSides.Bottom,
        )
    } else {
        WindowInsets.safeDrawing
    }
}

@Composable
fun catalogCollapsedBarWindowInsets(): WindowInsets {
    val dualPane = LocalCatalogPaneRole.current == CatalogPaneRole.List
    return if (dualPane) {
        TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Start + WindowInsetsSides.Top)
    } else {
        TopAppBarDefaults.windowInsets
    }
}

@Composable
fun Modifier.detailPaneHorizontalSafePadding(): Modifier {
    val sides = when (LocalCatalogPaneRole.current) {
        CatalogPaneRole.Detail -> WindowInsetsSides.End
        CatalogPaneRole.List -> WindowInsetsSides.Start
        CatalogPaneRole.Single -> WindowInsetsSides.Horizontal
    }
    return windowInsetsPadding(WindowInsets.safeDrawing.only(sides))
}

@Composable
fun catalogScaffoldContentPadding(
    innerPadding: PaddingValues,
    horizontalGutter: Dp = 0.dp,
    extraBottom: Dp = 16.dp,
    includeTopInset: Boolean = true,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = innerPadding.calculateStartPadding(layoutDirection) + horizontalGutter,
        top = if (includeTopInset) innerPadding.calculateTopPadding() else 0.dp,
        end = innerPadding.calculateEndPadding(layoutDirection) + horizontalGutter,
        bottom = innerPadding.calculateBottomPadding() + extraBottom,
    )
}

@Composable
fun safeDrawingPaddingValues(): PaddingValues = WindowInsets.safeDrawing.asPaddingValues()

@Composable
fun safeDrawingHorizontalPaddingValues(): PaddingValues =
    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
