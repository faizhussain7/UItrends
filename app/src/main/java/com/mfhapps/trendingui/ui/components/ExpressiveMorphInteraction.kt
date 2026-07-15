package com.mfhapps.trendingui.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.motion.expressiveFastSpatialSpec
import com.mfhapps.trendingui.ui.motion.expressiveSpatialSpec

@Immutable
data class ExpressiveMorphPress(
    val morphProgress: Float,
    val scale: Float,
    val shape: Shape,
)

@Stable
object ExpressiveMorphDefaults {
    val buttonRest: RoundedPolygon
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        get() = MaterialShapes.Square

    val buttonPressed: RoundedPolygon
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        get() = MaterialShapes.Circle

    val iconRest: RoundedPolygon
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        get() = MaterialShapes.Gem

    val iconPressed: RoundedPolygon
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        get() = MaterialShapes.Circle

    val cardRest: RoundedPolygon
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        get() = MaterialShapes.SoftBoom

    val cardPressed: RoundedPolygon
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        get() = MaterialShapes.Circle
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberExpressiveMorphPress(
    interactionSource: MutableInteractionSource,
    rest: RoundedPolygon = ExpressiveMorphDefaults.buttonRest,
    pressed: RoundedPolygon = ExpressiveMorphDefaults.buttonPressed,
    startAngle: Int = 0,
    hapticOnPress: Boolean = true,
): ExpressiveMorphPress {
    val isPressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val haptics = LocalHapticFeedback.current
    val morphProgress by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) 1f else 0f,
        animationSpec = expressiveSpatialSpec(),
        label = "expressiveMorphProgress",
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !reduceMotion) 0.96f else 1f,
        animationSpec = expressiveFastSpatialSpec(),
        label = "expressiveMorphScale",
    )
    val shape = rememberMorphShape(rest = rest, pressed = pressed, progress = morphProgress, startAngle = startAngle)

    LaunchedEffect(isPressed, hapticOnPress, reduceMotion) {
        if (isPressed && hapticOnPress && !reduceMotion) {
            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
        }
    }

    return ExpressiveMorphPress(
        morphProgress = morphProgress,
        scale = scale,
        shape = shape,
    )
}

fun resolveMorphDisplayShape(
    displayShape: Shape,
    morph: ExpressiveMorphPress,
): Shape = if (morph.morphProgress <= 0f) displayShape else morph.shape

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberDynamicPressShape(
    displayShape: Shape,
    morph: ExpressiveMorphPress,
    morphRest: RoundedPolygon,
    morphPressed: RoundedPolygon,
    startAngle: Int = 0,
): Shape {
    val morphEngine = remember(morphRest, morphPressed) { Morph(morphRest, morphPressed) }
    return remember(displayShape, morphEngine, startAngle) {
        DynamicPressShape(
            displayShape = displayShape,
            morph = morphEngine,
            progress = { morph.morphProgress },
            startAngle = startAngle,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberMorphShape(
    rest: RoundedPolygon,
    pressed: RoundedPolygon,
    progress: Float,
    startAngle: Int = 0,
): Shape {
    val morph = remember(rest, pressed) { Morph(rest, pressed) }
    return remember(morph, progress, startAngle) {
        MorphProgressShape(morph = morph, progress = { progress }, startAngle = startAngle)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private class DynamicPressShape(
    private val displayShape: Shape,
    private val morph: Morph,
    private val progress: () -> Float,
    private val startAngle: Int,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val p = progress()
        if (p <= 0f) {
            return displayShape.createOutline(size, layoutDirection, density)
        }
        val path = Path()
        morph.toPath(progress = p, path = path, startAngle = startAngle)
        val scaleMatrix = Matrix().apply { scale(x = size.width, y = size.height) }
        path.transform(scaleMatrix)
        path.translate(size.center - path.getBounds().center)
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private class MorphProgressShape(
    private val morph: Morph,
    private val progress: () -> Float,
    private val startAngle: Int,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path()
        morph.toPath(progress = progress(), path = path, startAngle = startAngle)
        val scaleMatrix = Matrix().apply { scale(x = size.width, y = size.height) }
        path.transform(scaleMatrix)
        path.translate(size.center - path.getBounds().center)
        return Outline.Generic(path)
    }
}

fun Modifier.expressiveClickable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    role: Role = Role.Button,
    interactionSource: MutableInteractionSource,
    morphPress: ExpressiveMorphPress,
): Modifier = scale(morphPress.scale)
    .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        role = role,
        onClick = onClick,
    )
