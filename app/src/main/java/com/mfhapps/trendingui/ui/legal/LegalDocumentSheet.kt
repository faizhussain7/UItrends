package com.mfhapps.trendingui.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.legal.LegalDocument
import com.mfhapps.trendingui.legal.LegalSection
import com.mfhapps.trendingui.ui.components.AppModalBottomSheet
import com.mfhapps.trendingui.ui.components.rememberAppModalSheetState

@Composable
fun LegalDocumentSheet(
    document: LegalDocument,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberAppModalSheetState(),
) {
    AppModalBottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        scrollable = false,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = LegalSheetDefaults.contentPadding,
            verticalArrangement = Arrangement.spacedBy(LegalSheetDefaults.sectionSpacing),
        ) {
            item(key = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                    )
                    Text(
                        text = document.subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(document.sections, key = { it.heading }) { section ->
                LegalSectionBlock(section = section)
            }
        }
    }
}

@Composable
private fun LegalSectionBlock(section: LegalSection) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = section.heading,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.primary,
        )
        section.paragraphs.forEach { paragraph ->
            Text(
                text = paragraph,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface,
            )
        }
        if (section.bullets.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                section.bullets.forEach { bullet ->
                    Text(
                        text = "• $bullet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
