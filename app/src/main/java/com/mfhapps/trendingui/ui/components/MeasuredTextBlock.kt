package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mfhapps.trendingui.core.text.MeasuredTextLayout

@Composable
fun MeasuredTextBlock(
    layout: MeasuredTextLayout,
    modifier: Modifier = Modifier,
    lineHeight: Dp = 22.dp,
) {
    val density = LocalDensity.current
    val blockHeight = with(density) { layout.height.toDp() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(blockHeight.coerceAtLeast(48.dp)),
        colors = CardDefaults.outlinedCardColors(),
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${layout.lineCount} lines",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${layout.height}px",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            layout.lines.forEach { line ->
                Text(
                    text = line.ifEmpty { " " },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = with(density) { lineHeight.toSp() },
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}
