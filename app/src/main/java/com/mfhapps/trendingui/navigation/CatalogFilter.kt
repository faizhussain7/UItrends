package com.mfhapps.trendingui.navigation

enum class CatalogFilter(val label: String) {
    All("All"),
    Featured("Featured"),
    Layout("Layout"),
    Motion("Motion"),
    Surfaces("Surfaces"),
    Ai("AI"),
    Sensors3d("3D"),
    Others("Others"),
    ;

    companion object {
        val homePickerEntries = listOf(
            All,
            Featured,
            Layout,
            Motion,
            Surfaces,
            Ai,
            Sensors3d,
            Others,
        )
    }
}

fun List<DemoCatalogEntry>.filterByCatalog(filter: CatalogFilter): List<DemoCatalogEntry> =
    when (filter) {
        CatalogFilter.All -> this
        CatalogFilter.Featured -> filter { it.isFeatured() }
        CatalogFilter.Layout -> filter { it.category == DemoCategory.Layout }
        CatalogFilter.Motion -> filter { it.category == DemoCategory.Motion }
        CatalogFilter.Surfaces -> filter { it.category == DemoCategory.Surfaces }
        CatalogFilter.Ai -> filter { it.category == DemoCategory.AiReading }
        CatalogFilter.Sensors3d -> filter { it.category == DemoCategory.Sensors3d }
        CatalogFilter.Others -> filter { !it.isFeatured() && it.category != DemoCategory.Layout }
    }
