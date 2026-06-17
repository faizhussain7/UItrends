package com.mfhapps.trendingui.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import dev.chrisbanes.haze.hazeSource

@Composable
fun CatalogGradientChrome(
    appearance: CatalogAppearance,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val reduceMotion = LocalReduceMotion.current
    val colors = appearance.colors
    val dynamicBrush = rememberDynamicHomeScreenBrush(
        gradients = appearance.gradients,
        colorScheme = scheme,
        reduceMotion = reduceMotion,
    )
    val (hazeState, hazeEnabled) = rememberCatalogHazeState()

    ProvideCatalogHaze(hazeState = hazeState, hazeEnabled = hazeEnabled) {
        CompositionLocalProvider(
            LocalCatalogAppearance provides appearance,
            LocalHomeCatalogColors provides colors,
            LocalContentColor provides colors.headerTitle,
        ) {
            Box(modifier = modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(dynamicBrush),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .then(
                            if (hazeEnabled) {
                                Modifier.hazeSource(state = hazeState)
                            } else {
                                Modifier
                            },
                        ),
                    content = content,
                )
            }
        }
    }
}
