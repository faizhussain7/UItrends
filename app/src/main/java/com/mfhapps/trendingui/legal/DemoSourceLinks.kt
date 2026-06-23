package com.mfhapps.trendingui.legal

import com.mfhapps.trendingui.navigation.DemoPaneKey

private const val SCREEN_ROOT = "app/src/main/java/com/mfhapps/trendingui/screens"

fun demoSourceCodeUrl(paneKey: DemoPaneKey): String {
    val relativePath = when (paneKey) {
        DemoPaneKey.Pretext -> "$SCREEN_ROOT/pretext"
        DemoPaneKey.Chat -> "$SCREEN_ROOT/chat"
        DemoPaneKey.Bento -> "$SCREEN_ROOT/bento"
        DemoPaneKey.Glass -> "$SCREEN_ROOT/glass"
        DemoPaneKey.Orbs -> "$SCREEN_ROOT/orbs"
        DemoPaneKey.Kinetic -> "$SCREEN_ROOT/kinetic"
        DemoPaneKey.Brutal -> "$SCREEN_ROOT/brutal"
        DemoPaneKey.Neumorphism -> "$SCREEN_ROOT/neumorphism"
        DemoPaneKey.ZeroUi -> "$SCREEN_ROOT/zeroui"
        DemoPaneKey.Spatial -> "$SCREEN_ROOT/spatial"
        DemoPaneKey.Semantic -> "$SCREEN_ROOT/semantic"
        DemoPaneKey.Copilot -> "$SCREEN_ROOT/copilot"
        DemoPaneKey.Calm -> "$SCREEN_ROOT/calm"
        DemoPaneKey.Masonry -> "$SCREEN_ROOT/masonry"
    }
    return "${AppLinks.GITHUB_TREE}/$relativePath"
}
