package com.mfhapps.trendingui.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Immutable
import androidx.graphics.shapes.RoundedPolygon
import kotlin.math.abs

@Immutable
data class CatalogMorphPair(
    val rest: RoundedPolygon,
    val pressed: RoundedPolygon,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object CatalogMorphShapes {
    private val morphPairs = listOf(
        CatalogMorphPair(MaterialShapes.SoftBoom, MaterialShapes.Circle),
        CatalogMorphPair(MaterialShapes.Cookie4Sided, MaterialShapes.Gem),
        CatalogMorphPair(MaterialShapes.Gem, MaterialShapes.Circle),
        CatalogMorphPair(MaterialShapes.Clover4Leaf, MaterialShapes.Sunny),
        CatalogMorphPair(MaterialShapes.Sunny, MaterialShapes.Circle),
        CatalogMorphPair(MaterialShapes.Cookie9Sided, MaterialShapes.Pill),
        CatalogMorphPair(MaterialShapes.Puffy, MaterialShapes.Circle),
        CatalogMorphPair(MaterialShapes.Pill, MaterialShapes.Slanted),
        CatalogMorphPair(MaterialShapes.Arrow, MaterialShapes.Circle),
        CatalogMorphPair(MaterialShapes.Slanted, MaterialShapes.SoftBoom),
    )

    val heroMorph: CatalogMorphPair
        get() = CatalogMorphPair(MaterialShapes.SoftBoom, MaterialShapes.Circle)

    fun forDemo(route: Any): CatalogMorphPair {
        val key = route::class.simpleName ?: route.hashCode().toString()
        return morphPairs[abs(key.hashCode()) % morphPairs.size]
    }

    fun forIndex(index: Int): CatalogMorphPair = morphPairs[abs(index) % morphPairs.size]

    fun iconForDemo(route: Any): RoundedPolygon = forDemo(route).rest
}
