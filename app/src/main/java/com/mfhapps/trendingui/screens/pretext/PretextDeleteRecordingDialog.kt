package com.mfhapps.trendingui.screens.pretext

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mfhapps.trendingui.ui.components.AppAlertDialog
import com.mfhapps.trendingui.ui.components.TextButton
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon

@Composable
fun PretextDeleteRecordingDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            DecorativeIcon(
                Icons.Outlined.Delete,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text("Delete recording?") },
        text = {
            Text(
                "This clip will be removed from your device. This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "Delete",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
