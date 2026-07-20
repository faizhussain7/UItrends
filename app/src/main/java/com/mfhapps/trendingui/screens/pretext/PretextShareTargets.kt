package com.mfhapps.trendingui.screens.pretext

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class PretextShareTarget(
    val id: String,
    val label: String,
    val packageName: String? = null,
)

data class PretextInstalledShareTargets(
    val primary: List<PretextShareTarget>,
    val extended: List<PretextShareTarget>,
)

val PretextPrimaryShareTargets: List<PretextShareTarget> = listOf(
    PretextShareTarget("whatsapp", "WhatsApp", "com.whatsapp"),
    PretextShareTarget("instagram", "Instagram", "com.instagram.android"),
    PretextShareTarget("facebook", "Facebook", "com.facebook.katana"),
    PretextShareTarget("snapchat", "Snapchat", "com.snapchat.android"),
    PretextShareTarget("telegram", "Telegram", "org.telegram.messenger"),
    PretextShareTarget("x", "X", "com.twitter.android"),
    PretextShareTarget("tiktok", "TikTok", "com.zhiliaoapp.musically"),
)

val PretextExtendedShareTargets: List<PretextShareTarget> = listOf(
    PretextShareTarget("messenger", "Messenger", "com.facebook.orca"),
    PretextShareTarget("messages", "Messages", "com.google.android.apps.messaging"),
    PretextShareTarget("gmail", "Gmail", "com.google.android.gm"),
    PretextShareTarget("drive", "Drive", "com.google.android.apps.docs"),
)

val PretextMoreShareTarget = PretextShareTarget("more", "Other apps")

fun buildInstalledShareTargets(
    context: Context,
    includeExtended: Boolean = false,
): List<PretextShareTarget> {
    return partitionInstalledShareTargets(context).let { installed ->
        buildList {
            addAll(installed.primary)
            if (includeExtended) addAll(installed.extended)
        }
    }
}

fun partitionInstalledShareTargets(context: Context): PretextInstalledShareTargets {
    val pm = context.packageManager
    val primary = PretextPrimaryShareTargets.filter { target ->
        target.packageName != null && isSharePackageInstalled(pm, target.packageName)
    }
    val primaryIds = primary.map { it.id }.toSet()
    val extended = PretextExtendedShareTargets.filter { target ->
        target.packageName != null &&
            target.id !in primaryIds &&
            isSharePackageInstalled(pm, target.packageName)
    }
    return PretextInstalledShareTargets(primary = primary, extended = extended)
}

fun isSharePackageInstalled(pm: PackageManager, packageName: String): Boolean {
    return runCatching {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, 0)
        }
        true
    }.getOrDefault(false)
}

fun resolveShareAppIcon(context: Context, packageName: String): Drawable? {
    return runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
}

fun shareRecordingToTarget(
    context: Context,
    videoUri: android.net.Uri,
    target: PretextShareTarget,
) {
    if (target.packageName == null) {
        PretextVideoActions.shareUriWithChooser(context, videoUri)
    } else {
        PretextVideoActions.shareUriToPackage(
            context = context,
            uri = videoUri,
            packageName = target.packageName,
        )
    }
}
