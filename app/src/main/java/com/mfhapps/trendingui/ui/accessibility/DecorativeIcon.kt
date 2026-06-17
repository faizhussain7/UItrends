package com.mfhapps.trendingui.ui.accessibility

import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.material3.LocalContentColor

fun Modifier.decorativeSemantics(): Modifier = clearAndSetSemantics { }

@Composable
fun DecorativeIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = modifier.decorativeSemantics(),
        tint = tint,
    )
}

@Composable
fun DecorativeImage(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier.decorativeSemantics(),
        contentScale = contentScale,
        colorFilter = colorFilter,
    )
}
