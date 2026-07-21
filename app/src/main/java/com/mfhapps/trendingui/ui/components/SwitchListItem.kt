package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.rememberControlHaptics

@Composable
fun SwitchListItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = Color.Unspecified,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptics = rememberControlHaptics()
    fun applyCheckedChange(value: Boolean) {
        if (!enabled || value == checked) return
        haptics.onToggle()
        onCheckedChange(value)
    }
    val toggle: () -> Unit = { applyCheckedChange(!checked) }

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                role = Role.Switch,
                onClick = toggle,
            ),
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = {
            AppSwitch(
                checked = checked,
                onCheckedChange = ::applyCheckedChange,
                enabled = enabled,
                interactionSource = interactionSource,
            )
        },
        colors = if (containerColor == Color.Unspecified) {
            ListItemDefaults.colors()
        } else {
            ListItemDefaults.colors(containerColor = containerColor)
        },
    )
}

@Composable
fun SwitchPreferenceRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    containerColor: Color = Color.Unspecified,
) {
    SwitchListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        containerColor = containerColor,
        modifier = modifier.padding(vertical = 2.dp),
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyMedium) },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
