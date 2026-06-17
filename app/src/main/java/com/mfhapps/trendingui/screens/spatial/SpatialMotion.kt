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
)

@Stable
data class SpatialTiltDegrees(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val enabled: Boolean = false,
)

private const val SpatialCameraDistanceMultiplier = 14f
private const val SpatialTiltDegreesScale = 5f

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
fun Modifier.spatialPerspective(tilt: SpatialTiltDegrees): Modifier {
    if (!tilt.enabled) return this
    val density = LocalDensity.current.density
    return graphicsLayer {
        rotationX = tilt.pitch
        rotationY = tilt.roll
        cameraDistance = SpatialCameraDistanceMultiplier * density
    }
}

fun spatialLayerLift(layerIndex: Int, selected: Boolean = false): Dp = when {
    selected -> 0.dp
    else -> (layerIndex % 3).dp
}

fun spatialDepthFactor(layerIndex: Int): Float = 1f + (layerIndex % 5) * 0.04f

fun defaultSpatialLayers(): List<SpatialLayer> = List(24) { index ->
    SpatialLayer(
        id = index,
        title = "Layer ${index + 1}",
        depthTier = index % 5,
    )
}
