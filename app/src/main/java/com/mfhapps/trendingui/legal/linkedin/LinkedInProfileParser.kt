package com.mfhapps.trendingui.legal.linkedin

internal object LinkedInProfileParser {
    private val openGraphRegex = Regex(
        """<meta\s+[^>]*?(?:property|name)\s*=\s*["']([^"']+)["'][^>]*?content\s*=\s*["']([^"']*)["'][^>]*>""",
        RegexOption.IGNORE_CASE,
    )
    private val openGraphReverseRegex = Regex(
        """<meta\s+[^>]*?content\s*=\s*["']([^"']*)["'][^>]*?(?:property|name)\s*=\s*["']([^"']+)["'][^>]*>""",
        RegexOption.IGNORE_CASE,
    )

    fun parseOpenGraph(html: String): Map<String, String> {
        val tags = mutableMapOf<String, String>()
        openGraphRegex.findAll(html).forEach { match ->
            tags[match.groupValues[1]] = decodeHtmlEntities(match.groupValues[2])
        }
        openGraphReverseRegex.findAll(html).forEach { match ->
            val key = match.groupValues[2]
            if (key !in tags) {
                tags[key] = decodeHtmlEntities(match.groupValues[1])
            }
        }
        return tags
    }

    fun fromOpenGraph(
        tags: Map<String, String>,
        profileUrl: String,
        fallbackImageUrl: String,
    ): LinkedInProfile {
        val title = tags["og:title"] ?: tags["twitter:title"]
        val description = tags["og:description"] ?: tags["description"] ?: tags["twitter:description"]
        val image = tags["og:image"] ?: tags["twitter:image"] ?: fallbackImageUrl
        return LinkedInProfile(
            name = title,
            headline = description,
            imageUrl = image,
            profileUrl = profileUrl,
        )
    }

    private fun decodeHtmlEntities(value: String): String =
        value
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
}
