package com.mfhapps.trendingui.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import com.mfhapps.trendingui.R


@Composable
fun Modifier.launcherIconGradientBackground(): Modifier {
    val start = colorResource(R.color.launcher_bg_start)
    val mid = colorResource(R.color.launcher_bg_mid)
    val end = colorResource(R.color.launcher_bg_end)
    return drawBehind {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(start, mid, end),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
        )
    }
}
