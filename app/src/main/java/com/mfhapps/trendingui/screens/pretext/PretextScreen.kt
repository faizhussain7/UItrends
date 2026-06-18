package com.mfhapps.trendingui.screens.pretext

import android.graphics.Typeface
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import com.mfhapps.trendingui.ui.components.HapticSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mfhapps.trendingui.navigation.pretextModeEnterForward
import com.mfhapps.trendingui.navigation.pretextModeExitForward
import com.mfhapps.trendingui.navigation.pretextModePopEnter
import com.mfhapps.trendingui.navigation.pretextModePopExit
import com.mfhapps.trendingui.core.FpsOverlay
import com.mfhapps.trendingui.core.text.MeasuredTextLayout
import com.mfhapps.trendingui.core.text.PreparedText
import com.mfhapps.trendingui.core.text.PrepareOptions
import com.mfhapps.trendingui.core.text.TextMeasurementEngine
import com.mfhapps.trendingui.core.text.WhiteSpaceMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.mfhapps.trendingui.ui.components.ContainedLoadingIndicator
import com.mfhapps.trendingui.ui.components.MeasuredTextBlock
import com.mfhapps.trendingui.ui.components.PretextLineCanvas
import com.mfhapps.trendingui.ui.components.PretextParagraphCanvas
import androidx.compose.runtime.saveable.rememberSaveable
import com.mfhapps.trendingui.ui.detail.DetailPaneGuideAction
import com.mfhapps.trendingui.ui.detail.LocalNestedBackDispatcher
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.detail.NestedBackEffect
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretextScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
) {
    var screenMode by rememberSaveable { mutableStateOf(PretextScreenMode.Playground) }
    var cameraMeasureMode by remember { mutableStateOf(PretextMeasureMode.Engine) }
    var visionTrackMode by remember { mutableStateOf(VisionTrackMode.Person) }
    var showBoundingBox by remember { mutableStateOf(true) }
    var cameraStage by remember { mutableStateOf(PretextCameraStage.CameraOverlay) }
    var cameraTooltipBlur by rememberSaveable { mutableStateOf(false) }

    val exitCamera = { screenMode = PretextScreenMode.Playground }

    NestedBackEffect(
        enabled = screenMode == PretextScreenMode.Camera,
        onBack = exitCamera,
    )

    AnimatedContent(
        targetState = screenMode,
        transitionSpec = {
            if (targetState == PretextScreenMode.Camera) {
                pretextModeEnterForward().togetherWith(pretextModeExitForward())
            } else {
                pretextModePopEnter().togetherWith(pretextModePopExit())
            }
        },
        label = "pretext_screen_mode",
    ) { mode ->
        when (mode) {
            PretextScreenMode.Camera -> {
                Box(Modifier.fillMaxSize()) {
                    PretextCameraPanel(
                        measureMode = cameraMeasureMode,
                        onMeasureModeChange = { cameraMeasureMode = it },
                        trackMode = visionTrackMode,
                        onTrackModeChange = { visionTrackMode = it },
                        stage = cameraStage,
                        onStageChange = { cameraStage = it },
                        showBoundingBox = showBoundingBox,
                        onShowBoundingBoxChange = { showBoundingBox = it },
                        tooltipBlurEnabled = cameraTooltipBlur,
                        onTooltipBlurEnabledChange = { cameraTooltipBlur = it },
                        onExitCamera = exitCamera,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            PretextScreenMode.Playground -> PretextPlaygroundScaffold(
                screenMode = screenMode,
                onScreenModeChange = { screenMode = it },
                onNavigateBack = onNavigateBack,
                guide = guide,
                tooltipBlurEnabled = cameraTooltipBlur,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PretextPlaygroundScaffold(
    screenMode: PretextScreenMode,
    onScreenModeChange: (PretextScreenMode) -> Unit,
    onNavigateBack: () -> Unit,
    guide: DemoTrendGuide?,
    tooltipBlurEnabled: Boolean,
) {
    val nestedBackDispatcher = LocalNestedBackDispatcher.current
    val listState = rememberLazyListState()
    val showCameraInHeader by rememberPretextPlaygroundCameraInHeader(listState)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Pretext",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Text engine + live camera",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back to catalog",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    PretextPlaygroundCameraHeaderAction(
                        visible = showCameraInHeader,
                        onClick = { onScreenModeChange(PretextScreenMode.Camera) },
                        blurBackdropEnabled = tooltipBlurEnabled,
                    )
                    if (guide != null) {
                        DetailPaneGuideAction(
                            guide = guide,
                            iconTint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        PretextPlaygroundContent(
            listState = listState,
            screenMode = screenMode,
            onScreenModeChange = onScreenModeChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PretextPlaygroundContent(
    listState: LazyListState,
    screenMode: PretextScreenMode,
    onScreenModeChange: (PretextScreenMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)

    var customText by remember { mutableStateOf(TextScript.Latin.sample) }
    var layoutMode by remember { mutableStateOf(PretextLayoutMode.Uniform) }
    var selectedScript by remember { mutableStateOf(TextScript.Latin) }
    var selectedFeature by remember { mutableStateOf(PretextFeature.Balance) }

    var containerWidthDp by remember { mutableFloatStateOf(300f) }
    var obstacleFraction by remember { mutableFloatStateOf(0.55f) }
    var prepared by remember { mutableStateOf<PreparedText?>(null) }
    var liveLayout by remember { mutableStateOf<MeasuredTextLayout?>(null) }
    var prepareMs by remember { mutableLongStateOf(0L) }
    var layout10kNs by remember { mutableLongStateOf(0L) }
    var engineHeight by remember { mutableIntStateOf(0) }
    var viewHeight by remember { mutableIntStateOf(0) }

    var benchEngineMs by remember { mutableLongStateOf(0L) }
    var benchViewMs by remember { mutableLongStateOf(0L) }
    var ratio by remember { mutableStateOf("—") }
    var running by remember { mutableStateOf(false) }
    var lastMeasure by remember { mutableLongStateOf(0L) }

    val fontSizePx = with(density) { 16.sp.toPx() }
    val lineHeightPx = with(density) { 22.sp.toPx() }
    val widthPx = with(density) { containerWidthDp.dp.roundToPx() }
    val obstaclePx = (widthPx * obstacleFraction).toInt()

    val benchmarkItems = remember {
        TextScript.entries.flatMap { script ->
            (0 until 100).map { i -> "${script.sample} #$i" }
        }
    }

    var virtualItems by remember { mutableStateOf<List<MeasuredTextLayout>>(emptyList()) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                if (scrolling) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
    }

    var wasImeOpen by remember { mutableStateOf(false) }
    LaunchedEffect(imeBottomPx) {
        val imeOpen = imeBottomPx > 0
        if (wasImeOpen && !imeOpen) {
            focusManager.clearFocus()
        }
        wasImeOpen = imeOpen
    }

    LaunchedEffect(widthPx) {
        virtualItems = withContext(Dispatchers.Default) {
            TextMeasurementEngine.prepareBatch(benchmarkItems.take(30), fontSizePx, Typeface.DEFAULT)
                .map { TextMeasurementEngine.layout(it, widthPx, lineHeightPx) }
        }
    }

    @OptIn(FlowPreview::class)
    LaunchedEffect(fontSizePx) {
        snapshotFlow { customText }
            .debounce(280)
            .distinctUntilChanged()
            .collect { text ->
                val options = if (text.contains('\n')) {
                    PrepareOptions(whiteSpace = WhiteSpaceMode.PreWrap)
                } else {
                    PrepareOptions()
                }
                val elapsed = measureTimeMillis {
                    prepared = TextMeasurementEngine.prepare(text, fontSizePx, Typeface.DEFAULT, options)
                }
                prepareMs = elapsed
            }
    }

    LaunchedEffect(prepared, widthPx, lineHeightPx, layoutMode, obstaclePx) {
        val prep = prepared ?: return@LaunchedEffect
        val layout = withContext(Dispatchers.Default) {
            computeLayout(prep, layoutMode, widthPx, lineHeightPx, obstaclePx)
        }
        liveLayout = layout
        engineHeight = layout.height
        val ns = withContext(Dispatchers.Default) {
            measureNanoTime {
                repeat(10_000) {
                    computeLayout(prep, layoutMode, widthPx, lineHeightPx, obstaclePx)
                }
            }
        }
        layout10kNs = ns
        viewHeight = withContext(Dispatchers.Default) {
            TextMeasurementEngine.measureViewHeight(
                context,
                prep.originalText,
                fontSizePx / density.density,
                widthPx,
            )
        }
        lastMeasure = prepareMs + (ns / 1_000_000 / 10)
    }

    val perLayoutMicroseconds = if (layout10kNs > 0) layout10kNs / 10_000 / 1_000 else 0L
    val attachState = rememberPretextStickyAttachState(listState)

    val runBenchmark: () -> Unit = {
        running = true
        scope.launch {
            benchEngineMs = withContext(Dispatchers.Default) {
                measureTimeMillis {
                    TextMeasurementEngine
                        .prepareBatch(benchmarkItems, fontSizePx, Typeface.DEFAULT)
                        .forEach { TextMeasurementEngine.layout(it, widthPx, lineHeightPx) }
                }
            }
            benchViewMs = withContext(Dispatchers.Default) {
                measureTimeMillis {
                    benchmarkItems.forEach { text ->
                        TextMeasurementEngine.measureViewHeight(
                            context,
                            text,
                            fontSizePx / density.density,
                            widthPx,
                        )
                    }
                }
            }
            ratio = if (benchEngineMs > 0) {
                "${benchViewMs / benchEngineMs.coerceAtLeast(1)}× vs View"
            } else {
                "—"
            }
            lastMeasure = benchEngineMs
            running = false
        }
    }

    FpsOverlay(lastMeasureMs = lastMeasure) {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .appHazeSource(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "intro") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PretextHeroCard()
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        tonalElevation = 1.dp,
                    ) {
                        PretextModeSelector(
                            selected = screenMode,
                            onSelected = onScreenModeChange,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                    PretextHowItWorksStrip()
                }
            }

            item(key = "playground_title") {
                PretextSectionTitle(
                    title = "Playground",
                    subtitle = "Tune the engine and inspect every layout decision.",
                    icon = Icons.Outlined.Tune,
                )
            }

            item(key = "layout_mode") {
                PretextLayoutModeSelector(
                    selected = layoutMode,
                    onSelected = { layoutMode = it },
                )
            }

            item(key = "script") {
                PretextScriptChips(
                    selected = selectedScript,
                    onSelected = { script ->
                        selectedScript = script
                        customText = script.sample
                    },
                )
            }

            stickyHeader(key = PretextPlaygroundListKeys.StickyStack) {
                PretextPlaygroundStickyStack(
                    attachState = attachState,
                    customText = customText,
                    onCustomTextChange = { customText = it },
                    containerWidthDp = containerWidthDp,
                    onContainerWidthChange = { containerWidthDp = it },
                    layoutMode = layoutMode,
                    obstacleFraction = obstacleFraction,
                    onObstacleFractionChange = { obstacleFraction = it },
                    running = running,
                    onRunBenchmark = runBenchmark,
                )
            }

            item(key = PretextPlaygroundListKeys.LivePreview) {
                ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PretextSectionTitle(
                            title = "Live preview",
                            subtitle = layoutMode.hint,
                            icon = Icons.Outlined.ViewAgenda,
                        )
                        Text(
                            "Container ${containerWidthDp.toInt()}dp",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        if (!attachState.controlsAttached) {
                            HapticSlider(
                                value = containerWidthDp,
                                onValueChange = { containerWidthDp = it },
                                valueRange = 120f..360f,
                            )
                        }
                        if (layoutMode == PretextLayoutMode.VariableWidth ||
                            layoutMode == PretextLayoutMode.ReflowCompare
                        ) {
                            Text(
                                "Obstacle ${(obstacleFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            if (!attachState.controlsAttached) {
                                HapticSlider(
                                    value = obstacleFraction,
                                    onValueChange = { obstacleFraction = it },
                                    valueRange = 0.25f..0.9f,
                                )
                            }
                        }
                        PretextMetricsGrid(
                            items = listOf(
                                "Lines" to "${liveLayout?.lineCount ?: 0}",
                                "Height" to "${engineHeight}px",
                                "prepare" to "${prepareMs}ms",
                                "layout()" to "${perLayoutMicroseconds}µs",
                                "Engine vs View" to
                                    "${engineHeight}px / ${viewHeight}px",
                                "Δ" to "${abs(engineHeight - viewHeight)}px",
                            ),
                        )
                        if (layoutMode == PretextLayoutMode.ReflowCompare) {
                            PretextReflowComparePanel(
                                prepared = prepared,
                                containerWidthPx = widthPx,
                                lineHeightPx = lineHeightPx,
                                obstacleLeftPx = obstaclePx,
                                fontSizePx = fontSizePx,
                            )
                        } else {
                            liveLayout?.let { layout ->
                                PretextLineCanvas(
                                    layout = layout,
                                    containerWidthPx = widthPx,
                                    lineHeightPx = lineHeightPx,
                                    obstacleStartPx = if (layoutMode == PretextLayoutMode.VariableWidth) {
                                        obstaclePx
                                    } else {
                                        null
                                    },
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(with(density) { layout.height.toDp().coerceAtLeast(48.dp) }),
                                ) {
                                    PretextParagraphCanvas(
                                        layout = layout,
                                        fontSizePx = fontSizePx,
                                        lineHeightPx = lineHeightPx,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                PretextSectionTitle(
                    title = "Engine showcase",
                    subtitle = "Each demo runs the new engine API live.",
                    icon = Icons.Outlined.AutoAwesome,
                )
            }

            item {
                PretextFeatureChips(
                    selected = selectedFeature,
                    onSelected = { selectedFeature = it },
                )
            }

            item {
                PretextFeatureShowcase(
                    feature = selectedFeature,
                    prepared = prepared,
                    typeface = Typeface.DEFAULT,
                )
            }

            item(key = PretextPlaygroundListKeys.Benchmark) {
                ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PretextSectionTitle(
                            title = "500-block benchmark",
                            subtitle = "prepareBatch() + layout() vs View.measure on the same strings.",
                            icon = Icons.Outlined.BarChart,
                        )
                        if (running) {
                            ContainedLoadingIndicator()
                        } else {
                            PretextMetricsGrid(
                                items = listOf(
                                    "Engine" to "${benchEngineMs}ms",
                                    "View.measure" to "${benchViewMs}ms",
                                    "Ratio" to ratio,
                                ),
                            )
                        }
                        if (!attachState.runAttached) {
                            Button(
                                enabled = !running,
                                onClick = runBenchmark,
                            ) { Text(if (running) "Running…" else "Run benchmark") }
                        }
                    }
                }
            }

            item(key = "virtual_list_title") {
                Text(
                    "Virtual list — engine heights",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            itemsIndexed(
                items = virtualItems,
                key = { index, _ -> "virtual_$index" },
                contentType = { _, _ -> "virtual_block" },
            ) { _, layout ->
                MeasuredTextBlock(layout = layout)
            }
        }
    }
}

private fun computeLayout(
    prep: PreparedText,
    mode: PretextLayoutMode,
    widthPx: Int,
    lineHeightPx: Float,
    obstaclePx: Int,
): MeasuredTextLayout = when (mode) {
    PretextLayoutMode.Uniform -> TextMeasurementEngine.layout(prep, widthPx, lineHeightPx)
    PretextLayoutMode.VariableWidth -> TextMeasurementEngine.layoutAroundObstacle(
        prepared = prep,
        containerWidthPx = widthPx,
        lineHeightPx = lineHeightPx,
        obstacleLeftPx = obstaclePx,
        obstacleTopPx = lineHeightPx,
        obstacleHeightPx = lineHeightPx * 2.5f,
    )
    PretextLayoutMode.LineRanges -> {
        val uniform = TextMeasurementEngine.layout(prep, widthPx, lineHeightPx)
        uniform.copy(lineWidthsPx = List(uniform.lineCount) { widthPx.toFloat() })
    }
    PretextLayoutMode.ReflowCompare -> TextMeasurementEngine.layout(prep, widthPx, lineHeightPx)
}
