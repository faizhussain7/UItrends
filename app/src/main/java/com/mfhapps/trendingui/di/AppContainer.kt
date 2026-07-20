package com.mfhapps.trendingui.di

import android.content.Context
import com.mfhapps.trendingui.launcher.LauncherIconRepository
import com.mfhapps.trendingui.play.PlayEngagementStore
import com.mfhapps.trendingui.play.PlayReviewRepository
import com.mfhapps.trendingui.play.PlayUpdateRepository
import com.mfhapps.trendingui.screens.pretext.PretextRecordingController
import com.mfhapps.trendingui.screens.pretext.PretextRecordingRepository
import com.mfhapps.trendingui.screens.pretext.PretextRecordingSettingsRepository
import com.mfhapps.trendingui.ui.theme.ThemeModeStore
import com.mfhapps.trendingui.ui.theme.ThemePreferencesRepository

class AppContainer(context: Context) {
    val themeMode: ThemeModeStore = ThemeModeStore.get(context.applicationContext)
    val launcherIcons: LauncherIconRepository = LauncherIconRepository(context.applicationContext)
    val themePreferences: ThemePreferencesRepository =
        ThemePreferencesRepository(context.applicationContext, themeMode)
    val playEngagement: PlayEngagementStore = PlayEngagementStore(context.applicationContext)
    val playUpdate: PlayUpdateRepository = PlayUpdateRepository(context.applicationContext)
    val playReview: PlayReviewRepository = PlayReviewRepository(playEngagement)
    val pretextRecordings: PretextRecordingRepository =
        PretextRecordingRepository(context.applicationContext)
    val pretextRecordingSettings: PretextRecordingSettingsRepository =
        PretextRecordingSettingsRepository(context.applicationContext)
    val pretextRecordingController: PretextRecordingController =
        PretextRecordingController(context.applicationContext, pretextRecordings)
}
