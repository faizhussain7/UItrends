package com.mfhapps.trendingui.ui.theme

import android.os.Build
import androidx.annotation.FontRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.mfhapps.trendingui.R

@Immutable
data class AppFontPair(
    val brand: FontFamily,
    val body: FontFamily,
)

object AppFonts {
    private val expressiveWeights = listOf(200, 300, 400, 500, 600, 700, 800, 900)

    private fun staticFamily(@FontRes resId: Int): FontFamily =
        FontFamily(Font(resId, FontWeight.Normal))

    private fun variableFamily(
        @FontRes resId: Int,
        weightValues: List<Int> = expressiveWeights,
    ): FontFamily {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return FontFamily(Font(resId, FontWeight.Normal))
        }
        return FontFamily(
            weightValues.distinct().sorted().map { weightValue ->
                Font(
                    resId = resId,
                    weight = FontWeight(weightValue),
                    variationSettings = FontVariation.Settings(
                        FontVariation.weight(weightValue),
                    ),
                )
            },
        )
    }

    private val unbounded by lazy {
        variableFamily(R.font.unbounded_variable, listOf(200, 300, 400, 500, 600, 700, 800, 900))
    }
    private val manrope by lazy {
        variableFamily(R.font.manrope_variable, listOf(200, 300, 400, 500, 600, 700, 800))
    }
    private val fraunces by lazy {
        variableFamily(R.font.fraunces_variable, listOf(100, 200, 300, 400, 500, 600, 700, 800, 900))
    }
    private val publicSans by lazy {
        variableFamily(R.font.public_sans_variable, listOf(100, 200, 300, 400, 500, 600, 700, 800, 900))
    }
    private val bricolageGrotesque by lazy {
        variableFamily(R.font.bricolage_grotesque_variable, listOf(200, 300, 400, 500, 600, 700, 800))
    }
    private val plusJakartaSans by lazy {
        variableFamily(R.font.plus_jakarta_sans_variable, listOf(200, 300, 400, 500, 600, 700, 800))
    }
    private val syne by lazy {
        variableFamily(R.font.syne_variable, listOf(400, 500, 600, 700, 800))
    }
    private val dmSans by lazy {
        variableFamily(R.font.dm_sans_variable, listOf(100, 200, 300, 400, 500, 600, 700, 800, 900))
    }
    private val anybody by lazy {
        variableFamily(R.font.anybody_variable, listOf(100, 200, 300, 400, 500, 600, 700, 800, 900))
    }
    private val outfit by lazy {
        variableFamily(R.font.outfit_variable, listOf(100, 200, 300, 400, 500, 600, 700, 800, 900))
    }
    private val bigShouldersDisplay by lazy {
        variableFamily(R.font.big_shoulders_display_variable, listOf(100, 200, 300, 400, 500, 600, 700, 800, 900))
    }
    private val sourceSans3 by lazy {
        variableFamily(R.font.source_sans_3_variable, listOf(200, 300, 400, 500, 600, 700, 800, 900))
    }
    private val youngSerif by lazy { staticFamily(R.font.young_serif_regular) }
    private val figtree by lazy {
        variableFamily(R.font.figtree_variable, listOf(300, 400, 500, 600, 700, 800, 900))
    }

    val brand: FontFamily get() = unbounded
    val body: FontFamily get() = manrope

    fun pairFor(style: AppFontStyle): AppFontPair = when (style) {
        AppFontStyle.UnboundedManrope -> AppFontPair(brand = unbounded, body = manrope)
        AppFontStyle.FrauncesPublicSans -> AppFontPair(brand = fraunces, body = publicSans)
        AppFontStyle.BricolageJakarta -> AppFontPair(brand = bricolageGrotesque, body = plusJakartaSans)
        AppFontStyle.SyneDmSans -> AppFontPair(brand = syne, body = dmSans)
        AppFontStyle.AnybodyOutfit -> AppFontPair(brand = anybody, body = outfit)
        AppFontStyle.BigShouldersSource -> AppFontPair(brand = bigShouldersDisplay, body = sourceSans3)
        AppFontStyle.YoungSerifFigtree -> AppFontPair(brand = youngSerif, body = figtree)
        AppFontStyle.System -> AppFontPair(brand = FontFamily.Default, body = FontFamily.Default)
    }

    fun fontFamily(style: AppFontStyle): FontFamily = pairFor(style).brand

    fun brandFont(style: AppFontStyle): FontFamily = pairFor(style).brand

    fun bodyFont(style: AppFontStyle): FontFamily = pairFor(style).body

    fun previewFont(style: AppFontStyle): FontFamily = pairFor(style).brand

    fun warmUp(style: AppFontStyle) {
        pairFor(style)
    }
}
