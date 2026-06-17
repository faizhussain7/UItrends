package com.mfhapps.trendingui.ui.guide

import com.mfhapps.trendingui.navigation.DemoPaneKey

data class TrendGuideLink(
    val label: String,
    val url: String,
)

data class DemoTrendGuide(
    val patternName: String,
    val summary: String,
    val whatItIs: String,
    val usageInThisDemo: String,
    val bestPractices: List<String>,
    val links: List<TrendGuideLink>,
)

fun trendGuideForPane(key: DemoPaneKey): DemoTrendGuide = when (key) {
    DemoPaneKey.Pretext -> pretextGuide()
    DemoPaneKey.Chat -> virtualChatGuide()
    DemoPaneKey.Bento -> bentoGuide()
    DemoPaneKey.Glass -> glassGuide()
    DemoPaneKey.Orbs -> orbsGuide()
    DemoPaneKey.Kinetic -> kineticGuide()
    DemoPaneKey.Brutal -> brutalGuide()
    DemoPaneKey.Neumorphism -> neumorphismGuide()
    DemoPaneKey.ZeroUi -> zeroUiGuide()
    DemoPaneKey.Spatial -> spatialGuide()
    DemoPaneKey.Semantic -> semanticGuide()
    DemoPaneKey.Copilot -> copilotGuide()
    DemoPaneKey.Calm -> calmGuide()
    DemoPaneKey.Masonry -> masonryGuide()
}

fun settingsTrendGuide(): DemoTrendGuide = DemoTrendGuide(
    patternName = "UITrends catalog",
    summary = "A Material 3 playground for contemporary Android UI patterns.",
    whatItIs = "UITrends bundles interactive demos of layout, motion, surface, and AI-adjacent patterns so you can compare implementations side by side on phone and tablet.",
    usageInThisDemo = "Pick a pattern from the catalog. Switch theme, dynamic color, brand primary accent, and catalog card layout (list, bento, or grid).",
    bestPractices = listOf(
        "Prefer system theme unless your brand requires a fixed light/dark mode.",
        "Enable dynamic color on Android 12+ for accessible, on-device palettes.",
        "Test demos in both compact and expanded widths—adaptive layouts behave differently.",
    ),
    links = listOf(
        TrendGuideLink("Material Design 3", "https://m3.material.io/"),
        TrendGuideLink("Compose Material 3", "https://developer.android.com/develop/ui/compose/designsystems/material3"),
        TrendGuideLink("Adaptive layouts", "https://developer.android.com/develop/ui/compose/layouts/adaptive"),
    ),
)

private fun pretextGuide() = DemoTrendGuide(
    patternName = "Custom text layout engine",
    summary = "High-performance paragraph and line layout with live camera-driven reflow.",
    whatItIs = "Pretext is a native text engine (not TextView measure loops) that prepares glyph runs once and reflows when constraints change—ideal for AR overlays, captions, and dynamic columns.",
    usageInThisDemo = "Use Playground to tune line height, alignment, and batch layout. Open Camera to see live reflow around detected regions. Compare Engine vs legacy measure in the reflow benchmark card.",
    bestPractices = listOf(
        "Prepare text off the main thread for large blocks; layout on demand when width changes.",
        "Keep overlay text on a barrier/scrim when video or blur sits behind it.",
        "Cap line length (~60 characters) for readability on foldables and tablets.",
    ),
    links = listOf(
        TrendGuideLink("Compose text performance", "https://developer.android.com/develop/ui/compose/text/performance"),
        TrendGuideLink("CameraX overview", "https://developer.android.com/media/camera/camerax"),
        TrendGuideLink("ML Kit object detection", "https://developers.google.com/ml-kit/vision/object-detection"),
    ),
)

private fun virtualChatGuide() = DemoTrendGuide(
    patternName = "Virtualized chat bubbles",
    summary = "Thousands of messages with shrink-wrapped bubbles and streaming layout.",
    whatItIs = "Chat UIs stress recycling, asymmetric bubbles, and incremental layout when tokens stream in. Lazy lists plus debounced recomposition keep frame times stable.",
    usageInThisDemo = "Scroll the history—bubbles are generated with varied widths. Watch how new assistant chunks append without relayout jank. Compare date dividers and scroll-to-bottom behavior.",
    bestPractices = listOf(
        "Give each message a stable key; avoid remeasuring the full transcript on every token.",
        "Debounce or batch streaming updates (50–100 ms) before invalidating layout.",
        "Reserve min touch targets and contrast for sent vs received bubbles.",
    ),
    links = listOf(
        TrendGuideLink("Lazy lists in Compose", "https://developer.android.com/develop/ui/compose/lists"),
        TrendGuideLink("Conversation design (Material)", "https://m2.material.io/components/cards#behavior"),
        TrendGuideLink("Accessibility: text scaling", "https://developer.android.com/develop/ui/compose/accessibility/semantics"),
    ),
)

private fun bentoGuide() = DemoTrendGuide(
    patternName = "Bento grid layout",
    summary = "Modular asymmetric tiles inspired by Japanese bento boxes.",
    whatItIs = "Bento grids break dashboards and marketing pages into unequal cells—one hero tile plus supporting metrics—so users scan instead of reading a long column.",
    usageInThisDemo = "Long-press a tile to enter edit mode and feel spring press feedback. Notice hero span vs 1×1 cells and how staggered placement guides attention.",
    bestPractices = listOf(
        "Let content dictate cell size; avoid uniform card walls.",
        "Keep gutter and corner radius consistent across the grid.",
        "Collapse to a single column on compact width with the hero first.",
    ),
    links = listOf(
        TrendGuideLink("Material layout basics", "https://m3.material.io/foundations/layout/understanding-layout"),
        TrendGuideLink("Compose staggered grids", "https://developer.android.com/reference/kotlin/androidx/compose/foundation/lazy/staggeredgrid/package-summary"),
        TrendGuideLink("Bento layout overview", "https://www.stan.vision/journal/revolutionizing-ui-ux-in-2024-with-bento-ui-grid-design-trend"),
    ),
)

private fun glassGuide() = DemoTrendGuide(
    patternName = "Glassmorphism",
    summary = "Frosted surfaces that blur content behind them while keeping foreground text sharp.",
    whatItIs = "Glassmorphism samples the backdrop, blurs it, and adds tint, border highlights, and sometimes noise. It signals layering without heavy shadows.",
    usageInThisDemo = "Scroll through frosted panels, thin/thick variants, a glass dock, media overlay, KPI chips, stacked layers, and notification rows. Haze blurs the live backdrop on capable devices; scrims keep text legible.",
    bestPractices = listOf(
        "Never blur only the foreground—blur what is behind the glass panel.",
        "Add a scrim or barrier behind text; contrast ratios must still pass WCAG.",
        "Use thin glass for secondary UI and thick glass for dense copy.",
        "Animate specular highlights subtly; respect reduce-motion.",
    ),
    links = listOf(
        TrendGuideLink("RenderEffect (Android)", "https://developer.android.com/reference/android/graphics/RenderEffect"),
        TrendGuideLink("Haze library (backdrop blur)", "https://chrisbanes.github.io/haze/"),
        TrendGuideLink("Material surfaces & elevation", "https://m3.material.io/styles/elevation/overview"),
    ),
)

private fun orbsGuide() = DemoTrendGuide(
    patternName = "Orbs & mesh gradients",
    summary = "Blend-mode blobs and mesh gradients with optional sensor parallax.",
    whatItIs = "Soft aurora backgrounds use additive blend modes and multi-stop gradients. Gyroscope tilt adds depth without skeuomorphic chrome.",
    usageInThisDemo = "Full-screen mesh backdrop behind the demo. Drag orbs in the playground, pick mesh presets (Aurora, Sunset, Brand, Midnight), compare Screen / Plus / Overlay / Color dodge on dark mesh, and browse static mesh tiles. Gyro parallax and haptics apply unless reduce motion is on.",
    bestPractices = listOf(
        "Keep motion subtle; offer a reduce-motion path that freezes parallax.",
        "Use four-point radial meshes instead of huge bitmaps when possible.",
        "Avoid placing critical controls on top of high-contrast gradient hotspots.",
        "Spring orbs back after drag so the field feels physical, not sticky.",
    ),
    links = listOf(
        TrendGuideLink("Compose graphics layers", "https://developer.android.com/develop/ui/compose/graphics/draw/overview"),
        TrendGuideLink("Blend modes", "https://developer.android.com/develop/ui/compose/graphics/draw/modifiers#blendmode"),
        TrendGuideLink("Reduce motion (Android)", "https://developer.android.com/develop/ui/compose/accessibility/semantics#reduce-motion"),
    ),
)

private fun kineticGuide() = DemoTrendGuide(
    patternName = "Kinetic typography",
    summary = "Scroll- and velocity-driven type that changes weight, spacing, and scale.",
    whatItIs = "Kinetic type treats letterforms as living elements: scroll position and fling velocity drive interpolation between styles, common in editorial and hero sections.",
    usageInThisDemo = "Scroll to morph the hero string—letter spacing tightens, weight increases, and width scale responds. Fast flings briefly emphasize bold strokes.",
    bestPractices = listOf(
        "Use TextMotion.Animated only where it aids comprehension, not everywhere.",
        "Clamp interpolation ranges so type never becomes illegible at extremes.",
        "Disable kinetic effects when reduce motion is enabled.",
    ),
    links = listOf(
        TrendGuideLink("Compose text style", "https://developer.android.com/develop/ui/compose/text"),
        TrendGuideLink("M3 expressive motion", "https://m3.material.io/styles/motion/overview/how-it-works"),
        TrendGuideLink("Variable fonts on Android", "https://developer.android.com/develop/ui/views/text-and-emoji/fonts-in-xml"),
    ),
)

private fun brutalGuide() = DemoTrendGuide(
    patternName = "Neo-brutalism",
    summary = "Hard edges, high contrast, and offset shadows without blur.",
    whatItIs = "Neo-brutalism rejects soft gradients for loud color blocks, 0 dp corners, and visible borders—popular in indie SaaS and Gen-Z brands.",
    usageInThisDemo = "Buttons use offset press states instead of elevation shadows. Typography is oversized; containers use 2 dp strokes and zero corner radius.",
    bestPractices = listOf(
        "Maintain WCAG contrast on loud background/ink pairs.",
        "Use haptics or clear pressed offsets—avoid fake depth without feedback.",
        "Pair brutal layouts with generous spacing so density stays readable.",
    ),
    links = listOf(
        TrendGuideLink("Material shape & corners", "https://m3.material.io/styles/shape/overview-principles"),
        TrendGuideLink("Accessibility: contrast", "https://developer.android.com/guide/topics/ui/accessibility/testing#contrast"),
        TrendGuideLink("Neo-brutalism in UI", "https://www.nngroup.com/articles/neo-brutalism/"),
    ),
)

private fun neumorphismGuide() = DemoTrendGuide(
    patternName = "Neumorphism (soft UI)",
    summary = "Extruded controls that look pressed into or raised from a shared surface.",
    whatItIs = "Neumorphism combines duplicate soft shadows (light + dark) on a monochromatic canvas. It peaked around 2020 and works best for toggles on muted backgrounds.",
    usageInThisDemo = "Drag the slider and toggle the switch—shadows invert for pressed vs raised states. Background matches surfaceContainerLow for a single-plane illusion.",
    bestPractices = listOf(
        "Use only on low-contrast monochrome surfaces; avoid for primary CTAs.",
        "Test in bright sunlight—soft shadows disappear outdoors.",
        "Prefer Material tonal elevation for production apps; neumorphism is decorative.",
    ),
    links = listOf(
        TrendGuideLink("Neumorphism usability (NN/g)", "https://www.nngroup.com/articles/neumorphism/"),
        TrendGuideLink("Material elevation tokens", "https://m3.material.io/styles/elevation/tokens"),
        TrendGuideLink("Custom draw in Compose", "https://developer.android.com/develop/ui/compose/graphics/draw/overview"),
    ),
)

private fun zeroUiGuide() = DemoTrendGuide(
    patternName = "Zero UI",
    summary = "Chrome that appears only when context demands it.",
    whatItIs = "Zero UI hides toolbars and FABs until gesture, focus, or idle timeout—common in readers, maps, and ambient assistants where content is the interface.",
    usageInThisDemo = "Try each section: focus search for chips, tap content for timed actions, long-press text to select, tap the reader canvas for edge chrome, and touch the player to wake idle controls.",
    bestPractices = listOf(
        "Always expose a discoverable affordance (hint text, coach mark, or first-run).",
        "Never hide the only way to complete a critical task.",
        "Announce revealed controls for TalkBack when they appear.",
    ),
    links = listOf(
        TrendGuideLink("Ambient computing (Google)", "https://developers.google.com/assistant"),
        TrendGuideLink("Compose gestures", "https://developer.android.com/develop/ui/compose/touch-input/pointer-input"),
        TrendGuideLink("Visibility & discoverability", "https://www.nngroup.com/articles/visibility-affordance/"),
    ),
)

private fun spatialGuide() = DemoTrendGuide(
    patternName = "Spatial depth & adaptive panes",
    summary = "List–detail scaffolds with subtle gyro parallax on scroll.",
    whatItIs = "Spatial UI uses perspective transforms and pane adaptivity so content feels layered. ListDetailPaneScaffold shows master and detail together on large screens.",
    usageInThisDemo = "Full-screen perspective grid behind list/detail. Pick a card for detail; system back or Back to layers returns to the list on phones. Tilt for ~6° gyro parallax when reduce motion is off.",
    bestPractices = listOf(
        "Use pane scaffolds instead of manual width math for list/detail.",
        "Keep parallax under ~5°; stronger motion causes discomfort.",
        "Preserve state in the detail pane when rotating or folding.",
    ),
    links = listOf(
        TrendGuideLink("List-detail pane scaffold", "https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail"),
        TrendGuideLink("Canonical layouts", "https://developer.android.com/develop/ui/compose/layouts/adaptive/canonical-layouts"),
        TrendGuideLink("Material motion: spatial", "https://m3.material.io/styles/motion/transitions/transition-patterns"),
    ),
)

private fun semanticGuide() = DemoTrendGuide(
    patternName = "Semantic motion",
    summary = "Animations that express cause and effect, not decoration.",
    whatItIs = "Semantic motion shows where an object came from and where it went—delete flows, shared elements, and predictive back reinforce mental models.",
    usageInThisDemo = "Delete a file to see it move toward trash; undo with predictive back. Folder transitions use shared bounds so hierarchy stays coherent.",
    bestPractices = listOf(
        "Match exit and enter transitions (shared axis, fade through, or shared element).",
        "Use PredictiveBackHandler for reversible destructive actions.",
        "Keep durations short; prefer Material motion schemes on M3 Expressive.",
    ),
    links = listOf(
        TrendGuideLink("Material motion overview", "https://m3.material.io/styles/motion/overview/how-it-works"),
        TrendGuideLink("Shared element transitions", "https://developer.android.com/develop/ui/compose/animation/shared-elements"),
        TrendGuideLink("Predictive back", "https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture"),
    ),
)

private fun copilotGuide() = DemoTrendGuide(
    patternName = "AI copilot sheet",
    summary = "Streaming assistant UI anchored in a bottom sheet with phased status.",
    whatItIs = "Copilot patterns combine partial-height sheets, token streaming, and explicit phases (thinking, searching, generating) so users trust latency.",
    usageInThisDemo = "Document preview stays visible above the sheet peek (sheet cannot hide). Drag up to expand copilot, down to peek. Send prompts for phased status and streamed markdown. Stop mid-flight.",
    bestPractices = listOf(
        "Show system status for long operations; never fake instant answers.",
        "Let users dismiss or stop generation without losing context.",
        "Render markdown safely; avoid executing untrusted content.",
    ),
    links = listOf(
        TrendGuideLink("Bottom sheet (Material)", "https://m3.material.io/components/bottom-sheets/overview"),
        TrendGuideLink("Compose ModalBottomSheet", "https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#ModalBottomSheet(kotlin.Function0,androidx.compose.ui.Modifier,androidx.compose.material3.SheetState,kotlin.Boolean,androidx.compose.ui.graphics.Shape,androidx.compose.ui.unit.Dp,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.unit.Dp,androidx.compose.ui.graphics.Color,kotlin.Function0,androidx.compose.foundation.layout.WindowInsets,kotlin.Function1)"),
        TrendGuideLink("People + AI guidelines (Google)", "https://pair.withgoogle.com/guidebook/"),
    ),
)

private fun calmGuide() = DemoTrendGuide(
    patternName = "Calm UI / reader mode",
    summary = "Low-chrome reading with supporting pane and gentle cross-fades.",
    whatItIs = "Calm technology keeps attention on long-form text: muted palettes, comfortable measure, optional serif, and side navigation on wide screens.",
    usageInThisDemo = "Read chapters with fade transitions. Tap display settings — the reader panel replaces the top bar. E-ink offers black, blue, or green ink (light or dark per system). Use chapter chips on phone or the contents rail on tablet.",
    bestPractices = listOf(
        "Target 45–75 characters per line; increase line height for body text.",
        "Honor system font scale and reduce motion for transitions.",
        "Use supporting panes for navigation, not modal stacks, on tablets.",
    ),
    links = listOf(
        TrendGuideLink("Supporting pane scaffold", "https://developer.android.com/develop/ui/compose/layouts/adaptive/supporting-pane"),
        TrendGuideLink("Calm technology principles", "https://calmtech.com/"),
        TrendGuideLink("Typography readability", "https://m3.material.io/styles/typography/overview"),
    ),
)

private fun masonryGuide() = DemoTrendGuide(
    patternName = "Immersive masonry scroll",
    summary = "Edge-to-edge hero, collapsing parallax, and staggered discovery tiles.",
    whatItIs = "Immersive feeds combine a full-bleed collapsing hero, scroll-linked palette shifts, and variable-height masonry. Content decks overlap the hero with rounded corners—common in Spotify, Pinterest, and gallery apps.",
    usageInThisDemo = "Scroll to collapse the hero (parallax + fade). Filter chips sit on a rounded content deck. Every fifth tile spans full width as a featured row; others stagger in two columns.",
    bestPractices = listOf(
        "Draw chrome edge-to-edge; float navigation over content with a transparent app bar.",
        "Drive hero motion with transform/opacity, not layout height, for smooth collapse.",
        "Pre-size image slots; use featured full-width spans sparingly for hierarchy.",
        "Gate parallax when reduce motion is on.",
    ),
    links = listOf(
        TrendGuideLink("Staggered grid (Compose)", "https://developer.android.com/reference/kotlin/androidx/compose/foundation/lazy/staggeredgrid/package-summary"),
        TrendGuideLink("Coil image loading", "https://coil-kt.github.io/coil/compose/"),
        TrendGuideLink("Scroll-driven design", "https://m3.material.io/styles/motion/overview/how-it-works"),
    ),
)
