package com.mfhapps.trendingui.legal.linkedin

data class LinkedInProfile(
    val name: String?,
    val headline: String?,
    val imageUrl: String?,
    val profileUrl: String,
) {
    val displayName: String?
        get() = name
            ?.substringBefore(" | LinkedIn")
            ?.substringBefore(" - LinkedIn")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
}
