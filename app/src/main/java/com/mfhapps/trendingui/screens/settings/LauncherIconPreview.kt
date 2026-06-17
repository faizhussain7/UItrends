package com.mfhapps.trendingui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.launcher.AppLauncherIcon
import com.mfhapps.trendingui.ui.accessibility.DecorativeImage
import com.mfhapps.trendingui.ui.theme.launcherIconGradientBackground

@Composable
fun LauncherIconPreview(
    icon: AppLauncherIcon,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    showFrame: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(percent = 24)

    val content: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .launcherIconGradientBackground(),
            contentAlignment = Alignment.Center,
        ) {
            DecorativeImage(
                painter = painterResource(icon.brandLogoRes),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
            )
        }
    }

    if (showFrame) {
        Surface(
            modifier = modifier.size(size),
            shape = shape,
            color = scheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 3.dp,
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f)),
        ) {
            Box(Modifier.padding(4.dp)) {
                content()
            }
        }
    } else {
        Box(modifier.size(size).clip(shape)) {
            content()
        }
    }
}
