package com.mfhapps.trendingui.play

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PlayUpdateRepository(
    context: Context,
) {
    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    fun installReadyEvents(): Flow<Unit> = callbackFlow {
        val listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                trySend(Unit)
            }
        }
        appUpdateManager.registerListener(listener)
        awaitClose { appUpdateManager.unregisterListener(listener) }
    }

    suspend fun checkAndStartUpdate(activity: Activity): Boolean {
        val info = runCatching { appUpdateManager.appUpdateInfo.await() }.getOrNull() ?: return false
        val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        if (!available || !info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            return false
        }
        val highPriority = info.updatePriority() >= 4 && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
        val type = if (highPriority) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
        appUpdateManager.startUpdateFlow(
            info,
            activity,
            AppUpdateOptions.newBuilder(type).build(),
        )
        return true
    }

    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }
}
