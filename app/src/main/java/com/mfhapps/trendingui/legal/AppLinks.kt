package com.mfhapps.trendingui.legal

object AppLinks {
    const val GITHUB_REPO = "https://github.com/faizhussain7/UItrends"
    private const val GITHUB_BRANCH = "master"
    const val GITHUB_TREE = "$GITHUB_REPO/tree/$GITHUB_BRANCH"
    const val LICENSE_FILE = "$GITHUB_REPO/blob/$GITHUB_BRANCH/LICENSE"
    const val THIRD_PARTY_NOTICES = "$GITHUB_REPO/blob/$GITHUB_BRANCH/THIRD_PARTY_NOTICES.md"
    const val README = "$GITHUB_REPO/blob/$GITHUB_BRANCH/README.md"
    const val APP_SOURCE_ROOT = "$GITHUB_TREE/app/src/main/java/com/mfhapps/trendingui"

    const val PRIVACY_GOOGLE_DOC_ID = "1A6Bv9J8nDqqbJWlRT5oYyeXDgtKMLndkBdYxvRQU-OU"
    const val TERMS_GOOGLE_DOC_ID = "1bzPHOzzByM8tfAoKkCc2Sjeg3Bvv97dpHF14MOpGTzw"

    fun googleDocViewUrl(docId: String): String =
        "https://docs.google.com/document/d/$docId/view"

    fun googleDocExportTxtUrl(docId: String): String =
        "https://docs.google.com/document/d/$docId/export?format=txt"
}
