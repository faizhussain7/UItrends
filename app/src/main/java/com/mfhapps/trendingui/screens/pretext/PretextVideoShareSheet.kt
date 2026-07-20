@file:OptIn(ExperimentalLayoutApi::class)

package com.mfhapps.trendingui.screens.pretext

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.components.AppModalBottomSheet
import com.mfhapps.trendingui.ui.components.TextButton

@Composable
fun PretextVideoShareSheet(
    videoUri: Uri,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val installedTargets = remember { partitionInstalledShareTargets(context) }

    AppModalBottomSheet(onDismiss = onDismiss, scrollable = false) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Share recording",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Choose where to send this clip.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (installedTargets.primary.isNotEmpty()) {
                PretextShareSection(title = "Social") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        installedTargets.primary.forEach { target ->
                            PretextShareSheetChip(
                                target = target,
                                icon = resolveShareAppIcon(context, target.packageName!!),
                                onClick = {
                                    shareRecordingToTarget(context, videoUri, target)
                                    onDismiss()
                                },
                            )
                        }
                    }
                }
            }

            if (installedTargets.extended.isNotEmpty()) {
                PretextShareSection(title = "More options") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        installedTargets.extended.forEach { target ->
                            PretextShareSheetChip(
                                target = target,
                                icon = resolveShareAppIcon(context, target.packageName!!),
                                onClick = {
                                    shareRecordingToTarget(context, videoUri, target)
                                    onDismiss()
                                },
                            )
                        }
                    }
                }
            }

            Surface(
                onClick = {
                    shareRecordingToTarget(context, videoUri, PretextMoreShareTarget)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PretextShareTargetRow(
                        label = PretextMoreShareTarget.label,
                        icon = null,
                        colors = PretextShareSurfaceStyle.Sheet.colors(),
                    )
                }
            }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}
