package com.mfhapps.trendingui.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.components.AppModalBottomSheet
import com.mfhapps.trendingui.ui.components.rememberAppModalSheetState
import com.mfhapps.trendingui.ui.detail.DetailChromeStyle
import com.mfhapps.trendingui.ui.detail.LocalDetailChromeStyle
import com.mfhapps.trendingui.ui.neumorphism.NeuGuideSheetFrame
import com.mfhapps.trendingui.ui.neumorphism.NeuShadows
import com.mfhapps.trendingui.ui.neumorphism.rememberNeuShadows
import com.mfhapps.trendingui.ui.glass.GlassGuideSheetFrame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoTrendGuideSheet(
    guide: DemoTrendGuide,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberAppModalSheetState(),
    chromeStyle: DetailChromeStyle = LocalDetailChromeStyle.current,
) {
    AppModalBottomSheet(
        onDismiss = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        scrollable = true,
        chromeStyle = chromeStyle,
    ) {
        when (chromeStyle) {
            DetailChromeStyle.NeoBrutal -> BrutalGuideSheetFrame {
                DemoTrendGuideSheetContent(
                    guide = guide,
                    chromeStyle = chromeStyle,
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp, top = 8.dp),
                )
            }
            DetailChromeStyle.Neumorphism -> NeuGuideSheetFrame {
                DemoTrendGuideSheetContent(
                    guide = guide,
                    chromeStyle = chromeStyle,
                    contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = 24.dp, top = 8.dp),
                )
            }
            DetailChromeStyle.Glass -> GlassGuideSheetFrame {
                DemoTrendGuideSheetContent(
                    guide = guide,
                    chromeStyle = chromeStyle,
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 24.dp, top = 4.dp),
                )
            }
            DetailChromeStyle.Orbs,
            DetailChromeStyle.Spatial,
            DetailChromeStyle.Copilot,
            DetailChromeStyle.Default -> DemoTrendGuideSheetContent(
                guide = guide,
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp, top = 4.dp),
            )
        }
    }
}

@Composable
fun DemoTrendGuideSheetContent(
    guide: DemoTrendGuide,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
    chromeStyle: DetailChromeStyle = DetailChromeStyle.Default,
) {
    val uriHandler = LocalUriHandler.current
    val brutal = chromeStyle == DetailChromeStyle.NeoBrutal
    val neu = chromeStyle == DetailChromeStyle.Neumorphism
    val glass = chromeStyle == DetailChromeStyle.Glass
    val scheme = MaterialTheme.colorScheme
    val ink = scheme.onSurface
    val neuShadows = if (neu) rememberNeuShadows() else null
    val sectionSpacing = when (chromeStyle) {
        DetailChromeStyle.NeoBrutal, DetailChromeStyle.Neumorphism, DetailChromeStyle.Glass -> 12.dp
        DetailChromeStyle.Orbs,
        DetailChromeStyle.Spatial,
        DetailChromeStyle.Copilot,
        DetailChromeStyle.Default -> 16.dp
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
    ) {
        when (chromeStyle) {
            DetailChromeStyle.NeoBrutal -> Text(
                text = guide.patternName.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = scheme.onTertiaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(scheme.tertiaryContainer)
                    .border(3.dp, ink)
                    .padding(14.dp),
            )
            DetailChromeStyle.Neumorphism -> {
                val neu = neuShadows!!
                Text(
                    text = guide.patternName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, neu.stroke, RoundedCornerShape(16.dp))
                        .background(scheme.surface)
                        .padding(14.dp),
                )
            }
            DetailChromeStyle.Glass -> Text(
                text = guide.patternName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            DetailChromeStyle.Orbs,
            DetailChromeStyle.Spatial,
            DetailChromeStyle.Copilot,
            DetailChromeStyle.Default -> Text(
                text = guide.patternName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = guide.summary,
            style = MaterialTheme.typography.titleMedium,
            color = when (chromeStyle) {
                DetailChromeStyle.NeoBrutal -> scheme.onSurface
                DetailChromeStyle.Neumorphism -> scheme.onSurface
                DetailChromeStyle.Glass -> scheme.onSurface
                DetailChromeStyle.Orbs,
                DetailChromeStyle.Spatial,
                DetailChromeStyle.Copilot -> scheme.onSurface
                DetailChromeStyle.Default -> scheme.primary
            },
            fontWeight = if (brutal || neu || glass) FontWeight.SemiBold else FontWeight.Normal,
            modifier = when (chromeStyle) {
                DetailChromeStyle.NeoBrutal -> Modifier
                    .fillMaxWidth()
                    .border(2.dp, ink)
                    .background(scheme.primaryContainer)
                    .padding(12.dp)
                DetailChromeStyle.Neumorphism -> Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, neuShadows!!.stroke, RoundedCornerShape(14.dp))
                    .background(scheme.surfaceContainerHigh)
                    .padding(12.dp)
                DetailChromeStyle.Glass -> Modifier.fillMaxWidth()
                DetailChromeStyle.Orbs,
                DetailChromeStyle.Spatial,
                DetailChromeStyle.Copilot,
                DetailChromeStyle.Default -> Modifier
            },
        )

        GuideSection(title = "What it is", chromeStyle = chromeStyle) {
            Text(
                text = guide.whatItIs,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        GuideSection(title = "In this demo", chromeStyle = chromeStyle) {
            Text(
                text = guide.usageInThisDemo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        GuideSection(title = "Best practices", chromeStyle = chromeStyle) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                guide.bestPractices.forEach { tip ->
                    Text(
                        text = "• $tip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (guide.links.isNotEmpty()) {
            GuideSection(title = "Learn more", chromeStyle = chromeStyle) {
                Column(verticalArrangement = Arrangement.spacedBy(if (brutal || neu) 8.dp else 0.dp)) {
                    guide.links.forEachIndexed { index, link ->
                        if (neu && neuShadows != null) {
                            NeuLinkItem(
                                label = link.label,
                                neu = neuShadows,
                                onClick = { uriHandler.openUri(link.url) },
                            )
                        } else if (brutal) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 3.dp, bottom = 3.dp),
                            ) {
                                Box(
                                    Modifier
                                        .matchParentSize()
                                        .padding(start = 3.dp, top = 3.dp)
                                        .background(ink),
                                )
                                RowLinkItem(
                                    label = link.label,
                                    ink = ink,
                                    fill = scheme.surfaceContainerHigh,
                                    onClick = { uriHandler.openUri(link.url) },
                                )
                            }
                        } else {
                            ListItem(
                                headlineContent = { Text(link.label) },
                                trailingContent = {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.OpenInNew,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(role = Role.Button) {
                                        uriHandler.openUri(link.url)
                                    },
                            )
                            if (index < guide.links.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrutalGuideSheetFrame(content: @Composable () -> Unit) {
    val ink = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(3.dp, ink)
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp),
    ) {
        content()
    }
}

@Composable
private fun RowLinkItem(
    label: String,
    ink: Color,
    fill: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, ink)
            .background(fill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun NeuLinkItem(
    label: String,
    neu: NeuShadows,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, neu.stroke, RoundedCornerShape(14.dp))
            .background(neu.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = scheme.primary,
            )
        }
    }
}

@Composable
private fun GuideSection(
    title: String,
    chromeStyle: DetailChromeStyle,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val ink = scheme.onSurface
    when (chromeStyle) {
        DetailChromeStyle.NeoBrutal -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, ink)
                .background(scheme.surfaceContainerHighest)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = scheme.primary,
            )
            content()
        }
        DetailChromeStyle.Neumorphism -> {
            val neu = rememberNeuShadows()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, neu.stroke, RoundedCornerShape(16.dp))
                    .background(scheme.surface)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.primary,
                )
                content()
            }
        }
        DetailChromeStyle.Glass -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            content()
        }
        DetailChromeStyle.Orbs,
        DetailChromeStyle.Spatial,
        DetailChromeStyle.Copilot,
        DetailChromeStyle.Default -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onBackground,
            )
            content()
        }
    }
}
