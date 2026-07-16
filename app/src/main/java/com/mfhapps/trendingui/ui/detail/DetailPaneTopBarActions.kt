package com.mfhapps.trendingui.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import com.mfhapps.trendingui.ui.components.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.brutal.BrutalChromeIconButton
import com.mfhapps.trendingui.ui.glass.GlassChromeIconButton
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import com.mfhapps.trendingui.ui.guide.DemoTrendGuideSheet
import com.mfhapps.trendingui.ui.neumorphism.NeuChromeIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPaneTopBarActions(
    guide: DemoTrendGuide?,
    modifier: Modifier = Modifier,
    sourceCodeUrl: String? = LocalDemoSourceCodeUrl.current,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = Color.Transparent,
    chromeStyle: DetailChromeStyle = LocalDetailChromeStyle.current,
    content: @Composable (() -> Unit)? = null,
) {
    if (guide == null && sourceCodeUrl == null && content == null) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        content?.invoke()
        if (sourceCodeUrl != null) {
            DetailPaneSourceCodeAction(
                url = sourceCodeUrl,
                iconTint = iconTint,
                containerColor = containerColor,
                chromeStyle = chromeStyle,
            )
        }
        if (guide != null) {
            DetailPaneGuideAction(
                guide = guide,
                iconTint = iconTint,
                containerColor = containerColor,
                chromeStyle = chromeStyle,
            )
        }
    }
}

@Composable
fun DetailPaneSourceCodeAction(
    url: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = Color.Transparent,
    chromeStyle: DetailChromeStyle = LocalDetailChromeStyle.current,
) {
    val uriHandler = LocalUriHandler.current
    val scheme = MaterialTheme.colorScheme
    DetailPaneChromeIconButton(
        onClick = { uriHandler.openUri(url) },
        icon = Icons.Outlined.Code,
        contentDescription = "View source code on GitHub",
        modifier = modifier,
        iconTint = iconTint,
        containerColor = containerColor,
        chromeStyle = chromeStyle,
        brutalFill = scheme.tertiaryContainer,
        brutalIconTint = scheme.onTertiaryContainer,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPaneGuideAction(
    guide: DemoTrendGuide,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = Color.Transparent,
    chromeStyle: DetailChromeStyle = LocalDetailChromeStyle.current,
) {
    var showGuide by rememberSaveable { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    DetailPaneChromeIconButton(
        onClick = { showGuide = true },
        icon = Icons.AutoMirrored.Outlined.HelpOutline,
        contentDescription = "UI pattern guide",
        modifier = modifier,
        iconTint = iconTint,
        containerColor = containerColor,
        chromeStyle = chromeStyle,
        brutalFill = scheme.secondaryContainer,
        brutalIconTint = scheme.onSecondaryContainer,
    )

    if (showGuide) {
        DemoTrendGuideSheet(
            guide = guide,
            onDismiss = { showGuide = false },
            chromeStyle = chromeStyle,
        )
    }
}

@Composable
private fun DetailPaneChromeIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = Color.Transparent,
    chromeStyle: DetailChromeStyle = LocalDetailChromeStyle.current,
    brutalFill: Color = MaterialTheme.colorScheme.secondaryContainer,
    brutalIconTint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    when (chromeStyle) {
        DetailChromeStyle.NeoBrutal -> BrutalChromeIconButton(
            onClick = onClick,
            icon = icon,
            contentDescription = contentDescription,
            modifier = modifier,
            fill = brutalFill,
            iconTint = brutalIconTint,
        )
        DetailChromeStyle.Neumorphism -> NeuChromeIconButton(
            onClick = onClick,
            icon = icon,
            contentDescription = contentDescription,
            modifier = modifier,
            iconTint = MaterialTheme.colorScheme.primary,
        )
        DetailChromeStyle.Glass -> GlassChromeIconButton(
            onClick = onClick,
            icon = icon,
            contentDescription = contentDescription,
            modifier = modifier,
        )
        DetailChromeStyle.Orbs,
        DetailChromeStyle.Spatial,
        DetailChromeStyle.Copilot,
        DetailChromeStyle.Default -> IconButton(
            onClick = onClick,
            modifier = modifier,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = iconTint,
                containerColor = containerColor,
            ),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
            )
        }
    }
}
