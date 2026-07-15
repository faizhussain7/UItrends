package com.mfhapps.trendingui.screens.orbs

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.mfhapps.trendingui.ui.components.ExpressiveMorphCache
import com.mfhapps.trendingui.ui.components.ExpressiveMorphTempo
import com.mfhapps.trendingui.ui.components.ExpressiveShapeCatalogTier
import com.mfhapps.trendingui.ui.components.expressiveMorphLoopPath

internal object ExpressiveOrbCatalog {

    private val morphCache = ExpressiveMorphCache()
    private val tempo = ExpressiveMorphTempo.Soft
    private val tier = ExpressiveShapeCatalogTier.OrbField

    fun pathForOrb(
        phaseOffset: Float,
        segmentClock: Float,
        center: Offset,
        radius: Float,
    ): Path = expressiveMorphLoopPath(
        morphCache = morphCache,
        segmentClock = segmentClock,
        phaseOffset = phaseOffset,
        center = center,
        radius = radius,
        tier = tier,
        tempo = tempo,
    )
}
