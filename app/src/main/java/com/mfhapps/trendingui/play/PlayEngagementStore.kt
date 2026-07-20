package com.mfhapps.trendingui.play

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.days

private val Context.playEngagementStore: DataStore<Preferences> by preferencesDataStore(
    name = "uitrends_play_engagement",
)

class PlayEngagementStore(
    private val context: Context,
) {
    private val demoExitsKey = intPreferencesKey("demo_exits")
    private val lastReviewPromptAtKey = longPreferencesKey("last_review_prompt_at")

    suspend fun recordDemoExit(): Int {
        var count = 0
        context.playEngagementStore.edit { prefs ->
            count = (prefs[demoExitsKey] ?: 0) + 1
            prefs[demoExitsKey] = count
        }
        return count
    }

    suspend fun shouldPromptReview(
        minDemoExits: Int = 3,
        cooldownMillis: Long = 30.days.inWholeMilliseconds,
    ): Boolean {
        val prefs = context.playEngagementStore.data.first()
        val exits = prefs[demoExitsKey] ?: 0
        if (exits < minDemoExits) return false
        val last = prefs[lastReviewPromptAtKey] ?: 0L
        return System.currentTimeMillis() - last >= cooldownMillis
    }

    suspend fun markReviewPrompted() {
        context.playEngagementStore.edit {
            it[lastReviewPromptAtKey] = System.currentTimeMillis()
        }
    }
}
