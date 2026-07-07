package com.mfhapps.trendingui.screens.neumorphism

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.demo.DemoAnimatedSection
import com.mfhapps.trendingui.ui.detail.DemoCollapsingScrollScaffold
import com.mfhapps.trendingui.ui.detail.DetailChromeStyle
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import com.mfhapps.trendingui.ui.neumorphism.NeuShadows
import com.mfhapps.trendingui.ui.neumorphism.rememberNeuShadows

private const val NeuScreenTitle = "Neumorphism"
private const val NeuScreenSubtitle = "Soft extruded surfaces with animated press depth"

private val NeuPressSpring = spring<Float>(dampingRatio = 0.72f, stiffness = 420f)
private val NeuToggleSpring = spring<Float>(dampingRatio = 0.62f, stiffness = 360f)
private val NeuValueSpring = spring<Float>(dampingRatio = 0.8f, stiffness = 520f)

private val NeuRaisedCorner = 24.dp
private val NeuRaisedShape = RoundedCornerShape(NeuRaisedCorner)
private val NeuRaisedShadowInset = 8.dp
private val NeuRaisedContentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)

@Composable
fun NeumorphismScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val neu = rememberNeuShadows()
    val reduceMotion = LocalReduceMotion.current
    val infinite = rememberInfiniteTransition(label = "neuBg")
    val bgPulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "neuBgPulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            lerp(neu.surface, scheme.primaryContainer, bgPulse * 0.12f),
                            neu.surface,
                            scheme.surfaceContainerHighest.copy(alpha = 0.35f),
                        ),
                        center = Offset(size.width * 0.3f, 0f),
                        radius = size.maxDimension,
                    ),
                )
            },
    ) {
        DemoCollapsingScrollScaffold(
            title = NeuScreenTitle,
            subtitle = NeuScreenSubtitle,
            chromeStyle = DetailChromeStyle.Neumorphism,
            onNavigateBack = onNavigateBack,
            guide = guide,
            verticalSpacing = 20.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                DemoAnimatedSection(index = 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        NeuSection(title = "Raised card", body = "Tap — shadows invert with a spring.")
                        NeuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(128.dp)
                                .semantics { contentDescription = "Neumorphic card" },
                            neu = neu,
                        ) {
                            Text(
                                text = "Soft UI card",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.titleMedium,
                                color = scheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "Dual blur shadows · spring press",
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                DemoAnimatedSection(index = 2) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NeuSection(title = "Toggle & slider", body = "Thumb and fill animate with spring physics.")
                NeuToggle(
                    modifier = Modifier
                        .size(140.dp, 58.dp)
                        .semantics {
                            role = Role.Switch
                            contentDescription = "Neumorphic toggle"
                        },
                    neu = neu,
                )
                NeuSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    neu = neu,
                )
            }
        }

        DemoAnimatedSection(index = 3) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NeuSection(title = "Raised button", body = "Same raised surface and corners as the card above.")
                NeuButton(
                    label = "Continue",
                    modifier = Modifier.fillMaxWidth(),
                    neu = neu,
                )
            }
        }

        DemoAnimatedSection(index = 4) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NeuSection(title = "Inset field", body = "Concave tray — inner shadows only.")
                NeuInsetPanel(modifier = Modifier.fillMaxWidth(), neu = neu) {
                    Text(
                        text = "Search patterns…",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        DemoAnimatedSection(index = 5) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NeuSection(title = "Icon cluster", body = "Circular extruded actions.")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    NeuIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Add",
                        neu = neu,
                        modifier = Modifier.size(48.dp),
                    )
                    NeuIconButton(
                        icon = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        neu = neu,
                        filled = true,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
        }

        DemoAnimatedSection(index = 6) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NeuSection(title = "Metric tiles", body = "Dashboard chips with press feedback.")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    NeuMetricTile(label = "Focus", value = "86%", modifier = Modifier.weight(1f), neu = neu)
                    NeuMetricTile(label = "Calm", value = "12m", modifier = Modifier.weight(1f), neu = neu)
                }
            }
        }

                DemoAnimatedSection(index = 7) {
                    NeuProgressRing(progress = 0.72f, neu = neu, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun NeuSection(title: String, body: String) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = scheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NeuCard(
    modifier: Modifier = Modifier,
    neu: NeuShadows = rememberNeuShadows(),
    content: @Composable () -> Unit = {},
) {
    var pressedTarget by remember { mutableFloatStateOf(0f) }
    val pressAnim by animateFloatAsState(
        targetValue = pressedTarget,
        animationSpec = NeuPressSpring,
        label = "neuPress",
    )
    val haptics = LocalHapticFeedback.current
    val cornerPx = with(LocalDensity.current) { NeuRaisedCorner.toPx() }
    val elevation by animateFloatAsState(
        targetValue = 1f - pressAnim * 0.55f,
        animationSpec = NeuPressSpring,
        label = "neuElev",
    )

    NeuSurface(
        modifier = modifier,
        shape = NeuRaisedShape,
        neu = neu,
        pressAnim = pressAnim,
        elevation = elevation,
        cornerPx = cornerPx,
        shadowInset = NeuRaisedShadowInset,
        contentPadding = NeuRaisedContentPadding,
        contentAlignment = Alignment.Center,
        onPressChange = { down ->
            pressedTarget = if (down) 1f else 0f
            if (down) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun NeuSurface(
    modifier: Modifier,
    shape: Shape,
    neu: NeuShadows,
    pressAnim: Float,
    elevation: Float,
    cornerPx: Float,
    shadowInset: Dp,
    contentPadding: PaddingValues,
    contentAlignment: Alignment = Alignment.Center,
    onPressChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    val shadowPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
        }
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
                        tryAwaitRelease()
                        onPressChange(false)
                    },
                )
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
                .padding(contentPadding)
                .graphicsLayer { alpha = 0.92f + elevation * 0.08f },
            contentAlignment = contentAlignment,
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

@Composable
private fun NeuButton(
    label: String,
    neu: NeuShadows,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    var pressedTarget by remember { mutableFloatStateOf(0f) }
    val pressAnim by animateFloatAsState(pressedTarget, NeuPressSpring, label = "neuBtn")
    val elevation by animateFloatAsState(1f - pressAnim * 0.55f, NeuPressSpring, label = "neuBtnElev")
    val haptics = LocalHapticFeedback.current

    val cornerPx = with(LocalDensity.current) { NeuRaisedCorner.toPx() }

    NeuSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = NeuRaisedShape,
        neu = neu,
        pressAnim = pressAnim,
        elevation = elevation,
        cornerPx = cornerPx,
        shadowInset = NeuRaisedShadowInset,
        contentPadding = NeuRaisedContentPadding,
        contentAlignment = Alignment.Center,
        onPressChange = { down ->
            pressedTarget = if (down) 1f else 0f
            if (down) haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleMedium,
            color = scheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NeuToggle(
    modifier: Modifier = Modifier,
    neu: NeuShadows = rememberNeuShadows(),
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(28.dp)
    var on by remember { mutableStateOf(false) }
    val thumbProgress by animateFloatAsState(
        targetValue = if (on) 1f else 0f,
        animationSpec = NeuToggleSpring,
        label = "toggle",
    )
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(shape)
            .border(1.dp, neu.stroke, shape)
            .toggleable(value = on, role = Role.Switch, onValueChange = {
                on = it
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            })
            .semantics {
                role = Role.Switch
                contentDescription = "Neumorphic toggle"
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(neu.surface, cornerRadius = CornerRadius(28f, 28f))
            drawRoundRect(
                color = neu.shade.copy(alpha = 0.12f),
                topLeft = Offset(6f, 6f),
                size = Size(size.width - 12f, size.height - 12f),
                cornerRadius = CornerRadius(24f, 24f),
            )
            val thumbRadius = size.height * 0.34f
            val thumbX = size.width * (0.2f + 0.6f * thumbProgress)
            val center = Offset(thumbX, size.height / 2f)
            drawCircle(neu.shade.copy(alpha = 0.4f), thumbRadius, center + Offset(3f, 3f))
            drawCircle(
                color = if (on) scheme.primary.copy(alpha = 0.82f) else neu.surface,
                radius = thumbRadius,
                center = center,
            )
            drawCircle(neu.highlight.copy(alpha = 0.45f), thumbRadius * 0.55f, center + Offset(-4f, -4f))
        }
    }
}

@Composable
private fun NeuSlider(
    modifier: Modifier = Modifier,
    neu: NeuShadows = rememberNeuShadows(),
) {
    val scheme = MaterialTheme.colorScheme
    var value by remember { mutableFloatStateOf(0.4f) }
    val displayValue by animateFloatAsState(value, NeuValueSpring, label = "neuSlider")
    val shape = RoundedCornerShape(18.dp)
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .semantics {
                contentDescription = "Neumorphic slider"
                progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f)
                setProgress { target ->
                    value = target.coerceIn(0f, 1f)
                    true
                }
            }
            .minimumInteractiveComponentSize()
            .clip(shape)
            .border(1.dp, neu.stroke, shape),
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        value = (offset.x / size.width).coerceIn(0f, 1f)
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        value = (value + dragAmount.x / size.width).coerceIn(0f, 1f)
                    }
                },
        ) {
            drawRoundRect(neu.surface, cornerRadius = CornerRadius(18f, 18f))
            val trackY = size.height / 2f
            val startX = 28f
            val endX = size.width - 28f
            drawRoundRect(
                color = neu.shade.copy(alpha = 0.14f),
                topLeft = Offset(startX, trackY - 5f),
                size = Size((endX - startX) * displayValue, 10f),
                cornerRadius = CornerRadius(5f, 5f),
            )
            drawLine(
                color = neu.shade.copy(alpha = 0.22f),
                start = Offset(startX, trackY),
                end = Offset(endX, trackY),
                strokeWidth = 5f,
            )
            val thumbCenter = Offset(startX + (endX - startX) * displayValue, trackY)
            drawCircle(neu.shade.copy(alpha = 0.45f), 22f, thumbCenter + Offset(3f, 3f))
            drawCircle(scheme.primary.copy(alpha = 0.85f), 20f, thumbCenter)
            drawCircle(neu.highlight.copy(alpha = 0.55f), 11f, thumbCenter + Offset(-5f, -5f))
        }
    }
}

@Composable
private fun NeuInsetPanel(
    neu: NeuShadows,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .border(1.dp, neu.stroke, shape)
            .drawBehind {
                val corner = 12.dp.toPx()
                drawRoundRect(
                    color = neu.shade.copy(alpha = 0.2f),
                    cornerRadius = CornerRadius(corner, corner),
                )
                val inset = 3.dp.toPx()
                drawRoundRect(
                    color = neu.surface,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2f, size.height - inset * 2f),
                    cornerRadius = CornerRadius(corner - inset, corner - inset),
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
    }
}

@Composable
private fun NeuIconButton(
    icon: ImageVector,
    contentDescription: String,
    neu: NeuShadows,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    var pressedTarget by remember { mutableFloatStateOf(0f) }
    val pressAnim by animateFloatAsState(pressedTarget, NeuPressSpring, label = "neuIcon")
    val elevation by animateFloatAsState(1f - pressAnim * 0.5f, NeuPressSpring, label = "neuIconElev")
    val haptics = LocalHapticFeedback.current
    val cornerPx = with(LocalDensity.current) { 24.dp.toPx() }

    NeuSurface(
        modifier = modifier,
        shape = CircleShape,
        neu = neu.copy(surface = if (filled) scheme.primaryContainer else neu.surface),
        pressAnim = pressAnim,
        elevation = elevation,
        cornerPx = cornerPx,
        shadowInset = 6.dp,
        contentPadding = PaddingValues(0.dp),
        onPressChange = { down ->
            pressedTarget = if (down) 1f else 0f
            if (down) haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = if (filled) scheme.onPrimaryContainer else scheme.primary,
        )
    }
}

@Composable
private fun NeuMetricTile(
    label: String,
    value: String,
    neu: NeuShadows,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    NeuCard(modifier = modifier.height(92.dp), neu = neu) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = scheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NeuProgressRing(
    progress: Float,
    neu: NeuShadows,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val animated by animateFloatAsState(progress, NeuValueSpring, label = "ring")
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 12f
                drawCircle(neu.shade.copy(alpha = 0.2f), size.minDimension / 2f - stroke, center)
                drawArc(
                    color = scheme.primary.copy(alpha = 0.85f),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(stroke, stroke),
                    size = Size(size.width - stroke * 2f, size.height - stroke * 2f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            Text(
                text = "${(animated * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                color = scheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = "Animated progress ring",
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
