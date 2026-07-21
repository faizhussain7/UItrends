package com.mfhapps.trendingui.screens.calm

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DisplaySettings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.mfhapps.trendingui.ui.components.FilledTonalButton
import androidx.compose.material3.Icon
import com.mfhapps.trendingui.ui.components.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.mfhapps.trendingui.ui.components.OutlinedButton
import com.mfhapps.trendingui.ui.components.HapticSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.mfhapps.trendingui.ui.platform.appBarTopWindowInsets
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.mfhapps.trendingui.ui.platform.isCompactWindowWidth
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.ButtonGroup
import com.mfhapps.trendingui.ui.components.SwitchPreferenceRow
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.detail.DetailPaneTopBarActions
import com.mfhapps.trendingui.ui.detail.LocalDetailPaneActive
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide

private data class CalmSection(
    val title: String,
    val body: String,
    val quote: String,
    val readMinutes: Int,
)

private val CalmSections = listOf(
    CalmSection(
        title = "Introduction",
        body = "Calm UI keeps attention on the words. No badges, no parallax, no drawer chrome—just a comfortable measure, quiet typography, and room to think. " +
            "This demo models a long-form reader: chapters in a strip, optional focus mode, and settings that stay out of the way until you ask for them.",
        quote = "Attention is the scarcest resource a screen can spend.",
        readMinutes = 2,
    ),
    CalmSection(
        title = "Measure & rhythm",
        body = "Body copy targets about sixty characters per line. Line height scales with your reader settings so long sessions stay easy on the eyes. " +
            "Widen type size or line height from the display controls—watch how the measure reflows without jumping the chapter chrome.",
        quote = "Sixty characters a line is where the eye stops working.",
        readMinutes = 3,
    ),
    CalmSection(
        title = "Gentle motion",
        body = "Section changes use a soft cross-fade only. When reduce motion is on at the system level, transitions shorten automatically. " +
            "Motion here is semantic: it marks a chapter change, not decoration layered on every scroll tick.",
        quote = "Motion should mark meaning, not decorate scrolling.",
        readMinutes = 2,
    ),
    CalmSection(
        title = "E-ink mode",
        body = "E-ink follows system light or dark and offers black, blue, or green ink on tinted paper—flat surfaces with no elevation tint. " +
            "Use it when you want print-like contrast without Material tonal containers competing with the prose.",
        quote = "Print-like contrast, none of the glare.",
        readMinutes = 2,
    ),
    CalmSection(
        title = "Reader controls",
        body = "Pull the reading options down from the top—type size, line height, serif, justification, e-ink, and focus mode. " +
            "Chapters stay one tap away in the strip below. Focus mode strips chrome so only the words remain.",
        quote = "The best chrome is the chrome you never notice.",
        readMinutes = 2,
    ),
)

private enum class CalmInkColor(val label: String) {
    Black("Black"),
    Blue("Blue"),
    Green("Green"),
}

private data class CalmEInkPalette(
    val background: Color,
    val onBackground: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val inkPreview: Color,
)

private fun calmEInkColorScheme(systemDark: Boolean, inkColor: CalmInkColor) =
    if (systemDark) {
        darkColorSchemeFrom(calmEInkDarkPalette(inkColor))
    } else {
        lightColorSchemeFrom(calmEInkLightPalette(inkColor))
    }

private fun calmEInkLightPalette(ink: CalmInkColor): CalmEInkPalette = when (ink) {
    CalmInkColor.Black -> CalmEInkPalette(
        background = Color(0xFFF4F0E6),
        onBackground = Color(0xFF1A1A1A),
        primary = Color(0xFF1A1A1A),
        onPrimary = Color(0xFFF4F0E6),
        primaryContainer = Color(0xFFE4E0D6),
        onPrimaryContainer = Color(0xFF1A1A1A),
        onSurfaceVariant = Color(0xFF3D3D3D),
        outline = Color(0xFF8C877A),
        outlineVariant = Color(0xFFCFC9BC),
        surfaceContainerLow = Color(0xFFEDE9DF),
        surfaceContainerHigh = Color(0xFFDBD7CD),
        surfaceContainerHighest = Color(0xFFD2CEC4),
        inkPreview = Color(0xFF1A1A1A),
    )
    CalmInkColor.Blue -> CalmEInkPalette(
        background = Color(0xFFEDF2F8),
        onBackground = Color(0xFF152A45),
        primary = Color(0xFF152A45),
        onPrimary = Color(0xFFEDF2F8),
        primaryContainer = Color(0xFFD4E3F0),
        onPrimaryContainer = Color(0xFF152A45),
        onSurfaceVariant = Color(0xFF4A6278),
        outline = Color(0xFF7A94AC),
        outlineVariant = Color(0xFFC5D4E2),
        surfaceContainerLow = Color(0xFFE4ECF4),
        surfaceContainerHigh = Color(0xFFD0DDE8),
        surfaceContainerHighest = Color(0xFFC4D4E4),
        inkPreview = Color(0xFF1E4A7A),
    )
    CalmInkColor.Green -> CalmEInkPalette(
        background = Color(0xFFEFF4EC),
        onBackground = Color(0xFF1A3220),
        primary = Color(0xFF1A3220),
        onPrimary = Color(0xFFEFF4EC),
        primaryContainer = Color(0xFFD6E6D8),
        onPrimaryContainer = Color(0xFF1A3220),
        onSurfaceVariant = Color(0xFF4A5E4E),
        outline = Color(0xFF7A947E),
        outlineVariant = Color(0xFFC5D6C8),
        surfaceContainerLow = Color(0xFFE6EFE8),
        surfaceContainerHigh = Color(0xFFD2E2D6),
        surfaceContainerHighest = Color(0xFFC6DACA),
        inkPreview = Color(0xFF1E5C32),
    )
}

private fun calmEInkDarkPalette(ink: CalmInkColor): CalmEInkPalette = when (ink) {
    CalmInkColor.Black -> CalmEInkPalette(
        background = Color(0xFF141412),
        onBackground = Color(0xFFE8E4DA),
        primary = Color(0xFFE8E4DA),
        onPrimary = Color(0xFF141412),
        primaryContainer = Color(0xFF2C2C28),
        onPrimaryContainer = Color(0xFFE8E4DA),
        onSurfaceVariant = Color(0xFFC4C0B6),
        outline = Color(0xFF7A766C),
        outlineVariant = Color(0xFF3A3A36),
        surfaceContainerLow = Color(0xFF1A1A18),
        surfaceContainerHigh = Color(0xFF2A2A26),
        surfaceContainerHighest = Color(0xFF32322E),
        inkPreview = Color(0xFFE8E4DA),
    )
    CalmInkColor.Blue -> CalmEInkPalette(
        background = Color(0xFF0D1218),
        onBackground = Color(0xFFC8DCEC),
        primary = Color(0xFFA8C8E8),
        onPrimary = Color(0xFF0D1218),
        primaryContainer = Color(0xFF1E3044),
        onPrimaryContainer = Color(0xFFC8DCEC),
        onSurfaceVariant = Color(0xFF94A8BC),
        outline = Color(0xFF5A7088),
        outlineVariant = Color(0xFF283848),
        surfaceContainerLow = Color(0xFF121A22),
        surfaceContainerHigh = Color(0xFF1C2834),
        surfaceContainerHighest = Color(0xFF243444),
        inkPreview = Color(0xFF7EB0DC),
    )
    CalmInkColor.Green -> CalmEInkPalette(
        background = Color(0xFF0E1210),
        onBackground = Color(0xFFD2E8D6),
        primary = Color(0xFF9BC4A4),
        onPrimary = Color(0xFF0E1210),
        primaryContainer = Color(0xFF1E2E22),
        onPrimaryContainer = Color(0xFFD2E8D6),
        onSurfaceVariant = Color(0xFF94AE98),
        outline = Color(0xFF5A7460),
        outlineVariant = Color(0xFF283830),
        surfaceContainerLow = Color(0xFF121A14),
        surfaceContainerHigh = Color(0xFF1C2A20),
        surfaceContainerHighest = Color(0xFF24362A),
        inkPreview = Color(0xFF88C49A),
    )
}

private fun lightColorSchemeFrom(p: CalmEInkPalette) = lightColorScheme(
    primary = p.primary,
    onPrimary = p.onPrimary,
    primaryContainer = p.primaryContainer,
    onPrimaryContainer = p.onPrimaryContainer,
    secondary = p.onSurfaceVariant,
    onSecondary = p.background,
    secondaryContainer = p.surfaceContainerLow,
    onSecondaryContainer = p.onBackground,
    tertiary = p.onSurfaceVariant,
    onTertiary = p.background,
    background = p.background,
    onBackground = p.onBackground,
    surface = p.background,
    onSurface = p.onBackground,
    surfaceVariant = p.surfaceContainerLow,
    onSurfaceVariant = p.onSurfaceVariant,
    outline = p.outline,
    outlineVariant = p.outlineVariant,
    surfaceContainerLowest = p.background,
    surfaceContainerLow = p.surfaceContainerLow,
    surfaceContainer = p.surfaceContainerLow,
    surfaceContainerHigh = p.surfaceContainerHigh,
    surfaceContainerHighest = p.surfaceContainerHighest,
    inverseSurface = p.onBackground,
    inverseOnSurface = p.background,
    scrim = Color(0x66000000),
)

private fun darkColorSchemeFrom(p: CalmEInkPalette) = darkColorScheme(
    primary = p.primary,
    onPrimary = p.onPrimary,
    primaryContainer = p.primaryContainer,
    onPrimaryContainer = p.onPrimaryContainer,
    secondary = p.onSurfaceVariant,
    onSecondary = p.background,
    secondaryContainer = p.surfaceContainerLow,
    onSecondaryContainer = p.onBackground,
    tertiary = p.onSurfaceVariant,
    onTertiary = p.background,
    background = p.background,
    onBackground = p.onBackground,
    surface = p.background,
    onSurface = p.onBackground,
    surfaceVariant = p.surfaceContainerLow,
    onSurfaceVariant = p.onSurfaceVariant,
    outline = p.outline,
    outlineVariant = p.outlineVariant,
    surfaceContainerLowest = p.background,
    surfaceContainerLow = p.surfaceContainerLow,
    surfaceContainer = p.surfaceContainerLow,
    surfaceContainerHigh = p.surfaceContainerHigh,
    surfaceContainerHighest = p.surfaceContainerHighest,
    inverseSurface = p.onBackground,
    inverseOnSurface = p.background,
    scrim = Color(0x99000000),
)

private fun calmEInkVariantLabel(inkColor: CalmInkColor, systemDark: Boolean): String {
    val tone = if (systemDark) "dark" else "light"
    return "E-ink · ${inkColor.label.lowercase()} · $tone"
}

@Composable
fun CalmUiScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
) {
    var fontScale by rememberSaveable { mutableFloatStateOf(1f) }
    var lineHeightScale by rememberSaveable { mutableFloatStateOf(1.08f) }
    var useSerif by rememberSaveable { mutableStateOf(false) }
    var justifyText by rememberSaveable { mutableStateOf(false) }
    var eInk by rememberSaveable { mutableStateOf(false) }
    var inkColorId by rememberSaveable { mutableStateOf(CalmInkColor.Black.name) }
    val inkColor = remember(inkColorId) {
        CalmInkColor.entries.find { it.name == inkColorId } ?: CalmInkColor.Black
    }
    var focusMode by rememberSaveable { mutableStateOf(false) }
    var readerPanelOpen by rememberSaveable { mutableStateOf(false) }
    var sectionIndex by rememberSaveable { mutableIntStateOf(0) }
    var sheetProgress by remember { mutableFloatStateOf(0f) }

    val systemDark = isSystemInDarkTheme()
    val base = MaterialTheme.colorScheme
    val calmScheme = remember(base, eInk, systemDark, inkColor) {
        if (eInk) {
            calmEInkColorScheme(systemDark, inkColor)
        } else {
            base.copy(
                background = base.surfaceContainerLowest,
                onBackground = base.onSurface,
                surface = base.surfaceContainerLowest,
                onSurface = base.onSurface,
                surfaceContainerLow = base.surfaceContainerLow,
                surfaceContainerHigh = base.surfaceContainerHigh,
            )
        }
    }

    val bodyFontSize = (17f * fontScale).sp
    val bodyLineHeight = (28f * fontScale * lineHeightScale).sp
    val headlineFontSize = (26f * fontScale).sp
    val headlineLineHeight = (34f * fontScale).sp
    val readerFamily = if (useSerif) FontFamily.Serif else FontFamily.SansSerif

    val calmTypography = MaterialTheme.typography.copy(
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = readerFamily,
            fontSize = bodyFontSize,
            lineHeight = bodyLineHeight,
            fontWeight = FontWeight.Normal,
            color = calmScheme.onBackground,
        ),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = readerFamily,
            fontSize = headlineFontSize,
            lineHeight = headlineLineHeight,
            fontWeight = FontWeight.Medium,
            color = calmScheme.onBackground,
        ),
        labelLarge = MaterialTheme.typography.labelLarge.copy(
            fontFamily = readerFamily,
            color = calmScheme.onSurfaceVariant,
        ),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = readerFamily,
            color = calmScheme.onSurfaceVariant,
        ),
    )

    val reduceMotion = LocalReduceMotion.current
    val inDetailPane = LocalDetailPaneActive.current
    val compact = isCompactWindowWidth()
    val showBackInTopBar = inDetailPane && compact
    val showTopBarActions = true
    val section = CalmSections[sectionIndex]
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(focusMode) {
        if (focusMode) {
            readerPanelOpen = false
        }
    }

    BackHandler(enabled = focusMode) {
        focusMode = false
    }

    val minutesLeft = remember(sectionIndex) {
        CalmSections.drop(sectionIndex + 1).sumOf { it.readMinutes }
    }
    val eInkVariantLabel = calmEInkVariantLabel(inkColor, systemDark)
    val progress = (sectionIndex + 1).toFloat() / CalmSections.size

    MaterialTheme(colorScheme = calmScheme, typography = calmTypography) {
        Box(
            Modifier
                .fillMaxSize()
                .appHazeSource()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val squeeze = 1f - 0.12f * sheetProgress
                        scaleX = squeeze
                        scaleY = squeeze
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    },
            ) {
                if (focusMode) {
                    CalmFocusReadingLayout(
                        sectionIndex = sectionIndex,
                        sectionCount = CalmSections.size,
                        navBarBottom = navBarBottom,
                        reduceMotion = reduceMotion,
                        justify = justifyText,
                        onExitFocus = { focusMode = false },
                        onPrevious = { if (sectionIndex > 0) sectionIndex-- },
                        onNext = { if (sectionIndex < CalmSections.lastIndex) sectionIndex++ },
                        onNavigateToSection = { sectionIndex = it },
                    )
                } else {
                    Row(Modifier.fillMaxSize()) {
                        if (!compact) {
                            CalmTableOfContentsRail(
                                sections = CalmSections,
                                selectedIndex = sectionIndex,
                                onSelect = { sectionIndex = it },
                                modifier = Modifier
                                    .width(220.dp)
                                    .fillMaxHeight()
                                    .windowInsetsPadding(appBarTopWindowInsets()),
                            )
                            VerticalDivider()
                        }

                        val contentAppBarInsets = calmContentAppBarInsets(hasLeadingRail = !compact)

                        Column(Modifier.weight(1f)) {
                            Column {
                                CalmReaderTopBar(
                                    showBack = showBackInTopBar,
                                    onNavigateBack = onNavigateBack,
                                    readerPanelOpen = readerPanelOpen,
                                    onToggleReaderPanel = { readerPanelOpen = !readerPanelOpen },
                                    eInk = eInk,
                                    eInkVariantLabel = eInkVariantLabel,
                                    guide = guide,
                                    showTopBarActions = showTopBarActions,
                                    windowInsets = contentAppBarInsets,
                                )

                                if (compact) {
                                    CalmChapterStrip(
                                        sections = CalmSections,
                                        selectedIndex = sectionIndex,
                                        reduceMotion = reduceMotion,
                                        onSelect = { sectionIndex = it },
                                    )
                                }

                                CalmReadingMeta(
                                    sectionIndex = sectionIndex,
                                    sectionCount = CalmSections.size,
                                    sectionMinutes = section.readMinutes,
                                    minutesLeft = minutesLeft,
                                    progress = progress,
                                )
                            }

                            CalmArticleLazyColumn(
                                sectionIndex = sectionIndex,
                                reduceMotion = reduceMotion,
                                justify = justifyText,
                                contentTopPadding = 8.dp,
                                onNavigateToSection = { sectionIndex = it },
                                modifier = Modifier.weight(1f),
                            )

                            CalmChapterNavigation(
                                sectionIndex = sectionIndex,
                                sectionCount = CalmSections.size,
                                onPrevious = { if (sectionIndex > 0) sectionIndex-- },
                                onNext = { if (sectionIndex < CalmSections.lastIndex) sectionIndex++ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .navigationBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                            )
                        }
                    }
                }
            }

            CalmTopSheet(
                visible = readerPanelOpen,
                onDismiss = { readerPanelOpen = false },
                onProgress = { sheetProgress = it },
            ) {
                ReaderSettingsContent(
                    fontScale = fontScale,
                    onFontScale = { fontScale = it },
                    lineHeightScale = lineHeightScale,
                    onLineHeightScale = { lineHeightScale = it },
                    useSerif = useSerif,
                    onUseSerif = { useSerif = it },
                    justify = justifyText,
                    onJustify = { justifyText = it },
                    eInk = eInk,
                    inkColor = inkColor,
                    eInkVariantLabel = eInkVariantLabel,
                    onEInk = { eInk = it },
                    onInkColor = { inkColorId = it.name },
                    onFocusMode = { focusMode = it },
                    onDismiss = { readerPanelOpen = false },
                )
            }
        }
    }
}

@Composable
private fun CalmArticleLazyColumn(
    sectionIndex: Int,
    reduceMotion: Boolean,
    justify: Boolean,
    contentTopPadding: Dp,
    onNavigateToSection: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(
            top = contentTopPadding,
            bottom = 24.dp,
        ),
    ) {
        item {
            CalmArticleBody(
                sectionIndex = sectionIndex,
                reduceMotion = reduceMotion,
                justify = justify,
                onNavigateToSection = onNavigateToSection,
            )
        }
    }
}

@Composable
private fun CalmArticleBody(
    sectionIndex: Int,
    reduceMotion: Boolean,
    justify: Boolean,
    onNavigateToSection: (Int) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = sectionIndex,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(tween(120)) togetherWith fadeOut(tween(120))
                } else {
                    fadeIn(tween(280)) togetherWith fadeOut(tween(220))
                }
            },
            label = "calmSection",
        ) { idx ->
            val s = CalmSections[idx]
            val scheme = MaterialTheme.colorScheme
            Column(Modifier.widthIn(max = 560.dp)) {
                Text(
                    text = (idx + 1).toString().padStart(2, '0'),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Light,
                    color = scheme.primary.copy(alpha = 0.45f),
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = s.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .semantics { heading() },
                )
                CalmPullQuote(
                    quote = s.quote,
                    modifier = Modifier.padding(top = 16.dp),
                )
                SelectionContainer {
                    Text(
                        text = s.body,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = if (justify) TextAlign.Justify else TextAlign.Start,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                CalmUpNextCard(
                    sectionIndex = idx,
                    onNavigateToSection = onNavigateToSection,
                    modifier = Modifier.padding(top = 24.dp, bottom = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun CalmPullQuote(
    quote: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Box(
            Modifier
                .fillMaxHeight()
                .width(3.dp)
                .clip(RoundedCornerShape(50))
                .background(scheme.primary),
        )
        Text(
            text = quote,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
            ),
            color = scheme.onSurface,
            modifier = Modifier.padding(start = 14.dp),
        )
    }
}

@Composable
private fun CalmUpNextCard(
    sectionIndex: Int,
    onNavigateToSection: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val isLast = sectionIndex >= CalmSections.lastIndex
    val targetIndex = if (isLast) 0 else sectionIndex + 1
    val target = CalmSections[targetIndex]
    Surface(
        onClick = { onNavigateToSection(targetIndex) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = scheme.surfaceContainerLow,
        contentColor = scheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (isLast) "The end — read again" else "Up next",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    text = target.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text(
                text = "${target.readMinutes} min",
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = scheme.primary,
            )
        }
    }
}

@Composable
private fun CalmFocusReadingLayout(
    sectionIndex: Int,
    sectionCount: Int,
    navBarBottom: Dp,
    reduceMotion: Boolean,
    justify: Boolean,
    onExitFocus: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onNavigateToSection: (Int) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(appBarTopWindowInsets())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Focus reading",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    text = "Chapter ${sectionIndex + 1} of $sectionCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(
                onClick = onExitFocus,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer,
                ),
            ) {
                Text(
                    text = "Exit focus",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        CalmArticleLazyColumn(
            sectionIndex = sectionIndex,
            reduceMotion = reduceMotion,
            justify = justify,
            contentTopPadding = 0.dp,
            onNavigateToSection = onNavigateToSection,
            modifier = Modifier.weight(1f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(scheme.background)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = sectionIndex > 0,
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Previous")
            }
            OutlinedButton(
                onClick = onNext,
                enabled = sectionIndex < sectionCount - 1,
            ) {
                Text("Next")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun CalmReaderOptionsButton(
    readerPanelOpen: Boolean,
    onToggleReaderPanel: () -> Unit,
) {
    IconButton(onClick = onToggleReaderPanel) {
        Icon(
            imageVector = Icons.Outlined.DisplaySettings,
            contentDescription = if (readerPanelOpen) {
                "Close reading options"
            } else {
                "Reading options"
            },
            tint = if (readerPanelOpen) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun calmContentAppBarInsets(hasLeadingRail: Boolean): WindowInsets =
    appBarTopWindowInsets()

@Composable
private fun CalmReaderTopBar(
    showBack: Boolean,
    onNavigateBack: () -> Unit,
    readerPanelOpen: Boolean,
    onToggleReaderPanel: () -> Unit,
    eInk: Boolean,
    eInkVariantLabel: String,
    guide: DemoTrendGuide?,
    showTopBarActions: Boolean,
    windowInsets: WindowInsets = appBarTopWindowInsets(),
) {
    val scheme = MaterialTheme.colorScheme
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Calm UI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (eInk) {
                    Text(
                        eInkVariantLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back to catalog",
                        tint = scheme.onBackground,
                    )
                }
            }
        },
        actions = {
            if (showTopBarActions) {
                DetailPaneTopBarActions(
                    guide = guide,
                    iconTint = scheme.onBackground,
                )
            }
            CalmReaderOptionsButton(
                readerPanelOpen = readerPanelOpen,
                onToggleReaderPanel = onToggleReaderPanel,
            )
        },
        windowInsets = windowInsets,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = scheme.background,
            scrolledContainerColor = scheme.background,
            titleContentColor = scheme.onBackground,
            navigationIconContentColor = scheme.onBackground,
            actionIconContentColor = scheme.onBackground,
        ),
    )
}

@Composable
private fun ReaderSettingsContent(
    fontScale: Float,
    onFontScale: (Float) -> Unit,
    lineHeightScale: Float,
    onLineHeightScale: (Float) -> Unit,
    useSerif: Boolean,
    onUseSerif: (Boolean) -> Unit,
    justify: Boolean,
    onJustify: (Boolean) -> Unit,
    eInk: Boolean,
    inkColor: CalmInkColor,
    eInkVariantLabel: String,
    onEInk: (Boolean) -> Unit,
    onInkColor: (CalmInkColor) -> Unit,
    onFocusMode: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val fontPct = (fontScale * 100f).toInt()
    val lhPct = (lineHeightScale * 100f).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Reader",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = if (eInk) eInkVariantLabel else "Type, ink & focus",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
            FilledTonalButton(
                onClick = onDismiss,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer,
                ),
            ) {
                Text("Done", fontWeight = FontWeight.SemiBold)
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = MaterialTheme.shapes.medium,
            color = scheme.surfaceContainerHigh,
            contentColor = scheme.onBackground,
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    text = "The quiet page keeps attention on the words.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .animateContentSize(),
                )
            }
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Text size", style = MaterialTheme.typography.bodyMedium)
                Text("$fontPct%", style = MaterialTheme.typography.labelMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("A", fontSize = 13.sp, color = scheme.onSurfaceVariant)
                HapticSlider(
                    value = fontScale,
                    onValueChange = onFontScale,
                    valueRange = 0.85f..1.55f,
                    steps = 13,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                )
                Text("A", fontSize = 21.sp, color = scheme.onSurfaceVariant)
            }
        }

        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Line height", style = MaterialTheme.typography.bodyMedium)
                Text("$lhPct%", style = MaterialTheme.typography.labelMedium)
            }
            HapticSlider(
                value = lineHeightScale,
                onValueChange = onLineHeightScale,
                valueRange = 0.95f..1.55f,
                steps = 11,
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Typeface",
                style = MaterialTheme.typography.bodyMedium,
            )
            ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                ToggleButton(
                    checked = !useSerif,
                    onCheckedChange = { if (it) onUseSerif(false) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Sans", fontFamily = FontFamily.SansSerif)
                }
                ToggleButton(
                    checked = useSerif,
                    onCheckedChange = { if (it) onUseSerif(true) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Serif", fontFamily = FontFamily.Serif)
                }
            }
        }

        SwitchPreferenceRow(
            title = "Justify text",
            subtitle = "Align body copy to both margins",
            checked = justify,
            onCheckedChange = onJustify,
            containerColor = Color.Transparent,
        )

        SwitchPreferenceRow(
            title = "E-ink mode",
            subtitle = "Black, blue, or green ink — follows system light or dark",
            checked = eInk,
            onCheckedChange = onEInk,
            containerColor = Color.Transparent,
        )

        AnimatedVisibility(visible = eInk) {
            CalmInkColorPicker(
                selected = inkColor,
                systemDark = isSystemInDarkTheme(),
                onSelect = onInkColor,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        SwitchPreferenceRow(
            title = "Focus mode",
            subtitle = "Hide toolbar and chapter chrome while reading",
            checked = false,
            onCheckedChange = {
                if (it) {
                    onFocusMode(true)
                    onDismiss()
                }
            },
            containerColor = Color.Transparent,
        )
    }
}

@Composable
private fun CalmInkColorPicker(
    selected: CalmInkColor,
    systemDark: Boolean,
    onSelect: (CalmInkColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Ink color",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CalmInkColor.entries.forEach { color ->
                val isSelected = selected == color
                val preview = if (systemDark) {
                    calmEInkDarkPalette(color).inkPreview
                } else {
                    calmEInkLightPalette(color).inkPreview
                }
                Surface(
                    onClick = { onSelect(color) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) {
                        scheme.primaryContainer
                    } else {
                        scheme.surfaceContainerHigh
                    },
                    contentColor = if (isSelected) {
                        scheme.onPrimaryContainer
                    } else {
                        scheme.onSurfaceVariant
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(preview),
                        )
                        Text(
                            text = color.label,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalmChapterStrip(
    sections: List<CalmSection>,
    selectedIndex: Int,
    reduceMotion: Boolean,
    onSelect: (Int) -> Unit,
) {
    val listState = rememberLazyListState()
    val safeIndex = selectedIndex.coerceIn(sections.indices)

    LaunchedEffect(safeIndex, sections.size) {
        if (sections.isEmpty()) return@LaunchedEffect
        if (reduceMotion) {
            listState.scrollToCenteredItem(safeIndex)
        } else {
            listState.animateScrollToCenteredItem(safeIndex)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(
            items = sections,
            key = { index, _ -> index },
        ) { index, section ->
            FilterChip(
                selected = index == safeIndex,
                onClick = { onSelect(index) },
                label = { Text(section.title) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        }
    }
}

private suspend fun LazyListState.scrollToCenteredItem(index: Int) {
    if (index < 0) return
    scrollToItem(index)
    scroll {
        val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return@scroll
        val viewportStart = layoutInfo.viewportStartOffset
        val viewportEnd = layoutInfo.viewportEndOffset
        val viewportCenter = viewportStart + (viewportEnd - viewportStart) / 2
        val itemCenter = item.offset + item.size / 2
        scrollBy((itemCenter - viewportCenter).toFloat())
    }
}

private suspend fun LazyListState.animateScrollToCenteredItem(index: Int) {
    if (index < 0) return
    animateScrollToItem(index)
    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return
    val viewportStart = layoutInfo.viewportStartOffset
    val viewportEnd = layoutInfo.viewportEndOffset
    val viewportCenter = viewportStart + (viewportEnd - viewportStart) / 2
    val itemCenter = item.offset + item.size / 2
    val delta = (itemCenter - viewportCenter).toFloat()
    if (delta != 0f) {
        animateScrollBy(delta)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CalmTableOfContentsRail(
    sections: List<CalmSection>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .background(scheme.surfaceContainerLow)
            .padding(vertical = 16.dp, horizontal = 12.dp),
    ) {
        Text(
            "Contents",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 12.dp)
                .semantics { heading() },
        )
        sections.forEachIndexed { index, section ->
            val selected = index == selectedIndex
            Surface(
                onClick = { onSelect(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Text(
                        text = "${section.readMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CalmReadingMeta(
    sectionIndex: Int,
    sectionCount: Int,
    sectionMinutes: Int,
    minutesLeft: Int,
    progress: Float,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Chapter ${sectionIndex + 1} of $sectionCount",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = if (minutesLeft > 0) {
                    "~$sectionMinutes min · $minutesLeft min left"
                } else {
                    "~$sectionMinutes min · last chapter"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(8.dp))
        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun CalmChapterNavigation(
    sectionIndex: Int,
    sectionCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = sectionIndex > 0,
        ) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Previous")
        }
        OutlinedButton(
            onClick = onNext,
            enabled = sectionIndex < sectionCount - 1,
        ) {
            Text("Next")
            Spacer(Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
        }
    }
}
