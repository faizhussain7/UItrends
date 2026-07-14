package com.mfhapps.trendingui.deeplink

import android.content.Intent
import com.mfhapps.trendingui.navigation.DemoPaneKey
import com.mfhapps.trendingui.navigation.SETTINGS_DETAIL_KEY
import com.mfhapps.trendingui.navigation.toDemoPaneKeyOrNull

object AppDeepLinks {
    const val ACTION_OPEN_DESTINATION = "com.mfhapps.trendingui.action.OPEN_DESTINATION"
    const val EXTRA_DESTINATION = "extra_destination"

    const val DEST_PRETEXT = "Pretext"
    const val DEST_GLASS = "Glass"
    const val DEST_MASONRY = "Masonry"
    const val DEST_SETTINGS = SETTINGS_DETAIL_KEY

    fun destinationFrom(intent: Intent?): String? {
        if (intent == null) return null
        val fromExtra = intent.getStringExtra(EXTRA_DESTINATION)?.takeIf { it.isNotBlank() }
        if (fromExtra != null) return normalize(fromExtra)
        if (intent.action == ACTION_OPEN_DESTINATION) {
            return intent.data?.lastPathSegment?.let(::normalize)
        }
        return null
    }

    fun isKnownDestination(destination: String): Boolean =
        destination == DEST_SETTINGS || destination.toDemoPaneKeyOrNull() != null

    fun intentFlags(): Int =
        Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
            Intent.FLAG_ACTIVITY_SINGLE_TOP

    private fun normalize(raw: String): String? {
        val trimmed = raw.trim()
        return when {
            trimmed.equals(DEST_SETTINGS, ignoreCase = true) ||
                trimmed.equals("settings", ignoreCase = true) -> DEST_SETTINGS
            else -> DemoPaneKey.entries
                .firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
                ?.name
        }
    }
}
