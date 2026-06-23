package com.mfhapps.trendingui.legal

enum class LegalDocumentKind {
    HowToUse,
    Terms,
    UsageRestrictions,
}

data class LegalDocument(
    val kind: LegalDocumentKind,
    val title: String,
    val subtitle: String,
    val sections: List<LegalSection>,
)

data class LegalSection(
    val heading: String,
    val paragraphs: List<String>,
    val bullets: List<String> = emptyList(),
)

fun legalDocument(kind: LegalDocumentKind): LegalDocument = when (kind) {
    LegalDocumentKind.HowToUse -> howToUseDocument()
    LegalDocumentKind.Terms -> termsDocument()
    LegalDocumentKind.UsageRestrictions -> usageRestrictionsDocument()
}

private fun howToUseDocument() = LegalDocument(
    kind = LegalDocumentKind.HowToUse,
    title = "How to use UITrends",
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

private fun termsDocument() = LegalDocument(
    kind = LegalDocumentKind.Terms,
    title = "Terms & conditions",
    subtitle = "Using the UITrends app",
    sections = listOf(
        LegalSection(
            heading = "Agreement",
            paragraphs = listOf(
                "By installing or using UITrends (“the app”), you agree to these terms. If you do not agree, do not use the app.",
                "The app is provided by MFH Apps as an educational showcase of user-interface techniques. It is not a production service-level agreement.",
            ),
        ),
        LegalSection(
            heading = "License to use",
            paragraphs = listOf(
                "MFH Apps grants you a personal, non-exclusive, non-transferable license to use the app for learning, evaluation, and demonstration on devices you control.",
                "Source code for the project is available under the MIT License on GitHub. Your use of the source is governed by that license and by each third-party dependency’s license.",
            ),
        ),
        LegalSection(
            heading = "No warranty",
            paragraphs = listOf(
                "THE APP IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.",
                "Demos may include experimental APIs, alpha Material components, and camera/ML features that can change or break between releases.",
            ),
        ),
        LegalSection(
            heading = "Limitation of liability",
            paragraphs = listOf(
                "TO THE MAXIMUM EXTENT PERMITTED BY LAW, MFH APPS SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES, OR ANY LOSS OF DATA, PROFITS, OR GOODWILL, ARISING FROM YOUR USE OF THE APP.",
            ),
        ),
        LegalSection(
            heading = "Changes",
            paragraphs = listOf(
                "These terms may be updated in future app releases. Continued use after an update constitutes acceptance of the revised terms.",
                "For the latest project license text, see the MIT LICENSE file in the repository.",
            ),
        ),
    ),
)

private fun usageRestrictionsDocument() = LegalDocument(
    kind = LegalDocumentKind.UsageRestrictions,
    title = "Usage restrictions",
    subtitle = "What you may and may not do",
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
