package com.mfhapps.trendingui.ui.theme

enum class AppFontStyle(
    val label: String,
    val description: String,
) {
    Inter(
        label = "Inter",
        description = "Modern UI sans-serif",
    ),
    Satoshi(
        label = "Satoshi",
        description = "Geometric sans-serif",
    ),
    Lato(
        label = "Lato",
        description = "Humanist sans-serif",
    ),
    Alegreya(
        label = "Alegreya",
        description = "Elegant reading serif",
    ),
    Lora(
        label = "Lora",
        description = "Contemporary serif",
    ),
    Poppins(
        label = "Poppins",
        description = "Geometric sans-serif",
    ),
    Roboto(
        label = "Roboto",
        description = "Android classic sans",
    ),
    OpenSans(
        label = "Open Sans",
        description = "Friendly sans-serif",
    ),
    Montserrat(
        label = "Montserrat",
        description = "Urban geometric sans",
    ),
    Raleway(
        label = "Raleway",
        description = "Elegant high-contrast sans",
    ),
    System(
        label = "System default",
        description = "Device typeface",
    ),
    ;

    companion object {
        fun fromStoredName(name: String?): AppFontStyle {
            if (name == null) return Raleway
            entries.find { it.name == name }?.let { return it }
            return when (name) {
                "Expressive" -> Roboto
                "Editorial" -> Lora
                "Brand" -> Raleway
                "System" -> System
                else -> Raleway
            }
        }
    }
}
