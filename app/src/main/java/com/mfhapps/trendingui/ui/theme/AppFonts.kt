package com.mfhapps.trendingui.ui.theme

import androidx.annotation.FontRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.mfhapps.trendingui.R

object AppFonts {
    private val catalogWeights = listOf(
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
        FontWeight.Bold,
    )


    private fun staticFamily(@FontRes resId: Int): FontFamily = FontFamily(
        *catalogWeights.map { weight -> Font(resId, weight) }.toTypedArray(),
    )


    private fun variableFamily(@FontRes resId: Int): FontFamily = FontFamily(
        *catalogWeights.map { weight ->
            Font(
                resId,
                weight,
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(weight.weight),
                ),
            )
        }.toTypedArray(),
    )

    private val inter = variableFamily(R.font.inter_variable)
    private val satoshi = variableFamily(R.font.satoshi_variable)
    private val lato = staticFamily(R.font.lato_regular)
    private val alegreya = variableFamily(R.font.alegreya_variable)
    private val lora = variableFamily(R.font.lora_variable)
    private val poppins = staticFamily(R.font.poppins_regular)
    private val roboto = variableFamily(R.font.roboto_variable)
    private val openSans = variableFamily(R.font.open_sans_variable)
    private val montserrat = variableFamily(R.font.montserrat_variable)
    private val raleway = variableFamily(R.font.raleway_variable)


    val brand: FontFamily get() = inter
    val body: FontFamily get() = inter

    fun fontFamily(style: AppFontStyle): FontFamily = when (style) {
        AppFontStyle.Inter -> inter
        AppFontStyle.Satoshi -> satoshi
        AppFontStyle.Lato -> lato
        AppFontStyle.Alegreya -> alegreya
        AppFontStyle.Lora -> lora
        AppFontStyle.Poppins -> poppins
        AppFontStyle.Roboto -> roboto
        AppFontStyle.OpenSans -> openSans
        AppFontStyle.Montserrat -> montserrat
        AppFontStyle.Raleway -> raleway
        AppFontStyle.System -> FontFamily.Default
    }

    fun brandFont(style: AppFontStyle): FontFamily = fontFamily(style)

    fun bodyFont(style: AppFontStyle): FontFamily = fontFamily(style)

    fun previewFont(style: AppFontStyle): FontFamily = fontFamily(style)
}
