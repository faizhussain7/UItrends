package com.mfhapps.trendingui.screens.pretext

import android.app.Activity
import android.Manifest
import android.graphics.Rect
import android.content.pm.PackageManager
import android.graphics.Typeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.TouchApp
import com.mfhapps.trendingui.ui.components.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.mfhapps.trendingui.ui.platform.appBarTopWindowInsets
import com.mfhapps.trendingui.ui.components.AppModalBottomSheet
import com.mfhapps.trendingui.ui.components.AppModalSheetStack
import com.mfhapps.trendingui.ui.components.ButtonGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mfhapps.trendingui.R
import com.mfhapps.trendingui.TrendingApplication
import com.mfhapps.trendingui.launcher.LauncherIconManager
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.material3.LocalContentColor
import android.graphics.Paint
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mfhapps.trendingui.core.text.LayoutRegion
import com.mfhapps.trendingui.core.text.PolygonObstacle
import com.mfhapps.trendingui.core.text.PositionedTextLayout
import com.mfhapps.trendingui.core.text.PreparedText
import com.mfhapps.trendingui.core.text.TextMeasurementEngine
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.components.LoadingIndicator
import com.mfhapps.trendingui.ui.components.PretextPositionedCanvas
import com.mfhapps.trendingui.ui.components.SwitchListItem
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PARAGRAPH = (
    "Point your camera at a person, face, or object. Pretext measures this paragraph once, " +
        "then reflows every line around the detected silhouette every frame. " +
        "Native Paint widths, pure-Kotlin layout arithmetic — closer to 0.0002 ms per layout. "
    ).repeat(15)

@ExperimentalGetImage
@Composable
fun PretextCameraPanel(
    measureMode: PretextMeasureMode,
    onMeasureModeChange: (PretextMeasureMode) -> Unit,
    trackMode: VisionTrackMode,
    onTrackModeChange: (VisionTrackMode) -> Unit,
    stage: PretextCameraStage,
    onStageChange: (PretextCameraStage) -> Unit,
    showBoundingBox: Boolean,
    onShowBoundingBoxChange: (Boolean) -> Unit,
    tooltipBlurEnabled: Boolean,
    onTooltipBlurEnabledChange: (Boolean) -> Unit,
    onExitCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val launcherIcon = remember(context) { LauncherIconManager.readActiveOrDefault(context) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasCameraPermission = granted }

    val fontSizePx = with(density) { 13.sp.toPx() }
    val lineHeightPx = with(density) { 18.sp.toPx() }
    val paddingPx = with(density) { 16.dp.toPx() }

    var prepared by remember { mutableStateOf<PreparedText?>(null) }
    var headlinePrepared by remember { mutableStateOf<PreparedText?>(null) }
    var bodyPrepared by remember { mutableStateOf<PreparedText?>(null) }
    var textLayoutStyle by remember { mutableStateOf(PretextCameraTextLayoutStyle.Newspaper) }
    var showSpotlight by remember { mutableStateOf(false) }
    var spotlightStrength by remember { mutableFloatStateOf(0.55f) }
    var showBlur by remember { mutableStateOf(false) }
    var blurRadiusDp by remember { mutableFloatStateOf(16f) }
    var showHalftone by remember { mutableStateOf(false) }
    var previewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val previewLayoutCache = remember { PretextPreviewLayoutCache() }
    val cameraSession = remember { PretextCameraSession(context) }
    val appContainer = remember(context) {
        (context.applicationContext as TrendingApplication).container
    }
    var torchOn by remember { mutableStateOf(false) }
    var torchAvailable by remember { mutableStateOf(false) }
    val recordingController = remember(appContainer) { appContainer.pretextRecordingController }
    val recordingSession by recordingController.state.collectAsStateWithLifecycle(
        initialValue = PretextRecordingSessionState.Idle,
    )
    val isRecording = recordingSession is PretextRecordingSessionState.Active
    val recordingStartedAt = (recordingSession as? PretextRecordingSessionState.Active)?.startedAtMillis ?: 0L
    val recordingRepository = remember { appContainer.pretextRecordings }
    val recordingSettingsRepository = remember { appContainer.pretextRecordingSettings }
    val recordingSettings by recordingSettingsRepository.settings.collectAsStateWithLifecycle(
        initialValue = PretextRecordingSettings(),
    )
    val recordingProfile = remember(recordingSettings) {
        recordingSettingsRepository.resolveProfile(recordingSettings)
    }
    var showRecordingsSheet by remember { mutableStateOf(false) }
    var captureBounds by remember { mutableStateOf<Rect?>(null) }
    val telemetryState = remember { mutableStateOf(VisionTelemetry()) }
    var telemetry by telemetryState

    val viewShapeState = remember { mutableStateOf<ViewShape?>(null) }
    var viewShape by viewShapeState
    var extraShapes by remember { mutableStateOf<List<ViewShape>>(emptyList()) }
    var editorialOrbs by remember { mutableStateOf<List<ViewShape>>(emptyList()) }
    var pinVisionContour by remember { mutableStateOf(true) }
    var draggingOrbIndex by remember { mutableIntStateOf(-1) }
    var manualOverride by remember { mutableStateOf(false) }
    var trackModeState by remember { mutableStateOf(trackMode) }
    var stageState by remember { mutableStateOf(stage) }
    val manualOverrideState = rememberUpdatedState(manualOverride)
    val stageTheme = stage.theme()

    LaunchedEffect(stage) { stageState = stage }

    LaunchedEffect(trackMode) {
        trackModeState = trackMode
    }

    val appContext = LocalContext.current.applicationContext
    val lensFacingState = remember { mutableIntStateOf(cameraSession.lensFacing) }
    LaunchedEffect(cameraSession.lensFacing) {
        lensFacingState.intValue = cameraSession.lensFacing
    }

    val pipeline = remember(appContext, previewLayoutCache) {
        PretextCameraPipeline(
            appContext = appContext,
            previewLayoutCache = previewLayoutCache,
            trackModeProvider = { trackModeState },
            stageProvider = { stageState },
            lensFacingProvider = { lensFacingState.intValue },
            onVisionFrame = { frame ->
                scope.launch(Dispatchers.Main.immediate) {
                    if (!manualOverrideState.value) {
                        viewShapeState.value = frame.primary
                        extraShapes = frame.extraShapes
                    }
                }
            },
            onTelemetry = { sample ->
                scope.launch(Dispatchers.Main.immediate) {
                    telemetryState.value = sample
                }
            },
                previewBitmapEnabledProvider = { showBlur },
                onPreviewBitmap = { bmp ->
                    scope.launch(Dispatchers.Main.immediate) {
                        previewBitmap?.recycle()
                        previewBitmap = bmp
                    }
                },
        )
    }

    val outlineAlpha by animateFloatAsState(if (showBoundingBox) 1f else 0f, label = "outline")
    val previewVisibility by animateFloatAsState(
        if (stage.showsLivePreview) 1f else 0f,
        label = "previewAlpha",
    )

    val generativeTransition = rememberInfiniteTransition(label = "generative")
    val generativePhase by generativeTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "generativePhase",
    )

    LaunchedEffect(prepared, textLayoutStyle, fontSizePx) {
        val prep = prepared ?: run {
            headlinePrepared = null
            bodyPrepared = null
            return@LaunchedEffect
        }
        if (textLayoutStyle == PretextCameraTextLayoutStyle.Newspaper ||
            textLayoutStyle == PretextCameraTextLayoutStyle.Magazine
        ) {
            val headlineFontPx = fontSizePx * 1.22f
            val (headline, body) = withContext(Dispatchers.Default) {
                PretextCameraReflowScheduler.prepareHeadlineBody(
                    fullText = prep.originalText,
                    headlineFontSizePx = headlineFontPx,
                    bodyFontSizePx = fontSizePx,
                )
            }
            headlinePrepared = headline
            bodyPrepared = body
        } else {
            headlinePrepared = null
            bodyPrepared = null
        }
    }

    LaunchedEffect(fontSizePx) {
        withContext(Dispatchers.Default) {
            prepared = TextMeasurementEngine.prepareSync(
                PARAGRAPH,
                fontSizePx,
                Typeface.create(Typeface.SERIF, Typeface.NORMAL),
            )
        }
    }

    DisposableEffect(activity, recordingController) {
        recordingController.attachActivity(activity)
        recordingController.setCaptureBoundsProvider { captureBounds }
        onDispose {
            recordingController.detachActivity(activity)
            recordingController.setCaptureBoundsProvider { null }
        }
    }

    DisposableEffect(lifecycleOwner, recordingController) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> recordingController.onUiHidden()
                Lifecycle.Event.ON_RESUME -> recordingController.onUiVisible()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            previewLayoutCache.detach()
            pipeline.close()
            recordingController.releaseIfIdle()
            cameraSession.close()
            PretextVisionLog.resetSession()
        }
    }

    LaunchedEffect(previewView, hasCameraPermission, cameraSession.lensFacing) {
        if (!hasCameraPermission) return@LaunchedEffect
        val pv = previewView ?: return@LaunchedEffect
        val mirrorX = cameraSession.lensFacing == CameraSelector.LENS_FACING_FRONT
        previewLayoutCache.attach(pv, mirrorX)
        cameraSession.bind(pv, lifecycleOwner, pipeline)
        torchAvailable = cameraSession.hasTorch()
    }

    if (!hasCameraPermission) {
        CameraPermissionCard(
            onGrant = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            modifier = modifier,
        )
        return
    }

    val hud = rememberPretextCameraHudState(isRecording = isRecording)
    LaunchedEffect(isRecording) {
        if (!isRecording) {
            hud.reveal()
        }
    }
    var measureSpeed by remember { mutableStateOf(PretextMeasureSpeed()) }

    val hudContent = buildPretextCameraHudContent(
        manualOverride = manualOverride,
        viewShape = viewShape,
        editorialOrbs = editorialOrbs,
        trackMode = trackModeState,
        telemetry = telemetry,
        stage = stage,
        stageTheme = stageTheme,
        torchAvailable = torchAvailable && stage.showsLivePreview,
        torchOn = torchOn,
        measureMode = measureMode,
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .appHazeSource()
            .pretextCameraHudRevealOnTap(hud),
    ) {
        val wPx = with(density) { maxWidth.roundToPx().toFloat() }
        val hPx = with(density) { maxHeight.roundToPx().toFloat() }

        LaunchedEffect(stage, wPx, hPx) {
            if (stage.supportsManualOrbs && editorialOrbs.isEmpty() && wPx > 0f && hPx > 0f) {
                editorialOrbs = listOf(
                    editorialOrbShapeAt(wPx * 0.30f, hPx * 0.36f, wPx, hPx),
                    editorialOrbShapeAt(wPx * 0.70f, hPx * 0.58f, wPx, hPx),
                )
            }
        }

        val layoutShapes = remember(
            stage,
            viewShape,
            extraShapes,
            editorialOrbs,
            pinVisionContour,
            wPx,
            hPx,
            generativePhase,
        ) {
            when {
                stage.supportsGenerativeObstacle && wPx > 0f && hPx > 0f ->
                    listOf(generativeOrbAt(wPx, hPx, generativePhase))
                stage.supportsManualOrbs -> buildList {
                    addAll(editorialOrbs)
                    if (pinVisionContour) viewShape?.let { add(it) }
                    if (stage.supportsMultiObstacle) addAll(extraShapes)
                }.take(3)
                else -> listOfNotNull(viewShape)
            }
        }

        var layoutFingerprint by remember { mutableIntStateOf(0) }
        var layoutShapesSnapshot by remember { mutableStateOf<List<ViewShape>>(emptyList()) }

        val faceReflowActive = layoutShapes.any { it.source == VisionSource.Face } ||
            trackModeState == VisionTrackMode.Face

        LaunchedEffect(layoutShapes, measureMode, faceReflowActive) {
            val pollMs = if (faceReflowActive) 33L else 66L
            while (isActive) {
                val fp = PretextCameraReflowScheduler.layoutFingerprint(layoutShapes)
                if (fp != layoutFingerprint) {
                    layoutFingerprint = fp
                    layoutShapesSnapshot = layoutShapes
                }
                delay(pollMs)
            }
        }

        val layoutStyleForReflow = textLayoutStyle
        val cameraTypeface = remember {
            Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }

        var layout by remember { mutableStateOf<PositionedTextLayout?>(null) }

        LaunchedEffect(
            prepared,
            headlinePrepared,
            bodyPrepared,
            layoutStyleForReflow,
            measureMode,
            wPx,
            hPx,
            layoutShapesSnapshot,
            layoutFingerprint,
            fontSizePx,
            lineHeightPx,
            paddingPx,
        ) {
            if (wPx <= 0f || hPx <= 0f) {
                layout = null
                return@LaunchedEffect
            }
            val region = LayoutRegion(
                x = paddingPx,
                y = paddingPx,
                width = wPx - paddingPx * 2f,
                height = hPx - paddingPx * 2f,
            )
            val needsPrepared = measureMode == PretextMeasureMode.Engine ||
                layoutStyleForReflow == PretextCameraTextLayoutStyle.DynamicFloat ||
                layoutStyleForReflow == PretextCameraTextLayoutStyle.Newspaper ||
                layoutStyleForReflow == PretextCameraTextLayoutStyle.Magazine
            if (needsPrepared && prepared == null) {
                layout = null
                return@LaunchedEffect
            }

            val request = CameraReflowRequest(
                prepared = prepared,
                sourceText = PARAGRAPH,
                fontSizePx = fontSizePx,
                typeface = cameraTypeface,
                shapes = layoutShapesSnapshot,
                region = region,
                lineHeightPx = lineHeightPx,
                style = layoutStyleForReflow,
                pageWidthPx = wPx,
                pageHeightPx = hPx,
                paddingPx = paddingPx,
                headlinePrepared = headlinePrepared,
                bodyPrepared = bodyPrepared,
            )
            val result = withContext(Dispatchers.Default) {
                PretextCameraReflowBenchmark.run(
                    request = request,
                    activeMode = measureMode,
                )
            }
            layout = result.layout
            measureSpeed = measureSpeed.blend(result.sample)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    if (isRecording) return@onGloballyPositioned
                    val bounds = coordinates.boundsInWindow()
                    captureBounds = Rect(
                        bounds.left.toInt(),
                        bounds.top.toInt(),
                        bounds.right.toInt(),
                        bounds.bottom.toInt(),
                    )
                },
        ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            update = { pv ->
                previewView = pv
                val mirrorX = cameraSession.lensFacing == CameraSelector.LENS_FACING_FRONT
                previewLayoutCache.attach(pv, mirrorX)
            },
        )

        val bmp = previewBitmap
        if (showBlur && bmp != null && stage.showsLivePreview && stage != PretextCameraStage.Ascii && stage != PretextCameraStage.VintageNews) {
            BlurredPreviewOverlay(
                bitmap = bmp,
                shape = viewShape,
                blurRadius = blurRadiusDp.dp,
                alpha = previewVisibility,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if ((showHalftone || stage == PretextCameraStage.VintageNews) && bmp != null && stage.showsLivePreview) {
            HalftonePreviewOverlay(
                bitmap = bmp,
                alpha = previewVisibility,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (stage.usesPaperBackdrop && previewVisibility < 0.99f) {
            StudioPaperMaskOverlay(
                shape = viewShape,
                modifier = Modifier.fillMaxSize(),
                alpha = 1f - previewVisibility,
            )
        }

        if (stage == PretextCameraStage.Terminal && previewVisibility > 0.01f) {
            TerminalBackdrop(
                modifier = Modifier.fillMaxSize(),
                alpha = previewVisibility,
            )
        }

        if (stageTheme.showCameraGradient && previewVisibility > 0.01f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .alpha(previewVisibility)
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(0.55f),
                            0.20f to Color.Transparent,
                            0.78f to Color.Transparent,
                            1f to Color.Black.copy(0.70f),
                        ),
                    ),
            )
        }

        if (stage.usesPaperBackdrop && previewVisibility < 0.99f) {
            StudioSilhouette(
                shape = viewShape,
                visibility = 1f - previewVisibility,
                showOutline = showBoundingBox,
                interiorFill = false,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (stage.supportsManualOrbs && previewVisibility < 0.99f) {
            EditorialOrbsLayer(
                orbs = editorialOrbs,
                visibility = 1f - previewVisibility,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (stage.supportsAsciiOverlay && previewVisibility > 0.01f) {
            AsciiScanOverlay(
                modifier = Modifier.fillMaxSize().alpha(previewVisibility * 0.35f),
                accent = stageTheme.accentCyan,
            )
        }

        val textColor = stageTheme.textColor
        val engineLayout = layout
        if (engineLayout == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator(indicatorSize = 36.dp, color = textColor)
            }
        } else {
            PretextPositionedCanvas(
                layout = engineLayout,
                fontSizePx = fontSizePx,
                lineHeightPx = lineHeightPx,
                textColor = textColor,
                typeface = cameraTypeface,
                modifier = Modifier.fillMaxSize(),
            )
        }

        val contourOutlineAlpha = when {
            stage.showsLivePreview -> outlineAlpha * previewVisibility
            stage.usesPaperBackdrop -> outlineAlpha
            else -> 0f
        }
        if ((stage.showsLivePreview || stage.usesPaperBackdrop) && stage != PretextCameraStage.Ascii) {
            if (showSpotlight && stage.showsLivePreview) {
                ShapeSpotlightOverlay(
                    shape = viewShape,
                    alpha = spotlightStrength.coerceIn(0f, 1f) * previewVisibility,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            TrackingOutline(
                shape = viewShape,
                alpha = contourOutlineAlpha,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (measureMode == PretextMeasureMode.Engine) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(stage, wPx, hPx, editorialOrbs) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                hud.reveal()
                                if (stage.supportsManualOrbs) {
                                    draggingOrbIndex = nearestOrbIndex(editorialOrbs, offset.x, offset.y)
                                } else {
                                    manualOverride = true
                                    viewShape = manualViewShapeAt(offset.x, offset.y, wPx, hPx)
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                if (stage.supportsManualOrbs && draggingOrbIndex >= 0) {
                                    val idx = draggingOrbIndex
                                    editorialOrbs = editorialOrbs.mapIndexed { i, orb ->
                                        if (i == idx) {
                                            editorialOrbShapeAt(
                                                change.position.x,
                                                change.position.y,
                                                wPx,
                                                hPx,
                                            )
                                        } else {
                                            orb
                                        }
                                    }
                                } else if (!stage.supportsManualOrbs) {
                                    viewShape = manualViewShapeAt(change.position.x, change.position.y, wPx, hPx)
                                }
                            },
                            onDragEnd = {
                                manualOverride = false
                                draggingOrbIndex = -1
                            },
                            onDragCancel = {
                                manualOverride = false
                                draggingOrbIndex = -1
                            },
                        )
                    },
            )
        }

        if (isRecording) {
            PretextRecordingWatermark(
                launcherIcon = launcherIcon,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .pretextRecordingWatermarkPlacement(chromeVisible = hud.isChromeVisible),
            )
        }
        }

        PretextCameraChromeOverlay(
            hud = hud,
            content = hudContent,
            measureMode = measureMode,
            measureSpeed = measureSpeed,
            isRecording = isRecording,
            recordingStartedAt = recordingStartedAt,
            onExitCamera = {
                if (!isRecording) onExitCamera()
            },
            onTorch = { torchOn = cameraSession.toggleTorch() },
            onFlip = {
                if (isRecording) return@PretextCameraChromeOverlay
                val pv = previewView ?: return@PretextCameraChromeOverlay
                scope.launch {
                    cameraSession.flipCamera(pv, lifecycleOwner, pipeline)
                    torchOn = false
                    torchAvailable = cameraSession.hasTorch()
                }
            },
            onToggleRecord = {
                if (isRecording) {
                    recordingController.stopRecording { saved ->
                        hud.reveal()
                    }
                } else {
                    val file = PretextVideoActions.createOutputFile(context)
                    val snapshot = captureBounds
                    if (snapshot == null) {
                        file.delete()
                        return@PretextCameraChromeOverlay
                    }
                    hud.hide()
                    if (!recordingController.startRecording(file, recordingProfile, snapshot)) {
                        file.delete()
                        hud.reveal()
                    }
                }
            },
            onOpenRecordings = { showRecordingsSheet = true },
            onOpenRecordingSettings = {
                if (!isRecording) {
                    hud.openRecordingSettings()
                }
            },
        )
    }

    if (showRecordingsSheet) {
        PretextRecordingsSheet(
            repository = recordingRepository,
            onDismiss = { showRecordingsSheet = false },
        )
    }

    if (hud.isRecordingSettingsOpen) {
        PretextRecordingSettingsSheet(
            settings = recordingSettings,
            repository = recordingSettingsRepository,
            onDismiss = { hud.closeRecordingSettings() },
        )
    }

    if (hud.isSettingsOpen) {
        CameraSettingsSheet(
            measureMode = measureMode,
            onMeasureModeChange = onMeasureModeChange,
            measureSpeed = measureSpeed,
            textLayoutStyle = textLayoutStyle,
            onTextLayoutStyleChange = { textLayoutStyle = it },
            trackMode = trackMode,
            onTrackModeChange = onTrackModeChange,
            stage = stage,
            onStageChange = onStageChange,
            showBoundingBox = showBoundingBox,
            onShowBoundingBoxChange = onShowBoundingBoxChange,
            tooltipBlurEnabled = tooltipBlurEnabled,
            onTooltipBlurEnabledChange = onTooltipBlurEnabledChange,
            showSpotlight = showSpotlight,
            onShowSpotlightChange = { showSpotlight = it },
            spotlightStrength = spotlightStrength,
            onSpotlightStrengthChange = { spotlightStrength = it },
            showBlur = showBlur,
            onShowBlurChange = { showBlur = it },
            blurRadiusDp = blurRadiusDp,
            onBlurRadiusDpChange = { blurRadiusDp = it },
            showHalftone = showHalftone,
            onShowHalftoneChange = { showHalftone = it },
            telemetry = telemetry,
            onDismiss = { hud.closeSettings() },
        )
    }
}

@Composable
private fun BlurredPreviewOverlay(
    bitmap: android.graphics.Bitmap,
    shape: ViewShape?,
    blurRadius: androidx.compose.ui.unit.Dp,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    if (alpha <= 0f) return
    val img = remember(bitmap) { bitmap.asImageBitmap() }
    Box(
        modifier = modifier
            .alpha(alpha)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        Image(
            bitmap = img,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().blur(blurRadius),
        )
        if (shape != null) {
            Canvas(Modifier.fillMaxSize()) {
                drawPath(
                    path = buildShapeClipPath(shape),
                    color = Color.Transparent,
                    blendMode = BlendMode.Clear,
                )
            }
        }
    }
}

@Composable
private fun ShapeSpotlightOverlay(
    shape: ViewShape?,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    if (shape == null || alpha <= 0f) return
    Canvas(modifier = modifier.alpha(alpha)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            reset()
            addRect(androidx.compose.ui.geometry.Rect(0f, 0f, w, h))
            val poly = shape.polygonPx?.points
            if (poly != null && poly.size >= 3) {
                moveTo(poly[0].first, poly[0].second)
                for (i in 1 until poly.size) {
                    lineTo(poly[i].first, poly[i].second)
                }
                close()
            } else {
                val b = shape.boundsPx
                addRoundRect(
                    RoundRect(
                        left = b.left,
                        top = b.top,
                        right = b.right,
                        bottom = b.bottom,
                        cornerRadius = CornerRadius(
                            x = (b.width() * 0.28f).coerceAtMost(b.height() * 0.5f),
                            y = (b.height() * 0.32f).coerceAtMost(b.width() * 0.5f),
                        ),
                    ),
                )
            }
            fillType = PathFillType.EvenOdd
        }
        drawPath(
            path = path,
            color = Color.Black.copy(alpha = 0.65f),
        )
    }
}

private fun nearestOrbIndex(orbs: List<ViewShape>, x: Float, y: Float): Int {
    if (orbs.isEmpty()) return -1
    var best = 0
    var bestDist = Float.MAX_VALUE
    orbs.forEachIndexed { i, orb ->
        val b = orb.boundsPx
        val cx = b.centerX()
        val cy = b.centerY()
        val d = (cx - x) * (cx - x) + (cy - y) * (cy - y)
        if (d < bestDist) {
            bestDist = d
            best = i
        }
    }
    return best
}

@Composable
private fun BoxScope.PretextCameraChromeOverlay(
    hud: PretextCameraHudState,
    content: PretextCameraHudContent,
    measureMode: PretextMeasureMode,
    measureSpeed: PretextMeasureSpeed,
    isRecording: Boolean,
    recordingStartedAt: Long,
    onExitCamera: () -> Unit,
    onTorch: () -> Unit,
    onFlip: () -> Unit,
    onToggleRecord: () -> Unit,
    onOpenRecordings: () -> Unit,
    onOpenRecordingSettings: () -> Unit,
) {
    AnimatedVisibility(
        visible = hud.isChromeVisible && !isRecording,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .windowInsetsPadding(appBarTopWindowInsets())
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        CameraTopHud(
            shapeLabel = content.shapeLabel,
            statusText = content.statusText,
            statusActive = content.statusActive,
            telemetry = content.telemetry,
            stage = content.stage,
            stageTheme = content.stageTheme,
            torchAvailable = content.torchAvailable,
            torchOn = content.torchOn,
            isRecording = isRecording,
            recordingStartedAt = recordingStartedAt,
            onExitCamera = onExitCamera,
            onTorch = {
                hud.reveal()
                onTorch()
            },
            onFlip = {
                hud.reveal()
                onFlip()
            },
            onOpenSettings = { hud.openSettings() },
            modifier = Modifier.fillMaxWidth(),
        )
    }

    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (content.showDragHint) {
            AnimatedVisibility(
                visible = hud.isChromeVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                DragHintPill()
            }
        }
        AnimatedVisibility(
            visible = hud.isChromeVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        ) {
            PretextRecordingDock(
                isRecording = isRecording,
                recordingStartedAt = recordingStartedAt,
                onToggleRecord = {
                    hud.reveal()
                    onToggleRecord()
                },
                onOpenRecordings = {
                    hud.reveal()
                    onOpenRecordings()
                },
                onOpenRecordingSettings = {
                    hud.reveal()
                    onOpenRecordingSettings()
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            if (!isRecording) {
                PretextMeasureSpeedChip(
                    measureMode = measureMode,
                    measureSpeed = measureSpeed,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PretextRecordingDock(
    isRecording: Boolean,
    recordingStartedAt: Long,
    onToggleRecord: () -> Unit,
    onOpenRecordings: () -> Unit,
    onOpenRecordingSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val elapsedLabel = rememberRecordingElapsedLabel(isRecording, recordingStartedAt)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = Color.Black.copy(alpha = 0.58f),
        contentColor = Color.White,
    ) {
        ButtonGroup(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            ToggleButton(
                checked = false,
                onCheckedChange = { onOpenRecordings() },
            ) {
                Icon(
                    Icons.Outlined.VideoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Clips", style = MaterialTheme.typography.labelLarge)
            }
            ToggleButton(
                checked = false,
                onCheckedChange = {
                    if (!isRecording) onOpenRecordingSettings()
                },
                modifier = Modifier.alpha(if (isRecording) 0.45f else 1f),
            ) {
                Icon(
                    Icons.Outlined.Tune,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Setup", style = MaterialTheme.typography.labelLarge)
            }
            ToggleButton(
                checked = isRecording,
                onCheckedChange = { onToggleRecord() },
            ) {
                Icon(
                    if (isRecording) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isRecording) Color(0xFFFF8A80) else LocalContentColor.current,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isRecording) "Stop · $elapsedLabel" else "Record",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isRecording) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun PretextMeasureSpeedChip(
    measureMode: PretextMeasureMode,
    measureSpeed: PretextMeasureSpeed,
    modifier: Modifier = Modifier,
) {
    val tag = measureSpeed.speedTag(measureMode)
    Text(
        text = buildString {
            append(measureMode.shortLabel)
            if (measureSpeed.nanosFor(measureMode) > 0L) {
                append(" · ")
                append(measureSpeed.format(measureMode))
                if (tag != null) {
                    append(" · ")
                    append(tag)
                }
            }
        },
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = Color.White.copy(alpha = 0.92f),
        fontSize = 10.sp,
        maxLines = 1,
        modifier = modifier
            .background(
                when (tag) {
                    "fast" -> Color(0xFF1B5E20).copy(alpha = 0.72f)
                    "slow" -> Color(0xFF4E342E).copy(alpha = 0.72f)
                    else -> Color.Black.copy(alpha = 0.45f)
                },
                RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 7.dp, vertical = 3.dp),
    )
}

@Composable
private fun CameraTopHud(
    shapeLabel: String,
    statusText: String,
    statusActive: Boolean,
    telemetry: VisionTelemetry,
    stage: PretextCameraStage,
    stageTheme: PretextStageTheme,
    torchAvailable: Boolean,
    torchOn: Boolean,
    isRecording: Boolean,
    recordingStartedAt: Long,
    onExitCamera: () -> Unit,
    onTorch: () -> Unit,
    onFlip: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hudButtonColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = Color.Black.copy(0.58f),
        contentColor = Color.White,
    )
    val elapsedLabel = rememberRecordingElapsedLabel(isRecording, recordingStartedAt)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = onExitCamera,
                colors = hudButtonColors,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to playground",
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusPill(
                    text = if (isRecording) "Recording $elapsedLabel" else statusText,
                    active = statusActive || isRecording,
                    dark = stageTheme.hudOnDark,
                    mono = stageTheme.monoHud,
                )
                if (statusActive && !isRecording) {
                    ShapeBadge(label = shapeLabel, dark = stageTheme.hudOnDark, mono = stageTheme.monoHud)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (torchAvailable) {
                    FilledTonalIconButton(
                        onClick = onTorch,
                        colors = hudButtonColors,
                    ) {
                        Icon(
                            if (torchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                            contentDescription = stringResource(R.string.action_torch),
                        )
                    }
                }
                FilledTonalIconButton(
                    onClick = onFlip,
                    enabled = !isRecording,
                    colors = hudButtonColors,
                ) {
                    Icon(
                        Icons.Default.Cameraswitch,
                        contentDescription = stringResource(R.string.action_flip_camera),
                    )
                }
                FilledIconButton(
                    onClick = onOpenSettings,
                    enabled = !isRecording,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Icon(
                        Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.action_settings),
                    )
                }
            }
        }

        if (telemetry.processedFps > 0f) {
            TelemetryBadge(
                telemetry = telemetry,
                dark = stageTheme.hudOnDark,
                mono = stageTheme.monoHud,
                accentCyan = stageTheme.accentCyan,
            )
        }
    }
}

@Composable
private fun ShapeBadge(label: String, dark: Boolean, mono: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (dark) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f)
        } else {
            Color.Black.copy(alpha = 0.42f)
        },
        contentColor = if (dark) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            Color.White
        },
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = if (mono) PretextTerminalMono else FontFamily.Default,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun StatusPill(text: String, active: Boolean, dark: Boolean, mono: Boolean = false) {
    val baseColor = if (active) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
    } else if (dark) {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.92f)
    } else {
        Color.Black.copy(alpha = 0.55f)
    }
    val contentColor = if (active) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else if (dark) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.White
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = baseColor,
        contentColor = contentColor,
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val dotSize by animateFloatAsState(if (active) 1f else 0.6f, label = "dot")
            Box(
                Modifier
                    .size((6 + 4 * dotSize).dp)
                    .background(
                        if (active) contentColor else contentColor.copy(0.55f),
                        shape = RoundedCornerShape(50),
                    ),
            )
            AnimatedContent(
                targetState = text,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "statusText",
            ) { state ->
                Text(
                    state,
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = if (mono) PretextTerminalMono else FontFamily.Default,
                )
            }
        }
    }
}

@Composable
private fun TelemetryBadge(
    telemetry: VisionTelemetry,
    dark: Boolean,
    mono: Boolean = false,
    accentCyan: Color = Color.Unspecified,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (dark) MaterialTheme.colorScheme.surfaceContainerHighest.copy(0.86f)
        else Color.Black.copy(0.45f),
        contentColor = if (dark) MaterialTheme.colorScheme.onSurface else Color.White,
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "${"%.1f".format(telemetry.processedFps)} fps",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = if (mono) PretextTerminalMono else FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                color = if (mono && accentCyan != Color.Unspecified) accentCyan else LocalContentColor.current,
            )
            HorizontalDivider(
                modifier = Modifier
                    .height(12.dp)
                    .width(1.dp),
                color = (if (dark) MaterialTheme.colorScheme.onSurface else Color.White)
                    .copy(alpha = 0.4f),
            )
            Text(
                "${telemetry.droppedFrames} dropped/s",
                style = MaterialTheme.typography.labelMedium,
            )
            HorizontalDivider(
                modifier = Modifier
                    .height(12.dp)
                    .width(1.dp),
                color = (if (dark) MaterialTheme.colorScheme.onSurface else Color.White)
                    .copy(alpha = 0.4f),
            )
            Text(
                telemetry.lastSource.label,
                style = MaterialTheme.typography.labelMedium,
            )
            if (telemetry.detectHitRate > 0f) {
                HorizontalDivider(
                    modifier = Modifier
                        .height(12.dp)
                        .width(1.dp),
                    color = (if (dark) MaterialTheme.colorScheme.onSurface else Color.White)
                        .copy(alpha = 0.4f),
                )
                Text(
                    "${"%.0f".format(telemetry.detectHitRate * 100f)}% hit",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            telemetry.lastAccuracy?.iouVsPrevious?.let { iou ->
                HorizontalDivider(
                    modifier = Modifier
                        .height(12.dp)
                        .width(1.dp),
                    color = (if (dark) MaterialTheme.colorScheme.onSurface else Color.White)
                        .copy(alpha = 0.4f),
                )
                Text(
                    "IoU ${"%.2f".format(iou)}",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun DragHintPill(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(0.55f),
        contentColor = Color.White,
        modifier = modifier,
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DecorativeIcon(
                Icons.Outlined.TouchApp,
                tint = Color.White,
            )
            Text("Drag to override · text follows your finger", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun TerminalBackdrop(modifier: Modifier = Modifier, alpha: Float) {
    Box(
        modifier = modifier
            .alpha(alpha)
            .background(Color(0xFF0A0E12)),
    )
}

@Composable
private fun EditorialOrbsLayer(
    orbs: List<ViewShape>,
    visibility: Float,
    modifier: Modifier = Modifier,
) {
    if (visibility <= 0.01f || orbs.isEmpty()) return
    val accent = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.tertiary
    Canvas(modifier = modifier.alpha(visibility)) {
        orbs.forEach { orb ->
            val poly = orb.polygonPx
            if (poly != null) {
                val path = buildPath(poly)
                drawPath(path, color = accent.copy(alpha = 0.22f))
                drawPath(path, color = secondary.copy(alpha = 0.55f), style = Stroke(2.5f))
            } else {
                val b = orb.boundsPx
                drawCircle(
                    color = accent.copy(alpha = 0.25f),
                    radius = b.width().coerceAtLeast(b.height()) * 0.5f,
                    center = Offset(b.centerX(), b.centerY()),
                )
                drawCircle(
                    color = secondary.copy(alpha = 0.7f),
                    radius = b.width().coerceAtLeast(b.height()) * 0.5f,
                    center = Offset(b.centerX(), b.centerY()),
                    style = Stroke(2f),
                )
            }
        }
    }
}

@Composable
private fun AsciiScanOverlay(
    modifier: Modifier = Modifier,
    accent: Color,
) {
    val chars = "@#*+.;: "
    Canvas(modifier) {
        val cell = 14f
        val cols = (size.width / cell).toInt().coerceAtLeast(1)
        val rows = (size.height / cell).toInt().coerceAtLeast(1)
        val paint = Paint().apply {
            color = accent.copy(alpha = 0.55f).toArgb()
            textSize = cell * 0.85f
            isAntiAlias = true
            typeface = Typeface.MONOSPACE
        }
        drawContext.canvas.nativeCanvas.apply {
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    val c = chars[(row * 31 + col * 17) % chars.length]
                    drawText(
                        c.toString(),
                        col * cell + cell * 0.15f,
                        row * cell + cell * 0.85f,
                        paint,
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioPaperMaskOverlay(
    shape: ViewShape?,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    if (alpha <= 0f) return
    val surface = MaterialTheme.colorScheme.surface
    val container = MaterialTheme.colorScheme.surfaceContainerLow
    val brush = remember(surface, container) {
        Brush.verticalGradient(listOf(surface, container))
    }
    Canvas(modifier = modifier.alpha(alpha)) {
        val paperPath = Path().apply {
            addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
            if (shape != null) {
                addPath(buildShapeClipPath(shape))
            }
            fillType = PathFillType.EvenOdd
        }
        drawPath(paperPath, brush = brush)
    }
}

@Composable
private fun StudioSilhouette(
    shape: ViewShape?,
    visibility: Float,
    showOutline: Boolean,
    modifier: Modifier = Modifier,
    interiorFill: Boolean = true,
) {
    val accent = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.surface
    val polygonPx = shape?.polygonPx
    val hasShape = shape != null
    Box(modifier) {
        if (polygonPx != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(52.dp),
            ) {
                drawContourLayers(
                    shape = shape,
                    visibility = visibility,
                    accent = accent,
                    secondary = secondary,
                    surface = surface,
                    glowOnly = true,
                )
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            drawContourLayers(
                shape = shape,
                visibility = visibility,
                accent = accent,
                secondary = secondary,
                surface = surface,
                glowOnly = false,
                stroke = showOutline || hasShape,
                fillAlpha = if (interiorFill && polygonPx != null) 0.34f else 0.20f,
                drawInteriorFill = interiorFill,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawContourLayers(
    shape: ViewShape?,
    visibility: Float,
    accent: Color,
    secondary: Color,
    surface: Color,
    glowOnly: Boolean,
    stroke: Boolean = true,
    fillAlpha: Float = 0.28f,
    drawInteriorFill: Boolean = true,
) {
    val polygonPx = shape?.polygonPx
    if (polygonPx != null) {
        val path = buildPath(polygonPx)
        val bounds = path.getBounds()
        if (glowOnly) {
            drawPath(path, color = accent.copy(alpha = 0.50f * visibility))
            drawPath(path, color = secondary.copy(alpha = 0.24f * visibility), style = Stroke(42f))
        } else {
            if (drawInteriorFill) {
                drawPath(
                    path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = fillAlpha * visibility),
                            secondary.copy(alpha = fillAlpha * 0.55f * visibility),
                            surface.copy(alpha = 0.02f * visibility),
                        ),
                        startY = bounds.top,
                        endY = bounds.bottom,
                    ),
                )
            }
            if (stroke) {
                drawPath(path, color = accent.copy(alpha = 0.85f * visibility), style = Stroke(3f))
                drawPath(path, color = Color.White.copy(alpha = 0.30f * visibility), style = Stroke(1.2f))
            }
        }
    } else if (!glowOnly && shape != null) {
        val bounds = shape.boundsPx
        val w = bounds.width()
        val h = bounds.height()
        drawRoundRect(
            color = accent.copy(alpha = 0.20f * visibility),
            topLeft = Offset(bounds.left, bounds.top),
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.22f, h * 0.22f),
        )
        if (stroke) {
            drawRoundRect(
                color = accent.copy(alpha = 0.60f * visibility),
                topLeft = Offset(bounds.left, bounds.top),
                size = Size(w, h),
                cornerRadius = CornerRadius(w * 0.22f, h * 0.22f),
                style = Stroke(2f),
            )
        }
    }
}

@Composable
private fun TrackingOutline(
    shape: ViewShape?,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    if (alpha <= 0.01f || shape == null) return
    val accent = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.tertiary
    val isFaceLive = shape.source == VisionSource.Face && shape.isLiveDetection
    val pulseTransition = rememberInfiniteTransition(label = "facePulse")
    val pulsePhase by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "facePulsePhase",
    )
    val strokeWidth = if (isFaceLive) 2.4f + pulsePhase * 0.6f else 2.5f
    Canvas(modifier) {
        val bounds = shape.boundsPx
        val bw = bounds.width()
        val bh = bounds.height()

        val polygonPx = shape.polygonPx
        if (polygonPx != null) {
            val path = buildPath(polygonPx)
            val pathBounds = path.getBounds()
            drawPath(path, color = accent.copy(alpha = 0.10f * alpha))
            drawPath(
                path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.95f * alpha),
                        secondary.copy(alpha = 0.75f * alpha),
                    ),
                    start = Offset(pathBounds.left, pathBounds.top),
                    end = Offset(pathBounds.right, pathBounds.bottom),
                ),
                style = Stroke(strokeWidth),
            )
        } else {
            val corner = kotlin.math.min(bw, bh) * 0.12f
            val cornerRadius = CornerRadius(corner, corner)
            drawRoundRect(
                color = accent.copy(alpha = 0.08f * alpha),
                topLeft = Offset(bounds.left, bounds.top),
                size = Size(bw, bh),
                cornerRadius = cornerRadius,
            )
            drawRoundRect(
                color = accent.copy(alpha = 0.95f * alpha),
                topLeft = Offset(bounds.left, bounds.top),
                size = Size(bw, bh),
                cornerRadius = cornerRadius,
                style = Stroke(strokeWidth),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraSettingsSheet(
    measureMode: PretextMeasureMode,
    onMeasureModeChange: (PretextMeasureMode) -> Unit,
    measureSpeed: PretextMeasureSpeed,
    textLayoutStyle: PretextCameraTextLayoutStyle,
    onTextLayoutStyleChange: (PretextCameraTextLayoutStyle) -> Unit,
    trackMode: VisionTrackMode,
    onTrackModeChange: (VisionTrackMode) -> Unit,
    stage: PretextCameraStage,
    onStageChange: (PretextCameraStage) -> Unit,
    showBoundingBox: Boolean,
    onShowBoundingBoxChange: (Boolean) -> Unit,
    tooltipBlurEnabled: Boolean,
    onTooltipBlurEnabledChange: (Boolean) -> Unit,
    showSpotlight: Boolean,
    onShowSpotlightChange: (Boolean) -> Unit,
    spotlightStrength: Float,
    onSpotlightStrengthChange: (Float) -> Unit,
    showBlur: Boolean,
    onShowBlurChange: (Boolean) -> Unit,
    blurRadiusDp: Float,
    onBlurRadiusDpChange: (Float) -> Unit,
    showHalftone: Boolean,
    onShowHalftoneChange: (Boolean) -> Unit,
    telemetry: VisionTelemetry,
    onDismiss: () -> Unit,
) {
    var page by rememberSaveable { mutableStateOf(CameraSettingsPage.Root) }
    var goingForward by rememberSaveable { mutableStateOf(true) }
    var advancedExpanded by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            page = CameraSettingsPage.Root
            goingForward = true
        }
    }

    AppModalBottomSheet(
        onDismiss = onDismiss,
        scrollable = false,
    ) {
        AppModalSheetStack(
            page = page,
            isRoot = page == CameraSettingsPage.Root,
            goingForward = goingForward,
            onPop = {
                goingForward = false
                page = CameraSettingsPage.Root
            },
        ) { current ->
            when (current) {
                CameraSettingsPage.Root -> {
                    Text(
                        "Camera settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            PretextStageSelector(
                                stage,
                                onStageChange,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                            PretextMeasureSelector(
                                selected = measureMode,
                                onSelected = onMeasureModeChange,
                                speed = measureSpeed,
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                            PretextMeasureEngineGuideEntry(
                                measureMode = measureMode,
                                onOpen = {
                                    goingForward = true
                                    page = CameraSettingsPage.EngineGuide
                                },
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                            PretextCameraLayoutSelector(
                                textLayoutStyle,
                                onTextLayoutStyleChange,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                    }

                    PretextCameraOptions(
                        trackMode = trackMode,
                        onTrackModeSelected = onTrackModeChange,
                        showBoundingBox = showBoundingBox,
                        onShowBoundingBoxChange = onShowBoundingBoxChange,
                        showSpotlight = showSpotlight,
                        onShowSpotlightChange = onShowSpotlightChange,
                        spotlightStrength = spotlightStrength,
                        onSpotlightStrengthChange = onSpotlightStrengthChange,
                        showBlur = showBlur,
                        onShowBlurChange = onShowBlurChange,
                        blurRadiusDp = blurRadiusDp,
                        onBlurRadiusDpChange = onBlurRadiusDpChange,
                        showHalftone = showHalftone,
                        onShowHalftoneChange = onShowHalftoneChange,
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        SwitchListItem(
                            checked = tooltipBlurEnabled,
                            onCheckedChange = onTooltipBlurEnabledChange,
                            containerColor = Color.Transparent,
                            headlineContent = { Text("Blur camera tooltip") },
                            supportingContent = {
                                Text("Use a frosted backdrop behind the camera tooltip on this screen.")
                            },
                            leadingContent = {
                                DecorativeIcon(
                                    Icons.Outlined.BlurOn,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ) {
                        Column {
                            ListItem(
                                headlineContent = { Text("Advanced") },
                                supportingContent = { Text("Pipeline stats and diagnostics.") },
                                leadingContent = {
                                    DecorativeIcon(
                                        Icons.Outlined.Tune,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp),
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        if (advancedExpanded) {
                                            Icons.Outlined.ExpandLess
                                        } else {
                                            Icons.Outlined.ExpandMore
                                        },
                                        contentDescription = null,
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier.pointerInput(advancedExpanded) {
                                    detectTapGestures { advancedExpanded = !advancedExpanded }
                                },
                            )
                            AnimatedVisibility(visible = advancedExpanded) {
                                Column(
                                    Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        buildString {
                                            append("Vision: ")
                                            append("%.1f".format(telemetry.processedFps))
                                            append(" fps · ")
                                            append(telemetry.droppedFrames)
                                            append(" dropped/s · hit ")
                                            append("%.0f".format(telemetry.detectHitRate * 100f))
                                            append('%')
                                            telemetry.lastBackend?.let {
                                                append(" · ")
                                                append(it)
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    telemetry.lastAccuracy?.let { a ->
                                        Text(
                                            buildString {
                                                append("Shape: ")
                                                append(a.tracking.name.lowercase())
                                                append(" · area ")
                                                append("%.0f".format(a.normBBoxArea * 100f))
                                                append("% norm")
                                                if (a.polygonVertices > 0) {
                                                    append(" · ")
                                                    append(a.polygonVertices)
                                                    append(" pts")
                                                }
                                                a.iouVsPrevious?.let {
                                                    append(" · IoU ")
                                                    append("%.2f".format(it))
                                                }
                                                append(" · detect ")
                                                append(a.detectMs)
                                                append("ms")
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                CameraSettingsPage.EngineGuide -> {
                    PretextMeasureEngineGuidePage(
                        mode = measureMode,
                        speed = measureSpeed,
                        onBack = {
                            goingForward = false
                            page = CameraSettingsPage.Root
                        },
                        onModeChange = onMeasureModeChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionCard(
    onGrant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            DecorativeIcon(
                Icons.Outlined.PhotoCamera,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text("Camera access", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Live tracking and per-frame text reflow need the camera.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            )
            Button(onClick = onGrant) { Text("Allow camera") }
        }
    }
}

private fun buildPath(polygon: PolygonObstacle): Path = Path().apply {
    val pts = polygon.points
    if (pts.isEmpty()) return@apply
    val (x0, y0) = pts[0]
    moveTo(x0, y0)
    for (i in 1 until pts.size) {
        val (x, y) = pts[i]
        lineTo(x, y)
    }
    close()
}

private fun buildShapeClipPath(shape: ViewShape): Path {
    val clip = Path()
    val poly = shape.polygonPx?.points
    if (poly != null && poly.size >= 3) {
        clip.moveTo(poly[0].first, poly[0].second)
        for (i in 1 until poly.size) clip.lineTo(poly[i].first, poly[i].second)
        clip.close()
    } else {
        val b = shape.boundsPx
        clip.addRoundRect(
            RoundRect(
                left = b.left,
                top = b.top,
                right = b.right,
                bottom = b.bottom,
                cornerRadius = CornerRadius(b.width() * 0.28f, b.height() * 0.32f),
            ),
        )
    }
    return clip
}

@Composable
private fun HalftonePreviewOverlay(
    bitmap: android.graphics.Bitmap,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    if (alpha <= 0f) return
    val img = remember(bitmap) { bitmap.asImageBitmap() }
    val colorMatrix = remember {
        androidx.compose.ui.graphics.ColorMatrix(
            floatArrayOf(
                0.393f + 0.607f * 0.3f, 0.769f - 0.769f * 0.3f, 0.189f - 0.189f * 0.3f, 0f, 0f,
                0.349f - 0.349f * 0.3f, 0.686f + 0.314f * 0.3f, 0.168f - 0.168f * 0.3f, 0f, 0f,
                0.272f - 0.272f * 0.3f, 0.534f - 0.534f * 0.3f, 0.131f + 0.869f * 0.3f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    Box(modifier = modifier.alpha(alpha)) {
        Image(
            bitmap = img,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(colorMatrix),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dotSpacing = 8f
            val dotRadius = 1.5f
            val dotColor = Color.Black.copy(alpha = 0.2f)

            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    y += dotSpacing
                }
                x += dotSpacing
            }
        }
    }
}
