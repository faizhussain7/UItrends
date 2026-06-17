package com.mfhapps.trendingui.ui.neumorphism

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val NeuSheetTopShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private val NeuChromePressSpring = spring<Float>(dampingRatio = 0.72f, stiffness = 420f)
private val NeuChromeCorner = 22.dp

@Immutable
data class NeuShadows(
    val surface: Color,
    val highlight: Color,
    val shade: Color,
    val stroke: Color,
)

@Composable
fun rememberNeuShadows(): NeuShadows {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.45f
    return remember(scheme) {
        NeuShadows(
            surface = scheme.surfaceContainerLow,
            highlight = if (dark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.82f),
            shade = if (dark) Color.Black.copy(alpha = 0.62f) else Color.Black.copy(alpha = 0.26f),
            stroke = scheme.outlineVariant.copy(alpha = if (dark) 0.4f else 0.7f),
        )
    }
}

@Composable
fun NeuChromeIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconTint: Color = MaterialTheme.colorScheme.primary,
) {
    val neu = rememberNeuShadows()
    var pressedTarget by remember { mutableFloatStateOf(0f) }
    val pressAnim by animateFloatAsState(pressedTarget, NeuChromePressSpring, label = "neuChromePress")
    val elevation by animateFloatAsState(1f - pressAnim * 0.5f, NeuChromePressSpring, label = "neuChromeElev")
    val haptics = LocalHapticFeedback.current
    val cornerPx = with(LocalDensity.current) { (NeuChromeCorner).toPx() }

    NeuChromeRaised(
        modifier = modifier.size(size),
        shape = CircleShape,
        neu = neu,
        pressAnim = pressAnim,
        elevation = elevation,
        cornerPx = cornerPx,
        shadowInset = 6.dp,
        onPressChange = { down ->
            pressedTarget = if (down) 1f else 0f
            if (down) haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
        onClick = onClick,
        contentDescription = contentDescription,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = iconTint,
        )
    }
}

@Composable
fun NeuSheetDragHandle(modifier: Modifier = Modifier) {
    val neu = rememberNeuShadows()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .width(48.dp)
                .padding(vertical = 3.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, neu.stroke, RoundedCornerShape(4.dp))
                .background(neu.surface)
                .padding(vertical = 2.dp),
        )
    }
}

@Composable
fun NeuGuideSheetFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val neu = rememberNeuShadows()
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(shape)
            .border(1.dp, neu.stroke, shape)
            .background(neu.surface)
            .padding(12.dp),
    ) {
        content()
    }
}

@Composable
private fun NeuChromeRaised(
    modifier: Modifier,
    shape: Shape,
    neu: NeuShadows,
    pressAnim: Float,
    elevation: Float,
    cornerPx: Float,
    shadowInset: Dp,
    onPressChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    val shadowPaint = remember {
        android.graphics.Paint().apply { isAntiAlias = true }
    }

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(shape)
            .border(1.dp, neu.stroke, shape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressChange(true)
                        val released = tryAwaitRelease()
                        onPressChange(false)
                        if (released) onClick()
                    },
                )
            }
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
    ) {
        NeuShadowCanvas(
            pressAnim = pressAnim,
            elevation = elevation,
            cornerPx = cornerPx,
            shadowInset = shadowInset,
            surface = neu.surface,
            highlight = neu.highlight,
            shade = neu.shade,
            shadowPaint = shadowPaint,
        )
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = 0.92f + elevation * 0.08f },
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun NeuShadowCanvas(
    pressAnim: Float,
    elevation: Float,
    cornerPx: Float,
    shadowInset: Dp,
    surface: Color,
    highlight: Color,
    shade: Color,
    shadowPaint: android.graphics.Paint,
) {
    val density = LocalDensity.current
    val pad = with(density) { shadowInset.toPx() }
    Canvas(Modifier.fillMaxSize()) {
        val inset = pressAnim
        val spread = 6f + elevation * 4f
        val blur = 10f + elevation * 6f
        shadowPaint.maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)

        val lightOffset = Offset(-spread + spread * 2f * inset, -spread + spread * 2f * inset)
        val darkOffset = Offset(spread - spread * 2f * inset, spread - spread * 2f * inset)
        val rect = androidx.compose.ui.geometry.Rect(
            Offset(pad, pad),
            Size(size.width - pad * 2f, size.height - pad * 2f),
        )

        drawIntoCanvas { canvas ->
            shadowPaint.color = highlight.toArgb()
            canvas.nativeCanvas.drawRoundRect(
                rect.left + lightOffset.x,
                rect.top + lightOffset.y,
                rect.right + lightOffset.x,
                rect.bottom + lightOffset.y,
                cornerPx,
                cornerPx,
                shadowPaint,
            )
            shadowPaint.color = shade.toArgb()
            canvas.nativeCanvas.drawRoundRect(
                rect.left + darkOffset.x,
                rect.top + darkOffset.y,
                rect.right + darkOffset.x,
                rect.bottom + darkOffset.y,
                cornerPx,
                cornerPx,
                shadowPaint,
            )
        }
        drawRoundRect(
            color = surface,
            topLeft = Offset(pad, pad),
            size = Size(size.width - pad * 2f, size.height - pad * 2f),
            cornerRadius = CornerRadius(cornerPx, cornerPx),
        )
    }
}
