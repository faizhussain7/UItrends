package com.mfhapps.trendingui.screens.orbs

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion

@Composable
fun ProvideOrbsDetailEnvironment(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        OrbsDetailBackdrop(Modifier.matchParentSize().appHazeSource())
        content()
    }
}

@Composable
fun OrbsDetailBackdrop(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val chrome = rememberOrbsChrome()
    val reduceMotion = LocalReduceMotion.current
    val infinite = rememberInfiniteTransition(label = "orbsBackdrop")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
    ) {
        val parallax = Offset(drift * 0.06f - 0.03f, (1f - drift) * 0.05f - 0.025f)
        drawMeshGradient(
            field = chrome.backdropField,
            baseAlpha = if (chrome.isDark) 0.82f else 0.76f,
            blendMode = chrome.meshBlendMode,
            parallax = parallax,
        )
        drawGlowOrbs(
            colors = chrome.backdropOrbColors,
            normalizedCenters = listOf(
                Offset(0.14f, 0.20f),
                Offset(0.86f, 0.26f),
                Offset(0.40f, 0.80f),
                Offset(0.70f, 0.50f),
            ),
            blendMode = chrome.orbBlendMode,
            strength = if (chrome.isDark) 0.98f else 0.94f,
            radiusFraction = if (chrome.isDark) 0.32f else 0.30f,
        )
    }
}
