package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
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
    val backdropBlur = LocalModalBackdropBlurEnabled.current

    RegisterModalBackdrop(visible = true, onDismiss = onDismissRequest)

    if (backdropBlur) {
        BasicAlertDialog(
            onDismissRequest = onDismissRequest,
            properties = properties,
        ) {
            ClearDialogWindowDim()
            AppAlertDialogContent(
                modifier = modifier,
                confirmButton = confirmButton,
                dismissButton = dismissButton,
                icon = icon,
                title = title,
                text = text,
            )
        }
    } else {
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
}

@Composable
private fun AppAlertDialogContent(
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Column(Modifier.padding(24.dp)) {
            icon?.let { iconContent ->
                CompositionLocalProvider(
                    LocalContentColor provides AlertDialogDefaults.iconContentColor,
                ) {
                    Box(
                        Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.CenterHorizontally),
                    ) {
                        iconContent()
                    }
                }
            }
            title?.let { titleContent ->
                CompositionLocalProvider(
                    LocalContentColor provides AlertDialogDefaults.titleContentColor,
                ) {
                    Box(
                        Modifier
                            .padding(bottom = 16.dp)
                            .align(
                                if (icon == null) {
                                    Alignment.Start
                                } else {
                                    Alignment.CenterHorizontally
                                },
                            ),
                    ) {
                        titleContent()
                    }
                }
            }
            text?.let { textContent ->
                CompositionLocalProvider(
                    LocalContentColor provides AlertDialogDefaults.textContentColor,
                ) {
                    Box(
                        Modifier
                            .padding(bottom = 24.dp)
                            .align(Alignment.Start),
                    ) {
                        textContent()
                    }
                }
            }
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                dismissButton?.invoke()
                confirmButton()
            }
        }
    }
}
