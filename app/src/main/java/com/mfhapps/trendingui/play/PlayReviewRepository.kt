package com.mfhapps.trendingui.play

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.tasks.await

class PlayReviewRepository(
    private val engagementStore: PlayEngagementStore,
) {
    suspend fun launchReview(activity: Activity, force: Boolean = false): Boolean {
        if (!force && !engagementStore.shouldPromptReview()) return false
        val manager = ReviewManagerFactory.create(activity)
        val request = runCatching { manager.requestReviewFlow().await() }.getOrNull() ?: return false
        runCatching { manager.launchReviewFlow(activity, request).await() }
        engagementStore.markReviewPrompted()
        return true
    }

    suspend fun onDemoClosed(activity: Activity) {
        engagementStore.recordDemoExit()
        launchReview(activity, force = false)
    }
}
