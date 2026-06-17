package com.mfhapps.trendingui.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

internal val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "uitrends_theme",
)

class ThemePreferencesRepository(
    private val context: Context,
    private val themeModeStore: ThemeModeStore,
) {
    val preferences: Flow<ThemePreferences> = combine(
        context.themeDataStore.data,
        themeModeStore.themeMode,
    ) { prefs, themeMode ->
        val legacyHue = prefs[Keys.customGradientHueLegacy]
        ThemePreferences(
            themeMode = themeMode,
            useDynamicColor = prefs[Keys.dynamicColor] ?: true,
            brandAccentColor = BrandAccentColor.entries.firstOrNull {
                it.name == prefs[Keys.brandAccent]
            } ?: BrandAccentColor.Rose,
            homeGradientStyle = HomeGradientStyle.RoseBloom,
            homeLayoutStyle = HomeLayoutStyle.entries.firstOrNull {
                it.name == prefs[Keys.homeLayout]
            } ?: HomeLayoutStyle.FeaturedList,
            customGradient = CustomGradientConfig(
                startHue = prefs[Keys.customStartHue] ?: legacyHue ?: DEFAULT_CUSTOM_GRADIENT_START_HUE,
                endHue = prefs[Keys.customEndHue]
                    ?: legacyHue?.let { (it + 60f).mod(360f) }
                    ?: DEFAULT_CUSTOM_GRADIENT_END_HUE,
                saturation = prefs[Keys.customSaturation] ?: DEFAULT_CUSTOM_GRADIENT_SATURATION,
            ).normalized(),
            blurModalBackdrop = prefs[Keys.blurModalBackdrop] ?: true,
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        themeModeStore.set(mode)
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.themeDataStore.edit { it[Keys.dynamicColor] = enabled }
    }

    suspend fun setBrandAccentColor(accent: BrandAccentColor) {
        context.themeDataStore.edit { it[Keys.brandAccent] = accent.name }
    }

    suspend fun setHomeLayoutStyle(style: HomeLayoutStyle) {
        context.themeDataStore.edit { it[Keys.homeLayout] = style.name }
    }

    suspend fun setCustomGradient(config: CustomGradientConfig) {
        val normalized = config.normalized()
        context.themeDataStore.edit {
            it[Keys.customStartHue] = normalized.startHue
            it[Keys.customEndHue] = normalized.endHue
            it[Keys.customSaturation] = normalized.saturation
        }
    }

    suspend fun setBlurModalBackdrop(enabled: Boolean) {
        context.themeDataStore.edit { it[Keys.blurModalBackdrop] = enabled }
    }

    private companion object Keys {
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val brandAccent = stringPreferencesKey("brand_accent")
        val homeLayout = stringPreferencesKey("home_layout")
        val blurModalBackdrop = booleanPreferencesKey("blur_modal_backdrop")
        val customStartHue = floatPreferencesKey("custom_gradient_start_hue")
        val customEndHue = floatPreferencesKey("custom_gradient_end_hue")
        val customSaturation = floatPreferencesKey("custom_gradient_saturation")
        val customGradientHueLegacy = floatPreferencesKey("custom_gradient_hue")
    }
}
