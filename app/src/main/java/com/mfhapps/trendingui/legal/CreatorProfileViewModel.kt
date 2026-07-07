package com.mfhapps.trendingui.legal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mfhapps.trendingui.legal.github.GitHubUserProfile
import com.mfhapps.trendingui.legal.linkedin.LinkedInProfile
import kotlinx.coroutines.launch

data class CreatorProfileUiState(
    val loading: Boolean = true,
    val github: GitHubUserProfile? = null,
    val linkedIn: LinkedInProfile? = null,
    val errorMessage: String? = null,
) {
    val displayName: String
        get() = linkedIn?.displayName
            ?: github?.displayName
            ?: "Mohammed Faiz Hussain"

    val headline: String
        get() = linkedIn?.headline?.takeIf { it.isNotBlank() }
            ?: github?.bio?.takeIf { it.isNotBlank() }
            ?: "Software developer · Android, React Native, and AI/ML"

    val photoUrl: String
        get() = linkedIn?.imageUrl?.takeIf { it.isNotBlank() }
            ?: github?.avatarUrlHighRes()
            ?: CreatorLinks.LINKEDIN_AVATAR_URL

    val photoUrlFullscreen: String
        get() = linkedIn?.imageUrl?.takeIf { it.isNotBlank() }
            ?: github?.avatarUrlHighRes(sizePx = 960)
            ?: CreatorLinks.LINKEDIN_AVATAR_URL

    val twitterHandle: String
        get() = github?.twitterUsername?.let { "@$it" } ?: "@${CreatorLinks.X_HANDLE}"
}

class CreatorProfileViewModel(
    private val repository: CreatorProfileRepository = CreatorProfileRepository(),
) : ViewModel() {
    var uiState by mutableStateOf(CreatorProfileUiState())
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, errorMessage = null)
            repository.fetchAll()
                .onSuccess { bundle ->
                    uiState = CreatorProfileUiState(
                        loading = false,
                        github = bundle.github,
                        linkedIn = bundle.linkedIn,
                        errorMessage = if (bundle.github == null && bundle.linkedIn == null) {
                            "Could not load profile data"
                        } else {
                            null
                        },
                    )
                }
                .onFailure { error ->
                    uiState = CreatorProfileUiState(
                        loading = false,
                        github = uiState.github,
                        linkedIn = uiState.linkedIn,
                        errorMessage = error.message ?: "Could not load profile",
                    )
                }
        }
    }
}
