package com.mfhapps.trendingui.ui.theme

enum class AppFontStyle(
    val label: String,
    val brandName: String,
    val plainName: String,
    val description: String,
) {
    Satoshi(
        label = "Satoshi × Inter",
        brandName = "Satoshi",
        plainName = "Inter",
        description = "Expressive geometric brand · screen-first UI plain",
    ),
    Montserrat(
        label = "Montserrat × Open Sans",
        brandName = "Montserrat",
        plainName = "Open Sans",
        description = "Urban geometry · humanist reading pair",
    ),
    Raleway(
        label = "Raleway × Inter",
        brandName = "Raleway",
        plainName = "Inter",
        description = "Elegant display · crisp UI plain",
    ),
    Lora(
        label = "Lora × Open Sans",
        brandName = "Lora",
        plainName = "Open Sans",
        description = "Editorial serif brand · warm sans plain",
    ),
    Alegreya(
        label = "Alegreya × Inter",
        brandName = "Alegreya",
        plainName = "Inter",
        description = "Literary serif brand · modern UI plain",
    ),
    OpenSans(
        label = "Open Sans",
        brandName = "Open Sans",
        plainName = "Open Sans",
        description = "Friendly humanist throughout",
    ),
    Inter(
        label = "Inter",
        brandName = "Inter",
        plainName = "Inter",
        description = "Modern UI sans throughout",
    ),
    Poppins(
        label = "Poppins × Inter",
        brandName = "Poppins",
        plainName = "Inter",
        description = "Rounded geometric brand · UI plain",
    ),
    Lato(
        label = "Lato × Inter",
        brandName = "Lato",
        plainName = "Inter",
        description = "Humanist display · UI plain",
    ),
    Roboto(
        label = "Roboto",
        brandName = "Roboto",
        plainName = "Roboto",
        description = "Material / Android baseline",
    ),
    System(
        label = "System default",
        brandName = "System",
        plainName = "System",
        description = "Device typeface",
    ),
    ;

    val isPaired: Boolean get() = brandName != plainName

    companion object {
        fun fromStoredName(name: String?): AppFontStyle {
            if (name == null) return Satoshi
            entries.find { it.name == name }?.let { return it }
            return when (name) {
                "Expressive" -> Satoshi
                "Editorial" -> Lora
                "Brand" -> Raleway
                "System" -> System
                else -> Satoshi
            }
        }
    }
}
