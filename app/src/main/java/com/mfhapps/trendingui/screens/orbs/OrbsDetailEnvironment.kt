package com.mfhapps.trendingui.screens.orbs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.ExpressiveMorphTempo
import com.mfhapps.trendingui.ui.components.ExpressiveShapeCatalogTier
import com.mfhapps.trendingui.ui.components.LocalExpressiveShapeMorphClock
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.components.expressivePhaseOffset
import com.mfhapps.trendingui.ui.components.rememberExpressiveShapeMorphClock
import kotlin.random.Random

@Composable
fun ProvideOrbsDetailEnvironment(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val appearanceState = remember { OrbsAppearanceState() }
    val expressive = appearanceState.expressive
    val sharedClock = rememberExpressiveShapeMorphClock(
        enabled = expressive,
        tier = ExpressiveShapeCatalogTier.OrbField,
        tempo = ExpressiveMorphTempo.Soft,
    )
    CompositionLocalProvider(
        LocalOrbsAppearanceState provides appearanceState,
        LocalExpressiveShapeMorphClock provides if (expressive) sharedClock else null,
    ) {
        Box(modifier.fillMaxSize()) {
            OrbsDetailBackdrop(Modifier.matchParentSize().appHazeSource())
            content()
        }
    }
}

@Composable
fun OrbsDetailBackdrop(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val chrome = rememberOrbsChrome()
    val reduceMotion = LocalReduceMotion.current
    val appearance = LocalOrbsAppearanceState.current
    val expressive = appearance?.expressive == true
    val presence = appearance?.presence ?: 1f
    val randomShapes = appearance?.randomShapes != false
    val shapeClock = rememberExpressiveShapeMorphClock(
        enabled = expressive,
        tier = ExpressiveShapeCatalogTier.OrbField,
        tempo = ExpressiveMorphTempo.Soft,
    )
    val randomPhases = remember {
        List(4) { expressivePhaseOffset(Random.nextInt(), ExpressiveShapeCatalogTier.OrbField) }
    }
    val sharedPhase = remember {
        expressivePhaseOffset(0x4241444B, ExpressiveShapeCatalogTier.OrbField)
    }
    val backdropOrbPhases = if (randomShapes) {
        randomPhases
    } else {
        List(4) { sharedPhase }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
    ) {
        val driftPhase = if (reduceMotion) 0f else shapeClock * 0.04f
        val parallax = Offset(driftPhase - 0.02f, -driftPhase * 0.8f)
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
            presence = presence,
            expressive = expressive,
            shapeClock = shapeClock,
            orbPhaseOffsets = backdropOrbPhases,
        )
    }
}
