package com.mfhapps.trendingui.screens.pretext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import com.mfhapps.trendingui.ui.components.AppModalBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretextRecordingSettingsSheet(
    settings: PretextRecordingSettings,
    repository: PretextRecordingSettingsRepository,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val resolvedProfile = remember(settings.qualityMode) {
        repository.resolveProfile(settings)
    }
    val deviceSummary = remember {
        PretextRecordingProfileResolver.deviceSummary(context)
    }

    AppModalBottomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Recording settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Frame rate, resolution, and bitrate for Pretext camera clips.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = resolvedProfile.summaryLabel(),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = if (settings.qualityMode == PretextRecordingQualityMode.Auto) {
                                "Resolved automatically for this device."
                            } else {
                                settings.qualityMode.description
                            },
                        )
                    },
                    leadingContent = {
                        DecorativeIcon(
                            Icons.Outlined.Speed,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column {
                    PretextSettingsMenuRow(
                        headline = "Quality",
                        value = settings.qualityMode.label,
                        onValueSelected = { label ->
                            val mode = PretextRecordingQualityMode.entries.firstOrNull { it.label == label }
                                ?: return@PretextSettingsMenuRow
                            scope.launch { repository.setQualityMode(mode) }
                        },
                        options = PretextRecordingQualityMode.entries.map { it.label },
                        supporting = settings.qualityMode.description,
                        leadingIcon = Icons.Outlined.HighQuality,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                ListItem(
                    headlineContent = { Text("Device profile") },
                    supportingContent = { Text(deviceSummary) },
                    leadingContent = {
                        DecorativeIcon(
                            Icons.Outlined.Memory,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}
