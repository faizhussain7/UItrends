package com.mfhapps.trendingui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.launcher.AppLauncherIcon

@Composable
fun AppIconPicker(
    selected: AppLauncherIcon,
    onSelect: (AppLauncherIcon) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppLauncherIcon.entries.chunked(2).forEach { rowIcons ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowIcons.forEach { icon ->
                    AppIconOption(
                        icon = icon,
                        selected = icon == selected,
                        onClick = { onSelect(icon) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowIcons.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AppIconOption(
    icon: AppLauncherIcon,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(icon.labelRes)
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    Surface(
        onClick = onClick,
        modifier = modifier
            .semantics {
                role = Role.RadioButton
                contentDescription = "$label app icon${if (selected) ", selected" else ""}"
            },
        shape = shape,
        color = if (selected) {
            scheme.primaryContainer.copy(alpha = 0.55f)
        } else {
            scheme.surfaceContainerLow
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) scheme.primary else scheme.outlineVariant.copy(alpha = 0.45f),
        ),
        tonalElevation = if (selected) 2.dp else 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    tint = scheme.primary,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LauncherIconPreview(
                    icon = icon,
                    size = 64.dp,
                    showFrame = false,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
