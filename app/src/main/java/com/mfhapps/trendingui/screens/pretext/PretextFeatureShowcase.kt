package com.mfhapps.trendingui.screens.pretext

import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import com.mfhapps.trendingui.ui.components.HapticSlider
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import androidx.compose.ui.unit.sp
import com.mfhapps.trendingui.core.text.LayoutRegion
import com.mfhapps.trendingui.core.text.PolygonObstacle
import com.mfhapps.trendingui.core.text.PreparedText
import com.mfhapps.trendingui.core.text.TextMeasurementEngine
import com.mfhapps.trendingui.core.text.TruncationMode
import com.mfhapps.trendingui.ui.components.PretextParagraphCanvas
import com.mfhapps.trendingui.ui.components.PretextPositionedCanvas
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

private const val HEADLINE = "Text measurement that never touches the layout pass."
private const val ARTICLE = (
    "Pretext prepares text once with native Paint, then runs pure-arithmetic layout. " +
        "Reflow around obstacles, predict heights before render, stream AI chat tokens — " +
        "every call is sub-millisecond and safe inside an animation frame."
    )
private val STREAM_TOKENS = listOf(
    "Stream", "ing ", "AI ", "chat ", "tokens ", "with ", "no ", "re-prepare ", "cost. ",
    "Each ", "token ", "uses ", "prepareStreaming() ", "to ", "extend ", "the ", "existing ", "PreparedText.",
)

@Composable
fun PretextFeatureShowcase(
    feature: PretextFeature,
    prepared: PreparedText?,
    typeface: Typeface,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DecorativeIcon(
                    Icons.Outlined.AutoAwesome,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    feature.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            when (feature) {
                PretextFeature.Balance -> BalanceDemo(typeface)
                PretextFeature.Truncate -> TruncateDemo(typeface)
                PretextFeature.FitFont -> FitFontDemo(typeface)
                PretextFeature.Polygon -> PolygonDemo(prepared)
                PretextFeature.Streaming -> StreamingDemo(typeface)
                PretextFeature.ShrinkWrap -> ShrinkWrapDemo(typeface)
            }
        }
    }
}

@Composable
private fun BalanceDemo(typeface: Typeface) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { 22.sp.toPx() }
    val lineHeightPx = with(density) { 28.sp.toPx() }
    val prep = remember(fontSizePx) {
        TextMeasurementEngine.prepareSync(HEADLINE, fontSizePx, typeface)
    }
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
    ) {
        val containerPx = with(density) { maxWidth.toPx().toInt() }
        val natural = remember(prep, containerPx) {
            TextMeasurementEngine.layout(prep, containerPx, lineHeightPx)
        }
        val balanced = remember(prep, containerPx) {
            TextMeasurementEngine.balanceLines(prep, containerPx, lineHeightPx)
        }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            DemoRow(label = "Natural", layout = natural, fontSizePx = fontSizePx, lineHeightPx = lineHeightPx)
            DemoRow(label = "Balanced", layout = balanced, fontSizePx = fontSizePx, lineHeightPx = lineHeightPx)
        }
    }
}

@Composable
private fun TruncateDemo(typeface: Typeface) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { 16.sp.toPx() }
    val lineHeightPx = with(density) { 22.sp.toPx() }
    var maxLines by remember { mutableIntStateOf(2) }
    val prep = remember(fontSizePx) {
        TextMeasurementEngine.prepareSync(ARTICLE, fontSizePx, typeface)
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val widthPx = with(density) { maxWidth.toPx().toInt() }
        val results = remember(prep, widthPx, maxLines) {
            TruncationMode.entries.associateWith {
                TextMeasurementEngine.truncateText(prep, widthPx, lineHeightPx, maxLines, mode = it)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Max lines: $maxLines",
                style = MaterialTheme.typography.labelLarge,
            )
            HapticSlider(
                value = maxLines.toFloat(),
                onValueChange = { maxLines = it.toInt() },
                valueRange = 1f..4f,
                steps = 2,
            )
            results.forEach { (mode, res) ->
                val accent = MaterialTheme.colorScheme.primary
                Column {
                    Text(
                        mode.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                    )
                    Text(
                        res.text,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun FitFontDemo(typeface: Typeface) {
    val density = LocalDensity.current
    var heightDp by remember { mutableFloatStateOf(72f) }
    val maxHeightPx = with(density) { heightDp.dp.toPx() }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Container height: ${heightDp.toInt()}dp", style = MaterialTheme.typography.labelLarge)
        HapticSlider(
            value = heightDp,
            onValueChange = { heightDp = it },
            valueRange = 48f..200f,
        )
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val widthPx = with(density) { maxWidth.toPx().toInt() }
            val fit = remember(widthPx, maxHeightPx) {
                TextMeasurementEngine.fitFontSize(
                    text = HEADLINE,
                    typeface = typeface,
                    maxWidthPx = widthPx,
                    maxHeightPx = maxHeightPx,
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(heightDp.dp),
            ) {
                PretextParagraphCanvas(
                    layout = fit.layout,
                    fontSizePx = fit.fontSizePx,
                    lineHeightPx = fit.fontSizePx * 1.25f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                "${"%.1f".format(with(density) { fit.fontSizePx.toDp().value })}sp · ${fit.layout.lineCount} lines",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun PolygonDemo(prepared: PreparedText?) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { 14.sp.toPx() }
    val lineHeightPx = with(density) { 19.sp.toPx() }
    BoxWithConstraints(Modifier.fillMaxWidth().height(240.dp)) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val starPolygon = remember(widthPx, heightPx) {
            val cx = widthPx * 0.5f
            val cy = heightPx * 0.5f
            val outer = kotlin.math.min(widthPx, heightPx) * 0.32f
            val inner = outer * 0.45f
            val points = (0 until 10).map { i ->
                val angle = (i / 10f) * 2f * PI.toFloat() - PI.toFloat() / 2f
                val r = if (i % 2 == 0) outer else inner
                Pair(cx + r * cos(angle), cy + r * sin(angle))
            }
            PolygonObstacle(points)
        }
        val layout = remember(prepared, widthPx, heightPx, starPolygon) {
            prepared?.let {
                TextMeasurementEngine.layoutColumn(
                    prepared = it,
                    region = LayoutRegion(8f, 8f, widthPx - 16f, heightPx - 16f),
                    lineHeightPx = lineHeightPx,
                    polygonObstacles = listOf(starPolygon),
                )
            }
        }
        val outlineColor = MaterialTheme.colorScheme.primary
        Canvas(Modifier.fillMaxWidth().height(240.dp)) {
            val path = Path()
            starPolygon.points.forEachIndexed { i, (x, y) ->
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = outlineColor.copy(alpha = 0.16f))
            drawPath(path, color = outlineColor.copy(alpha = 0.9f), style = Stroke(2f))
        }
        if (layout != null) {
            PretextPositionedCanvas(
                layout = layout,
                fontSizePx = fontSizePx,
                lineHeightPx = lineHeightPx,
                textColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().height(240.dp),
            )
        }
    }
}

@Composable
private fun ShrinkWrapDemo(typeface: Typeface) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { 15.sp.toPx() }
    val lineHeightPx = with(density) { 21.sp.toPx() }
    val sample =
        "Shrink-wrap measures the tightest width for a paragraph, then reflows with layoutDynamic() " +
            "when each line gets a different max width — the same pattern as chenglou/pretext demos."
    val prep = remember(fontSizePx) {
        TextMeasurementEngine.prepareSync(sample, fontSizePx, typeface)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(200.dp)) {
            val widthPx = with(density) { maxWidth.toPx() }
            val stats = remember(prep, widthPx) {
                TextMeasurementEngine.measureLineStats(prep, widthPx)
            }
            val tightWidth = remember(prep, widthPx, stats.lineCount) {
                TextMeasurementEngine.findTightestWidth(
                    prepared = prep,
                    targetLines = stats.lineCount.coerceAtLeast(1),
                    lineHeightPx = lineHeightPx,
                    minWidthPx = 120,
                    maxWidthPx = widthPx.toInt(),
                ).first
            }
            val layout = remember(prep, widthPx, tightWidth) {
                TextMeasurementEngine.layoutDynamic(
                    prepared = prep,
                    region = LayoutRegion(0f, 0f, widthPx, with(density) { 200.dp.toPx() }),
                    lineHeightPx = lineHeightPx,
                    maxWidthAtLineTop = { lineTop ->
                        val t = (lineTop / lineHeightPx).toInt()
                        when (t % 4) {
                            0 -> tightWidth.toFloat()
                            1 -> widthPx * 0.72f
                            2 -> widthPx * 0.55f
                            else -> widthPx
                        }
                    },
                )
            }
            PretextPositionedCanvas(
                layout = layout,
                fontSizePx = fontSizePx,
                lineHeightPx = lineHeightPx,
                textColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().height(200.dp),
            )
            Text(
                "${stats.lineCount} lines · max ${stats.maxLineWidth.toInt()}px · tight ${tightWidth}px",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.BottomStart),
            )
        }
    }
}

@Composable
private fun StreamingDemo(typeface: Typeface) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { 15.sp.toPx() }
    val lineHeightPx = with(density) { 21.sp.toPx() }
    var tokenIndex by remember { mutableIntStateOf(0) }
    var prep by remember { mutableStateOf<PreparedText?>(null) }

    LaunchedEffect(fontSizePx) {
        prep = TextMeasurementEngine.prepareSync("", fontSizePx, typeface)
        while (true) {
            val target = STREAM_TOKENS[tokenIndex % STREAM_TOKENS.size]
            prep = prep?.let {
                TextMeasurementEngine.prepareStreaming(it, target, fontSizePx, typeface)
            }
            tokenIndex++
            delay(160)
            if (tokenIndex % STREAM_TOKENS.size == 0) {
                prep = TextMeasurementEngine.prepareSync("", fontSizePx, typeface)
                delay(800)
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val widthPx = with(density) { maxWidth.toPx().toInt() }
        val layout = prep?.let { TextMeasurementEngine.layout(it, widthPx, lineHeightPx) }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Tokens streamed: ${tokenIndex.coerceAtMost(STREAM_TOKENS.size)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (layout != null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(with(density) { layout.height.toDp().coerceAtLeast(80.dp) }),
                ) {
                    PretextParagraphCanvas(
                        layout = layout,
                        fontSizePx = fontSizePx,
                        lineHeightPx = lineHeightPx,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoRow(
    label: String,
    layout: com.mfhapps.trendingui.core.text.MeasuredTextLayout,
    fontSizePx: Float,
    lineHeightPx: Float,
) {
    val density = LocalDensity.current
    Column {
        Text(
            "$label · ${layout.lineCount} lines",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(with(density) { layout.height.toDp().coerceAtLeast(48.dp) }),
        ) {
            PretextParagraphCanvas(
                layout = layout,
                fontSizePx = fontSizePx,
                lineHeightPx = lineHeightPx,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
