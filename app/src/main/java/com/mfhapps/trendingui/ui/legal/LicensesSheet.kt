package com.mfhapps.trendingui.ui.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.legal.AppLinks
import com.mfhapps.trendingui.legal.LicenseFamily
import com.mfhapps.trendingui.legal.OpenSourceComponent
import com.mfhapps.trendingui.legal.appOwnedLicense
import com.mfhapps.trendingui.legal.openSourceComponents
import com.mfhapps.trendingui.legal.openSourceComponentsForFamily
import com.mfhapps.trendingui.ui.components.AppModalBottomSheet
import com.mfhapps.trendingui.ui.components.rememberAppModalSheetState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LicensesSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberAppModalSheetState(),
) {
    var selectedFamily by rememberSaveable { mutableStateOf(LicenseFamily.All) }
    val filtered = openSourceComponentsForFamily(selectedFamily)
    val uriHandler = LocalUriHandler.current
    val scheme = MaterialTheme.colorScheme

    AppModalBottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        scrollable = false,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = LegalSheetDefaults.contentPadding,
            verticalArrangement = Arrangement.spacedBy(LegalSheetDefaults.listItemSpacing),
        ) {
            item(key = "hero") {
                LicenseHeroCard(
                    onOpenMitLicense = { uriHandler.openUri(AppLinks.LICENSE_FILE) },
                    onOpenRepo = { uriHandler.openUri(AppLinks.GITHUB_REPO) },
                )
            }

            item(key = "stats") {
                LicenseStatsRow(componentCount = openSourceComponents.size)
            }

            item(key = "filters") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LicenseFamily.entries.forEach { family ->
                        FilterChip(
                            selected = selectedFamily == family,
                            onClick = { selectedFamily = family },
                            label = { Text(family.label) },
                        )
                    }
                }
            }

            items(filtered, key = { it.name }) { component ->
                LicenseComponentCard(
                    component = component,
                    onOpenUrl = component.url?.let { url -> { uriHandler.openUri(url) } },
                )
            }

            item(key = "footer") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button) {
                            uriHandler.openUri(AppLinks.THIRD_PARTY_NOTICES)
                        },
                    color = scheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Full third-party notices",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Complete dependency table on GitHub",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = scheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseHeroCard(
    onOpenMitLicense: () -> Unit,
    onOpenRepo: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = scheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Open source at the core",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onPrimaryContainer,
            )
            Text(
                text = "UITrends app code is MIT-licensed. Libraries and model assets ship under their own licenses—review before redistributing.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onPrimaryContainer.copy(alpha = 0.88f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LicenseBadge(
                    label = appOwnedLicense.license,
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary,
                    onClick = onOpenMitLicense,
                )
                LicenseBadge(
                    label = "GitHub",
                    containerColor = scheme.surface.copy(alpha = 0.55f),
                    contentColor = scheme.onPrimaryContainer,
                    onClick = onOpenRepo,
                )
            }
        }
    }
}

@Composable
private fun LicenseStatsRow(componentCount: Int) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LicenseStatChip(
            value = "$componentCount",
            label = "Notices",
            modifier = Modifier.weight(1f),
        )
        LicenseStatChip(
            value = "MIT",
            label = "App code",
            modifier = Modifier.weight(1f),
        )
        LicenseStatChip(
            value = "Apache",
            label = "Most deps",
            modifier = Modifier.weight(1f),
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(top = 4.dp),
        color = scheme.outlineVariant.copy(alpha = 0.35f),
    )
}

@Composable
private fun LicenseStatChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = scheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LicenseComponentCard(
    component: OpenSourceComponent,
    onOpenUrl: (() -> Unit)?,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onOpenUrl != null) {
                    Modifier.clickable(role = Role.Button, onClick = onOpenUrl)
                } else {
                    Modifier
                },
            ),
        shape = shape,
        color = scheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = component.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            LicenseBadge(
                label = component.license,
                containerColor = licenseTint(component.family).copy(alpha = 0.18f),
                contentColor = licenseTint(component.family),
            )
            if (onOpenUrl != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun LicenseBadge(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = contentColor,
        )
    }
}

@Composable
private fun licenseTint(family: LicenseFamily): Color {
    val scheme = MaterialTheme.colorScheme
    return when (family) {
        LicenseFamily.Mit -> scheme.tertiary
        LicenseFamily.Apache -> scheme.primary
        LicenseFamily.Bsd -> scheme.secondary
        LicenseFamily.Other -> scheme.error
        LicenseFamily.All -> scheme.onSurface
    }
}
