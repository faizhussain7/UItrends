package com.mfhapps.trendingui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.Dashboard
import com.mfhapps.trendingui.ui.components.AppAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import com.mfhapps.trendingui.ui.components.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.R
import com.mfhapps.trendingui.launcher.AppLauncherIcon
import com.mfhapps.trendingui.ui.settings.SettingsCardContent
import com.mfhapps.trendingui.ui.settings.SettingsHomeDivider
import com.mfhapps.trendingui.ui.settings.SettingsHomeRowHeader
import com.mfhapps.trendingui.ui.settings.SettingsHomeStudioPanel
import com.mfhapps.trendingui.ui.settings.SettingsSectionTitle
import com.mfhapps.trendingui.ui.settings.SettingsSwitchRow
import com.mfhapps.trendingui.ui.theme.HomeLayoutStyle
import com.mfhapps.trendingui.ui.theme.ThemePreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppearanceSettings(
    preferences: ThemePreferences,
    selectedLauncherIcon: AppLauncherIcon,
    onLayoutChange: (HomeLayoutStyle) -> Unit,
    onSyncLauncherIconWithThemeChange: (Boolean) -> Unit,
    onLauncherIconChange: (AppLauncherIcon) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var pendingLauncherIcon by remember { mutableStateOf<AppLauncherIcon?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsSectionTitle(
            title = "Home catalog",
            subtitle = "Layout and launcher icon.",
        )

        SettingsHomeStudioPanel {
            SettingsHomeRowHeader(
                icon = Icons.Outlined.Dashboard,
                title = "Card layout",
                subtitle = "How demos appear on home",
            )
            SettingsCardContent {
                HomeLayoutPicker(
                    selected = preferences.homeLayoutStyle,
                    onSelected = onLayoutChange,
                )
            }

            SettingsHomeDivider()

            SettingsHomeRowHeader(
                icon = Icons.Outlined.Apps,
                title = "App icon",
                subtitle = "Uses Android activity aliases; may close the app once",
            )
            SettingsCardContent {
                AppIconPicker(
                    selected = selectedLauncherIcon,
                    onSelect = { icon ->
                        if (icon != selectedLauncherIcon) {
                            pendingLauncherIcon = icon
                        }
                    },
                )
            }

            SettingsHomeDivider()

            SettingsSwitchRow(
                checked = preferences.syncLauncherIconWithTheme,
                onCheckedChange = onSyncLauncherIconWithThemeChange,
                icon = Icons.Outlined.Brightness6,
                title = "Match app icon to theme",
                subtitle = "Switch light and dark icon variants when the theme changes",
            )
        }
    }

    pendingLauncherIcon?.let { icon ->
        val iconLabel = stringResource(icon.labelRes)
        AppAlertDialog(
            onDismissRequest = { pendingLauncherIcon = null },
            title = { Text(stringResource(R.string.launcher_icon_confirm_title)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LauncherIconPreview(
                        icon = icon,
                        size = 80.dp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(stringResource(R.string.launcher_icon_confirm_message))
                    Text(
                        text = stringResource(R.string.launcher_icon_confirm_new, iconLabel),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLauncherIconChange(icon)
                        pendingLauncherIcon = null
                        Toast.makeText(
                            context,
                            R.string.launcher_icon_changed,
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                ) {
                    Text(stringResource(R.string.launcher_icon_confirm_change))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingLauncherIcon = null }) {
                    Text(stringResource(R.string.launcher_icon_confirm_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeLayoutPicker(
    selected: HomeLayoutStyle,
    onSelected: (HomeLayoutStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val styles = HomeLayoutStyle.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        styles.forEachIndexed { index, style ->
            val label = when (style) {
                HomeLayoutStyle.FeaturedList -> "List"
                HomeLayoutStyle.BentoGrid -> "Bento"
                HomeLayoutStyle.CompactTiles -> "Grid"
            }
            SegmentedButton(
                selected = selected == style,
                onClick = { onSelected(style) },
                shape = SegmentedButtonDefaults.itemShape(index, styles.size),
                label = { Text(label) },
            )
        }
    }
}
