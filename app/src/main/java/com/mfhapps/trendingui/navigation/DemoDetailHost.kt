package com.mfhapps.trendingui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mfhapps.trendingui.legal.demoSourceCodeUrl
import com.mfhapps.trendingui.screens.bento.BentoGridScreen
import com.mfhapps.trendingui.screens.brutal.NeoBrutalismScreen
import com.mfhapps.trendingui.screens.calm.CalmUiScreen
import com.mfhapps.trendingui.screens.chat.VirtualChatScreen
import com.mfhapps.trendingui.screens.copilot.AiCopilotScreen
import com.mfhapps.trendingui.screens.glass.GlassmorphismScreen
import com.mfhapps.trendingui.screens.kinetic.KineticTypographyScreen
import com.mfhapps.trendingui.screens.masonry.ImmersiveScrollScreen
import com.mfhapps.trendingui.screens.neumorphism.NeumorphismScreen
import com.mfhapps.trendingui.screens.orbs.OrbsMeshScreen
import com.mfhapps.trendingui.screens.pretext.PretextScreen
import com.mfhapps.trendingui.screens.semantic.SemanticMotionScreen
import com.mfhapps.trendingui.screens.spatial.SpatialDepthScreen
import com.mfhapps.trendingui.screens.zeroui.ZeroUiScreen
import com.mfhapps.trendingui.ui.detail.DetailChromeStyle
import com.mfhapps.trendingui.ui.detail.DetailPaneScaffold
import com.mfhapps.trendingui.ui.detail.LocalDemoSourceCodeUrl
import com.mfhapps.trendingui.ui.guide.trendGuideForPane
import com.mfhapps.trendingui.ui.platform.isLowRamDevice

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DemoDetailHost(
    paneKey: DemoPaneKey,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entry = demoEntryForPaneKey(paneKey) ?: return
    val context = LocalContext.current
    val glassHazeEnabled = remember(context) { !context.isLowRamDevice() }

    CompositionLocalProvider(LocalDemoSourceCodeUrl provides demoSourceCodeUrl(paneKey)) {
        DetailPaneScaffold(
            modifier = modifier,
            title = entry.title,
            subtitle = entry.subtitle,
            guide = trendGuideForPane(paneKey),
            onNavigateBack = onNavigateBack,
            immersiveBackground = paneKey == DemoPaneKey.Masonry ||
                paneKey == DemoPaneKey.Kinetic ||
                paneKey == DemoPaneKey.Glass ||
                paneKey == DemoPaneKey.Orbs ||
                paneKey == DemoPaneKey.Spatial ||
                paneKey == DemoPaneKey.Copilot,
            hideCompactTopBar = paneKey == DemoPaneKey.Calm ||
                paneKey == DemoPaneKey.Orbs ||
                paneKey == DemoPaneKey.Spatial ||
                paneKey == DemoPaneKey.Pretext,
            contentOwnsTitle = paneKey == DemoPaneKey.Masonry ||
                paneKey == DemoPaneKey.Kinetic ||
                paneKey == DemoPaneKey.Brutal ||
                paneKey == DemoPaneKey.Neumorphism ||
                paneKey == DemoPaneKey.Calm ||
                paneKey == DemoPaneKey.Glass ||
                paneKey == DemoPaneKey.Orbs ||
                paneKey == DemoPaneKey.Spatial ||
                paneKey == DemoPaneKey.Copilot ||
                paneKey == DemoPaneKey.Pretext,
            glassHazeEnabled = glassHazeEnabled,
            chromeStyle = when (paneKey) {
                DemoPaneKey.Brutal -> DetailChromeStyle.NeoBrutal
                DemoPaneKey.Neumorphism -> DetailChromeStyle.Neumorphism
                DemoPaneKey.Glass -> DetailChromeStyle.Glass
                DemoPaneKey.Orbs -> DetailChromeStyle.Orbs
                DemoPaneKey.Spatial -> DetailChromeStyle.Spatial
                DemoPaneKey.Copilot -> DetailChromeStyle.Copilot
                else -> DetailChromeStyle.Default
            },
        ) {
            DemoScreenForPane(paneKey, onNavigateBack)
        }
    }
}

@Composable
private fun DemoScreenForPane(
    paneKey: DemoPaneKey,
    onNavigateBack: () -> Unit = {},
) {
    when (paneKey) {
        DemoPaneKey.Pretext -> PretextScreen(
            onNavigateBack = onNavigateBack,
            guide = trendGuideForPane(DemoPaneKey.Pretext),
        )
        DemoPaneKey.Chat -> VirtualChatScreen()
        DemoPaneKey.Bento -> BentoGridScreen()
        DemoPaneKey.Glass -> GlassmorphismScreen()
        DemoPaneKey.Orbs -> OrbsMeshScreen(
            onNavigateBack = onNavigateBack,
            guide = trendGuideForPane(DemoPaneKey.Orbs),
        )
        DemoPaneKey.Kinetic -> KineticTypographyScreen()
        DemoPaneKey.Brutal -> NeoBrutalismScreen()
        DemoPaneKey.Neumorphism -> NeumorphismScreen()
        DemoPaneKey.ZeroUi -> ZeroUiScreen()
        DemoPaneKey.Spatial -> SpatialDepthScreen(
            onNavigateBack = onNavigateBack,
            guide = trendGuideForPane(DemoPaneKey.Spatial),
        )
        DemoPaneKey.Semantic -> SemanticMotionScreen()
        DemoPaneKey.Copilot -> AiCopilotScreen()
        DemoPaneKey.Calm -> CalmUiScreen(
            onNavigateBack = onNavigateBack,
            guide = trendGuideForPane(DemoPaneKey.Calm),
        )
        DemoPaneKey.Masonry -> ImmersiveScrollScreen()
    }
}
