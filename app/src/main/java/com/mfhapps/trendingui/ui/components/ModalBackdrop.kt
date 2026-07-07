package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.mfhapps.trendingui.screens.glass.LocalGlassBackdrop
import com.mfhapps.trendingui.ui.platform.supportsBackdropBlur
import com.mfhapps.trendingui.ui.theme.LocalCatalogHazeEnabled
import com.mfhapps.trendingui.ui.theme.LocalCatalogHazeState
import com.mfhapps.trendingui.ui.theme.ModalBackdropStyle
import com.mfhapps.trendingui.ui.theme.toCollapsedHeaderHazeStyle
import com.mfhapps.trendingui.ui.theme.toHazeStyle
import com.mfhapps.trendingui.ui.theme.ModalBackdropBlurType
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

val LocalModalBackdropBlurEnabled = compositionLocalOf { false }

val LocalModalBackdropStyle = compositionLocalOf { ModalBackdropStyle.Default }

@Composable
internal fun ClearDialogWindowDim() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.parent as? DialogWindowProvider)?.window
        val previousDim = window?.attributes?.dimAmount
        window?.setDimAmount(0f)
        onDispose {
            if (window != null && previousDim != null) {
                window.setDimAmount(previousDim)
            }
        }
    }
}

internal val LocalAppHazeState = compositionLocalOf<HazeState?> { null }

internal val LocalModalBackdropController = compositionLocalOf { ModalBackdropController() }

internal data class ModalBackdropRegistration(
    val id: Any,
    val onDismiss: () -> Unit,
    val excludeTop: Dp = 0.dp,
    val hazeState: HazeState? = null,
)

class ModalBackdropController internal constructor() {
    internal val registrations = mutableStateListOf<ModalBackdropRegistration>()

    fun register(
        id: Any,
        onDismiss: () -> Unit,
        excludeTop: Dp = 0.dp,
        hazeState: HazeState? = null,
    ) {
        val index = registrations.indexOfFirst { it.id === id }
        if (index >= 0) {
            val current = registrations[index]
            if (
                current.onDismiss === onDismiss &&
                current.excludeTop == excludeTop &&
                current.hazeState === hazeState
            ) {
                return
            }
            registrations[index] = ModalBackdropRegistration(id, onDismiss, excludeTop, hazeState)
        } else {
            registrations.add(ModalBackdropRegistration(id, onDismiss, excludeTop, hazeState))
        }
    }

    fun unregister(id: Any) {
        registrations.removeAll { it.id === id }
    }

    fun dismissTop() {
        registrations.lastOrNull()?.onDismiss?.invoke()
    }
}

@Composable
fun rememberModalBackdropController(): ModalBackdropController = remember { ModalBackdropController() }

@Composable
fun ProvideAppModalBackdrop(
    blurEnabled: Boolean,
    backdropStyle: ModalBackdropStyle = ModalBackdropStyle.Default,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val deviceSupportsBlur = remember(context) { context.supportsBackdropBlur() }
    val hazeState = rememberHazeState(blurEnabled = deviceSupportsBlur)
    val controller = rememberModalBackdropController()

    val effectiveBlur = blurEnabled && deviceSupportsBlur

    CompositionLocalProvider(
        LocalModalBackdropBlurEnabled provides effectiveBlur,
        LocalModalBackdropStyle provides backdropStyle.normalized(),
        LocalAppHazeState provides hazeState,
        LocalModalBackdropController provides controller,
    ) {
        Box(Modifier.fillMaxSize()) {
            content()
            ModalBackdropScrim(
                controller = controller,
                modifier = Modifier.zIndex(8f),
            )
        }
    }
}

@Composable
internal fun ModalBackdropScrim(
    controller: ModalBackdropController,
    modifier: Modifier = Modifier,
) {
    val registrations = controller.registrations
    val hazeState = registrations.lastOrNull()?.hazeState ?: activeAppHazeState()
    val isVisible = registrations.isNotEmpty()
    if (hazeState == null || !isVisible) return

    val excludeTop = registrations.maxOfOrNull { it.excludeTop } ?: 0.dp
    val dismissInteraction = remember { MutableInteractionSource() }
    val backdropStyle = LocalModalBackdropStyle.current

    val blurredModifier = Modifier
        .modalBackdropHazeEffect(state = hazeState, style = backdropStyle)
        .clickable(
            indication = null,
            interactionSource = dismissInteraction,
            onClick = controller::dismissTop,
        )

    if (excludeTop <= 0.dp) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(blurredModifier),
        )
    } else {
        Column(modifier = modifier.fillMaxSize()) {
            Spacer(Modifier.fillMaxWidth().height(excludeTop))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .then(blurredModifier),
            )
        }
    }
}

@Composable
internal fun RegisterModalBackdrop(
    visible: Boolean,
    onDismiss: () -> Unit,
    excludeTop: Dp = 0.dp,
    backdropEnabled: Boolean = LocalModalBackdropBlurEnabled.current,
) {
    val registrationId = remember { Any() }
    RegisterModalBackdrop(
        id = registrationId,
        visible = visible,
        onDismiss = onDismiss,
        excludeTop = excludeTop,
        backdropEnabled = backdropEnabled,
    )
}

@Composable
internal fun RegisterModalBackdrop(
    id: Any,
    visible: Boolean,
    onDismiss: () -> Unit,
    excludeTop: Dp = 0.dp,
    backdropEnabled: Boolean = LocalModalBackdropBlurEnabled.current,
) {
    val context = LocalContext.current
    val deviceSupportsBlur = remember(context) { context.supportsBackdropBlur() }
    val effectiveBackdrop = backdropEnabled && deviceSupportsBlur
    val controller = LocalModalBackdropController.current
    val currentOnDismiss = rememberUpdatedState(onDismiss)
    val hazeState = activeAppHazeState()

    DisposableEffect(id) {
        onDispose { controller.unregister(id) }
    }

    SideEffect {
        if (effectiveBackdrop && visible) {
            controller.register(
                id = id,
                onDismiss = { currentOnDismiss.value() },
                excludeTop = excludeTop,
                hazeState = hazeState,
            )
        } else {
            controller.unregister(id)
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun rememberModalBackdropHazeStyle(
    style: ModalBackdropStyle = LocalModalBackdropStyle.current,
) = run {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.45f
    val normalized = style.normalized()
    val materialBase = when (normalized.blurType) {
        ModalBackdropBlurType.Frosted -> HazeMaterials.thick(containerColor = scheme.surface)
        else -> null
    }
    remember(normalized, scheme, isDark, materialBase) {
        materialBase ?: normalized.toHazeStyle(scheme, isDark)
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
internal fun rememberCollapsedHeaderHazeStyle(
    style: ModalBackdropStyle = LocalModalBackdropStyle.current,
) = run {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.45f
    val normalized = style.normalized().forCollapsedHeader()
    val materialBase = when (normalized.blurType) {
        ModalBackdropBlurType.Frosted -> HazeMaterials.regular(containerColor = scheme.surface)
        else -> null
    }
    remember(normalized, scheme, isDark, materialBase) {
        materialBase ?: normalized.toCollapsedHeaderHazeStyle(scheme, isDark)
    }
}

@Composable
fun activeAppHazeState(): HazeState? {
    val glass = LocalGlassBackdrop.current
    if (glass != null && glass.hazeEnabled) return glass.hazeState
    return LocalCatalogHazeState.current ?: LocalAppHazeState.current
}

@Composable
private fun activeAppHazeEnabled(): Boolean {
    val glass = LocalGlassBackdrop.current
    if (glass != null) return glass.hazeEnabled
    return if (LocalCatalogHazeState.current != null) {
        LocalCatalogHazeEnabled.current
    } else {
        LocalModalBackdropBlurEnabled.current
    }
}

@Composable
fun Modifier.collapsedHeaderBlur(
    collapsedFraction: Float,
    collapseThreshold: Float = 0.02f,
): Modifier {
    val blurEnabled = collapsedHeaderBlurActive(collapsedFraction, collapseThreshold)
    val hazeState = activeAppHazeState()
    val backdropStyle = LocalModalBackdropStyle.current
    return if (blurEnabled && hazeState != null) {
        then(
            Modifier.modalBackdropHazeEffect(
                state = hazeState,
                style = backdropStyle,
                collapsedHeader = true,
            ),
        )
    } else {
        this
    }
}

@Composable
fun Modifier.appHazeSource(zIndex: Float = 0f): Modifier {
    val context = LocalContext.current
    val deviceSupportsBlur = remember(context) { context.supportsBackdropBlur() }
    val state = activeAppHazeState()
    return if (deviceSupportsBlur && state != null) {
        then(Modifier.hazeSource(state = state, zIndex = zIndex))
    } else {
        this
    }
}

@Composable
fun collapsedHeaderBlurActive(
    collapsedFraction: Float,
    collapseThreshold: Float = 0.02f,
): Boolean {
    if (!activeAppHazeEnabled()) return false
    if (activeAppHazeState() == null) return false
    return collapsedFraction >= collapseThreshold
}

@Composable
fun CollapsedHeaderBackdrop(
    collapsedFraction: Float,
    modifier: Modifier = Modifier,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    tonalElevation: Dp = 2.dp,
    collapseThreshold: Float = 0.02f,
    content: @Composable BoxScope.() -> Unit,
) {
    val blurEnabled = collapsedHeaderBlurActive(collapsedFraction, collapseThreshold)
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.45f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .collapsedHeaderBlur(collapsedFraction, collapseThreshold),
        color = if (blurEnabled) Color.Transparent else surfaceColor,
        tonalElevation = if (blurEnabled) 0.dp else tonalElevation,
        shadowElevation = if (blurEnabled && !isDark) 1.dp else 0.dp,
    ) {
        Box(Modifier.fillMaxWidth(), content = content)
    }
}

val LocalCollapsingTopBarHeight = compositionLocalOf { 0.dp }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingBlurTopBarLayout(
    scrollBehavior: TopAppBarScrollBehavior,
    collapsedFraction: Float,
    modifier: Modifier = Modifier,
    collapseThreshold: Float = 0.02f,
    topBar: @Composable BoxScope.(barModifier: Modifier) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    var topBarHeight by remember { mutableStateOf(0.dp) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        CompositionLocalProvider(LocalCollapsingTopBarHeight provides topBarHeight) {
            content()
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(1f)
                .onSizeChanged { size ->
                    topBarHeight = with(density) { size.height.toDp() }
                },
        ) {
            topBar(
                Modifier
                    .fillMaxWidth()
                    .collapsedHeaderBlur(collapsedFraction, collapseThreshold),
            )
        }
    }
}

@Composable
fun collapsingTopBarContentPadding(
    extra: PaddingValues = PaddingValues(0.dp),
): PaddingValues {
    val topBarHeight = LocalCollapsingTopBarHeight.current
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        top = topBarHeight + extra.calculateTopPadding(),
        start = extra.calculateStartPadding(layoutDirection),
        end = extra.calculateEndPadding(layoutDirection),
        bottom = extra.calculateBottomPadding(),
    )
}

@Composable
fun CollapsedTopAppBarBackdrop(
    collapsedFraction: Float,
    modifier: Modifier = Modifier,
    collapseThreshold: Float = 0.02f,
    content: @Composable BoxScope.(barModifier: Modifier) -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        content(
            Modifier
                .fillMaxWidth()
                .collapsedHeaderBlur(collapsedFraction, collapseThreshold),
        )
    }
}

@Composable
fun rememberCollapsedTopAppBarColors(
    collapsedFraction: Float,
    scrolledContainerColor: Color,
    containerColor: Color = Color.Transparent,
    navigationIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    collapseThreshold: Float = 0.02f,
): TopAppBarColors {
    val blurActive = collapsedHeaderBlurActive(collapsedFraction, collapseThreshold)
    val transparent = Color.Transparent
    return TopAppBarDefaults.topAppBarColors(
        containerColor = if (blurActive) transparent else containerColor,
        scrolledContainerColor = if (blurActive) transparent else scrolledContainerColor,
        navigationIconContentColor = navigationIconContentColor,
        titleContentColor = titleContentColor,
        actionIconContentColor = actionIconContentColor,
    )
}
