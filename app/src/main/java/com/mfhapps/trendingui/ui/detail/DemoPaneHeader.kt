package com.mfhapps.trendingui.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.platform.appBarTopWindowInsets
import com.mfhapps.trendingui.ui.platform.isCompactWindowWidth

@Composable
fun DemoPaneHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val inDetailPane = LocalDetailPaneActive.current
    val compact = isCompactWindowWidth()
if (inDetailPane && compact) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (inDetailPane) {
                    Modifier.windowInsetsPadding(appBarTopWindowInsets())
                } else {
                    Modifier
                },
            ),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
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
}
