package com.mfhapps.trendingui.legal

object CreatorSocialAssets {
    const val GITHUB = "social/github.svg"
    const val GITHUB_ON_DARK = "social/github-dark.svg"
    const val GMAIL = "social/gmail.svg"
    const val LINKEDIN = "social/linkedin.svg"
    const val X = "social/x.svg"
    const val X_ON_DARK = "social/x-dark.svg"
    const val WHATSAPP = "social/whatsapp.svg"

    fun androidAssetUri(assetPath: String): String = "file:///android_asset/$assetPath"
}
