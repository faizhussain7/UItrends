@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalLayoutApi::class,
)

package com.mfhapps.trendingui.screens.pretext

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PretextRecordingCard(
    recording: PretextRecording,
    onPreview: () -> Unit,
    onOpenShareSheet: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val formattedDate = remember(recording.createdAtMillis) {
        DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(recording.createdAtMillis))
    }
    var durationMs by remember(recording.contentUri) { mutableLongStateOf(0L) }
    LaunchedEffect(recording.contentUri) {
        durationMs = queryRecordingDurationMs(context, recording.uri())
    }
    val shareTargets = remember { partitionInstalledShareTargets(context).primary }
    val fileSizeLabel = remember(recording.sizeBytes) { formatRecordingFileSize(recording.sizeBytes) }
    val durationLabel = remember(durationMs) { formatRecordingDuration(durationMs) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        color = scheme.surfaceContainerHigh,
        contentColor = scheme.onSurface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            PretextRecordingVerticalPreview(
                uri = recording.uri(),
                contentDescription = "Preview ${recording.displayName}",
                durationLabel = durationLabel,
                onClick = onPreview,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(PretextRecordingConfig.CARD_THUMB_HEIGHT),
                verticalArrangement = Arrangement.spacedBy(PretextRecordingConfig.CARD_SECTION_SPACING),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onPreview,
                            ),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Pretext clip",
                            style = MaterialTheme.typography.titleSmallEmphasized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(PretextRecordingConfig.SHARE_ICON_BUTTON_SIZE),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = scheme.error,
                        ),
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete recording",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(PretextRecordingConfig.SHARE_ICON_BUTTON_SPACING),
                    verticalArrangement = Arrangement.spacedBy(PretextRecordingConfig.SHARE_ICON_BUTTON_SPACING),
                ) {
                    if (fileSizeLabel.isNotBlank()) {
                        PretextRecordingMetaChip(label = fileSizeLabel)
                    }
                    if (durationLabel.isNotBlank()) {
                        PretextRecordingMetaChip(label = durationLabel)
                    }
                    PretextRecordingMetaChip(label = PretextRecordingConfig.ASPECT_LABEL)
                }

                PretextQuickShareGrid(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    targets = shareTargets,
                    videoUri = recording.uri(),
                    onOpenShareSheet = onOpenShareSheet,
                    style = PretextShareSurfaceStyle.Card,
                )
            }
        }
    }
}

@Composable
private fun PretextRecordingVerticalPreview(
    uri: android.net.Uri,
    contentDescription: String,
    durationLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val thumbShape = MaterialTheme.shapes.medium

    Box(
        modifier = modifier
            .width(PretextRecordingConfig.CARD_THUMB_WIDTH)
            .aspectRatio(PretextRecordingConfig.ASPECT_RATIO)
            .clip(thumbShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        PretextRecordingThumbnail(
            uri = uri,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to scheme.scrim.copy(alpha = 0.04f),
                        0.45f to scheme.scrim.copy(alpha = 0.0f),
                        1f to scheme.scrim.copy(alpha = 0.52f),
                    ),
                ),
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
            shape = MaterialTheme.shapes.small,
            color = scheme.primaryContainer.copy(alpha = 0.92f),
            contentColor = scheme.onPrimaryContainer,
            tonalElevation = 0.dp,
        ) {
            Text(
                text = "Pretext",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .size(40.dp),
            shape = CircleShape,
            color = scheme.primary.copy(alpha = 0.94f),
            contentColor = scheme.onPrimary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        if (durationLabel.isNotBlank()) {
            Text(
                text = durationLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = PretextRecordingTheme.overlayContent,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(PretextRecordingTheme.overlayScrim, MaterialTheme.shapes.extraSmall)
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun PretextRecordingMetaChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = scheme.surfaceContainerHighest,
        contentColor = scheme.onSurfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
