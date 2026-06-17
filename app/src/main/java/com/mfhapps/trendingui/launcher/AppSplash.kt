package com.mfhapps.trendingui.launcher

import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.animation.PathInterpolator
import androidx.core.animation.doOnEnd
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
        splashScreen.setOnExitAnimationListener {
            it.remove()
            onSplashExitComplete()
        }
        return splashScreen
    }

    val exitInterpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)

    splashScreen.setOnExitAnimationListener { provider ->
        val splashView = provider.view
        val iconView = provider.iconView

        val waitForIconMs = remainingIconAnimationMs(provider)
        splashView.postOnAnimationDelayed({
            val iconFade = ObjectAnimator.ofFloat(iconView, View.ALPHA, iconView.alpha, 0f).apply {
                duration = 280L
                interpolator = exitInterpolator
            }
            val iconScaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, iconView.scaleX, 0.94f).apply {
                duration = 280L
                interpolator = exitInterpolator
            }
            val iconScaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, iconView.scaleY, 0.94f).apply {
                duration = 280L
                interpolator = exitInterpolator
            }
            val shellFade = ObjectAnimator.ofFloat(splashView, View.ALPHA, splashView.alpha, 0f).apply {
                duration = 220L
                startDelay = 120L
                interpolator = exitInterpolator
                doOnEnd {
                    provider.remove()
                    onSplashExitComplete()
                }
            }

            iconFade.start()
            iconScaleX.start()
            iconScaleY.start()
            shellFade.start()
        }, waitForIconMs)
    }

    return splashScreen
}

private fun remainingIconAnimationMs(provider: SplashScreenViewProvider): Long {
    val duration = provider.iconAnimationDurationMillis
    if (duration <= 0L) return 0L
    val elapsed = SystemClock.uptimeMillis() - provider.iconAnimationStartMillis
    val remaining = duration - elapsed
    return remaining.coerceIn(0L, SplashScreenTiming.CROSSFADE_MS)
}
