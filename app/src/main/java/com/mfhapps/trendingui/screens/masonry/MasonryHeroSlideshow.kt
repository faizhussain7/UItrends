package com.mfhapps.trendingui.screens.masonry

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs

internal fun masonryHeroImageUrl(seed: String): String =
    "https://picsum.photos/seed/$seed/1600/1000"

internal val HeroImageSeeds = listOf(
    "masonry-hero-a",
    "masonry-hero-b",
    "masonry-hero-c",
    "masonry-hero-d",
    "masonry-hero-e",
    "masonry-hero-f",
)

internal const val HeroHoldDurationMs = 5_200L
internal const val HeroCrossfadeDurationMs = 1_400
internal val HeroCrossfadeEasing = FastOutSlowInEasing

@Immutable
internal data class MasonryHeroBackgroundLayers(
    val outgoing: Brush,
    val incoming: Brush,
    val progress: Float,
)

@Stable
internal data class MasonryHeroLoopState(
    val displayedIndex: Int,
    val overlayIndex: Int?,
    val crossfadeProgress: Float,
    val kenBurnsScale: Float,
    val palettes: List<List<Color>>,
    val background: MasonryHeroBackgroundLayers,
)

private fun ColorScheme.fallbackPalette(): List<Color> = listOf(
    primary,
    secondary,
    tertiary,
    surfaceContainerLow,
    background,
)

private fun samplePalette(bitmap: Bitmap, scheme: ColorScheme): List<Color> {
    val sampleSize = 16
    val scaled = bitmap.scale(sampleSize, sampleSize)
    val pixels = buildList {
        for (y in 0 until sampleSize step 2) {
            for (x in 0 until sampleSize step 2) {
                add(Color(scaled[x, y]).copy(alpha = 1f))
            }
        }
    }
    val ranked = pixels
        .distinctBy { (it.red * 255).toInt() shl 16 or (it.green * 255).toInt() shl 8 or (it.blue * 255).toInt() }
        .sortedByDescending { color ->
            val lum = color.luminance()
            val saturation = maxOf(color.red, color.green, color.blue) -
                minOf(color.red, color.green, color.blue)
            saturation * (1f - abs(lum - 0.48f))
        }

    val primary = ranked.getOrElse(0) { scheme.primary }
    val secondary = ranked.getOrElse(1) { lerp(primary, scheme.secondary, 0.45f) }
    val tertiary = ranked.getOrElse(2) { lerp(primary, scheme.tertiary, 0.55f) }
    return listOf(primary, secondary, tertiary, scheme.surfaceContainerLow, scheme.background)
}

private suspend fun loadPalette(
    context: Context,
    seed: String,
    scheme: ColorScheme,
): List<Color> {
    val request = ImageRequest.Builder(context)
        .data(masonryHeroImageUrl(seed))
        .allowHardware(false)
        .crossfade(false)
        .size(128)
        .build()

    return when (val result = context.imageLoader.execute(request)) {
        is SuccessResult -> samplePalette(result.drawable.toBitmap(), scheme)
        else -> scheme.fallbackPalette()
    }
}

private fun preloadHeroImage(context: Context, seed: String) {
    context.imageLoader.enqueue(
        ImageRequest.Builder(context)
            .data(masonryHeroImageUrl(seed))
            .crossfade(false)
            .size(1280)
            .build(),
    )
}

private fun buildGradientBrush(
    palette: List<Color>,
    wave: Float,
    drift: Float,
    scheme: ColorScheme,
): Brush {
    val c0 = palette.getOrElse(0) { scheme.primary }
    val c1 = palette.getOrElse(1) { scheme.secondary }
    val c2 = palette.getOrElse(2) { scheme.tertiary }
    val c3 = palette.getOrElse(3) { scheme.surfaceContainerLow }
    val c4 = palette.getOrElse(4) { scheme.background }

    return Brush.linearGradient(
        colors = listOf(
            lerp(c0, c1, wave * 0.32f),
            lerp(c1, c2, drift * 0.38f),
            lerp(c2, c0, (wave + drift) * 0.18f),
            c3,
            c4,
        ),
        start = Offset.Zero,
        end = Offset(920f + drift * 420f, 2100f + wave * 360f),
    )
}

@Composable
internal fun rememberMasonryHeroLoopState(
    colorScheme: ColorScheme,
    reduceMotion: Boolean,
): MasonryHeroLoopState {
    val context = LocalContext.current
    val fallbackPalettes = remember(colorScheme) {
        List(HeroImageSeeds.size) { colorScheme.fallbackPalette() }
    }

    var palettes by remember { mutableStateOf(fallbackPalettes) }
    var displayedIndex by remember { mutableIntStateOf(0) }
    var overlayIndex by remember { mutableStateOf<Int?>(null) }
    val crossfade = remember { Animatable(1f) }
    val kenBurns = remember { Animatable(1f) }

    LaunchedEffect(colorScheme) {
        palettes = coroutineScope {
            HeroImageSeeds.mapIndexed { index, seed ->
                async { loadPalette(context, seed, colorScheme) }
            }.awaitAll()
        }
        HeroImageSeeds.forEach { preloadHeroImage(context, it) }
    }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) {
            kenBurns.snapTo(1f)
            return@LaunchedEffect
        }
        while (isActive) {
            kenBurns.snapTo(1f)
            kenBurns.animateTo(
                targetValue = 1.06f,
                animationSpec = tween(
                    durationMillis = HeroHoldDurationMs.toInt(),
                    easing = HeroCrossfadeEasing,
                ),
            )
        }
    }

    LaunchedEffect(reduceMotion, colorScheme) {
        if (reduceMotion) return@LaunchedEffect
        var index = displayedIndex
        while (isActive) {
            delay(HeroHoldDurationMs)
            val nextIndex = (index + 1) % HeroImageSeeds.size
            val preloadAfter = (nextIndex + 1) % HeroImageSeeds.size
            preloadHeroImage(context, HeroImageSeeds[nextIndex])
            preloadHeroImage(context, HeroImageSeeds[preloadAfter])

            overlayIndex = nextIndex
            crossfade.snapTo(0f)
            crossfade.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = HeroCrossfadeDurationMs,
                    easing = HeroCrossfadeEasing,
                ),
            )
            displayedIndex = nextIndex
            overlayIndex = null
            crossfade.snapTo(1f)
            index = nextIndex
        }
    }

    val infinite = rememberInfiniteTransition(label = "masonryHeroBgMotion")
    val wave by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = HeroCrossfadeEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave",
    )
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )

    val progress = crossfade.value
    val fromIndex = overlayIndex ?: displayedIndex
    val toIndex = overlayIndex ?: displayedIndex
    val background = MasonryHeroBackgroundLayers(
        outgoing = buildGradientBrush(palettes[fromIndex], wave, drift, colorScheme),
        incoming = buildGradientBrush(palettes[toIndex], wave, drift, colorScheme),
        progress = if (overlayIndex != null) progress else 1f,
    )

    return MasonryHeroLoopState(
        displayedIndex = displayedIndex,
        overlayIndex = overlayIndex,
        crossfadeProgress = progress,
        kenBurnsScale = if (reduceMotion) 1f else kenBurns.value,
        palettes = palettes,
        background = background,
    )
}

@Composable
internal fun MasonryHeroSlideshow(
    state: MasonryHeroLoopState,
    parallaxOffset: Float,
    scrollImageScale: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val kenBurns = state.kenBurnsScale
    val overlay = state.overlayIndex
    val progress = state.crossfadeProgress

    Box(modifier = modifier) {
        MasonryHeroPhoto(
            context = context,
            seed = HeroImageSeeds[state.displayedIndex],
            parallaxOffset = parallaxOffset,
            scrollImageScale = scrollImageScale,
            kenBurnsScale = kenBurns,
            alpha = if (overlay != null) 1f - progress else 1f,
        )
        if (overlay != null) {
            MasonryHeroPhoto(
                context = context,
                seed = HeroImageSeeds[overlay],
                parallaxOffset = parallaxOffset,
                scrollImageScale = scrollImageScale,
                kenBurnsScale = kenBurns,
                alpha = progress,
            )
        }
    }
}

@Composable
private fun MasonryHeroPhoto(
    context: Context,
    seed: String,
    parallaxOffset: Float,
    scrollImageScale: Float,
    kenBurnsScale: Float,
    alpha: Float,
) {
    val request = remember(seed) {
        ImageRequest.Builder(context)
            .data(masonryHeroImageUrl(seed))
            .crossfade(false)
            .size(Size.ORIGINAL)
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scrollImageScale * kenBurnsScale
                scaleY = scrollImageScale * kenBurnsScale
                translationY = -parallaxOffset * 0.45f
            },
        contentScale = ContentScale.Crop,
    )
}

@Composable
internal fun rememberMasonryDetailBackground(
    imageSeed: String,
    colorScheme: ColorScheme,
    reduceMotion: Boolean,
): MasonryHeroBackgroundLayers {
    val context = LocalContext.current
    var palette by remember(imageSeed, colorScheme) {
        mutableStateOf(colorScheme.fallbackPalette())
    }

    LaunchedEffect(imageSeed, colorScheme) {
        palette = loadPalette(context, imageSeed, colorScheme)
        preloadHeroImage(context, imageSeed)
    }

    val infinite = rememberInfiniteTransition(label = "masonryDetailBgMotion")
    val wave by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = HeroCrossfadeEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "wave",
    )
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = if (reduceMotion) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )

    val brush = buildGradientBrush(palette, wave, drift, colorScheme)
    return MasonryHeroBackgroundLayers(
        outgoing = brush,
        incoming = brush,
        progress = 1f,
    )
}
