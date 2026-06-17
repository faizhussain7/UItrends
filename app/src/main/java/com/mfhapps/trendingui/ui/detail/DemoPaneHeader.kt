package com.mfhapps.trendingui.ui.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mfhapps.trendingui.ui.platform.isCompactWindowWidth
import androidx.compose.ui.unit.dp


@Composable
fun DemoPaneHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val inDetailPane = LocalDetailPaneActive.current
    val compact = isCompactWindowWidth()
    val showInContent = !inDetailPane || !compact

    if (!showInContent) return

    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier,
    )
    if (subtitle != null) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )
    }
}
