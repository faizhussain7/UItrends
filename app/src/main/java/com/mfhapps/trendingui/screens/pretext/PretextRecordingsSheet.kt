package com.mfhapps.trendingui.screens.pretext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mfhapps.trendingui.ui.components.AppModalBottomSheet
import com.mfhapps.trendingui.ui.components.TextButton
import kotlinx.coroutines.launch

private sealed interface PretextRecordingsOverlay {
    data object None : PretextRecordingsOverlay
    data class Player(val recording: PretextRecording) : PretextRecordingsOverlay
    data class Share(val recording: PretextRecording) : PretextRecordingsOverlay
    data class DeleteConfirm(val recording: PretextRecording) : PretextRecordingsOverlay
}

@Composable
fun PretextRecordingsSheet(
    repository: PretextRecordingRepository,
    onDismiss: () -> Unit,
) {
    val recordings by repository.recordings.collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var overlay by remember { mutableStateOf<PretextRecordingsOverlay>(PretextRecordingsOverlay.None) }

    when (val active = overlay) {
        is PretextRecordingsOverlay.Player -> {
            PretextFullscreenVideoPlayerHost(
                recording = active.recording,
                onDismiss = { overlay = PretextRecordingsOverlay.None },
                onShare = { recording -> overlay = PretextRecordingsOverlay.Share(recording) },
                onDeleteClick = { recording -> overlay = PretextRecordingsOverlay.DeleteConfirm(recording) },
            )
        }
        is PretextRecordingsOverlay.Share -> {
            PretextVideoShareSheet(
                videoUri = active.recording.uri(),
                onDismiss = { overlay = PretextRecordingsOverlay.None },
            )
        }
        is PretextRecordingsOverlay.DeleteConfirm -> {
            PretextDeleteRecordingDialog(
                onConfirm = {
                    scope.launch {
                        repository.deleteRecording(active.recording)
                        overlay = PretextRecordingsOverlay.None
                    }
                },
                onDismiss = { overlay = PretextRecordingsOverlay.None },
            )
        }
        PretextRecordingsOverlay.None -> Unit
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
                    text = "Recordings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Saved Pretext clips with live text reflow and contours.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (recordings.isEmpty()) {
                Text(
                    text = "No clips yet. Tap Record in the camera dock to capture your first video.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                recordings.forEach { recording ->
                    PretextRecordingCard(
                        recording = recording,
                        onPreview = { overlay = PretextRecordingsOverlay.Player(recording) },
                        onOpenShareSheet = { overlay = PretextRecordingsOverlay.Share(recording) },
                        onDeleteClick = { overlay = PretextRecordingsOverlay.DeleteConfirm(recording) },
                    )
                }
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close")
            }
        }
    }
}
