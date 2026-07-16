package com.mfhapps.trendingui.screens.orbs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import com.mfhapps.trendingui.ui.motion.ExpressiveMotion
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp as lerpTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.core.sensor.GyroTilt
import com.mfhapps.trendingui.core.sensor.rememberGyroscopeTilt
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.CollapsingBlurTopBarLayout
import com.mfhapps.trendingui.ui.components.HapticSlider
import com.mfhapps.trendingui.ui.components.IconButton
import com.mfhapps.trendingui.ui.components.SwitchListItem
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.components.collapsingTopBarContentPadding
import com.mfhapps.trendingui.ui.components.ExpressiveMorphTempo
import com.mfhapps.trendingui.ui.components.ExpressiveShapeCatalogTier
import com.mfhapps.trendingui.ui.components.expressivePhaseOffset
import com.mfhapps.trendingui.ui.components.rememberExpressiveShapeMorphClock
import com.mfhapps.trendingui.ui.components.rememberCollapsedTopAppBarColors
import com.mfhapps.trendingui.ui.demo.DemoAnimatedSection
import com.mfhapps.trendingui.ui.detail.DetailPaneTopBarActions
import com.mfhapps.trendingui.ui.detail.LocalNestedBackDispatcher
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import com.mfhapps.trendingui.ui.platform.appBarTopWindowInsets
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

private val OrbsCardShape = RoundedCornerShape(24.dp)
private val OrbsTileShape = RoundedCornerShape(18.dp)
private val OrbsSectionShape = RoundedCornerShape(14.dp)
private val OrbsHorizontalPadding = 20.dp
private const val OrbsScreenTitle = "Orbs & mesh"
private const val OrbsScreenSubtitle = "Theme-tinted mesh field · balanced orbs · gyro parallax"

private data class BlendModeSample(
    val label: String,
    val subtitle: String,
    val mode: BlendMode,
)

private val BlendSamples = listOf(
    BlendModeSample("Screen", "Soft additive glow", BlendMode.Screen),
    BlendModeSample("Plus", "Brighter stacks", BlendMode.Plus),
    BlendModeSample("Overlay", "Contrast punch", BlendMode.Overlay),
    BlendModeSample("Color dodge", "Hot highlights", BlendMode.ColorDodge),
)

private val CompareOrbCenters = listOf(
    Offset(0.28f, 0.38f),
    Offset(0.68f, 0.48f),
    Offset(0.48f, 0.70f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrbsMeshScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
) {
    val chrome = rememberOrbsChrome()
    val scheme = MaterialTheme.colorScheme
    val tilt = rememberGyroscopeTilt(0.3f)
    val reduceMotion = LocalReduceMotion.current
    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    val collapsedFraction by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f) }
    }
    var selectedPreset by remember { mutableStateOf(MeshPreset.Aurora) }
    var useMaterialShapes by remember { mutableStateOf(false) }
    var randomOrbShapes by remember { mutableStateOf(true) }
    var orbPresence by remember { mutableFloatStateOf(1f) }
    val orbsAppearance = LocalOrbsAppearanceState.current
    androidx.compose.runtime.SideEffect {
        orbsAppearance?.expressive = useMaterialShapes
        orbsAppearance?.presence = orbPresence
        orbsAppearance?.randomShapes = randomOrbShapes
    }
    val playgroundField = remember(selectedPreset, scheme) {
        selectedPreset.themeTinted(scheme)
    }

    CollapsingBlurTopBarLayout(
        scrollBehavior = scrollBehavior,
        collapsedFraction = collapsedFraction,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        topBar = { barModifier ->
            OrbsCollapsingTopBar(
                scrollBehavior = scrollBehavior,
                collapsedFraction = collapsedFraction,
                onNavigateBack = onNavigateBack,
                guide = guide,
                modifier = barModifier,
            )
        },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .appHazeSource(),
            contentPadding = collapsingTopBarContentPadding(
                extra = PaddingValues(bottom = 28.dp),
            ),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            item(key = "playground") {
                DemoAnimatedSection(index = 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = OrbsHorizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OrbsSection(
                            title = "Live playground",
                            body = "Drag orbs — they spring back. Tilt for parallax (unless reduce motion).",
                            chrome = chrome,
                        )
                        OrbsDemoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp),
                            contentScrimAlpha = chrome.playgroundScrim,
                            darkStage = !chrome.isDark,
                        ) {
                            InteractiveOrbsCanvas(
                                modifier = Modifier.fillMaxSize(),
                                meshField = playgroundField,
                                orbColors = chrome.playgroundOrbColors,
                                isDark = chrome.isDark,
                                tilt = tilt,
                                reduceMotion = reduceMotion,
                                presence = orbPresence,
                                expressive = useMaterialShapes,
                                randomShapes = randomOrbShapes,
                                onOrbGrab = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                            )
                        }
                    }
                }
            }

            item(key = "appearance") {
                DemoAnimatedSection(index = 1) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = OrbsHorizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OrbsSection(
                            title = "Orb appearance",
                            body = "One visibility control for the field. Material shapes loop through the M3 catalog on the playground and backdrop.",
                            chrome = chrome,
                        )
                        OrbAppearanceControls(
                            presence = orbPresence,
                            onPresenceChange = { orbPresence = it },
                            useMaterialShapes = useMaterialShapes,
                            onUseMaterialShapesChange = { useMaterialShapes = it },
                            randomShapes = randomOrbShapes,
                            onRandomShapesChange = { randomOrbShapes = it },
                            chrome = chrome,
                        )
                    }
                }
            }

            item(key = "presets") {
                DemoAnimatedSection(index = 2) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = OrbsHorizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OrbsSection(
                            title = "Mesh presets",
                            body = "Tap a swatch to change the playground palette.",
                            chrome = chrome,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            MeshPreset.entries.forEach { preset ->
                                MeshPresetTile(
                                    preset = preset,
                                    selected = preset == selectedPreset,
                                    onClick = { selectedPreset = preset },
                                    modifier = Modifier.weight(1f),
                                    chrome = chrome,
                                )
                            }
                        }
                    }
                }
            }

            item(key = "gallery-row-1") {
                DemoAnimatedSection(index = 3) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = OrbsHorizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OrbsSection(
                            title = "Mesh gallery",
                            body = "Four-point radial meshes tinted with your theme.",
                            chrome = chrome,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            MeshPreviewCard(
                                preset = MeshPreset.Aurora,
                                modifier = Modifier.weight(1f),
                                chrome = chrome,
                            )
                            MeshPreviewCard(
                                preset = MeshPreset.Sunset,
                                modifier = Modifier.weight(1f),
                                chrome = chrome,
                            )
                        }
                    }
                }
            }

            item(key = "gallery-row-2") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = OrbsHorizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MeshPreviewCard(
                        preset = MeshPreset.Brand,
                        modifier = Modifier.weight(1f),
                        chrome = chrome,
                    )
                    MeshPreviewCard(
                        preset = MeshPreset.Midnight,
                        modifier = Modifier.weight(1f),
                        chrome = chrome,
                    )
                }
            }

            item(key = "blend") {
                DemoAnimatedSection(index = 4) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = OrbsHorizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OrbsSection(
                            title = "Blend modes",
                            body = "Same theme orbs on a dark mesh — compare how each mode stacks.",
                            chrome = chrome,
                        )
                        BlendSamples.chunked(2).forEach { rowSamples ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowSamples.forEach { sample ->
                                    BlendCompareCard(
                                        sample = sample,
                                        presence = orbPresence,
                                        expressive = useMaterialShapes,
                                        randomShapes = randomOrbShapes,
                                        modifier = Modifier.weight(1f),
                                        chrome = chrome,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "tips") {
                DemoAnimatedSection(index = 5) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = OrbsHorizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OrbsSection(
                            title = "Orb field tips",
                            body = "Production patterns for motion-safe aurora backgrounds.",
                            chrome = chrome,
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = OrbsSectionShape,
                            color = chrome.readableSurface,
                            tonalElevation = 0.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                TipLine("Drag visibility to Off for mesh-only, or up for a clear vivid field.", chrome)
                                TipLine("Material shapes loop the full M3 catalog on playground and screen backdrop orbs.", chrome)
                                TipLine("Freeze gyro parallax when reduce motion is enabled.", chrome)
                                TipLine("Prefer gradients over full-screen bitmaps for battery life.", chrome)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrbsCollapsingTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    collapsedFraction: Float,
    onNavigateBack: () -> Unit,
    guide: DemoTrendGuide?,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val nestedBackDispatcher = LocalNestedBackDispatcher.current
    val titleStyle = lerpTextStyle(
        start = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        stop = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        fraction = collapsedFraction,
    )
    val subtitleAlpha = (1f - collapsedFraction * 1.35f).coerceIn(0f, 1f)

    LargeTopAppBar(
        modifier = modifier,
        windowInsets = appBarTopWindowInsets(),
        scrollBehavior = scrollBehavior,
        colors = rememberCollapsedTopAppBarColors(
            collapsedFraction = collapsedFraction,
            containerColor = Color.Transparent,
            scrolledContainerColor = scheme.surface,
            navigationIconContentColor = scheme.onSurface,
            titleContentColor = scheme.onSurface,
            actionIconContentColor = scheme.onSurface,
        ),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = OrbsScreenTitle,
                    style = titleStyle,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitleAlpha > 0.01f) {
                    Text(
                        text = OrbsScreenSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer { alpha = subtitleAlpha },
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    if (nestedBackDispatcher.handler?.invoke() != true) {
                        onNavigateBack()
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to catalog",
                    tint = scheme.onSurface,
                )
            }
        },
        actions = {
            DetailPaneTopBarActions(
                guide = guide,
                iconTint = scheme.onSurface,
            )
        },
    )
}

@Composable
private fun OrbsSection(title: String, body: String, chrome: OrbsChrome) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrbsSectionShape,
        color = chrome.readableSurface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = chrome.sectionAccent,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = chrome.bodyColor,
            )
        }
    }
}

@Composable
private fun TipLine(text: String, chrome: OrbsChrome) {
    Text(
        text = "· $text",
        style = MaterialTheme.typography.bodySmall,
        color = chrome.bodyColor,
    )
}

@Composable
private fun OrbAppearanceControls(
    presence: Float,
    onPresenceChange: (Float) -> Unit,
    useMaterialShapes: Boolean,
    onUseMaterialShapesChange: (Boolean) -> Unit,
    randomShapes: Boolean,
    onRandomShapesChange: (Boolean) -> Unit,
    chrome: OrbsChrome,
) {
    val presenceLabel = when {
        presence <= 0.02f -> "Off"
        presence < 0.35f -> "Soft"
        presence < 0.72f -> "Clear"
        else -> "Vivid"
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = OrbsCardShape,
        color = chrome.readableSurface,
        tonalElevation = 0.dp,
    ) {
        Column {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Visibility",
                            style = MaterialTheme.typography.titleSmall,
                            color = chrome.titleColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Hide · Soft · Clear · Vivid",
                            style = MaterialTheme.typography.bodySmall,
                            color = chrome.bodyColor,
                        )
                    }
                    Text(
                        text = "$presenceLabel · ${(presence * 100f).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = chrome.sectionAccent,
                    )
                }
                HapticSlider(
                    value = presence,
                    onValueChange = onPresenceChange,
                    valueRange = 0f..1f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SwitchListItem(
                checked = useMaterialShapes,
                onCheckedChange = onUseMaterialShapesChange,
                containerColor = Color.Transparent,
                headlineContent = {
                    Text(
                        text = "Material shapes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = chrome.titleColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                supportingContent = {
                    Text(
                        text = if (useMaterialShapes) {
                            "Live M3 catalog on playground and backdrop orbs"
                        } else {
                            "Classic circular glow orbs"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = chrome.bodyColor,
                    )
                },
            )

            if (useMaterialShapes) {
                SwitchListItem(
                    checked = randomShapes,
                    onCheckedChange = onRandomShapesChange,
                    containerColor = Color.Transparent,
                    headlineContent = {
                        Text(
                            text = "Random shapes",
                            style = MaterialTheme.typography.bodyLarge,
                            color = chrome.titleColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = if (randomShapes) {
                                "Each orb morphs from a different catalog start"
                            } else {
                                "Every orb shares the same shape"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = chrome.bodyColor,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun OrbsDemoCard(
    modifier: Modifier = Modifier,
    contentScrimAlpha: Float = 0.18f,
    darkStage: Boolean = false,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = OrbsCardShape,
        color = if (darkStage) {
            Color(0xFF12101A)
        } else {
            scheme.surface.copy(alpha = contentScrimAlpha)
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        content()
    }
}

@Composable
private fun MeshPresetTile(
    preset: MeshPreset,
    selected: Boolean,
    onClick: () -> Unit,
    chrome: OrbsChrome,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val field = remember(preset, scheme) { preset.themeTinted(scheme) }
    Surface(
        onClick = onClick,
        modifier = modifier.height(92.dp),
        shape = OrbsTileShape,
        color = if (selected) {
            chrome.readableSurfaceStrong
        } else {
            chrome.readableSurface
        },
        tonalElevation = 0.dp,
        border = if (selected) {
            BorderStroke(2.dp, chrome.sectionAccent)
        } else {
            null
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                drawMeshGradient(field, baseAlpha = 0.88f, blendMode = chrome.meshBlendMode)
            }
            Text(
                text = preset.title,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) chrome.sectionAccent else chrome.titleColor,
            )
        }
    }
}

@Composable
private fun MeshPreviewCard(
    preset: MeshPreset,
    chrome: OrbsChrome,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val field = remember(preset, scheme) { preset.themeTinted(scheme) }
    Surface(
        modifier = modifier.height(128.dp),
        shape = OrbsTileShape,
        color = chrome.readableSurface,
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.fillMaxSize()) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                drawMeshGradient(field, baseAlpha = 0.88f, blendMode = chrome.meshBlendMode)
            }
            Text(
                text = preset.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = chrome.titleColor,
            )
        }
    }
}

@Composable
private fun BlendCompareCard(
    sample: BlendModeSample,
    chrome: OrbsChrome,
    presence: Float,
    expressive: Boolean,
    randomShapes: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val field = remember(scheme) { MeshPreset.Midnight.themeTinted(scheme) }
    val animatedClock = rememberExpressiveShapeMorphClock(
        enabled = expressive,
        tier = ExpressiveShapeCatalogTier.OrbField,
        tempo = ExpressiveMorphTempo.Soft,
    )
    val compareOrbPhases = remember {
        List(CompareOrbCenters.size) {
            expressivePhaseOffset(Random.nextInt(), ExpressiveShapeCatalogTier.OrbField)
        }
    }
    val sharedPhase = remember {
        expressivePhaseOffset(sample.label.hashCode(), ExpressiveShapeCatalogTier.OrbField)
    }
    val shapeClock = if (expressive) animatedClock else 0f
    val orbPhases = if (randomShapes) {
        compareOrbPhases
    } else {
        List(CompareOrbCenters.size) { sharedPhase }
    }
    Surface(
        modifier = modifier.height(168.dp),
        shape = OrbsTileShape,
        color = chrome.readableSurface,
        tonalElevation = 0.dp,
    ) {
        Column(Modifier.fillMaxSize()) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                drawMeshGradient(field, baseAlpha = 0.92f, blendMode = BlendMode.Screen)
                drawGlowOrbs(
                    colors = chrome.playgroundOrbColors,
                    normalizedCenters = CompareOrbCenters,
                    blendMode = sample.mode,
                    strength = 0.98f,
                    radiusFraction = 0.28f,
                    presence = presence,
                    expressive = expressive,
                    shapeClock = shapeClock,
                    orbPhaseOffsets = orbPhases,
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = sample.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = chrome.titleColor,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = sample.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = chrome.bodyColor,
                )
            }
        }
    }
}

@Composable
private fun InteractiveOrbsCanvas(
    meshField: MeshColorField,
    orbColors: List<Color>,
    isDark: Boolean,
    tilt: GyroTilt,
    reduceMotion: Boolean,
    presence: Float,
    expressive: Boolean,
    randomShapes: Boolean,
    onOrbGrab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var draggedIndex by remember { mutableIntStateOf(-1) }
    val orbBases = remember {
        listOf(
            Offset(0.22f, 0.28f),
            Offset(0.58f, 0.48f),
            Offset(0.38f, 0.72f),
            Offset(0.78f, 0.32f),
        )
    }

    val animOffsets = remember { List(4) { Animatable(Offset.Zero, Offset.VectorConverter) } }
    val dragOffsets = remember { List(4) { Animatable(Offset.Zero, Offset.VectorConverter) } }
    val driftDurations = remember { List(4) { Random.nextInt(9000, 15000) } }
    val randomPhaseOffsets = remember {
        List(4) { expressivePhaseOffset(Random.nextInt(), ExpressiveShapeCatalogTier.OrbField) }
    }
    val sharedPhase = remember {
        expressivePhaseOffset(0x4F524253, ExpressiveShapeCatalogTier.OrbField)
    }
    val orbPhaseOffsets = if (randomShapes) randomPhaseOffsets else List(4) { sharedPhase }
    val shapeClock = rememberExpressiveShapeMorphClock(
        enabled = expressive,
        tier = ExpressiveShapeCatalogTier.OrbField,
        tempo = ExpressiveMorphTempo.Soft,
    )

    animOffsets.forEachIndexed { index, anim ->
        LaunchedEffect(index, reduceMotion, meshField) {
            if (reduceMotion) return@LaunchedEffect
            while (true) {
                val dur = driftDurations[index]
                anim.animateTo(
                    Offset(Random.nextFloat() * 0.16f, Random.nextFloat() * 0.16f),
                    tween(dur),
                )
                anim.animateTo(Offset.Zero, tween(dur))
            }
        }
    }

    Canvas(
        modifier = modifier.pointerInput(meshField) {
            detectDragGestures(
                onDragStart = { offset ->
                    draggedIndex = orbBases.indices.minByOrNull { i ->
                        val cx = size.width * orbBases[i].x
                        val cy = size.height * orbBases[i].y
                        (offset - Offset(cx, cy)).getDistance()
                    } ?: -1
                    if (draggedIndex >= 0) onOrbGrab()
                },
                onDrag = { change, drag ->
                    change.consume()
                    val idx = draggedIndex
                    if (idx in dragOffsets.indices) {
                        scope.launch { dragOffsets[idx].snapTo(dragOffsets[idx].value + drag) }
                    }
                },
                onDragEnd = {
                    val idx = draggedIndex
                    draggedIndex = -1
                    if (idx in dragOffsets.indices) {
                        scope.launch {
                            dragOffsets[idx].animateTo(
                                Offset.Zero,
                                ExpressiveMotion.dragSettle,
                            )
                        }
                    }
                },
                onDragCancel = {
                    val idx = draggedIndex
                    draggedIndex = -1
                    if (idx in dragOffsets.indices) {
                        scope.launch {
                            dragOffsets[idx].animateTo(Offset.Zero, ExpressiveMotion.dragSettle)
                        }
                    }
                },
            )
        },
    ) {
        val parallax = if (reduceMotion) {
            Offset.Zero
        } else {
            Offset(tilt.roll.coerceIn(-0.2f, 0.2f), tilt.pitch.coerceIn(-0.2f, 0.2f))
        }
        if (!isDark) {
            drawRect(Color(0xFF12101A))
        }
        drawMeshGradient(
            meshField,
            baseAlpha = if (isDark) 0.84f else 0.82f,
            blendMode = BlendMode.Screen,
            parallax = parallax,
        )

        val tiltX = parallax.x
        val tiltY = parallax.y

        orbBases.forEachIndexed { index, base ->
            val anim = animOffsets[index].value
            val drag = dragOffsets[index].value
            val cx = size.width * (base.x + anim.x + tiltX * 0.12f) + drag.x
            val cy = size.height * (base.y + anim.y + tiltY * 0.12f) + drag.y
            val isDragged = index == draggedIndex

            drawGlowOrbs(
                colors = listOf(orbColors[index % orbColors.size]),
                normalizedCenters = listOf(Offset(cx / size.width, cy / size.height)),
                blendMode = BlendMode.Screen,
                strength = if (isDragged) 1.1f else 0.98f,
                radiusFraction = if (isDragged) 0.30f else 0.28f,
                vividCore = false,
                presence = presence,
                expressive = expressive,
                shapeClock = shapeClock,
                selectedIndex = if (isDragged) 0 else -1,
                orbPhaseOffsets = listOf(orbPhaseOffsets[index]),
            )
        }
    }
}
