package com.mfhapps.trendingui.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    RegisterModalBackdrop(visible = true, onDismiss = onDismissRequest)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        properties = properties,
        containerColor = AlertDialogDefaults.containerColor,
        iconContentColor = AlertDialogDefaults.iconContentColor,
        titleContentColor = AlertDialogDefaults.titleContentColor,
        textContentColor = AlertDialogDefaults.textContentColor,
        tonalElevation = AlertDialogDefaults.TonalElevation,
        shape = AlertDialogDefaults.shape,
    )
}
