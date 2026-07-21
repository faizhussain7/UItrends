package com.mfhapps.trendingui.play

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.gms.ads.MobileAds
import com.mfhapps.trendingui.BuildConfig

object AdsConfig {
    val appId: String
        get() = BuildConfig.ADMOB_APP_ID

    val bannerUnitId: String
        get() = BuildConfig.ADMOB_BANNER_UNIT_ID

    val adsEnabled: Boolean
        get() = appId.isNotBlank() && bannerUnitId.isNotBlank()

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

internal fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
