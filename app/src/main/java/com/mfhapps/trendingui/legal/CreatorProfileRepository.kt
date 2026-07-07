package com.mfhapps.trendingui.legal

import com.mfhapps.trendingui.legal.github.GitHubUserProfile
import com.mfhapps.trendingui.legal.linkedin.LinkedInProfile
import com.mfhapps.trendingui.legal.linkedin.LinkedInProfileParser
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

data class CreatorProfileBundle(
    val github: GitHubUserProfile?,
    val linkedIn: LinkedInProfile?,
)

class CreatorProfileRepository(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun fetchAll(): Result<CreatorProfileBundle> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                val githubDeferred = async { fetchGitHubProfile().getOrNull() }
                val linkedInDeferred = async { fetchLinkedInProfile().getOrNull() }
                CreatorProfileBundle(
                    github = githubDeferred.await(),
                    linkedIn = linkedInDeferred.await(),
                )
            }
        }
    }

    suspend fun fetchGitHubProfile(): Result<GitHubUserProfile> = withContext(Dispatchers.IO) {
        runCatching {
            httpGet(
                url = CreatorLinks.GITHUB_API,
                accept = "application/vnd.github+json",
            ).let { body ->
                json.decodeFromString<GitHubUserProfile>(body)
            }
        }
    }

    suspend fun fetchLinkedInProfile(): Result<LinkedInProfile> = withContext(Dispatchers.IO) {
        runCatching {
            val html = httpGet(
                url = CreatorLinks.LINKEDIN_PROFILE,
                accept = "text/html,application/xhtml+xml",
                userAgent = CreatorLinks.LINKEDIN_FETCH_USER_AGENT,
            )
            val tags = LinkedInProfileParser.parseOpenGraph(html)
            val profile = LinkedInProfileParser.fromOpenGraph(
                tags = tags,
                profileUrl = CreatorLinks.LINKEDIN_PROFILE,
                fallbackImageUrl = CreatorLinks.LINKEDIN_AVATAR_URL,
            )
            if (
                profile.displayName == null &&
                profile.headline.isNullOrBlank() &&
                profile.imageUrl == CreatorLinks.LINKEDIN_AVATAR_URL
            ) {
                LinkedInProfile(
                    name = null,
                    headline = null,
                    imageUrl = CreatorLinks.LINKEDIN_AVATAR_URL,
                    profileUrl = CreatorLinks.LINKEDIN_PROFILE,
                )
            } else {
                profile
            }
        }
    }

    private fun httpGet(
        url: String,
        accept: String,
        userAgent: String = "UITrends-Android",
    ): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("Accept", accept)
            setRequestProperty("User-Agent", userAgent)
            connectTimeout = 12_000
            readTimeout = 12_000
        }
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                error("Request failed ($code)")
            }
            return body
        } finally {
            connection.disconnect()
        }
    }
}
