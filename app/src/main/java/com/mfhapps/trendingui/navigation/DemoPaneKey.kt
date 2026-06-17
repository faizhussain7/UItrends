package com.mfhapps.trendingui.navigation

enum class DemoPaneKey {
    Pretext,
    Chat,
    Bento,
    Glass,
    Orbs,
    Kinetic,
    Brutal,
    Neumorphism,
    ZeroUi,
    Spatial,
    Semantic,
    Copilot,
    Calm,
    Masonry,
}


val DemoPaneKey.navKey: String
    get() = name

fun String.toDemoPaneKeyOrNull(): DemoPaneKey? =
    DemoPaneKey.entries.find { it.name == this }

fun Any.toDemoPaneKey(): DemoPaneKey? = when (this) {
    PretextRoute -> DemoPaneKey.Pretext
    ChatRoute -> DemoPaneKey.Chat
    BentoRoute -> DemoPaneKey.Bento
    GlassRoute -> DemoPaneKey.Glass
    OrbsRoute -> DemoPaneKey.Orbs
    KineticRoute -> DemoPaneKey.Kinetic
    BrutalRoute -> DemoPaneKey.Brutal
    NeumorphismRoute -> DemoPaneKey.Neumorphism
    ZeroUiRoute -> DemoPaneKey.ZeroUi
    SpatialRoute -> DemoPaneKey.Spatial
    SemanticRoute -> DemoPaneKey.Semantic
    CopilotRoute -> DemoPaneKey.Copilot
    CalmRoute -> DemoPaneKey.Calm
    MasonryRoute -> DemoPaneKey.Masonry
    else -> null
}

fun DemoPaneKey.toRoute(): Any = when (this) {
    DemoPaneKey.Pretext -> PretextRoute
    DemoPaneKey.Chat -> ChatRoute
    DemoPaneKey.Bento -> BentoRoute
    DemoPaneKey.Glass -> GlassRoute
    DemoPaneKey.Orbs -> OrbsRoute
    DemoPaneKey.Kinetic -> KineticRoute
    DemoPaneKey.Brutal -> BrutalRoute
    DemoPaneKey.Neumorphism -> NeumorphismRoute
    DemoPaneKey.ZeroUi -> ZeroUiRoute
    DemoPaneKey.Spatial -> SpatialRoute
    DemoPaneKey.Semantic -> SemanticRoute
    DemoPaneKey.Copilot -> CopilotRoute
    DemoPaneKey.Calm -> CalmRoute
    DemoPaneKey.Masonry -> MasonryRoute
}

fun demoEntryForPaneKey(key: DemoPaneKey): DemoCatalogEntry? =
    demoCatalogEntries.firstOrNull { it.route.toDemoPaneKey() == key }
