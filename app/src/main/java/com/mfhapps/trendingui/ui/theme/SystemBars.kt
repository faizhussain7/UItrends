package com.mfhapps.trendingui.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.WindowCompat

private class SystemBarAppearanceStack {
    private data class Entry(val token: Any, val appearance: SystemBarAppearance)

    private val entries = mutableStateListOf<Entry>()

    val current: SystemBarAppearance? get() = entries.lastOrNull()?.appearance

    val revision: Int get() = entries.size

    fun set(token: Any, appearance: SystemBarAppearance) {
        val index = entries.indexOfFirst { it.token == token }
        if (index >= 0) {
            if (entries[index].appearance != appearance) {
                entries[index] = Entry(token, appearance)
            }
        } else {
            entries.add(Entry(token, appearance))
        }
    }

    fun remove(token: Any) {
        entries.removeAll { it.token == token }
    }
}

private val LocalSystemBarAppearanceStack = staticCompositionLocalOf { SystemBarAppearanceStack() }

@Composable
private fun rememberDefaultGradientChromeAppearance(): SystemBarAppearance {
    val gradients = LocalBrandGradients.current
    val scheme = MaterialTheme.colorScheme
    return remember(gradients, scheme) {
        SystemBarAppearance.forGradientChrome(
            statusBarBackdrop = gradients.statusBarBackdrop,
            chromeSamples = gradients.chromeBackdropSamples,
            navigationBackdrop = scheme.background,
        )
    }
}


@Composable
fun SystemBarAppearanceHost(
    splashHeld: Boolean = false,
    splashExitGeneration: Int = 0,
    content: @Composable () -> Unit,
) {
    val stack = remember { SystemBarAppearanceStack() }
    val fallback = rememberDefaultGradientChromeAppearance()

    CompositionLocalProvider(LocalSystemBarAppearanceStack provides stack) {
        content()
    }


    val stackRevision = stack.revision
    val appearance = stack.current ?: fallback
    SystemBarIconEffect(appearance, stackRevision)

    val view = LocalView.current
    LaunchedEffect(splashExitGeneration, appearance) {
        if (splashExitGeneration > 0 && !view.isInEditMode) {
            applySystemBarAppearance(view.context as Activity, view, appearance)
        }
    }
}


@Composable
fun PublishSystemBarAppearance(appearance: SystemBarAppearance) {
    val stack = LocalSystemBarAppearanceStack.current
    val token = remember { Any() }
    SideEffect {
        stack.set(token, appearance)
    }
    DisposableEffect(token) {
        onDispose { stack.remove(token) }
    }
}

@Composable
private fun SystemBarIconEffect(appearance: SystemBarAppearance, stackRevision: Int) {
    val view = LocalView.current
    if (view.isInEditMode) return

    SideEffect {
        applySystemBarAppearance(view.context as Activity, view, appearance)
    }
}

internal fun applySystemBarAppearance(
    activity: Activity,
    view: android.view.View,
    appearance: SystemBarAppearance,
) {
    val window = activity.window
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }
    WindowCompat.getInsetsController(window, view).apply {
        isAppearanceLightStatusBars = appearance.lightStatusBarIcons
        isAppearanceLightNavigationBars = appearance.lightNavigationBarIcons
    }
}

@Composable
fun SyncActivityWindowBackground() {
    val background = MaterialTheme.colorScheme.background
    val view = LocalView.current
    if (view.isInEditMode) return

    SideEffect {
        val activity = view.context as? Activity ?: return@SideEffect
        activity.window.setBackgroundDrawable(background.toArgb().toDrawable())
    }
}

@Composable
fun CatalogHomeSystemBars(compactToolbarVisible: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val gradientChrome = rememberDefaultGradientChromeAppearance()
    val appearance = remember(compactToolbarVisible, scheme, gradientChrome) {
        if (compactToolbarVisible) {
            SystemBarAppearance(
                lightStatusBarIcons = StatusBarIconPolicy.prefersLightIcons(scheme.surface),
                lightNavigationBarIcons = StatusBarIconPolicy.prefersLightIcons(scheme.background),
            )
        } else {
            gradientChrome
        }
    }
    PublishSystemBarAppearance(appearance)
}

@Composable
fun SettingsCollapsingSystemBars(collapsedFraction: Float) {
    val scheme = MaterialTheme.colorScheme
    val appearance = remember(collapsedFraction, scheme) {
        SystemBarAppearance.forCollapsingToolbar(
            collapsedFraction = collapsedFraction,
            background = scheme.background,
            surface = scheme.surface,
        )
    }
    PublishSystemBarAppearance(appearance)
}

@Composable
fun ImmersiveDetailSystemBars() {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.45f
    val scrim = scheme.surface.copy(alpha = if (isDark) 0.58f else 0.82f)
    val appearance = remember(scheme, scrim) {
        SystemBarAppearance.forImmersiveOverlay(
            background = scheme.background,
            overlaySample = scrim,
        )
    }
    PublishSystemBarAppearance(appearance)
}

@Composable
fun DetailMaterialSystemBars() {
    val scheme = MaterialTheme.colorScheme
    val appearance = remember(scheme) {
        SystemBarAppearance.forBackdrop(scheme.background)
    }
    PublishSystemBarAppearance(appearance)
}

@Composable
fun MaterialSystemBarsForBackdrop(backdrop: Color) {
    val appearance = remember(backdrop) {
        SystemBarAppearance.forBackdrop(backdrop)
    }
    PublishSystemBarAppearance(appearance)
}


fun prefersLightSystemBarIcons(backdrop: Color): Boolean = StatusBarIconPolicy.prefersLightIcons(backdrop)
