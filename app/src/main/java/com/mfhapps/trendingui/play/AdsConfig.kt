package com.mfhapps.trendingui.play

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.mfhapps.trendingui.BuildConfig

object AdsConfig {
    val appId: String
        get() = BuildConfig.ADMOB_APP_ID

    val bannerUnitId: String
        get() = BuildConfig.ADMOB_BANNER_UNIT_ID

    val adsEnabled: Boolean
        get() = appId.isNotBlank() && bannerUnitId.isNotBlank()

    val catalogBannerReserveDp = 66

    @Volatile
    private var initialized = false

    @Volatile
    private var initializing = false

    private val pendingCallbacks = mutableListOf<() -> Unit>()

    @Synchronized
    fun initialize(context: Context, onReady: () -> Unit = {}) {
        if (!adsEnabled) return
        if (initialized) {
            onReady()
            return
        }
        pendingCallbacks += onReady
        if (initializing) return
        initializing = true
        MobileAds.initialize(context.applicationContext) {
            synchronized(this) {
                initialized = true
                initializing = false
                val callbacks = pendingCallbacks.toList()
                pendingCallbacks.clear()
                callbacks.forEach { it() }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
fun HomeBannerAd(
    modifier: Modifier = Modifier,
) {
    if (!AdsConfig.adsEnabled) return

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() ?: context }
    val lifecycleOwner = LocalLifecycleOwner.current
    val unitId = AdsConfig.bannerUnitId
    var adViewRef by remember { mutableStateOf<AdView?>(null) }

    AndroidView(
        factory = {
            AdView(activity).apply {
                adUnitId = unitId
                setAdSize(AdSize.BANNER)
                adViewRef = this
                AdsConfig.initialize(context.applicationContext) {
                    loadAd(AdRequest.Builder().build())
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .wrapContentHeight(),
    )

    DisposableEffect(lifecycleOwner, adViewRef) {
        val adView = adViewRef
        if (adView == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                adView.resume()
            }
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                adView.destroy()
                adViewRef = null
            }
        }
    }
}
