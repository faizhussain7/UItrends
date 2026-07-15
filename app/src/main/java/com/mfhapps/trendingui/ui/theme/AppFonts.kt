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

    private val inter by lazy { variableFamily(R.font.inter_variable) }
    private val satoshi by lazy {
        variableFamily(R.font.satoshi_variable, listOf(300, 400, 500, 600, 700, 900))
    }
    private val lato by lazy { staticFamily(R.font.lato_regular) }
    private val alegreya by lazy { variableFamily(R.font.alegreya_variable) }
    private val lora by lazy { variableFamily(R.font.lora_variable, listOf(400, 500, 600, 700)) }
    private val poppins by lazy { staticFamily(R.font.poppins_regular) }
    private val roboto by lazy { variableFamily(R.font.roboto_variable) }
    private val openSans by lazy {
        variableFamily(R.font.open_sans_variable, listOf(300, 400, 500, 600, 700, 800))
    }
    private val montserrat by lazy { variableFamily(R.font.montserrat_variable) }
    private val raleway by lazy { variableFamily(R.font.raleway_variable) }

    val brand: FontFamily get() = satoshi
    val body: FontFamily get() = inter

    fun pairFor(style: AppFontStyle): AppFontPair = when (style) {
        AppFontStyle.Satoshi -> AppFontPair(brand = satoshi, body = inter)
        AppFontStyle.Montserrat -> AppFontPair(brand = montserrat, body = openSans)
        AppFontStyle.Raleway -> AppFontPair(brand = raleway, body = inter)
        AppFontStyle.Lora -> AppFontPair(brand = lora, body = openSans)
        AppFontStyle.Alegreya -> AppFontPair(brand = alegreya, body = inter)
        AppFontStyle.OpenSans -> AppFontPair(brand = openSans, body = openSans)
        AppFontStyle.Inter -> AppFontPair(brand = inter, body = inter)
        AppFontStyle.Poppins -> AppFontPair(brand = poppins, body = inter)
        AppFontStyle.Lato -> AppFontPair(brand = lato, body = inter)
        AppFontStyle.Roboto -> AppFontPair(brand = roboto, body = roboto)
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
