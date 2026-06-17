package com.mfhapps.trendingui.launcher

import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import com.mfhapps.trendingui.R

object SplashDrawableCache {

    fun warmUp(context: Context) {
        ContextCompat.getDrawable(context, R.drawable.ic_splash_catalog_static)
        AppLauncherIcon.entries.forEach { icon ->
            ContextCompat.getDrawable(context, icon.brandLogoRes)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.getDrawable(context, R.drawable.avd_splash_catalog)
            ContextCompat.getDrawable(context, R.drawable.ic_splash_catalog_vector)
        }
    }
}
