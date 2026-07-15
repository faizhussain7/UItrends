package com.mfhapps.trendingui.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import kotlin.math.floor


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object MaterialExpressiveShapeCatalog {
    val shapes: List<RoundedPolygon> by lazy {
        listOf(
            MaterialShapes.Square,
            MaterialShapes.Slanted,
            MaterialShapes.Arch,
            MaterialShapes.Fan,
            MaterialShapes.Arrow,
            MaterialShapes.SemiCircle,
            MaterialShapes.Oval,
            MaterialShapes.Pill,
            MaterialShapes.Triangle,
            MaterialShapes.Diamond,
            MaterialShapes.ClamShell,
            MaterialShapes.Pentagon,
            MaterialShapes.Gem,
            MaterialShapes.Sunny,
            MaterialShapes.VerySunny,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Cookie6Sided,
            MaterialShapes.Cookie7Sided,
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Cookie12Sided,
            MaterialShapes.Ghostish,
            MaterialShapes.Clover4Leaf,
            MaterialShapes.Clover8Leaf,
            MaterialShapes.Burst,
            MaterialShapes.SoftBurst,
            MaterialShapes.Boom,
            MaterialShapes.SoftBoom,
            MaterialShapes.Flower,
            MaterialShapes.Puffy,
            MaterialShapes.PuffyDiamond,
            MaterialShapes.PixelCircle,
            MaterialShapes.PixelTriangle,
            MaterialShapes.Bun,
            MaterialShapes.Heart,
        )
    }

    const val SegmentMillis = 2_400
}

@Immutable
data class MaterialExpressiveLoopFrame(
    val rest: RoundedPolygon,
    val pressed: RoundedPolygon,
    val progress: Float,
)


fun smoothExpressiveMorph(progress: Float): Float {
    val t = progress.coerceIn(0f, 1f)
    val step = t * t * (3f - 2f * t)
    return step * step * (3f - 2f * step)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun materialExpressiveLoopFrame(
    segmentClock: Float,
    phaseOffset: Float = 0f,
): MaterialExpressiveLoopFrame {
    val shapes = MaterialExpressiveShapeCatalog.shapes
    val n = shapes.size.coerceAtLeast(1)
    val wrapped = segmentClock + phaseOffset
    val index = floor(wrapped.toDouble()).toInt().let { raw ->
        ((raw % n) + n) % n
    }
    val segmentProgress = smoothExpressiveMorph(wrapped - floor(wrapped.toDouble()).toFloat())
    return MaterialExpressiveLoopFrame(
        rest = shapes[index],
        pressed = shapes[(index + 1) % n],
        progress = segmentProgress,
    )
}


fun stableExpressivePhaseOffset(seed: Int): Float {
    val n = MaterialExpressiveShapeCatalog.shapes.size.coerceAtLeast(1).toFloat()
    var hash = seed + 0x9E3779B9
    hash = hash xor (hash shl 13)
    hash = hash xor (hash shr 17)
    hash = hash xor (hash shl 5)
    val unit = (hash and 0x7fffffff) / Int.MAX_VALUE.toFloat()
    return unit * n
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberMaterialExpressiveLoopClock(
    enabled: Boolean,
): Float {
    val reduceMotion = LocalReduceMotion.current
    val shapes = MaterialExpressiveShapeCatalog.shapes
    val segmentCount = shapes.size.coerceAtLeast(1)
    val active = enabled && !reduceMotion
    val infinite = rememberInfiniteTransition(label = "materialExpressiveLoop")
    val clock by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (active) segmentCount.toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = MaterialExpressiveShapeCatalog.SegmentMillis * segmentCount,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "segmentClock",
    )
    return if (active) clock else 0f
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberMaterialExpressiveLoopShape(
    enabled: Boolean,
    phaseOffset: Float = 0f,
): Shape {
    val clock = rememberMaterialExpressiveLoopClock(enabled)
    val frame = materialExpressiveLoopFrame(clock, phaseOffset)
    return rememberMorphShape(
        rest = frame.rest,
        pressed = frame.pressed,
        progress = frame.progress,
    )
}

@Stable
class MaterialExpressiveMorphCache {
    private val cache = HashMap<Long, Morph>(128)

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun morph(from: RoundedPolygon, to: RoundedPolygon): Morph {
        val key = (System.identityHashCode(from).toLong() shl 32) or
            (System.identityHashCode(to).toLong() and 0xffffffffL)
        return cache.getOrPut(key) { Morph(from, to) }
    }
}
