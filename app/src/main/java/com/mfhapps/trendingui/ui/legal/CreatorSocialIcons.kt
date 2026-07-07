package com.mfhapps.trendingui.ui.legal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.mfhapps.trendingui.legal.CreatorSocialAssets

enum class CreatorSocialBrand {
    Gmail,
    GitHub,
    LinkedIn,
    X,
    WhatsApp,
}

private fun CreatorSocialBrand.assetPath(onDarkSurface: Boolean): String = when (this) {
    CreatorSocialBrand.Gmail -> CreatorSocialAssets.GMAIL
    CreatorSocialBrand.GitHub -> if (onDarkSurface) CreatorSocialAssets.GITHUB_ON_DARK else CreatorSocialAssets.GITHUB
    CreatorSocialBrand.LinkedIn -> CreatorSocialAssets.LINKEDIN
    CreatorSocialBrand.X -> if (onDarkSurface) CreatorSocialAssets.X_ON_DARK else CreatorSocialAssets.X
    CreatorSocialBrand.WhatsApp -> CreatorSocialAssets.WHATSAPP
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CreatorBrandIcon(
    brand: CreatorSocialBrand,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialShapes.Gem.toShape(),
    background: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    iconSize: Dp = 22.dp,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val iconPixels = with(density) { iconSize.roundToPx() }
    val onDarkSurface = background.luminance() < 0.45f
    val assetPath = brand.assetPath(onDarkSurface)

    Surface(
        modifier = modifier,
        shape = shape,
        color = background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(CreatorSocialAssets.androidAssetUri(assetPath))
                    .decoderFactory(SvgDecoder.Factory())
                    .size(iconPixels)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
fun CreatorWhatsAppBrandIcon(
    modifier: Modifier = Modifier,
) {
    CreatorBrandIcon(
        brand = CreatorSocialBrand.WhatsApp,
        modifier = modifier,
        shape = CircleShape,
        background = MaterialTheme.colorScheme.tertiaryContainer,
    )
}
