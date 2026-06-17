package com.mfhapps.trendingui.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

enum class BrandAccentColor(
    val label: String,
    val swatch: Color,
) {
    Rose("Rose", Rose40),
    Ocean("Ocean", Color(0xFF00658E)),
    Forest("Forest", Color(0xFF386A20)),
    Plum("Plum", Color(0xFF7B4A9E)),
    Slate("Slate", BlueGray40),
}

fun BrandAccentColor.colorScheme(darkTheme: Boolean): ColorScheme {
    val base = if (darkTheme) BrandDarkScheme else BrandLightScheme
    return base.withAccentHarmony(harmony(darkTheme))
}


private data class AccentHarmony(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val inversePrimary: Color,
    val surfaceTint: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
)

private fun ColorScheme.withAccentHarmony(harmony: AccentHarmony): ColorScheme {
    return copy(
        primary = harmony.primary,
        onPrimary = harmony.onPrimary,
        primaryContainer = harmony.primaryContainer,
        onPrimaryContainer = harmony.onPrimaryContainer,
        inversePrimary = harmony.inversePrimary,
        surfaceTint = harmony.surfaceTint,
        secondary = harmony.secondary,
        onSecondary = harmony.onSecondary,
        secondaryContainer = harmony.secondaryContainer,
        onSecondaryContainer = harmony.onSecondaryContainer,
        tertiary = harmony.tertiary,
        onTertiary = harmony.onTertiary,
        tertiaryContainer = harmony.tertiaryContainer,
        onTertiaryContainer = harmony.onTertiaryContainer,
    )
}

private fun BrandAccentColor.harmony(darkTheme: Boolean): AccentHarmony = when (this) {
    BrandAccentColor.Rose -> if (darkTheme) roseHarmonyDark() else roseHarmonyLight()
    BrandAccentColor.Ocean -> if (darkTheme) oceanHarmonyDark() else oceanHarmonyLight()
    BrandAccentColor.Forest -> if (darkTheme) forestHarmonyDark() else forestHarmonyLight()
    BrandAccentColor.Plum -> if (darkTheme) plumHarmonyDark() else plumHarmonyLight()
    BrandAccentColor.Slate -> if (darkTheme) slateHarmonyDark() else slateHarmonyLight()
}


private fun roseHarmonyLight() = AccentHarmony(
    primary = Rose40,
    onPrimary = Color.White,
    primaryContainer = Rose90,
    onPrimaryContainer = Rose10,
    inversePrimary = Rose80,
    surfaceTint = Rose40,
    secondary = Rose30,
    onSecondary = Color.White,
    secondaryContainer = Rose95,
    onSecondaryContainer = Rose20,
    tertiary = Honey40,
    onTertiary = Color.White,
    tertiaryContainer = Honey90,
    onTertiaryContainer = Honey10,
)

private fun roseHarmonyDark() = AccentHarmony(
    primary = Rose80,
    onPrimary = Rose20,
    primaryContainer = Rose30,
    onPrimaryContainer = Rose90,
    inversePrimary = Rose40,
    surfaceTint = Rose80,
    secondary = Rose80,
    onSecondary = Rose20,
    secondaryContainer = Rose30,
    onSecondaryContainer = Rose90,
    tertiary = Honey80,
    onTertiary = Honey20,
    tertiaryContainer = Honey30,
    onTertiaryContainer = Honey90,
)

private fun oceanHarmonyLight() = AccentHarmony(
    primary = Color(0xFF00658E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6FF),
    onPrimaryContainer = Color(0xFF001E2F),
    inversePrimary = Color(0xFF8ECDFF),
    surfaceTint = Color(0xFF00658E),
    secondary = Color(0xFF004E6B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8E4F5),
    onSecondaryContainer = Color(0xFF001E2F),
    tertiary = Color(0xFFB8860B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE082),
    onTertiaryContainer = Color(0xFF3D2E00),
)

private fun oceanHarmonyDark() = AccentHarmony(
    primary = Color(0xFF8ECDFF),
    onPrimary = Color(0xFF00344A),
    primaryContainer = Color(0xFF004C6A),
    onPrimaryContainer = Color(0xFFC8E6FF),
    inversePrimary = Color(0xFF00658E),
    surfaceTint = Color(0xFF8ECDFF),
    secondary = Color(0xFF7AB8D4),
    onSecondary = Color(0xFF00344A),
    secondaryContainer = Color(0xFF003A52),
    onSecondaryContainer = Color(0xFFB8E4F5),
    tertiary = Color(0xFFFFCA4D),
    onTertiary = Color(0xFF3D2E00),
    tertiaryContainer = Color(0xFF5C4500),
    onTertiaryContainer = Color(0xFFFFE082),
)

private fun forestHarmonyLight() = AccentHarmony(
    primary = Color(0xFF386A20),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB7F397),
    onPrimaryContainer = Color(0xFF082100),
    inversePrimary = Color(0xFF9CD67D),
    surfaceTint = Color(0xFF386A20),
    secondary = Color(0xFF2D5016),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4E8C2),
    onSecondaryContainer = Color(0xFF0D1F06),
    tertiary = Color(0xFF8B6914),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5DFA0),
    onTertiaryContainer = Color(0xFF2A1F00),
)

private fun forestHarmonyDark() = AccentHarmony(
    primary = Color(0xFF9CD67D),
    onPrimary = Color(0xFF193800),
    primaryContainer = Color(0xFF2F5018),
    onPrimaryContainer = Color(0xFFB7F397),
    inversePrimary = Color(0xFF386A20),
    surfaceTint = Color(0xFF9CD67D),
    secondary = Color(0xFF7CB364),
    onSecondary = Color(0xFF193800),
    secondaryContainer = Color(0xFF1E3A10),
    onSecondaryContainer = Color(0xFFD4E8C2),
    tertiary = Color(0xFFE8C547),
    onTertiary = Color(0xFF2A1F00),
    tertiaryContainer = Color(0xFF4A3800),
    onTertiaryContainer = Color(0xFFF5DFA0),
)

private fun plumHarmonyLight() = AccentHarmony(
    primary = Color(0xFF7B4A9E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0D8FF),
    onPrimaryContainer = Color(0xFF2F0A44),
    inversePrimary = Color(0xFFE0B5FF),
    surfaceTint = Color(0xFF7B4A9E),
    secondary = Color(0xFF5C3374),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8D4F0),
    onSecondaryContainer = Color(0xFF2F0A44),
    tertiary = Color(0xFF2E7D6E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB8E8DF),
    onTertiaryContainer = Color(0xFF002018),
)

private fun plumHarmonyDark() = AccentHarmony(
    primary = Color(0xFFE0B5FF),
    onPrimary = Color(0xFF43125C),
    primaryContainer = Color(0xFF5C3374),
    onPrimaryContainer = Color(0xFFF0D8FF),
    inversePrimary = Color(0xFF7B4A9E),
    surfaceTint = Color(0xFFE0B5FF),
    secondary = Color(0xFFC9A3E0),
    onSecondary = Color(0xFF43125C),
    secondaryContainer = Color(0xFF4A2860),
    onSecondaryContainer = Color(0xFFE8D4F0),
    tertiary = Color(0xFF6BC4B0),
    onTertiary = Color(0xFF002018),
    tertiaryContainer = Color(0xFF004D40),
    onTertiaryContainer = Color(0xFFB8E8DF),
)

private fun slateHarmonyLight() = AccentHarmony(
    primary = BlueGray40,
    onPrimary = Color.White,
    primaryContainer = BlueGray90,
    onPrimaryContainer = BlueGray10,
    inversePrimary = BlueGray80,
    surfaceTint = BlueGray40,
    secondary = BlueGray30,
    onSecondary = Color.White,
    secondaryContainer = BlueGray95,
    onSecondaryContainer = BlueGray10,
    tertiary = Honey40,
    onTertiary = Color.White,
    tertiaryContainer = Honey90,
    onTertiaryContainer = Honey10,
)

private fun slateHarmonyDark() = AccentHarmony(
    primary = BlueGray80,
    onPrimary = BlueGray20,
    primaryContainer = BlueGray30,
    onPrimaryContainer = BlueGray90,
    inversePrimary = BlueGray40,
    surfaceTint = BlueGray80,
    secondary = BlueGray80,
    onSecondary = BlueGray20,
    secondaryContainer = BlueGray30,
    onSecondaryContainer = BlueGray90,
    tertiary = Honey80,
    onTertiary = Honey20,
    tertiaryContainer = Honey30,
    onTertiaryContainer = Honey90,
)
