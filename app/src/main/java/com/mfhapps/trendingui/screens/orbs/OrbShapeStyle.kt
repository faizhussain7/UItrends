package com.mfhapps.trendingui.screens.orbs

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toPath
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Path
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import java.util.EnumMap

@Immutable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
enum class OrbShapeStyle(
    val label: String,
    val polygon: RoundedPolygon?,
) {
    Circle(
        label = "Circle",
        polygon = null,
    ),
    SoftBoom(
        label = "Soft boom",
        polygon = MaterialShapes.SoftBoom,
    ),
    Gem(
        label = "Gem",
        polygon = MaterialShapes.Gem,
    ),
    Sunny(
        label = "Sunny",
        polygon = MaterialShapes.Sunny,
    ),
    Cookie(
        label = "Cookie",
        polygon = MaterialShapes.Cookie4Sided,
    ),
    Clover(
        label = "Clover",
        polygon = MaterialShapes.Clover4Leaf,
    );


    fun unitPath(): Path? {
        val polygon = polygon ?: return null
        return UnitPaths.getOrPut(this) {
            Path().also { path ->
                Morph(polygon, polygon).toPath(
                    progress = 0f,
                    path = path,
                    startAngle = 0,
                )
            }
        }
    }

    private companion object {
        val UnitPaths = EnumMap<OrbShapeStyle, Path>(OrbShapeStyle::class.java)
    }
}
