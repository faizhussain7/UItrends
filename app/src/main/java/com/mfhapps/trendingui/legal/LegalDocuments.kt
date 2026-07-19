package com.mfhapps.trendingui.legal

enum class LegalDocumentKind {
    HowToUse,
    Privacy,
    Terms,
    UsageRestrictions,
    ;

    val title: String
        get() = when (this) {
            HowToUse -> "How to use UITrends"
            Privacy -> "Privacy Policy"
            Terms -> "Terms & conditions"
            UsageRestrictions -> "Usage restrictions"
        }

    val subtitle: String
        get() = when (this) {
            HowToUse -> "Catalog, demos, settings, and accessibility"
            Privacy -> "How UITrends handles data on your device"
            Terms -> "Using the UITrends app"
            UsageRestrictions -> "What you may and may not do"
        }

    val googleDocId: String?
        get() = when (this) {
            Privacy -> AppLinks.PRIVACY_GOOGLE_DOC_ID
            Terms -> AppLinks.TERMS_GOOGLE_DOC_ID
            HowToUse, UsageRestrictions -> null
        }

    val isRemote: Boolean
        get() = !googleDocId.isNullOrBlank()

    val viewUrl: String?
        get() = googleDocId?.let(AppLinks::googleDocViewUrl)
}

data class LegalDocument(
    val kind: LegalDocumentKind,
    val title: String,
    val subtitle: String,
    val sections: List<LegalSection>,
    val viewUrl: String? = null,
)

data class LegalSection(
    val heading: String,
    val paragraphs: List<String>,
    val bullets: List<String> = emptyList(),
)

sealed interface LegalDocumentUiState {
    data object Loading : LegalDocumentUiState
    data class Ready(val document: LegalDocument) : LegalDocumentUiState
    data class Error(
        val title: String,
        val message: String,
        val viewUrl: String?,
    ) : LegalDocumentUiState
}

fun bundledLegalDocument(kind: LegalDocumentKind): LegalDocument = when (kind) {
    LegalDocumentKind.HowToUse -> howToUseDocument()
    LegalDocumentKind.UsageRestrictions -> usageRestrictionsDocument()
    LegalDocumentKind.Privacy,
    LegalDocumentKind.Terms,
    -> error("${kind.name} must be loaded remotely")
}

private fun howToUseDocument() = LegalDocument(
    kind = LegalDocumentKind.HowToUse,
    title = LegalDocumentKind.HowToUse.title,
    subtitle = "Explore UI patterns on your device",
    sections = listOf(
        LegalSection(
            heading = "Catalog",
            paragraphs = listOf(
                "UITrends is a playground for contemporary Android UI patterns. Open the home catalog and tap any demo card to launch an interactive example.",
                "On tablets and foldables, the catalog uses an adaptive list–detail layout so you can browse demos and read details side by side.",
            ),
            bullets = listOf(
                "Swipe or scroll the catalog; switch between list, bento, and grid layouts in Settings → Home appearance.",
                "Use the guide (?) icon on a demo to learn what the pattern is, how this sample uses it, and links to official docs.",
                "Use the source code icon to open that demo’s Kotlin sources on GitHub.",
            ),
        ),
        LegalSection(
            heading = "Settings",
            paragraphs = listOf(
                "Theme, dynamic color, accent, launcher icon, and backdrop blur preferences live under Settings. Changes apply across the catalog and demos.",
            ),
            bullets = listOf(
                "Dynamic color follows your wallpaper on Android 12+.",
                "Turn off dynamic color to pick a fixed brand accent.",
                "Launcher icon sync can match your light/dark theme when enabled.",
            ),
        ),
        LegalSection(
            heading = "Camera & on-device AI demos",
            paragraphs = listOf(
                "Pretext and related vision features use the camera and on-device models. Grant camera permission when prompted. Processing stays on your device unless you open an external link.",
            ),
            bullets = listOf(
                "Vision model files download on first build; see Open source & licenses for attribution.",
                "Copilot and chat demos simulate streaming UI—they do not send your text to a live cloud API in this app.",
            ),
        ),
        LegalSection(
            heading = "Accessibility",
            paragraphs = listOf(
                "UITrends respects system font scale, reduce motion, and TalkBack where implemented. If motion feels strong, enable Reduce motion in system settings.",
            ),
        ),
    ),
)

private fun usageRestrictionsDocument() = LegalDocument(
    kind = LegalDocumentKind.UsageRestrictions,
    title = LegalDocumentKind.UsageRestrictions.title,
    subtitle = LegalDocumentKind.UsageRestrictions.subtitle,
    sections = listOf(
        LegalSection(
            heading = "Permitted use",
            paragraphs = listOf(
                "You may use UITrends to study Android UI patterns, compare implementations, and share screenshots or recordings for education and portfolio work.",
                "You may fork, modify, and redistribute the open-source project subject to the MIT License and all third-party notices.",
            ),
            bullets = listOf(
                "Attribute MFH Apps and upstream libraries when redistributing code or binaries.",
                "Keep license and NOTICE files required by dependencies in derivative builds.",
            ),
        ),
        LegalSection(
            heading = "Prohibited use",
            paragraphs = listOf(
                "You may not use the app or its branding to misrepresent affiliation with Google, Material Design, or any third-party library author.",
            ),
            bullets = listOf(
                "Do not reverse engineer the app to remove license checks or safety limits beyond what open source already provides.",
                "Do not use camera or vision demos to surveil people without consent or in violation of local law.",
                "Do not scrape, overload, or abuse any external URLs opened from the app.",
                "Do not ship bundled model weights (e.g. YOLOv8 NCNN) in commercial products without reviewing Ultralytics and upstream licenses.",
            ),
        ),
        LegalSection(
            heading = "Third-party content",
            paragraphs = listOf(
                "Sample images, fonts, and model files may be subject to separate licenses. UITrends links to upstream projects; compliance when you redistribute is your responsibility.",
                "Material Design, Jetpack, LiteRT, MediaPipe, Coil, Haze, and other dependencies remain property of their respective owners.",
            ),
        ),
        LegalSection(
            heading = "Termination",
            paragraphs = listOf(
                "You may stop using the app at any time by uninstalling it. MFH Apps may discontinue or modify features without notice.",
            ),
        ),
    ),
)
