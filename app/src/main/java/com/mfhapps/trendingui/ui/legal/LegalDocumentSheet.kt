package com.mfhapps.trendingui.ui.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.legal.LegalDocument
import com.mfhapps.trendingui.legal.LegalDocumentKind
import com.mfhapps.trendingui.legal.LegalDocumentRepository
import com.mfhapps.trendingui.legal.LegalDocumentUiState
import com.mfhapps.trendingui.legal.LegalSection
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import com.mfhapps.trendingui.ui.components.AppModalBottomSheet
import com.mfhapps.trendingui.ui.components.Button
import com.mfhapps.trendingui.ui.components.LoadingIndicator
import com.mfhapps.trendingui.ui.components.rememberAppModalSheetState

@Composable
fun LegalDocumentSheet(
    kind: LegalDocumentKind,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberAppModalSheetState(),
    repository: LegalDocumentRepository = remember { LegalDocumentRepository() },
) {
    var reloadToken by remember(kind) { mutableIntStateOf(0) }
    var state by remember(kind) {
        mutableStateOf<LegalDocumentUiState>(LegalDocumentUiState.Loading)
    }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(kind, reloadToken) {
        state = LegalDocumentUiState.Loading
        state = repository.load(kind)
    }

    AppModalBottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        scrollable = false,
    ) {
        when (val current = state) {
            LegalDocumentUiState.Loading -> LegalDocumentLoading()
            is LegalDocumentUiState.Ready -> LegalDocumentContent(
                document = current.document,
                onOpenSource = current.document.viewUrl?.let { url ->
                    { uriHandler.openUri(url) }
                },
            )
            is LegalDocumentUiState.Error -> LegalDocumentError(
                title = current.title,
                message = current.message,
                onRetry = { reloadToken++ },
                onOpenSource = current.viewUrl?.let { url ->
                    { uriHandler.openUri(url) }
                },
            )
        }
    }
}

@Composable
private fun LegalDocumentLoading() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(LegalSheetDefaults.contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 48.dp),
        ) {
            LoadingIndicator()
            Text(
                text = "Loading document…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LegalDocumentContent(
    document: LegalDocument,
    onOpenSource: (() -> Unit)?,
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
                if (onOpenSource != null) {
                    TextButton(
                        onClick = onOpenSource,
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        DecorativeIcon(
                            Icons.AutoMirrored.Outlined.OpenInNew,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "View online",
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
        }

        items(document.sections, key = { it.heading }) { section ->
            LegalSectionBlock(section = section)
        }
    }
}

@Composable
private fun LegalDocumentError(
    title: String,
    message: String,
    onRetry: () -> Unit,
    onOpenSource: (() -> Unit)?,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(LegalSheetDefaults.contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DecorativeIcon(
            Icons.Outlined.CloudOff,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(28.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmallEmphasized,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRetry) {
                Text("Retry")
            }
            if (onOpenSource != null) {
                TextButton(onClick = onOpenSource) {
                    Text("View online")
                }
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
