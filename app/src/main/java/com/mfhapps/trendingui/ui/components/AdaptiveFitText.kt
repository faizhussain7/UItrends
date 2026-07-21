package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

@Composable
fun AdaptiveFitText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current,
    maxLines: Int = 2,
    maxHeight: Dp = Dp.Unspecified,
    minFontSize: TextUnit = 14.sp,
    allowTruncation: Boolean = true,
    overflow: TextOverflow = if (allowTruncation) TextOverflow.Ellipsis else TextOverflow.Visible,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val maxFontSp = if (style.fontSize.isSpecified) style.fontSize.value else 28f
    val minFontSp = minFontSize.value.coerceAtMost(maxFontSp)
    val lineHeightMult = when {
        style.lineHeight.isSpecified && style.fontSize.isSpecified && style.fontSize.value > 0f ->
            style.lineHeight.value / style.fontSize.value
        else -> 1.2f
    }
    val resolvedMaxLines = if (allowTruncation) maxLines else Int.MAX_VALUE

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (allowTruncation && maxHeight != Dp.Unspecified) {
                    Modifier.heightIn(max = maxHeight)
                } else {
                    Modifier
                },
            ),
    ) {
        val widthPx = constraints.maxWidth.coerceAtLeast(1)
        val heightCapPx = when {
            maxHeight != Dp.Unspecified -> with(density) { maxHeight.toPx() }
            allowTruncation && constraints.hasBoundedHeight -> constraints.maxHeight.toFloat()
            allowTruncation ->
                with(density) { (maxFontSp * lineHeightMult).sp.toPx() } *
                    maxLines.coerceAtLeast(1) * 1.15f
            else -> Float.MAX_VALUE
        }
        val fittedSp = remember(
            text,
            widthPx,
            heightCapPx,
            style,
            minFontSp,
            maxFontSp,
            resolvedMaxLines,
        ) {
            fitFontSize(
                textMeasurer = textMeasurer,
                text = text,
                style = style,
                widthPx = widthPx,
                heightCapPx = heightCapPx,
                maxLines = resolvedMaxLines,
                minFontSp = minFontSp,
                maxFontSp = maxFontSp,
                lineHeightMult = lineHeightMult,
            )
        }
        Text(
            text = text,
            color = color,
            style = style.copy(
                fontSize = fittedSp.sp,
                lineHeight = (fittedSp * lineHeightMult).sp,
            ),
            maxLines = resolvedMaxLines,
            softWrap = true,
            overflow = overflow,
        )
    }
}

private fun fitFontSize(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    widthPx: Int,
    heightCapPx: Float,
    maxLines: Int,
    minFontSp: Float,
    maxFontSp: Float,
    lineHeightMult: Float,
): Float {
    fun measure(fontSp: Float): TextLayoutResult = textMeasurer.measure(
        text = text,
        style = style.copy(
            fontSize = fontSp.sp,
            lineHeight = (fontSp * lineHeightMult).sp,
        ),
        constraints = Constraints(maxWidth = widthPx),
        softWrap = true,
    )

    fun fits(result: TextLayoutResult): Boolean =
        !result.didOverflowWidth &&
            result.size.height <= heightCapPx &&
            result.lineCount <= maxLines &&
            !result.hasMidWordBreak(text)

    if (fits(measure(maxFontSp))) return maxFontSp

    var lo = minFontSp
    var hi = maxFontSp
    var best = minFontSp
    repeat(12) {
        if (hi - lo < 0.25f) return@repeat
        val mid = (lo + hi) * 0.5f
        if (fits(measure(mid))) {
            best = mid
            lo = mid
        } else {
            hi = mid
        }
    }
    return best
}

private fun TextLayoutResult.hasMidWordBreak(text: String): Boolean {
    for (line in 0 until lineCount - 1) {
        val breakOffset = getLineEnd(line, visibleEnd = false)
        if (breakOffset <= 0 || breakOffset >= text.length) continue
        val before = text[breakOffset - 1]
        val after = text[breakOffset]
        if (!before.isWhitespace() && before != '-' && !after.isWhitespace()) {
            return true
        }
    }
    return false
}
