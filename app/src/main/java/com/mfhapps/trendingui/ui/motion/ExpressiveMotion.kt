package com.mfhapps.trendingui.ui.motion

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object ExpressiveMotion {

    private const val DefaultSpatialDamping = 0.8f
    private const val DefaultSpatialStiffness = 380f
    private const val FastSpatialDamping = 0.6f
    private const val FastSpatialStiffness = 800f
    private const val SlowSpatialDamping = 0.8f
    private const val SlowSpatialStiffness = 200f

    private const val DefaultEffectsDamping = 1.0f
    private const val DefaultEffectsStiffness = 1600f
    private const val FastEffectsDamping = 1.0f
    private const val FastEffectsStiffness = 3800f
    private const val SlowEffectsDamping = 1.0f
    private const val SlowEffectsStiffness = 800f

    fun <T> defaultSpatial(): SpringSpec<T> = spring(
        dampingRatio = DefaultSpatialDamping,
        stiffness = DefaultSpatialStiffness,
    )

    fun <T> fastSpatial(): SpringSpec<T> = spring(
        dampingRatio = FastSpatialDamping,
        stiffness = FastSpatialStiffness,
    )

    fun <T> slowSpatial(): SpringSpec<T> = spring(
        dampingRatio = SlowSpatialDamping,
        stiffness = SlowSpatialStiffness,
    )

    fun <T> defaultEffects(): SpringSpec<T> = spring(
        dampingRatio = DefaultEffectsDamping,
        stiffness = DefaultEffectsStiffness,
    )

    fun <T> fastEffects(): SpringSpec<T> = spring(
        dampingRatio = FastEffectsDamping,
        stiffness = FastEffectsStiffness,
    )

    fun <T> slowEffects(): SpringSpec<T> = spring(
        dampingRatio = SlowEffectsDamping,
        stiffness = SlowEffectsStiffness,
    )

    val navOffset: FiniteAnimationSpec<IntOffset> = defaultSpatial()
    val navFloat: FiniteAnimationSpec<Float> = defaultSpatial()
    val navEffects: FiniteAnimationSpec<Float> = defaultEffects()
    val sharedBounds: FiniteAnimationSpec<Rect> = slowSpatial()
    val pressMorph: FiniteAnimationSpec<Float> = defaultSpatial()
    val pressScale: FiniteAnimationSpec<Float> = fastSpatial()
    val sectionEnterOffset: FiniteAnimationSpec<IntOffset> = defaultSpatial()
    val sectionEnterEffects: FiniteAnimationSpec<Float> = defaultEffects()
    val dragSettle: FiniteAnimationSpec<Offset> = slowSpatial()
    val zoomSpatial: FiniteAnimationSpec<Float> = defaultSpatial()
    val contentFade: FiniteAnimationSpec<Float> = defaultEffects()
    val contentFadeFast: FiniteAnimationSpec<Float> = fastEffects()
}

@Composable
@ReadOnlyComposable
fun expressiveMotionScheme(): MotionScheme = MaterialTheme.motionScheme

@Composable
@ReadOnlyComposable
fun <T> expressiveSpatialSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.defaultSpatialSpec()

@Composable
@ReadOnlyComposable
fun <T> expressiveFastSpatialSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.fastSpatialSpec()

@Composable
@ReadOnlyComposable
fun <T> expressiveSlowSpatialSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.slowSpatialSpec()

@Composable
@ReadOnlyComposable
fun <T> expressiveEffectsSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.defaultEffectsSpec()

@Composable
@ReadOnlyComposable
fun <T> expressiveFastEffectsSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.fastEffectsSpec()

@Composable
@ReadOnlyComposable
fun <T> expressiveSlowEffectsSpec(): FiniteAnimationSpec<T> =
    MaterialTheme.motionScheme.slowEffectsSpec()
