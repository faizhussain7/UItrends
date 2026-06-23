package com.mfhapps.trendingui.ui.legal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.legal.AppLinks
import com.mfhapps.trendingui.legal.LegalDocumentKind
import com.mfhapps.trendingui.legal.legalDocument
import com.mfhapps.trendingui.ui.settings.SettingsIconBadge
import com.mfhapps.trendingui.ui.settings.SettingsSectionCard
import com.mfhapps.trendingui.ui.settings.SettingsSectionDivider

private sealed interface SettingsLegalAction {
    data class Document(val kind: LegalDocumentKind) : SettingsLegalAction
    data object Licenses : SettingsLegalAction
    data class ExternalLink(val url: String) : SettingsLegalAction
}

private data class SettingsLegalEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeColors: (ColorScheme) -> Pair<Color, Color>,
    val action: SettingsLegalAction,
)

private val settingsLegalEntries = listOf(
    SettingsLegalEntry(
        title = "How to use UITrends",
        subtitle = "Catalog, demos, settings, and accessibility",
        icon = Icons.AutoMirrored.Outlined.MenuBook,
        badgeColors = { it.primaryContainer to it.onPrimaryContainer },
        action = SettingsLegalAction.Document(LegalDocumentKind.HowToUse),
    ),
    SettingsLegalEntry(
        title = "Terms & conditions",
        subtitle = "Agreement, warranty, and liability",
        icon = Icons.Outlined.Gavel,
        badgeColors = { it.secondaryContainer to it.onSecondaryContainer },
        action = SettingsLegalAction.Document(LegalDocumentKind.Terms),
    ),
    SettingsLegalEntry(
        title = "Usage restrictions",
        subtitle = "Permitted use, model weights, and redistribution",
        icon = Icons.Outlined.Policy,
        badgeColors = { it.tertiaryContainer to it.onTertiaryContainer },
        action = SettingsLegalAction.Document(LegalDocumentKind.UsageRestrictions),
    ),
    SettingsLegalEntry(
        title = "Open source & licenses",
        subtitle = "MIT app code, libraries, and model notices",
        icon = Icons.Outlined.Key,
        badgeColors = { it.surfaceVariant to it.onSurfaceVariant },
        action = SettingsLegalAction.Licenses,
    ),
    SettingsLegalEntry(
        title = "View on GitHub",
        subtitle = "Source, issues, and contributions",
        icon = Icons.Outlined.Code,
        badgeColors = { it.primaryContainer to it.onPrimaryContainer },
        action = SettingsLegalAction.ExternalLink(AppLinks.GITHUB_REPO),
    ),
    SettingsLegalEntry(
        title = "Third-party notices file",
        subtitle = "Full dependency table in the repository",
        icon = Icons.Outlined.Info,
        badgeColors = { it.surfaceContainerHigh to it.onSurface },
        action = SettingsLegalAction.ExternalLink(AppLinks.THIRD_PARTY_NOTICES),
    ),
)

@Composable
fun SettingsLegalPanel(
    modifier: Modifier = Modifier,
) {
    var showLicenses by rememberSaveable { mutableStateOf(false) }
    var activeDocument by rememberSaveable { mutableStateOf<LegalDocumentKind?>(null) }
    val uriHandler = LocalUriHandler.current
    val scheme = MaterialTheme.colorScheme

    SettingsSectionCard(modifier = modifier) {
        settingsLegalEntries.forEachIndexed { index, entry ->
            if (index > 0) {
                SettingsSectionDivider()
            }
            val (containerColor, contentColor) = entry.badgeColors(scheme)
            val external = entry.action is SettingsLegalAction.ExternalLink
            SettingsLegalRow(
                title = entry.title,
                subtitle = entry.subtitle,
                icon = entry.icon,
                containerColor = containerColor,
                contentColor = contentColor,
                trailingExternal = external,
                onClick = {
                    when (val action = entry.action) {
                        is SettingsLegalAction.Document -> activeDocument = action.kind
                        SettingsLegalAction.Licenses -> showLicenses = true
                        is SettingsLegalAction.ExternalLink -> uriHandler.openUri(action.url)
                    }
                },
            )
        }
    }

    if (showLicenses) {
        LicensesSheet(onDismiss = { showLicenses = false })
    }

    activeDocument?.let { kind ->
        LegalDocumentSheet(
            document = legalDocument(kind),
            onDismiss = { activeDocument = null },
        )
    }
}

@Composable
private fun SettingsLegalRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingExternal: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SettingsIconBadge(
            icon = icon,
            containerColor = containerColor,
            contentColor = contentColor,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = if (trailingExternal) {
                Icons.AutoMirrored.Outlined.OpenInNew
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            },
            contentDescription = null,
            tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}
