package com.mfhapps.trendingui.core.text

import android.graphics.Paint
import java.text.BreakIterator
import java.util.Locale

enum class WhiteSpaceMode {
    Normal,
    PreWrap,
}

enum class WordBreakMode {
    Normal,
    KeepAll,
}

data class PrepareOptions(
    val whiteSpace: WhiteSpaceMode = WhiteSpaceMode.Normal,
    val wordBreak: WordBreakMode = WordBreakMode.Normal,
    val letterSpacingPx: Float = 0f,
)

data class UnitGraphemeFit(
    val clusters: Array<String>,
    val advancesPx: FloatArray,
) {
    fun widthSum(fromGrapheme: Int = 0, toGrapheme: Int = clusters.size): Float {
        var sum = 0f
        for (i in fromGrapheme until toGrapheme.coerceAtMost(clusters.size)) {
            sum += advancesPx[i]
        }
        return sum
    }
}

object PretextTextAnalysis {

    private val closingPunctuation = setOf('.', ',', ';', ':', '!', '?', ')', ']', '}', '»', '"', '\'')

    fun breakIntoUnits(text: String, options: PrepareOptions = PrepareOptions()): List<String> {
        if (text.isEmpty()) return listOf("")

        val source = when (options.whiteSpace) {
            WhiteSpaceMode.Normal -> text.replace(Regex("\\s+"), " ").trim()
            WhiteSpaceMode.PreWrap -> text
        }
        if (source.isEmpty()) return listOf("")

        if (options.whiteSpace == WhiteSpaceMode.PreWrap) {
            return breakPreWrap(source, options)
        }

        val bidi = java.text.Bidi(source, java.text.Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT)
        if (bidi.isRightToLeft || containsArabic(source)) {
            return graphemeUnits(source)
        }

        return gluePunctuation(breakNormalLatinAndCjk(source, options))
    }

    fun graphemes(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        val iterator = BreakIterator.getCharacterInstance(Locale.getDefault())
        iterator.setText(text)
        val out = ArrayList<String>()
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            out += text.substring(start, end)
            start = end
            end = iterator.next()
        }
        return out
    }

    fun needsGraphemeBreakdown(unit: String): Boolean {
        if (unit.isEmpty()) return false
        if (isHardBreak(unit) || isWhitespaceUnit(unit)) return false
        return graphemes(unit).size > 1
    }

    fun isWhitespaceUnit(unit: String): Boolean =
        unit == " " || unit == "\t"

    fun isHardBreak(unit: String): Boolean =
        unit == "\n"

    fun measureGraphemeFit(
        unit: String,
        paint: Paint,
        fontSizePx: Float,
        typefaceHash: Int,
        letterSpacingPx: Float,
        measureCluster: (Paint, String, Float, Int) -> Float,
    ): UnitGraphemeFit {
        val clusters = graphemes(unit).toTypedArray()
        val advances = FloatArray(clusters.size) { i ->
            measureCluster(paint, clusters[i], fontSizePx, typefaceHash) + letterSpacingPx
        }
        return UnitGraphemeFit(clusters, advances)
    }

    private fun breakNormalLatinAndCjk(source: String, options: PrepareOptions): List<String> {
        val raw = mutableListOf<String>()
        var word = StringBuilder()
        var wordGraphemes = mutableListOf<String>()

        fun flushWord() {
            if (word.isNotEmpty()) {
                raw += word.toString()
                word = StringBuilder()
                wordGraphemes = mutableListOf()
            }
        }

        for (g in graphemes(source)) {
            val cp = g.codePointAt(0)
            when {
                g == "\n" -> {
                    flushWord()
                    raw += "\n"
                }
                g == " " || g == "\t" -> {
                    flushWord()
                    raw += g
                }
                isCjk(cp) -> {
                    flushWord()
                    raw += g
                }
                options.wordBreak == WordBreakMode.KeepAll -> {
                    flushWord()
                    raw += g
                }
                else -> {
                    wordGraphemes += g
                    word.append(g)
                }
            }
        }
        flushWord()
        return raw.filter { it.isNotEmpty() }
    }

    private fun breakPreWrap(source: String, options: PrepareOptions): List<String> {
        val units = mutableListOf<String>()
        var word = StringBuilder()

        fun flushWord() {
            if (word.isNotEmpty()) {
                units += word.toString()
                word = StringBuilder()
            }
        }

        for (g in graphemes(source)) {
            when (g) {
                "\n" -> {
                    flushWord()
                    units += "\n"
                }
                "\t" -> {
                    flushWord()
                    units += "\t"
                }
                " " -> {
                    flushWord()
                    units += " "
                }
                else -> {
                    val cp = g.codePointAt(0)
                    if (isCjk(cp) || options.wordBreak == WordBreakMode.KeepAll) {
                        flushWord()
                        units += g
                    } else {
                        word.append(g)
                    }
                }
            }
        }
        flushWord()
        return gluePunctuation(units.filter { it.isNotEmpty() })
    }

    private fun graphemeUnits(text: String): List<String> = graphemes(text)

    private fun gluePunctuation(units: List<String>): List<String> {
        if (units.isEmpty()) return units
        val out = ArrayList<String>(units.size)
        for (unit in units) {
            if (unit.length == 1 && unit[0] in closingPunctuation && out.isNotEmpty()) {
                out[out.lastIndex] = out.last() + unit
            } else {
                out += unit
            }
        }
        return out
    }

    private fun containsArabic(text: String): Boolean =
        text.any {
            when (Character.UnicodeBlock.of(it)) {
                Character.UnicodeBlock.ARABIC,
                Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A,
                Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B,
                -> true
                else -> false
            }
        }

    private fun isCjk(codePoint: Int): Boolean =
        when (Character.UnicodeScript.of(codePoint)) {
            Character.UnicodeScript.HAN,
            Character.UnicodeScript.HIRAGANA,
            Character.UnicodeScript.KATAKANA,
            Character.UnicodeScript.HANGUL,
            -> true
            else -> false
        }
}
