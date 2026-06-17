package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.platform.supportsBackdropBlur
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

val LocalModalBackdropBlurEnabled = compositionLocalOf { false }

internal val LocalAppHazeState = compositionLocalOf<HazeState?> { null }

internal val LocalModalBackdropController = compositionLocalOf { ModalBackdropController() }

internal val LocalCollapsedHeaderBackdropController = compositionLocalOf { CollapsedHeaderBackdropController() }

internal data class ModalBackdropRegistration(
    val id: Any,
    val onDismiss: () -> Unit,
    val excludeTop: Dp = 0.dp,
)

internal data class CollapsedHeaderBackdropRegistration(
    val id: Any,
    val height: Dp,
    val content: @Composable () -> Unit,
)

class ModalBackdropController internal constructor() {
    internal val registrations = mutableStateListOf<ModalBackdropRegistration>()

    fun register(id: Any, onDismiss: () -> Unit, excludeTop: Dp = 0.dp) {
        val index = registrations.indexOfFirst { it.id === id }
        if (index >= 0) {
            val current = registrations[index]
            if (current.onDismiss === onDismiss && current.excludeTop == excludeTop) return
            registrations[index] = ModalBackdropRegistration(id, onDismiss, excludeTop)
        } else {
            registrations.add(ModalBackdropRegistration(id, onDismiss, excludeTop))
        }
    }

    fun updateExcludeTop(id: Any, excludeTop: Dp) {
        val index = registrations.indexOfFirst { it.id === id }
        if (index < 0) return
        val current = registrations[index]
        if (current.excludeTop == excludeTop) return
        registrations[index] = current.copy(excludeTop = excludeTop)
    }

    fun unregister(id: Any) {
        registrations.removeAll { it.id === id }
    }

    fun dismissTop() {
        registrations.lastOrNull()?.onDismiss?.invoke()
    }

    internal val maxExcludeTop: Dp
        get() = registrations.maxOfOrNull { it.excludeTop } ?: 0.dp
}

class CollapsedHeaderBackdropController internal constructor() {
    internal val registrations = mutableStateListOf<CollapsedHeaderBackdropRegistration>()

    fun register(
        id: Any,
        height: Dp,
        content: @Composable () -> Unit,
    ) {
        val index = registrations.indexOfFirst { it.id === id }
        if (index >= 0) {
            registrations[index] = CollapsedHeaderBackdropRegistration(id, height, content)
        } else {
            registrations.add(CollapsedHeaderBackdropRegistration(id, height, content))
        }
    }

    fun unregister(id: Any) {
        registrations.removeAll { it.id === id }
    }

    internal val maxHeight: Dp
        get() = registrations.maxOfOrNull { it.height } ?: 0.dp
}

@Composable
fun rememberModalBackdropController(): ModalBackdropController = remember { ModalBackdropController() }

@Composable
fun rememberCollapsedHeaderBackdropController(): CollapsedHeaderBackdropController =
    remember { CollapsedHeaderBackdropController() }

@Composable
fun ProvideAppModalBackdrop(
    blurEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val deviceSupportsBlur = remember(context) { context.supportsBackdropBlur() }
    val effectiveBlur = blurEnabled && deviceSupportsBlur
    val hazeState = rememberHazeState(blurEnabled = effectiveBlur)
    val controller = rememberModalBackdropController()
    val collapsedHeaderController = rememberCollapsedHeaderBackdropController()

    CompositionLocalProvider(
        LocalModalBackdropBlurEnabled provides effectiveBlur,
        LocalAppHazeState provides hazeState,
        LocalModalBackdropController provides controller,
        LocalCollapsedHeaderBackdropController provides collapsedHeaderController,
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (effectiveBlur) {
                            Modifier.hazeSource(state = hazeState, zIndex = 0f)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                content()
            }
            CollapsedHeaderBackdropScrim(
                controller = collapsedHeaderController,
                modifier = Modifier.zIndex(1f),
            )
            CollapsedHeaderChromeHost(
                controller = collapsedHeaderController,
                modifier = Modifier.zIndex(2f),
            )
            ModalBackdropScrim(
                controller = controller,
                modifier = Modifier.zIndex(3f),
            )
        }
    }
}

@Composable
private fun CollapsedHeaderBackdropScrim(
    controller: CollapsedHeaderBackdropController,
    modifier: Modifier = Modifier,
) {
    val blurEnabled = LocalModalBackdropBlurEnabled.current
    val hazeState = LocalAppHazeState.current
    val registrations = controller.registrations
    val height = registrations.maxOfOrNull { it.height } ?: 0.dp
    if (!blurEnabled || hazeState == null || height <= 0.dp) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .hazeEffect(state = hazeState, style = rememberModalBackdropHazeStyle())
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.08f)),
    )
}

@Composable
private fun CollapsedHeaderChromeHost(
    controller: CollapsedHeaderBackdropController,
    modifier: Modifier = Modifier,
) {
    val registrations = controller.registrations
    if (registrations.isEmpty()) return

    Box(modifier = modifier.fillMaxSize()) {
        registrations.forEach { registration ->
            if (registration.height <= 0.dp) return@forEach
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .height(registration.height),
            ) {
                registration.content()
            }
        }
    }
}

@Composable
internal fun ModalBackdropScrim(
    controller: ModalBackdropController,
    modifier: Modifier = Modifier,
) {
    val blurEnabled = LocalModalBackdropBlurEnabled.current
    val hazeState = LocalAppHazeState.current
    val isVisible = controller.registrations.isNotEmpty()
    if (!blurEnabled || hazeState == null || !isVisible) return

    val excludeTop = controller.maxExcludeTop
    val scheme = MaterialTheme.colorScheme
    val style = rememberModalBackdropHazeStyle()
    val dismissInteraction = remember { MutableInteractionSource() }

    val blurredModifier = Modifier
        .hazeEffect(state = hazeState, style = style)
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
) {
    val registrationId = remember { Any() }
    RegisterModalBackdrop(
        id = registrationId,
        visible = visible,
        onDismiss = onDismiss,
        excludeTop = excludeTop,
    )
}

@Composable
internal fun RegisterModalBackdrop(
    id: Any,
    visible: Boolean,
    onDismiss: () -> Unit,
    excludeTop: Dp = 0.dp,
) {
    val blurEnabled = LocalModalBackdropBlurEnabled.current
    val controller = LocalModalBackdropController.current
    val currentOnDismiss = rememberUpdatedState(onDismiss)

    DisposableEffect(blurEnabled, id) {
        onDispose { controller.unregister(id) }
    }

    SideEffect {
        if (blurEnabled && visible) {
            controller.register(id, { currentOnDismiss.value() }, excludeTop)
        } else {
            controller.unregister(id)
        }
    }
}

@Composable
private fun rememberModalBackdropHazeStyle() = run {
    val scheme = MaterialTheme.colorScheme
    remember(scheme) {
        HazeDefaults.style(
            backgroundColor = scheme.background,
            blurRadius = 22.dp,
            tint = HazeDefaults.tint(scheme.scrim.copy(alpha = 0.4f)),
            noiseFactor = 0.05f,
        )
    }
}

@Composable
fun collapsedHeaderBlurActive(
    collapsedFraction: Float,
    collapseThreshold: Float = 0.02f,
): Boolean {
    if (!LocalModalBackdropBlurEnabled.current) return false
    if (LocalAppHazeState.current == null) return false
    return collapsedFraction >= collapseThreshold
}

@Composable
fun Modifier.collapsedHeaderBlur(
    collapsedFraction: Float,
    collapseThreshold: Float = 0.02f,
): Modifier {
    if (!collapsedHeaderBlurActive(collapsedFraction, collapseThreshold)) return this
    return this
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
    val blurActive = collapsedHeaderBlurActive(collapsedFraction, collapseThreshold)
    val controller = LocalCollapsedHeaderBackdropController.current
    val density = LocalDensity.current
    val registrationId = remember { Any() }
    var measuredHeight by remember { mutableStateOf(0.dp) }
    val currentContent by rememberUpdatedState(content)

    DisposableEffect(registrationId) {
        onDispose { controller.unregister(registrationId) }
    }

    LaunchedEffect(blurActive, measuredHeight) {
        if (blurActive && measuredHeight > 0.dp) {
            controller.register(registrationId, measuredHeight) {
                Box(Modifier.fillMaxWidth()) {
                    currentContent()
                }
            }
        } else {
            controller.unregister(registrationId)
        }
    }

    if (!blurActive) {
        Surface(
            modifier = modifier.onSizeChanged { size ->
                measuredHeight = with(density) { size.height.toDp() }
            },
            color = surfaceColor,
            tonalElevation = tonalElevation,
        ) {
            Box(Modifier.fillMaxWidth(), content = content)
        }
        return
    }
    Surface(
        modifier = modifier.onSizeChanged { size ->
            measuredHeight = with(density) { size.height.toDp() }
        },
        color = Color.Transparent,
        tonalElevation = 0.dp,
    ) {
        Box(Modifier.fillMaxWidth().alpha(0.001f), content = content)
    }
}

@Composable
fun CollapsedTopAppBarBackdrop(
    collapsedFraction: Float,
    modifier: Modifier = Modifier,
    collapseThreshold: Float = 0.02f,
    content: @Composable BoxScope.() -> Unit,
) {
    CollapsedHeaderBackdrop(
        collapsedFraction = collapsedFraction,
        modifier = modifier,
        surfaceColor = Color.Transparent,
        tonalElevation = 0.dp,
        collapseThreshold = collapseThreshold,
        content = content,
    )
}

@Composable
fun collapsedHeaderSurfaceColor(
    fallback: Color,
    collapsedFraction: Float,
    collapseThreshold: Float = 0.02f,
): Color {
    if (!collapsedHeaderBlurActive(collapsedFraction, collapseThreshold)) return fallback
    return Color.Transparent
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
    return TopAppBarDefaults.topAppBarColors(
        containerColor = if (blurActive) Color.Transparent else containerColor,
        scrolledContainerColor = if (blurActive) Color.Transparent else scrolledContainerColor,
        navigationIconContentColor = navigationIconContentColor,
        titleContentColor = titleContentColor,
        actionIconContentColor = actionIconContentColor,
    )
}
