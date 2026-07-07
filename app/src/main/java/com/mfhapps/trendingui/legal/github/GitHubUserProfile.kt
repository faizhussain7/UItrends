package com.mfhapps.trendingui.legal.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubUserProfile(
    val login: String,
    val name: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String,
    @SerialName("html_url") val htmlUrl: String,
    val blog: String? = null,
    val location: String? = null,
    val company: String? = null,
    @SerialName("twitter_username") val twitterUsername: String? = null,
    @SerialName("public_repos") val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
) {
    fun avatarUrlHighRes(sizePx: Int = 460): String {
        val base = avatarUrl.substringBefore('?')
        return "$base?s=$sizePx"
    }

    val displayName: String
        get() = name?.takeIf { it.isNotBlank() } ?: login
}
