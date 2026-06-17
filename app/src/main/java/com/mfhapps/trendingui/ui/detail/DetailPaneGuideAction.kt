package com.mfhapps.trendingui.ui.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mfhapps.trendingui.ui.brutal.BrutalChromeIconButton
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide
import com.mfhapps.trendingui.ui.neumorphism.NeuChromeIconButton
import com.mfhapps.trendingui.ui.glass.GlassChromeIconButton
import com.mfhapps.trendingui.ui.guide.DemoTrendGuideSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPaneGuideAction(
    guide: DemoTrendGuide,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    chromeStyle: DetailChromeStyle = LocalDetailChromeStyle.current,
) {
    var showGuide by rememberSaveable { mutableStateOf(false) }
    when (chromeStyle) {
        DetailChromeStyle.NeoBrutal -> BrutalChromeIconButton(
            onClick = { showGuide = true },
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = "UI pattern guide",
            modifier = modifier,
            fill = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        DetailChromeStyle.Neumorphism -> NeuChromeIconButton(
            onClick = { showGuide = true },
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = "UI pattern guide",
            modifier = modifier,
            iconTint = MaterialTheme.colorScheme.primary,
        )
        DetailChromeStyle.Glass -> GlassChromeIconButton(
            onClick = { showGuide = true },
            icon = Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = "UI pattern guide",
            modifier = modifier,
        )
        DetailChromeStyle.Orbs,
        DetailChromeStyle.Spatial,
        DetailChromeStyle.Copilot,
        DetailChromeStyle.Default ->
            IconButton(
                onClick = { showGuide = true },
                modifier = modifier,
                colors = IconButtonDefaults.iconButtonColors(contentColor = iconTint),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "UI pattern guide",
                    tint = iconTint,
                )
            }
    }

    if (showGuide) {
        DemoTrendGuideSheet(
            guide = guide,
            onDismiss = { showGuide = false },
            chromeStyle = chromeStyle,
        )
    }
}
