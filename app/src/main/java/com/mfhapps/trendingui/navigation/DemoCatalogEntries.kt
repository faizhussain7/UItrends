package com.mfhapps.trendingui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.ui.graphics.vector.ImageVector

enum class DemoCategory {
    Layout,
    Motion,
    Surfaces,
    AiReading,
    Sensors3d,
}

enum class DemoTag {
    Adaptive,
    Camera,
    AI,
    Motion,
}

data class DemoCatalogEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val category: DemoCategory,
    val tags: Set<DemoTag> = emptySet(),
    val route: Any,
)

val demoCatalogEntries = listOf(
    DemoCatalogEntry(
        title = "Pretext Engine",
        subtitle = "Paragraph layout + CameraX live reflow",
        icon = Icons.Default.Speed,
        category = DemoCategory.Sensors3d,
        tags = setOf(DemoTag.Camera),
        route = PretextRoute,
    ),
    DemoCatalogEntry(
        title = "Virtual Chat",
        subtitle = "500 shrink-wrapped bubbles · streaming layout",
        icon = Icons.AutoMirrored.Filled.Chat,
        category = DemoCategory.AiReading,
        tags = setOf(DemoTag.AI),
        route = ChatRoute,
    ),
    DemoCatalogEntry(
        title = "Bento Grid",
        subtitle = "Staggered cards · spring press feedback",
        icon = Icons.Default.Dashboard,
        category = DemoCategory.Layout,
        route = BentoRoute,
    ),
    DemoCatalogEntry(
        title = "Glassmorphism",
        subtitle = "Blurred layers · frosted surfaces",
        icon = Icons.Default.BlurOn,
        category = DemoCategory.Surfaces,
        route = GlassRoute,
    ),
    DemoCatalogEntry(
        title = "Orbs & Mesh",
        subtitle = "Blend-mode orbs · gyro parallax",
        icon = Icons.Default.BubbleChart,
        category = DemoCategory.Sensors3d,
        route = OrbsRoute,
    ),
    DemoCatalogEntry(
        title = "Kinetic Type",
        subtitle = "Scroll-driven typography",
        icon = Icons.Default.TextFields,
        category = DemoCategory.Motion,
        tags = setOf(DemoTag.Motion),
        route = KineticRoute,
    ),
    DemoCatalogEntry(
        title = "Neo-Brutalism",
        subtitle = "Hard shadows · bold borders",
        icon = Icons.Default.Palette,
        category = DemoCategory.Surfaces,
        route = BrutalRoute,
    ),
    DemoCatalogEntry(
        title = "Neumorphism",
        subtitle = "Soft extruded controls",
        icon = Icons.Default.Layers,
        category = DemoCategory.Surfaces,
        route = NeumorphismRoute,
    ),
    DemoCatalogEntry(
        title = "Zero UI",
        subtitle = "Contextual chrome · minimal chrome",
        icon = Icons.Default.TouchApp,
        category = DemoCategory.AiReading,
        tags = setOf(DemoTag.AI),
        route = ZeroUiRoute,
    ),
    DemoCatalogEntry(
        title = "Spatial Depth",
        subtitle = "List-detail adaptive panes",
        icon = Icons.Default.ViewInAr,
        category = DemoCategory.Sensors3d,
        tags = setOf(DemoTag.Adaptive),
        route = SpatialRoute,
    ),
    DemoCatalogEntry(
        title = "Semantic Motion",
        subtitle = "Cause → effect animations",
        icon = Icons.Default.Download,
        category = DemoCategory.Motion,
        tags = setOf(DemoTag.Motion),
        route = SemanticRoute,
    ),
    DemoCatalogEntry(
        title = "AI Copilot",
        subtitle = "Streaming tokens · bottom sheet",
        icon = Icons.Default.SmartToy,
        category = DemoCategory.AiReading,
        tags = setOf(DemoTag.AI),
        route = CopilotRoute,
    ),
    DemoCatalogEntry(
        title = "Calm UI",
        subtitle = "Supporting pane · low contrast",
        icon = Icons.Default.Spa,
        category = DemoCategory.AiReading,
        tags = setOf(DemoTag.Adaptive),
        route = CalmRoute,
    ),
    DemoCatalogEntry(
        title = "Immersive Masonry",
        subtitle = "Scroll-driven color · masonry",
        icon = Icons.Default.GridView,
        category = DemoCategory.Layout,
        route = MasonryRoute,
    ),
)

val featuredDemoTitles: Set<String> = setOf(
    "Bento Grid",
    "Immersive Masonry",
    "Orbs & Mesh",
    "Pretext Engine",
)

fun DemoCatalogEntry.isFeatured(): Boolean = title in featuredDemoTitles

fun featuredHeroDemo(): DemoCatalogEntry =
    demoCatalogEntries.firstOrNull { it.title == "Pretext Engine" }
        ?: demoCatalogEntries.firstOrNull { it.isFeatured() }
        ?: demoCatalogEntries.first()

fun demoEntryForRoute(routeClassName: String): DemoCatalogEntry? =
    demoCatalogEntries.firstOrNull { it.route::class.simpleName == routeClassName }

fun demoEntryForBackStackRoute(route: String?): DemoCatalogEntry? {
    if (route.isNullOrEmpty()) return null
    return demoCatalogEntries.firstOrNull { entry ->
        route.contains(entry.route::class.simpleName.orEmpty())
    }
}
