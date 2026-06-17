package com.mfhapps.trendingui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.settings.SettingsAccentSwatch
import com.mfhapps.trendingui.ui.theme.BrandAccentColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrandAccentColorPicker(
    selected: BrandAccentColor,
    onSelected: (BrandAccentColor) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BrandAccentColor.entries.forEach { accent ->
            SettingsAccentSwatch(
                color = accent.swatch,
                selected = selected == accent,
                onClick = { onSelected(accent) },
                enabled = enabled,
                contentDescription = "${accent.label} accent${if (selected == accent) ", selected" else ""}",
            )
        }
    }
}
