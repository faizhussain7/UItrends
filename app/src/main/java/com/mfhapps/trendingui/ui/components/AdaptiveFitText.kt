package com.mfhapps.trendingui.ui.components

import android.graphics.Typeface
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import com.mfhapps.trendingui.core.text.TextMeasurementEngine

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
    val baseFontPx = with(density) {
        if (style.fontSize.isSpecified) style.fontSize.toPx() else 28.sp.toPx()
    }
    val minFontPx = with(density) { minFontSize.toPx() }
    val lineHeightMult = when {
        style.lineHeight.isSpecified && style.fontSize.isSpecified && style.fontSize.value > 0f ->
            style.lineHeight.value / style.fontSize.value
        else -> 1.2f
    }
    val typeface = remember(style.fontWeight) {
        when {
            (style.fontWeight?.weight ?: FontWeight.Normal.weight) >= FontWeight.Bold.weight ->
                Typeface.DEFAULT_BOLD
            (style.fontWeight?.weight ?: FontWeight.Normal.weight) >= FontWeight.Medium.weight ->
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            else -> Typeface.DEFAULT
        }
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
            !allowTruncation -> with(density) { 480.dp.toPx() }
            maxHeight != Dp.Unspecified -> with(density) { maxHeight.toPx() }
            constraints.hasBoundedHeight -> constraints.maxHeight.toFloat()
            else -> baseFontPx * lineHeightMult * maxLines.coerceAtLeast(1) * 1.15f
        }
        val fit = remember(
            text,
            widthPx,
            heightCapPx,
            baseFontPx,
            minFontPx,
            typeface,
            lineHeightMult,
            allowTruncation,
        ) {
            TextMeasurementEngine.fitFontSize(
                text = text,
                typeface = typeface,
                maxWidthPx = widthPx,
                maxHeightPx = heightCapPx,
                lineHeightMultiplier = lineHeightMult,
                minFontSizePx = minFontPx,
                maxFontSizePx = baseFontPx,
            )
        }
        val fittedSp = with(density) { fit.fontSizePx.toSp() }
        Text(
            text = text,
            color = color,
            style = style.copy(
                fontSize = fittedSp,
                lineHeight = fittedSp * lineHeightMult,
            ),
            maxLines = resolvedMaxLines,
            softWrap = true,
            overflow = overflow,
        )
    }
}
