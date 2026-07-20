package com.mfhapps.trendingui.launcher

import android.app.Activity
import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

object AppProcessRestarter {

    fun restart(context: Context) {
        val packageName = context.packageName
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            ?: return
        context.startActivity(launchIntent)
        if (context is Activity) {
            context.finishAffinity()
        }
        exitProcess(0)
    }
}
