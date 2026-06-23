package com.mfhapps.trendingui.launcher

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.PathInterpolator
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.R as CoreSplashR
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider

fun Activity.installTrendingSplashScreen(
    shouldKeepOnScreen: () -> Boolean,
    onSplashExitComplete: () -> Unit = {},
): SplashScreen {
    val splashScreen = installSplashScreen()
    splashScreen.setKeepOnScreenCondition(shouldKeepOnScreen)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        splashScreen.setOnExitAnimationListener { provider ->
            provider.remove()
            onSplashExitComplete()
        }
        return splashScreen
    }

    val exitInterpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)

    splashScreen.setOnExitAnimationListener { provider ->
        val splashView = provider.view
        val iconView = provider.resolveSplashIconView()
        val waitForIconMs = remainingIconAnimationMs(provider, hasIcon = iconView != null)

        splashView.postOnAnimationDelayed({
            runSplashExitAnimation(
                provider = provider,
                splashView = splashView,
                iconView = iconView,
                exitInterpolator = exitInterpolator,
                onComplete = onSplashExitComplete,
            )
        }, waitForIconMs)
    }

    return splashScreen
}

private fun SplashScreenViewProvider.resolveSplashIconView(): View? {
    view.findViewById<View>(CoreSplashR.id.splashscreen_icon_view)?.let { return it }
    return findFirstImageView(view)
}

private fun findFirstImageView(root: View): View? {
    if (root is ImageView) return root
    if (root !is ViewGroup) return null
    for (index in 0 until root.childCount) {
        findFirstImageView(root.getChildAt(index))?.let { return it }
    }
    return null
}

private fun remainingIconAnimationMs(
    provider: SplashScreenViewProvider,
    hasIcon: Boolean,
): Long {
    if (!hasIcon) return 0L
    return try {
        val duration = provider.iconAnimationDurationMillis
        if (duration <= 0L) return 0L
        val elapsed = SystemClock.uptimeMillis() - provider.iconAnimationStartMillis
        val remaining = duration - elapsed
        remaining.coerceIn(0L, SplashScreenTiming.crossfadeMs)
    } catch (_: RuntimeException) {
        0L
    }
}

private fun runSplashExitAnimation(
    provider: SplashScreenViewProvider,
    splashView: View,
    iconView: View?,
    exitInterpolator: Interpolator,
    onComplete: () -> Unit,
) {
    if (iconView != null) {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(iconView, View.ALPHA, iconView.alpha, 0f).apply {
                    duration = 280L
                    interpolator = exitInterpolator
                },
                ObjectAnimator.ofFloat(iconView, View.SCALE_X, iconView.scaleX, 0.94f).apply {
                    duration = 280L
                    interpolator = exitInterpolator
                },
                ObjectAnimator.ofFloat(iconView, View.SCALE_Y, iconView.scaleY, 0.94f).apply {
                    duration = 280L
                    interpolator = exitInterpolator
                },
            )
            start()
        }
    }

    ObjectAnimator.ofFloat(splashView, View.ALPHA, splashView.alpha, 0f).apply {
        duration = 220L
        startDelay = if (iconView != null) 120L else 0L
        interpolator = exitInterpolator
        doOnEnd {
            provider.remove()
            onComplete()
        }
    }.start()
}
