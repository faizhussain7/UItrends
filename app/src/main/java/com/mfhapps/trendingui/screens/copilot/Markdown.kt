package com.mfhapps.trendingui.screens.copilot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(text) { parseBlocks(text) }
    val scheme = MaterialTheme.colorScheme
    val codeBg = scheme.surfaceContainerHighest

    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) Spacer(Modifier.height(6.dp))
            when (block) {
                is MdBlock.Heading -> Text(
                    text = renderInline(block.text, scheme.onSurface),
                    style = if (block.level <= 1) MaterialTheme.typography.titleLarge
                    else MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                )
                is MdBlock.Paragraph -> Text(
                    text = renderInline(block.text, scheme.onSurface),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                )
                is MdBlock.Bullet -> Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                ) {
                    Text(
                        "•  ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.primary,
                    )
                    Text(
                        renderInline(block.text, scheme.onSurface),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface,
                    )
                }
                is MdBlock.CodeBlock -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(codeBg)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                        ),
                        color = scheme.onSurface,
                    )
                }
            }
        }
    }
}

private sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Bullet(val text: String) : MdBlock
    data class CodeBlock(val text: String) : MdBlock
}

private fun parseBlocks(input: String): List<MdBlock> {
    val out = mutableListOf<MdBlock>()
    val lines = input.split('\n')
    var i = 0
    val para = StringBuilder()
    fun flushPara() {
        if (para.isNotBlank()) {
            out.add(MdBlock.Paragraph(para.toString().trim()))
        }
        para.clear()
    }
    while (i < lines.size) {
        val line = lines[i]
        when {
            line.trim().startsWith("```") -> {
                flushPara()
                val buf = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    buf.appendLine(lines[i])
                    i++
                }
                out.add(MdBlock.CodeBlock(buf.toString().trimEnd()))
                if (i < lines.size) i++
            }
            line.startsWith("# ") -> { flushPara(); out.add(MdBlock.Heading(1, line.removePrefix("# "))); i++ }
            line.startsWith("## ") -> { flushPara(); out.add(MdBlock.Heading(2, line.removePrefix("## "))); i++ }
            line.startsWith("- ") || line.startsWith("* ") -> {
                flushPara()
                out.add(MdBlock.Bullet(line.removePrefix("- ").removePrefix("* ")))
                i++
            }
            line.isBlank() -> { flushPara(); i++ }
            else -> {
                if (para.isNotEmpty()) para.append(' ')
                para.append(line)
                i++
            }
        }
    }
    flushPara()
    return out
}


private fun renderInline(input: String, defaultColor: Color): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < input.length) {
        val rest = input.substring(i)
        when {
            rest.startsWith("**") -> {
                val end = input.indexOf("**", i + 2)
                if (end >= 0) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                        append(input.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(rest); i = input.length
                }
            }
            rest.startsWith("`") -> {
                val end = input.indexOf("`", i + 1)
                if (end >= 0) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x22808080),
                            fontSize = 14.sp,
                            color = defaultColor,
                        ),
                    ) {
                        append(input.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(rest); i = input.length
                }
            }
            rest.startsWith("*") && rest.length > 1 && rest[1] != '*' -> {
                val end = input.indexOf("*", i + 1)
                if (end >= 0) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor)) {
                        append(input.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(rest); i = input.length
                }
            }
            else -> {
                withStyle(SpanStyle(color = defaultColor)) {
                    append(input[i])
                }
                i++
            }
        }
    }
}
