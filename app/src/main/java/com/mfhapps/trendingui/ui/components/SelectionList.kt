package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class SelectionOption(
    val title: String,
    val subtitle: String? = null,
)

@Composable
fun SelectionList(
    label: String,
    options: List<SelectionOption>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column {
                options.forEachIndexed { index, option ->
                    val selected = index == selectedIndex
                    ListItem(
                        headlineContent = { Text(option.title) },
                        supportingContent = option.subtitle?.let {
                            { Text(it, style = MaterialTheme.typography.bodySmall) }
                        },
                        modifier = Modifier.clickable { onSelected(index) },
                        colors = ListItemDefaults.colors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        ),
                    )
                }
            }
        }
    }
}
