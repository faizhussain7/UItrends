package com.mfhapps.trendingui.legal

object GoogleDocsLegalParser {

    private val numberedHeading = Regex("""^\d+(\.\d+)*[.)]?\s+\S.*""")
    private val markdownHeading = Regex("""^#{1,6}\s+(.+)$""")
    private val bulletLine = Regex("""^[\t ]*[•\-\*\u2022]\s*(.+)$""")

    fun parse(
        kind: LegalDocumentKind,
        raw: String,
        viewUrl: String,
    ): LegalDocument {
        val lines = raw
            .removePrefix("\uFEFF")
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .lines()
            .map { it.trimEnd() }

        val nonEmpty = lines.filter { it.isNotBlank() }
        val title = nonEmpty.firstOrNull()
            ?.removePrefix("#")
            ?.trim()
            ?.takeIf { it.length in 3..120 }
            ?: kind.title

        val subtitle = nonEmpty
            .drop(1)
            .firstOrNull { line ->
                !isHeading(line) && !isBullet(line) && line.length in 8..160
            }
            ?: kind.subtitle

        val startIndex = lines.indexOfFirst { it.isNotBlank() }.let { first ->
            if (first < 0) {
                0
            } else {
                var i = first + 1
                while (i < lines.size && lines[i].isBlank()) i++
                if (i < lines.size && lines[i].trim() == subtitle) i + 1 else first + 1
            }
        }

        val sections = mutableListOf<LegalSection>()
        var heading = "Overview"
        val paragraphs = mutableListOf<String>()
        val bullets = mutableListOf<String>()

        fun flush() {
            if (paragraphs.isEmpty() && bullets.isEmpty()) return
            sections += LegalSection(
                heading = heading,
                paragraphs = paragraphs.toList(),
                bullets = bullets.toList(),
            )
            paragraphs.clear()
            bullets.clear()
        }

        for (index in startIndex until lines.size) {
            val line = lines[index].trim()
            if (line.isEmpty()) continue

            val md = markdownHeading.matchEntire(line)
            when {
                md != null -> {
                    flush()
                    heading = md.groupValues[1].trim()
                }
                isHeading(line) -> {
                    flush()
                    heading = line.trim()
                }
                isBullet(line) -> {
                    bullets += bulletLine.matchEntire(line)?.groupValues?.get(1)?.trim().orEmpty()
                        .ifBlank { line.trimStart('•', '-', '*', ' ', '\t') }
                }
                else -> paragraphs += line
            }
        }
        flush()

        if (sections.isEmpty()) {
            sections += LegalSection(
                heading = "Document",
                paragraphs = nonEmpty.drop(1).ifEmpty { listOf(raw.trim()) },
            )
        }

        return LegalDocument(
            kind = kind,
            title = title,
            subtitle = subtitle,
            sections = sections,
            viewUrl = viewUrl,
        )
    }

    private fun isHeading(line: String): Boolean {
        if (markdownHeading.matches(line)) return true
        if (!numberedHeading.matches(line)) return false
        return line.length <= 100 && !line.endsWith(",")
    }

    private fun isBullet(line: String): Boolean = bulletLine.matches(line)
}
