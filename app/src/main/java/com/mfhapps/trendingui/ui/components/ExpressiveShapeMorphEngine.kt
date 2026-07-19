package com.mfhapps.trendingui.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import kotlin.math.floor

@Immutable
enum class ExpressiveShapeCatalogTier {
    Full,
    OrbField,
    Accent,
    Badge,
    Contained,
    Fullscreen,
}

@Immutable
data class ExpressiveMorphTempo(
    val segmentMillis: Int = 2_400,
    val dwellFraction: Float = 0.11f,
) {
    init {
        require(segmentMillis > 0)
        require(dwellFraction in 0f..0.45f)
    }

    companion object {
        val Default = ExpressiveMorphTempo()
        val Soft = ExpressiveMorphTempo(segmentMillis = 2_800, dwellFraction = 0.14f)
        val Snappy = ExpressiveMorphTempo(segmentMillis = 1_900, dwellFraction = 0.08f)
        val Fullscreen = ExpressiveMorphTempo(segmentMillis = 2_400, dwellFraction = 0.12f)
    }
}

@Immutable
data class ExpressiveMorphFrame(
    val from: RoundedPolygon,
    val to: RoundedPolygon,
    val progress: Float,
    val segmentIndex: Int,
)

val LocalExpressiveShapeMorphClock = compositionLocalOf<Float?> { null }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object ExpressiveShapeCatalogs {

    val full: List<RoundedPolygon> by lazy {
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

    val orbField: List<RoundedPolygon> by lazy {
        listOf(
            MaterialShapes.SoftBoom,
            MaterialShapes.Puffy,
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Gem,
            MaterialShapes.Sunny,
            MaterialShapes.Clover4Leaf,
            MaterialShapes.SoftBurst,
            MaterialShapes.Flower,
            MaterialShapes.Pill,
            MaterialShapes.Oval,
            MaterialShapes.Ghostish,
            MaterialShapes.Bun,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.PuffyDiamond,
            MaterialShapes.ClamShell,
            MaterialShapes.Heart,
        )
    }

    val accent: List<RoundedPolygon> by lazy {
        listOf(
            MaterialShapes.Sunny,
            MaterialShapes.SoftBoom,
            MaterialShapes.Gem,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Clover4Leaf,
            MaterialShapes.Puffy,
            MaterialShapes.SoftBurst,
            MaterialShapes.Flower,
        )
    }

    val badge: List<RoundedPolygon> by lazy {
        listOf(
            MaterialShapes.SoftBoom,
            MaterialShapes.Gem,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Clover4Leaf,
            MaterialShapes.Sunny,
            MaterialShapes.Circle,
        )
    }

    val contained: List<RoundedPolygon> by lazy {
        listOf(
            MaterialShapes.Circle,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Cookie12Sided,
            MaterialShapes.Circle,
        )
    }

    val fullscreen: List<RoundedPolygon> by lazy {
        listOf(
            MaterialShapes.Gem,
            MaterialShapes.Sunny,
            MaterialShapes.Circle,
            MaterialShapes.Cookie4Sided,
            MaterialShapes.SoftBoom,
        )
    }

    fun shapes(tier: ExpressiveShapeCatalogTier): List<RoundedPolygon> = when (tier) {
        ExpressiveShapeCatalogTier.Full -> full
        ExpressiveShapeCatalogTier.OrbField -> orbField
        ExpressiveShapeCatalogTier.Accent -> accent
        ExpressiveShapeCatalogTier.Badge -> badge
        ExpressiveShapeCatalogTier.Contained -> contained
        ExpressiveShapeCatalogTier.Fullscreen -> fullscreen
    }
}

object ExpressiveShapeMorphMath {

    fun smoothstep(t: Float): Float {
        val x = t.coerceIn(0f, 1f)
        return x * x * (3f - 2f * x)
    }

    fun smootherstep(t: Float): Float {
        val x = t.coerceIn(0f, 1f)
        return x * x * x * (x * (x * 6f - 15f) + 10f)
    }

    fun easedSegmentProgress(raw: Float, dwellFraction: Float = 0.11f): Float {
        val t = raw.coerceIn(0f, 1f)
        val dwell = dwellFraction.coerceIn(0f, 0.45f)
        if (dwell <= 0f) return smootherstep(t)
        if (t <= dwell) return 0f
        if (t >= 1f - dwell) return 1f
        val morphWindow = 1f - 2f * dwell
        val u = (t - dwell) / morphWindow
        return smootherstep(u)
    }

    fun phaseFromSeed(seed: Int, catalogSize: Int): Float {
        val n = catalogSize.coerceAtLeast(1).toFloat()
        var hash = seed xor 0x9E3779B9.toInt()
        hash = hash xor (hash shl 13)
        hash = hash xor (hash ushr 17)
        hash = hash xor (hash shl 5)
        val unit = (hash and 0x7fffffff).toFloat() / Int.MAX_VALUE.toFloat()
        return unit * n
    }

    fun resolveFrame(
        segmentClock: Float,
        shapes: List<RoundedPolygon>,
        phaseOffset: Float = 0f,
        dwellFraction: Float = ExpressiveMorphTempo.Default.dwellFraction,
    ): ExpressiveMorphFrame {
        val n = shapes.size.coerceAtLeast(1)
        val wrapped = segmentClock + phaseOffset
        val base = floor(wrapped.toDouble()).toInt()
        val index = ((base % n) + n) % n
        val frac = (wrapped - floor(wrapped.toDouble()).toFloat()).coerceIn(0f, 1f)
        return ExpressiveMorphFrame(
            from = shapes[index],
            to = shapes[(index + 1) % n],
            progress = easedSegmentProgress(frac, dwellFraction),
            segmentIndex = index,
        )
    }
}

@Stable
class ExpressiveMorphCache {
    private val cache = HashMap<Long, Morph>(128)

    fun morph(from: RoundedPolygon, to: RoundedPolygon): Morph {
        val key = (System.identityHashCode(from).toLong() shl 32) or
            (System.identityHashCode(to).toLong() and 0xffffffffL)
        return cache.getOrPut(key) { Morph(from, to) }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun Morph.toComposePath(
    progress: Float,
    center: Offset,
    radius: Float,
    startAngle: Int = 0,
    path: Path = Path(),
): Path {
    toPath(progress = progress.coerceIn(0f, 1f), path = path, startAngle = startAngle)
    path.transform(
        Matrix().apply {
            scale(x = radius * 2f, y = radius * 2f)
        },
    )
    path.translate(center - path.getBounds().center)
    return path
}

@Composable
fun rememberExpressiveShapeMorphClock(
    enabled: Boolean,
    tier: ExpressiveShapeCatalogTier = ExpressiveShapeCatalogTier.Full,
    tempo: ExpressiveMorphTempo = ExpressiveMorphTempo.Default,
): Float {
    val reduceMotion = LocalReduceMotion.current
    val shared = LocalExpressiveShapeMorphClock.current
    val shapes = remember(tier) { ExpressiveShapeCatalogs.shapes(tier) }
    val segmentCount = shapes.size.coerceAtLeast(1)
    val active = enabled && !reduceMotion

    if (shared != null) {
        return if (active) shared else 0f
    }

    val infinite = rememberInfiniteTransition(label = "expressiveShapeMorph")
    val clock by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (active) segmentCount.toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (tempo.segmentMillis * segmentCount).coerceAtLeast(tempo.segmentMillis),
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "expressiveSegmentClock",
    )
    return if (active) clock else 0f
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberExpressiveMorphLoopShape(
    enabled: Boolean,
    tier: ExpressiveShapeCatalogTier = ExpressiveShapeCatalogTier.Accent,
    tempo: ExpressiveMorphTempo = ExpressiveMorphTempo.Default,
    phaseOffset: Float = 0f,
    introFrom: RoundedPolygon? = null,
    introMillis: Int = 720,
): Shape {
    val reduceMotion = LocalReduceMotion.current
    val shapes = remember(tier) { ExpressiveShapeCatalogs.shapes(tier) }
    val clock = rememberExpressiveShapeMorphClock(enabled = enabled, tier = tier, tempo = tempo)

    val introProgress by animateFloatAsState(
        targetValue = if (enabled && !reduceMotion && introFrom != null) 1f else 0f,
        animationSpec = tween(durationMillis = introMillis, easing = FastOutSlowInEasing),
        label = "expressiveMorphIntro",
    )

    val frame = when {
        reduceMotion || !enabled -> {
            val idle = introFrom ?: shapes.first()
            ExpressiveMorphFrame(idle, idle, 0f, 0)
        }
        introFrom != null && introProgress < 1f -> {
            ExpressiveMorphFrame(
                from = introFrom,
                to = shapes.first(),
                progress = ExpressiveShapeMorphMath.smootherstep(introProgress),
                segmentIndex = 0,
            )
        }
        else -> ExpressiveShapeMorphMath.resolveFrame(
            segmentClock = clock,
            shapes = shapes,
            phaseOffset = phaseOffset,
            dwellFraction = tempo.dwellFraction,
        )
    }

    return rememberMorphShape(
        rest = frame.from,
        pressed = frame.to,
        progress = frame.progress,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun expressiveMorphLoopPath(
    morphCache: ExpressiveMorphCache,
    segmentClock: Float,
    phaseOffset: Float,
    center: Offset,
    radius: Float,
    tier: ExpressiveShapeCatalogTier = ExpressiveShapeCatalogTier.OrbField,
    tempo: ExpressiveMorphTempo = ExpressiveMorphTempo.Default,
): Path {
    val shapes = ExpressiveShapeCatalogs.shapes(tier)
    val frame = ExpressiveShapeMorphMath.resolveFrame(
        segmentClock = segmentClock,
        shapes = shapes,
        phaseOffset = phaseOffset,
        dwellFraction = tempo.dwellFraction,
    )
    return morphCache.morph(frame.from, frame.to).toComposePath(
        progress = frame.progress,
        center = center,
        radius = radius,
    )
}

fun expressivePhaseOffset(
    seed: Int,
    tier: ExpressiveShapeCatalogTier = ExpressiveShapeCatalogTier.OrbField,
): Float {
    val n = ExpressiveShapeCatalogs.shapes(tier).size
    return ExpressiveShapeMorphMath.phaseFromSeed(seed, n)
}

@Composable
fun rememberExpressiveAccentShape(
    seed: Int = 0,
    tier: ExpressiveShapeCatalogTier = ExpressiveShapeCatalogTier.Accent,
    tempo: ExpressiveMorphTempo = ExpressiveMorphTempo.Soft,
    enabled: Boolean = true,
): Shape {
    val reduceMotion = LocalReduceMotion.current
    val phase = remember(seed, tier) { expressivePhaseOffset(seed, tier) }
    return rememberExpressiveMorphLoopShape(
        enabled = enabled && !reduceMotion,
        tier = tier,
        tempo = tempo,
        phaseOffset = phase,
    )
}

@Composable
fun rememberExpressiveBadgeShape(
    seed: Int = 0,
    tempo: ExpressiveMorphTempo = ExpressiveMorphTempo.Soft,
    enabled: Boolean = true,
): Shape = rememberExpressiveAccentShape(
    seed = seed,
    tier = ExpressiveShapeCatalogTier.Badge,
    tempo = tempo,
    enabled = enabled,
)
