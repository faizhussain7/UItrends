package com.mfhapps.trendingui.ui.theme

import android.os.Build
import androidx.annotation.FontRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.mfhapps.trendingui.R

object AppFonts {
    private val catalogWeightValues = listOf(400, 500, 700)

    private fun staticFamily(@FontRes resId: Int): FontFamily =
        FontFamily(Font(resId, FontWeight.Normal))

    private fun variableFamily(
        @FontRes resId: Int,
        weightValues: List<Int> = catalogWeightValues,
    ): FontFamily {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return FontFamily(Font(resId, FontWeight.Normal))
        }
        return FontFamily(
            weightValues.map { weightValue ->
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
        variableFamily(
            resId = R.font.satoshi_variable,
            weightValues = listOf(300, 400, 500, 700, 900),
        )
    }
    private val lato by lazy { staticFamily(R.font.lato_regular) }
    private val alegreya by lazy { variableFamily(R.font.alegreya_variable) }
    private val lora by lazy { variableFamily(R.font.lora_variable, listOf(400, 500, 700)) }
    private val poppins by lazy { staticFamily(R.font.poppins_regular) }
    private val roboto by lazy { variableFamily(R.font.roboto_variable) }
    private val openSans by lazy { variableFamily(R.font.open_sans_variable, listOf(300, 400, 500, 700, 800)) }
    private val montserrat by lazy { variableFamily(R.font.montserrat_variable, listOf(100, 400, 500, 700, 900)) }
    private val raleway by lazy { variableFamily(R.font.raleway_variable, listOf(100, 400, 500, 700, 900)) }

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

    fun warmUp(style: AppFontStyle) {
        fontFamily(style)
    }
}
