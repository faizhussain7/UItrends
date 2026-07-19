package com.mfhapps.trendingui.ui.theme

enum class AppFontStyle(
    val label: String,
    val brandName: String,
    val plainName: String,
    val description: String,
) {
    UnboundedManrope(
        label = "Unbounded × Manrope",
        brandName = "Unbounded",
        plainName = "Manrope",
        description = "Bold display brand · calm UI plain",
    ),
    FrauncesPublicSans(
        label = "Fraunces × Public Sans",
        brandName = "Fraunces",
        plainName = "Public Sans",
        description = "Soft expressive serif · accessible UI plain",
    ),
    BricolageJakarta(
        label = "Bricolage Grotesque × Plus Jakarta Sans",
        brandName = "Bricolage Grotesque",
        plainName = "Plus Jakarta Sans",
        description = "Characterful grotesque · product UI plain",
    ),
    SyneDmSans(
        label = "Syne × DM Sans",
        brandName = "Syne",
        plainName = "DM Sans",
        description = "Artful display brand · dense UI plain",
    ),
    AnybodyOutfit(
        label = "Anybody × Outfit",
        brandName = "Anybody",
        plainName = "Outfit",
        description = "Width-playful display · geometric UI plain",
    ),
    BigShouldersSource(
        label = "Big Shoulders Display × Source Sans 3",
        brandName = "Big Shoulders Display",
        plainName = "Source Sans 3",
        description = "Condensed poster brand · classic UI plain",
    ),
    YoungSerifFigtree(
        label = "Young Serif × Figtree",
        brandName = "Young Serif",
        plainName = "Figtree",
        description = "Quirky editorial brand · warm UI plain",
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
            if (name == null) return UnboundedManrope
            entries.find { it.name == name }?.let { return it }
            return when (name) {
                "FrauncesManrope", "InstrumentSerif", "SyneOutfit", "SoraManrope",
                "DmPair", "Satoshi", "Inter", "Poppins", "Lato", "Roboto",
                "OpenSans", "Montserrat", "Raleway", "Expressive", "Brand",
                "Jakarta", "DmSans", "OutfitManrope", "SoraDmSans",
                "GeistPublicSans", "FigtreeSourceSans", "RedHat",
                "InterTightPublicSans", "SpaceOutfit", "SpaceNewsreader",
                -> UnboundedManrope
                "Lora", "Alegreya", "Editorial" -> FrauncesPublicSans
                "System" -> System
                else -> UnboundedManrope
            }
        }
    }
}
