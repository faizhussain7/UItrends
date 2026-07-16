package com.mfhapps.trendingui.screens.spatial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.core.sensor.GyroTilt
import com.mfhapps.trendingui.core.sensor.rememberGyroscopeTilt
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion

@Immutable
data class SpatialLayer(
    val id: Int,
    val title: String,
    val depthTier: Int,
    val blurb: String,
    val role: String,
)

@Immutable
data class SpatialDepthSection(
    val tier: Int,
    val title: String,
    val caption: String,
    val layers: List<SpatialLayer>,
)

@Stable
data class SpatialTiltDegrees(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val enabled: Boolean = false,
)

private const val SpatialCameraDistanceMultiplier = 14f
private const val SpatialTiltDegreesScale = 5f

private val TierTitles = listOf(
    "Near field",
    "Foreground",
    "Mid depth",
    "Background",
    "Far plane",
)

private val TierCaptions = listOf(
    "Closest cards · strongest lift",
    "Primary interactive layer",
    "Balanced parallax response",
    "Soft recession · quieter elevation",
    "Horizon plane · lightest presence",
)

private val LayerRoles = listOf(
    "Hero plate",
    "Focus card",
    "Support slab",
    "Ambient pane",
    "Horizon shard",
)

private val LayerBlurbs = listOf(
    "Anchors the nearest Z plane with the strongest elevation cue.",
    "Holds primary focus while the list scrolls beneath the collapsing header.",
    "Shares gyro tilt with the detail pane for coherent perspective.",
    "Recedes gently so selected neighbors can rise without fighting for attention.",
    "Marks the far plane—useful for sensing depth without heavy motion.",
)

@Composable
fun rememberSpatialTiltDegrees(sensitivity: Float = 0.25f): SpatialTiltDegrees {
    val rawTilt = rememberGyroscopeTilt(sensitivity)
    val reduceMotion = LocalReduceMotion.current
    val tilt by remember {
        derivedStateOf { rawTilt.toSpatialTiltDegrees(reduceMotion) }
    }
    return tilt
}

private fun GyroTilt.toSpatialTiltDegrees(reduceMotion: Boolean): SpatialTiltDegrees {
    if (reduceMotion) return SpatialTiltDegrees()
    return SpatialTiltDegrees(
        pitch = pitch.coerceIn(-1f, 1f) * SpatialTiltDegreesScale,
        roll = roll.coerceIn(-1f, 1f) * SpatialTiltDegreesScale,
        enabled = true,
    )
}

@Composable
fun Modifier.spatialPerspective(
    tilt: SpatialTiltDegrees,
    intensity: Float = 1f,
): Modifier {
    if (!tilt.enabled || intensity <= 0f) return this
    val density = LocalDensity.current.density
    val scale = intensity.coerceIn(0f, 1.25f)
    return graphicsLayer {
        rotationX = tilt.pitch * scale
        rotationY = tilt.roll * scale
        cameraDistance = SpatialCameraDistanceMultiplier * density
    }
}

fun spatialLayerLift(layerIndex: Int, selected: Boolean = false): Dp = when {
    selected -> 0.dp
    else -> ((layerIndex % 3) * 2).dp
}

fun spatialDepthFactor(layerIndex: Int): Float = 1f + (layerIndex % 5) * 0.04f

fun spatialTierTitle(tier: Int): String = TierTitles.getOrElse(tier.coerceIn(0, 4)) { "Depth $tier" }

fun spatialTierCaption(tier: Int): String = TierCaptions.getOrElse(tier.coerceIn(0, 4)) { "Spatial layer" }

fun defaultSpatialLayers(): List<SpatialLayer> = List(24) { index ->
    val tier = index % 5
    SpatialLayer(
        id = index,
        title = "Layer ${index + 1}",
        depthTier = tier,
        blurb = LayerBlurbs[tier],
        role = LayerRoles[tier],
    )
}

fun List<SpatialLayer>.toDepthSections(): List<SpatialDepthSection> =
    groupBy { it.depthTier }
        .toSortedMap()
        .map { (tier, layers) ->
            SpatialDepthSection(
                tier = tier,
                title = spatialTierTitle(tier),
                caption = spatialTierCaption(tier),
                layers = layers.sortedBy { it.id },
            )
        }
