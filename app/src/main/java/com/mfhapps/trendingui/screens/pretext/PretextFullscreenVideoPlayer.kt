package com.mfhapps.trendingui.screens.pretext

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Forward10
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Replay10
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PretextFullscreenVideoPlayerHost(
    recording: PretextRecording?,
    onDismiss: () -> Unit,
    onShare: (PretextRecording) -> Unit,
    onDeleteClick: (PretextRecording) -> Unit,
) {
    if (recording != null) {
        PretextFullscreenVideoPlayer(
            recording = recording,
            onDismiss = onDismiss,
            onShare = { onShare(recording) },
            onDeleteClick = { onDeleteClick(recording) },
        )
    }
}

@Composable
private fun PretextFullscreenVideoPlayer(
    recording: PretextRecording,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val context = LocalContext.current
    val videoUri = recording.uri()
    val player = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var scrubbing by remember { mutableStateOf(false) }
    val overlayState = rememberPretextPlayerOverlayState(
        isPlaying = isPlaying,
        scrubbing = scrubbing,
    )

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    BackHandler(onBack = onDismiss)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pretextPlayerOverlayToggleOnTap(overlayState),
        ) {
            ContentFrame(
                player = player,
                modifier = Modifier.fillMaxSize(),
            )

            AnimatedVisibility(
                visible = overlayState.isVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter),
            ) {
                PretextPlayerTopBar(
                    fileSizeLabel = formatRecordingFileSize(recording.sizeBytes),
                    onClose = {
                        overlayState.reveal()
                        onDismiss()
                    },
                    onDelete = {
                        overlayState.reveal()
                        onDeleteClick()
                    },
                )
            }

            AnimatedVisibility(
                visible = overlayState.isVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                PretextPlaybackControls(
                    player = player,
                    videoUri = videoUri,
                    shareTargets = remember { partitionInstalledShareTargets(context).primary },
                    onUserInteraction = { overlayState.reveal() },
                    onScrubbingChange = { scrubbing = it },
                    onOpenShareSheet = onShare,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PretextPlayerTopBar(
    fileSizeLabel: String,
    onClose: () -> Unit,
    onDelete: () -> Unit,
) {
    val playerColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = PretextRecordingTheme.overlayScrimSoft,
        contentColor = PretextRecordingTheme.overlayContent,
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.72f),
                    1f to Color.Transparent,
                ),
            )
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
                ),
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(onClick = onClose, colors = playerColors) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pretext clip",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PretextRecordingTheme.overlayContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (fileSizeLabel.isNotBlank()) {
                    Text(
                        text = fileSizeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = PretextRecordingTheme.overlayContent.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            FilledTonalIconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = PretextRecordingTheme.overlayScrimSoft,
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete recording")
            }
        }
    }
}

@Composable
private fun PretextPlaybackControls(
    player: Player,
    videoUri: Uri,
    shareTargets: List<PretextShareTarget>,
    onUserInteraction: () -> Unit,
    onScrubbingChange: (Boolean) -> Unit,
    onOpenShareSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var positionMs by remember { mutableLongStateOf(player.currentPosition) }
    var durationMs by remember { mutableLongStateOf(player.duration.coerceAtLeast(0L)) }
    var scrubbing by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    durationMs = player.duration.coerceAtLeast(0L)
                }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(scrubbing) {
        onScrubbingChange(scrubbing)
    }

    LaunchedEffect(player, scrubbing) {
        while (isActive && !scrubbing) {
            positionMs = player.currentPosition.coerceAtLeast(0L)
            val duration = player.duration
            if (duration > 0L) durationMs = duration
            delay(PretextRecordingConfig.PLAYBACK_POSITION_POLL_MS)
        }
    }

    val sliderValue = when {
        durationMs <= 0L -> 0f
        scrubbing -> scrubFraction
        else -> (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    val playerColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = PretextRecordingTheme.overlayScrimSoft,
        contentColor = PretextRecordingTheme.overlayContent,
    )

    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.30f to Color.Black.copy(alpha = 0.55f),
                    1f to Color.Black.copy(alpha = 0.88f),
                ),
            )
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal,
                ),
            )
            .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Slider(
            value = sliderValue,
            onValueChange = { value ->
                onUserInteraction()
                scrubbing = true
                scrubFraction = value
            },
            onValueChangeFinished = {
                if (durationMs > 0L) {
                    player.seekTo((scrubFraction * durationMs).toLong())
                    positionMs = player.currentPosition
                }
                scrubbing = false
                onUserInteraction()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.White.copy(alpha = 0.28f),
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalIconButton(
                onClick = {
                    onUserInteraction()
                    player.seekTo(0L)
                },
                colors = playerColors,
            ) {
                Icon(Icons.Outlined.Replay, contentDescription = "Restart")
            }
            FilledTonalIconButton(
                onClick = {
                    onUserInteraction()
                    player.seekTo((player.currentPosition - PretextRecordingConfig.PLAYBACK_SKIP_MS).coerceAtLeast(0L))
                },
                colors = playerColors,
            ) {
                Icon(Icons.Outlined.Replay10, contentDescription = "Back 10 seconds")
            }
            FilledIconButton(
                onClick = {
                    onUserInteraction()
                    if (player.isPlaying) player.pause() else player.play()
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                )
            }
            FilledTonalIconButton(
                onClick = {
                    onUserInteraction()
                    val maxPosition = player.duration.coerceAtLeast(0L)
                    player.seekTo(
                        (player.currentPosition + PretextRecordingConfig.PLAYBACK_SKIP_MS).coerceAtMost(maxPosition),
                    )
                },
                colors = playerColors,
            ) {
                Icon(Icons.Outlined.Forward10, contentDescription = "Forward 10 seconds")
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color.Black.copy(alpha = 0.35f),
                contentColor = PretextRecordingTheme.overlayContent,
            ) {
                Text(
                    text = "${formatRecordingDuration(positionMs)} / ${formatRecordingDuration(durationMs)}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }

        PretextQuickShareRow(
            targets = shareTargets,
            videoUri = videoUri,
            onOpenShareSheet = onOpenShareSheet,
            style = PretextShareSurfaceStyle.PlayerOverlay,
            onInteraction = onUserInteraction,
        )
    }
}
