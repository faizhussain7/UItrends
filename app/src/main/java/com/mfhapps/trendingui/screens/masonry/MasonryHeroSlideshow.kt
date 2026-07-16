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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
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

@Immutable
internal data class MasonryAdaptiveChrome(
    val onImagePrimary: Color,
    val onImageSecondary: Color,
    val badgeContainer: Color,
    val badgeLabel: Color,
    val heroScrimMid: Color,
    val heroScrimBottom: Color,
    val imageScrimStrong: Color,
    val accentOrb: Color,
    val deckSurface: Color,
    val deckTitle: Color,
    val cardSurface: Color,
    val cardBlend: Color,
    val elevatedSurface: Color,
    val sectionSurface: Color,
    val contentTitle: Color,
    val contentBody: Color,
    val contentMuted: Color,
    val cardTitle: Color,
    val cardSubtitle: Color,
    val chipContainer: Color,
    val chipContainerSelected: Color,
    val chipLabel: Color,
    val chipLabelSelected: Color,
    val chipBorder: Color,
    val chipBorderSelected: Color,
    val categoryContainer: Color,
    val categoryLabel: Color,
    val tagContainer: Color,
    val tagLabel: Color,
    val iconContainer: Color,
    val iconContent: Color,
    val divider: Color,
    val primary: Color,
    val onPrimary: Color,
    val backgroundTint: Color,
    val topBarForeground: Color,
    val topBarIconTint: Color,
    val topBarIconContainer: Color,
)

internal typealias MasonryHeroChrome = MasonryAdaptiveChrome

internal val LocalMasonryAdaptiveChrome = staticCompositionLocalOf<MasonryAdaptiveChrome?> { null }

@Stable
internal data class MasonryHeroLoopState(
    val displayedIndex: Int,
    val overlayIndex: Int?,
    val crossfadeProgress: Float,
    val kenBurnsScale: Float,
    val palettes: List<List<Color>>,
    val background: MasonryHeroBackgroundLayers,
    val chrome: MasonryAdaptiveChrome,
)

private fun contrastOn(background: Color): Color =
    if (background.luminance() > 0.57f) Color(0xFF141210) else Color.White

private fun blendColors(colors: List<Color>, fallback: Color): Color {
    if (colors.isEmpty()) return fallback
    return colors.drop(1).fold(colors.first()) { acc, color ->
        lerp(acc, color, 0.42f)
    }
}

internal fun paletteChrome(palette: List<Color>, scheme: ColorScheme): MasonryAdaptiveChrome {
    val dominant = palette.getOrElse(0) { scheme.primary }
    val accent = palette.getOrElse(1) { scheme.secondary }
    val tertiary = palette.getOrElse(2) { scheme.tertiary }
    val surfaceTint = palette.getOrElse(3) { scheme.surfaceContainerLow }
    val backgroundTint = palette.getOrElse(4) { scheme.background }
    val topBand = palette.take(3).ifEmpty { listOf(dominant) }
    val bottomBand = listOf(tertiary, surfaceTint, dominant)
    val topBlend = blendColors(topBand, dominant)
    val bottomBlend = blendColors(bottomBand, dominant)
    val surfaceBlend = blendColors(listOf(surfaceTint, accent, tertiary), scheme.surface)
    val topLum = topBlend.luminance()
    val heroLum = dominant.luminance()
    val isLightTop = topLum > 0.57f
    val isLightHero = heroLum > 0.54f

    val onImagePrimary = contrastOn(bottomBlend)
    val onImageSecondary = onImagePrimary.copy(alpha = 0.88f)
    val badgeContainer = lerp(accent, bottomBlend, 0.28f).copy(alpha = if (isLightHero) 0.90f else 0.80f)
    val badgeLabel = contrastOn(badgeContainer)
    val heroScrimMid = bottomBlend.copy(alpha = if (isLightHero) 0.20f else 0.38f)
    val heroScrimBottom = lerp(
        bottomBlend,
        if (isLightHero) Color.White else Color.Black,
        if (isLightHero) 0.10f else 0.62f,
    ).copy(alpha = if (isLightHero) 0.58f else 0.84f)
    val imageScrimStrong = lerp(
        bottomBlend,
        if (isLightHero) Color.Black else Color.Black,
        if (isLightHero) 0.35f else 0.72f,
    ).copy(alpha = if (isLightHero) 0.62f else 0.78f)
    val accentOrb = lerp(accent, dominant, 0.40f).copy(alpha = if (isLightHero) 0.18f else 0.24f)
    val deckSurface = lerp(scheme.surface, surfaceBlend, 0.52f)
    val deckTitle = contrastOn(lerp(deckSurface, accent, 0.10f))
    val cardSurface = lerp(deckSurface, lerp(accent, surfaceTint, 0.35f), 0.14f)
    val cardBlend = cardSurface
    val elevatedSurface = lerp(deckSurface, accent, 0.08f)
    val sectionSurface = lerp(deckSurface, surfaceTint, 0.18f)
    val contentTitle = contrastOn(deckSurface)
    val contentBody = contrastOn(lerp(deckSurface, accent, 0.06f)).copy(alpha = 0.88f)
    val contentMuted = contentTitle.copy(alpha = 0.68f)
    val chipContainer = lerp(accent, deckSurface, 0.22f).copy(alpha = if (isLightHero) 0.18f else 0.26f)
    val chipContainerSelected = lerp(accent, dominant, 0.18f).copy(alpha = if (isLightHero) 0.84f else 0.74f)
    val chipLabel = contrastOn(lerp(chipContainer, deckSurface, 0.45f))
    val chipLabelSelected = contrastOn(chipContainerSelected)
    val categoryContainer = lerp(accent, deckSurface, 0.30f).copy(alpha = if (isLightHero) 0.72f else 0.68f)
    val categoryLabel = contrastOn(categoryContainer)
    val tagContainer = lerp(tertiary, accent, 0.35f).copy(alpha = if (isLightHero) 0.82f else 0.76f)
    val tagLabel = contrastOn(tagContainer)
    val iconContainer = lerp(accent, deckSurface, 0.20f).copy(alpha = if (isLightHero) 0.88f else 0.80f)
    val iconContent = contrastOn(iconContainer)
    val divider = lerp(accent, deckSurface, 0.55f).copy(alpha = 0.45f)
    val primary = accent
    val onPrimary = contrastOn(accent)
    val topBarForeground = contrastOn(topBlend)
    val topBarIconTint = topBarForeground
    val topBarIconContainer = lerp(
        lerp(
            Color.Black.copy(alpha = if (isLightTop) 0.12f else 0f),
            Color.White.copy(alpha = if (isLightTop) 0f else 0.16f),
            topLum,
        ),
        accent.copy(alpha = if (isLightTop) 0.72f else 0.78f),
        0.66f,
    )

    return MasonryAdaptiveChrome(
        onImagePrimary = onImagePrimary,
        onImageSecondary = onImageSecondary,
        badgeContainer = badgeContainer,
        badgeLabel = badgeLabel,
        heroScrimMid = heroScrimMid,
        heroScrimBottom = heroScrimBottom,
        imageScrimStrong = imageScrimStrong,
        accentOrb = accentOrb,
        deckSurface = deckSurface,
        deckTitle = deckTitle,
        cardSurface = cardSurface,
        cardBlend = cardBlend,
        elevatedSurface = elevatedSurface,
        sectionSurface = sectionSurface,
        contentTitle = contentTitle,
        contentBody = contentBody,
        contentMuted = contentMuted,
        cardTitle = contentTitle,
        cardSubtitle = contentMuted,
        chipContainer = chipContainer,
        chipContainerSelected = chipContainerSelected,
        chipLabel = chipLabel,
        chipLabelSelected = chipLabelSelected,
        chipBorder = accent.copy(alpha = if (isLightHero) 0.28f else 0.40f),
        chipBorderSelected = accent.copy(alpha = if (isLightHero) 0.82f else 0.88f),
        categoryContainer = categoryContainer,
        categoryLabel = categoryLabel,
        tagContainer = tagContainer,
        tagLabel = tagLabel,
        iconContainer = iconContainer,
        iconContent = iconContent,
        divider = divider,
        primary = primary,
        onPrimary = onPrimary,
        backgroundTint = backgroundTint,
        topBarForeground = topBarForeground,
        topBarIconTint = topBarIconTint,
        topBarIconContainer = topBarIconContainer,
    )
}

internal fun MasonryAdaptiveChrome.toMaterialScheme(base: ColorScheme): ColorScheme = base.copy(
    primary = primary,
    onPrimary = onPrimary,
    secondary = lerp(primary, base.secondary, 0.35f),
    onSecondary = contrastOn(lerp(primary, base.secondary, 0.35f)),
    tertiary = lerp(primary, base.tertiary, 0.45f),
    onTertiary = contrastOn(lerp(primary, base.tertiary, 0.45f)),
    background = lerp(base.background, backgroundTint, 0.38f),
    onBackground = contentTitle,
    surface = deckSurface,
    onSurface = contentTitle,
    onSurfaceVariant = contentMuted,
    surfaceContainerLow = sectionSurface,
    surfaceContainerHigh = elevatedSurface,
    surfaceContainerHighest = cardSurface,
    primaryContainer = iconContainer,
    onPrimaryContainer = iconContent,
    secondaryContainer = tagContainer,
    onSecondaryContainer = tagLabel,
    tertiaryContainer = categoryContainer,
    onTertiaryContainer = categoryLabel,
    outlineVariant = divider,
    scrim = imageScrimStrong,
)

@Composable
internal fun MasonryAdaptiveTheme(
    chrome: MasonryAdaptiveChrome,
    content: @Composable () -> Unit,
) {
    val baseScheme = MaterialTheme.colorScheme
    val adaptedScheme = remember(chrome, baseScheme) { chrome.toMaterialScheme(baseScheme) }
    CompositionLocalProvider(LocalMasonryAdaptiveChrome provides chrome) {
        MaterialTheme(colorScheme = adaptedScheme, content = content)
    }
}

private fun blendPalettes(
    from: List<Color>,
    to: List<Color>,
    progress: Float,
): List<Color> {
    val size = maxOf(from.size, to.size)
    return List(size) { index ->
        lerp(
            from.getOrElse(index) { from.last() },
            to.getOrElse(index) { to.last() },
            progress,
        )
    }
}

private fun ColorScheme.fallbackPalette(): List<Color> = listOf(
    primary,
    secondary,
    tertiary,
    surfaceContainerLow,
    background,
)

private fun samplePalette(bitmap: Bitmap, scheme: ColorScheme): List<Color> {
    val sampleWidth = 16
    val sampleHeight = 20
    val scaled = bitmap.scale(sampleWidth, sampleHeight)
    val pixels = buildList {
        for (y in 0 until sampleHeight) {
            val weight = when {
                y < sampleHeight * 0.30f -> 4
                y < sampleHeight * 0.55f -> 2
                else -> 1
            }
            for (x in 0 until sampleWidth step 2) {
                val color = Color(scaled[x, y]).copy(alpha = 1f)
                repeat(weight) { add(color) }
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
    val activePalette = remember(displayedIndex, overlayIndex, progress, palettes) {
        val from = palettes.getOrElse(fromIndex) { colorScheme.fallbackPalette() }
        if (overlayIndex != null) {
            val to = palettes.getOrElse(toIndex) { from }
            blendPalettes(from, to, progress)
        } else {
            from
        }
    }
    val chrome = remember(activePalette, colorScheme) {
        paletteChrome(activePalette, colorScheme)
    }
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
        chrome = chrome,
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
internal fun rememberMasonryPalette(
    imageSeed: String,
    colorScheme: ColorScheme,
): List<Color> {
    val context = LocalContext.current
    var palette by remember(imageSeed, colorScheme) {
        mutableStateOf(colorScheme.fallbackPalette())
    }

    LaunchedEffect(imageSeed, colorScheme) {
        palette = loadPalette(context, imageSeed, colorScheme)
        preloadHeroImage(context, imageSeed)
    }

    return palette
}

@Stable
internal data class MasonryImageAdaptiveState(
    val chrome: MasonryAdaptiveChrome,
    val background: MasonryHeroBackgroundLayers,
)

@Composable
internal fun rememberMasonryImageAdaptiveState(
    imageSeed: String,
    colorScheme: ColorScheme,
    reduceMotion: Boolean,
): MasonryImageAdaptiveState {
    val palette = rememberMasonryPalette(imageSeed, colorScheme)
    val chrome = remember(palette, colorScheme) { paletteChrome(palette, colorScheme) }
    val background = rememberMasonryDetailBackground(palette, colorScheme, reduceMotion)
    return remember(chrome, background) { MasonryImageAdaptiveState(chrome, background) }
}

@Composable
internal fun rememberMasonryAdaptiveChrome(
    imageSeed: String,
    colorScheme: ColorScheme,
): MasonryAdaptiveChrome {
    val palette = rememberMasonryPalette(
        imageSeed = imageSeed,
        colorScheme = colorScheme,
    )
    return remember(palette, colorScheme) { paletteChrome(palette, colorScheme) }
}

@Composable
internal fun rememberMasonryDetailChrome(
    imageSeed: String,
    colorScheme: ColorScheme,
): MasonryAdaptiveChrome = rememberMasonryAdaptiveChrome(imageSeed, colorScheme)

@Composable
internal fun rememberMasonryDetailBackground(
    palette: List<Color>,
    colorScheme: ColorScheme,
    reduceMotion: Boolean,
): MasonryHeroBackgroundLayers {
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
