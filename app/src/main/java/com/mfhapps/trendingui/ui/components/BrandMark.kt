package com.mfhapps.trendingui.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.R
import com.mfhapps.trendingui.ui.accessibility.DecorativeImage


@Composable
fun BrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    animated: Boolean = true,
    design: Int = 0,
    contentColor: Color = Color.Unspecified,
    useMonochromeAsset: Boolean = false,
) {
    val scale = if (animated) {
        val transition = rememberInfiniteTransition(label = "brandPulse")
        val value by transition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "brandScale",
        )
        value
    } else {
        1f
    }

    val drawableRes = if (useMonochromeAsset) {
        brandMonochromeDrawable(design)
    } else {
        brandLogoDrawable(design)
    }
    val colorFilter = if (contentColor != Color.Unspecified) {
        ColorFilter.tint(contentColor, BlendMode.SrcIn)
    } else {
        null
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        DecorativeImage(
            painter = painterResource(drawableRes),
            modifier = Modifier
                .scale(scale)
                .size(size),
            colorFilter = colorFilter,
        )
    }
}

fun brandLogoDrawable(design: Int): Int = when (design) {
    1 -> R.drawable.ic_brand_logo_1
    2 -> R.drawable.ic_brand_logo_2
    3 -> R.drawable.ic_brand_logo_3
    4 -> R.drawable.ic_brand_logo_4
    else -> R.drawable.ic_brand_logo
}

fun brandMonochromeDrawable(design: Int): Int = when (design) {
    1 -> R.drawable.ic_brand_logo_monochrome
    2 -> R.drawable.ic_brand_logo_monochrome_2
    3 -> R.drawable.ic_brand_logo_monochrome_3
    4 -> R.drawable.ic_brand_logo_monochrome_4
    else -> R.drawable.ic_brand_logo_monochrome
}
