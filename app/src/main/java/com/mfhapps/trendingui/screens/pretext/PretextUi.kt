package com.mfhapps.trendingui.screens.pretext

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.WrapText
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import com.mfhapps.trendingui.ui.components.HapticSlider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import com.mfhapps.trendingui.ui.components.AdaptiveFitText
import com.mfhapps.trendingui.ui.components.CatalogMorphShapes
import com.mfhapps.trendingui.ui.components.ExpressiveMorphTempo
import com.mfhapps.trendingui.ui.components.ExpressiveShapeCatalogTier
import com.mfhapps.trendingui.ui.components.ShapeClickableSurface
import com.mfhapps.trendingui.ui.components.SwitchListItem
import com.mfhapps.trendingui.ui.components.expressivePhaseOffset
import com.mfhapps.trendingui.ui.components.rememberExpressiveMorphLoopShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon

private val PretextSheetInset = 16.dp

private fun Modifier.pretextSheetInset(): Modifier = padding(horizontal = PretextSheetInset)

@Composable
private fun pretextFilterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    containerColor = MaterialTheme.colorScheme.surface,
    labelColor = MaterialTheme.colorScheme.onSurface,
    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun pretextSegmentedButtonColors() = SegmentedButtonDefaults.colors(
    activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    inactiveContainerColor = MaterialTheme.colorScheme.surface,
    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    activeBorderColor = MaterialTheme.colorScheme.outline,
    inactiveBorderColor = MaterialTheme.colorScheme.outline,
)


@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PretextHeroCard(
    modifier: Modifier = Modifier,
    fps: Int? = null,
    lastMeasureMs: Long = 0L,
    arrivedFromCamera: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val heroMorph = CatalogMorphShapes.heroMorph
    val brush = Brush.linearGradient(
        listOf(
            if (arrivedFromCamera) scheme.tertiaryContainer else scheme.primaryContainer,
            if (arrivedFromCamera) scheme.primaryContainer else scheme.tertiaryContainer,
        ),
    )
    val onHero = if (arrivedFromCamera) scheme.onTertiaryContainer else scheme.onPrimaryContainer
    ShapeClickableSurface(
        onClick = {},
        shape = RoundedCornerShape(36.dp),
        morphRest = heroMorph.rest,
        morphPressed = heroMorph.pressed,
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = onHero,
        rippleColor = onHero.copy(alpha = 0.22f),
        shadowElevation = 8.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(brush)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                ExpressiveShapeBadge(
                    icon = if (arrivedFromCamera) Icons.Outlined.Videocam else Icons.Outlined.AutoAwesome,
                    modifier = Modifier.size(72.dp),
                    backgroundColor = if (arrivedFromCamera) scheme.tertiary else scheme.primary,
                    iconColor = if (arrivedFromCamera) scheme.onTertiary else scheme.onPrimary,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    if (arrivedFromCamera) {
                        Text(
                            text = "From live camera",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = onHero.copy(alpha = 0.78f),
                        )
                    }
                    AdaptiveFitText(
                        text = if (arrivedFromCamera) {
                            "Text playground"
                        } else {
                            "Pretext text engine"
                        },
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = onHero,
                        allowTruncation = false,
                        minFontSize = 18.sp,
                    )
                    AdaptiveFitText(
                        text = if (arrivedFromCamera) {
                            "Tune the layout, then jump back."
                        } else {
                            "Measure once. Reflow around anything the camera sees."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = onHero.copy(alpha = 0.86f),
                        allowTruncation = false,
                        minFontSize = 12.sp,
                    )
                    if (fps != null) {
                        Text(
                            text = "FPS $fps · ${lastMeasureMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = onHero.copy(alpha = 0.72f),
                            fontSize = 10.sp,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveShapeBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    iconColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val reduceMotion = com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion.current
    val badgePhase = remember {
        expressivePhaseOffset(kotlin.random.Random.nextInt(), ExpressiveShapeCatalogTier.Badge)
    }
    val badgeLoopShape = rememberExpressiveMorphLoopShape(
        enabled = !reduceMotion,
        tier = ExpressiveShapeCatalogTier.Badge,
        tempo = ExpressiveMorphTempo.Soft,
        phaseOffset = badgePhase,
    )
    Surface(
        modifier = modifier,
        shape = badgeLoopShape,
        color = backgroundColor,
        contentColor = iconColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center,
        ) {
            DecorativeIcon(
                icon,
                tint = iconColor,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
fun PretextSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (icon != null) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                DecorativeIcon(
                    icon,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun PretextHowItWorksStrip(modifier: Modifier = Modifier) {
    val steps = listOf(
        Icons.Outlined.TextFields to "Measure text",
        Icons.Outlined.CenterFocusStrong to "Detect shape",
        Icons.Outlined.Speed to "Reflow lines",
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            steps.forEachIndexed { index, (icon, label) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DecorativeIcon(icon, modifier = Modifier.size(20.dp))
                    Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                if (index < steps.lastIndex) {
                    Text("→", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


fun PretextScreenMode.icon(): ImageVector = when (this) {
    PretextScreenMode.Camera -> Icons.Outlined.Videocam
    PretextScreenMode.Playground -> Icons.Outlined.TextFields
}

@Composable
fun PretextModeSelector(
    selected: PretextScreenMode,
    onSelected: (PretextScreenMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Pick a view — tap, or hold & slide",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PretextLiquidGlassTabs(
            selected = selected,
            onSelected = onSelected,
            modifier = Modifier.fillMaxWidth(),
        )
        androidx.compose.animation.AnimatedContent(
            targetState = selected,
            label = "pretextModeCaption",
        ) { mode ->
            Text(
                text = mode.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretextSettingsMenuRow(
    headline: String,
    value: String,
    onValueSelected: (String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leadingIcon: ImageVector? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(headline) },
                supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(value)
                        if (supporting != null) {
                            Text(
                                supporting,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                leadingContent = leadingIcon?.let { icon ->
                    {
                        DecorativeIcon(
                            icon,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
                trailingContent = {
                    DecorativeIcon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            expanded = false
                            onValueSelected(option)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretextLayoutModeSelector(
    selected: PretextLayoutMode,
    onSelected: (PretextLayoutMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    PretextSettingsMenuRow(
        headline = "Layout mode",
        value = selected.label,
        supporting = selected.hint,
        leadingIcon = Icons.Outlined.ViewColumn,
        options = PretextLayoutMode.entries.map { it.label },
        onValueSelected = { label ->
            PretextLayoutMode.entries.firstOrNull { it.label == label }?.let(onSelected)
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretextScriptChips(
    selected: TextScript,
    onSelected: (TextScript) -> Unit,
    modifier: Modifier = Modifier,
) {
    PretextSettingsMenuRow(
        headline = "Sample script",
        value = selected.label,
        leadingIcon = Icons.AutoMirrored.Outlined.Article,
        options = TextScript.entries.map { it.label },
        onValueSelected = { label ->
            TextScript.entries.firstOrNull { it.label == label }?.let(onSelected)
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretextStageSelector(
    selected: PretextCameraStage,
    onSelected: (PretextCameraStage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            modifier = Modifier.pretextSheetInset(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                "Live typography stage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Each stage changes how type and the camera respond to movement.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = PretextSheetInset),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(PretextCameraStage.entries, key = { it.name }) { stage ->
                val active = stage == selected
                Surface(
                    onClick = { onSelected(stage) },
                    modifier = Modifier
                        .width(if (active) 172.dp else 148.dp)
                        .height(112.dp),
                    shape = RoundedCornerShape(if (active) 28.dp else 20.dp),
                    color = if (active) scheme.primaryContainer else scheme.surfaceContainer,
                    contentColor = if (active) scheme.onPrimaryContainer else scheme.onSurface,
                    tonalElevation = if (active) 3.dp else 0.dp,
                ) {
                    Column(
                        Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(if (active) 16.dp else 12.dp),
                            color = if (active) {
                                scheme.primary
                            } else {
                                scheme.surfaceContainerHighest
                            },
                            contentColor = if (active) scheme.onPrimary else scheme.onSurfaceVariant,
                        ) {
                            Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                DecorativeIcon(stage.icon(), modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(
                            stage.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 2,
                        )
                    }
                }
            }
        }
        Text(
            selected.hint,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.pretextSheetInset(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PretextTrackModeSelector(
    selected: VisionTrackMode,
    onSelected: (VisionTrackMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "What to track",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.pretextSheetInset(),
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .pretextSheetInset(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VisionTrackMode.entries.forEach { mode ->
                FilterChip(
                    selected = selected == mode,
                    onClick = { onSelected(mode) },
                    label = { Text(mode.label) },
                    leadingIcon = {
                        DecorativeIcon(
                            when (mode) {
                                VisionTrackMode.Person -> Icons.Outlined.Person
                                VisionTrackMode.Face -> Icons.Outlined.Face
                                VisionTrackMode.Object -> Icons.Outlined.Category
                                VisionTrackMode.Auto -> Icons.Outlined.AutoAwesome
                            },
                            modifier = Modifier.size(18.dp),
                            tint = if (selected == mode) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    },
                    colors = pretextFilterChipColors(),
                )
            }
        }
        Text(
            when (selected) {
                VisionTrackMode.Person ->
                    "Finds up to ${PretextVisionLimits.MAX_PERSONS} people and reflows around each silhouette."
                VisionTrackMode.Face ->
                    "Tracks up to ${PretextVisionLimits.MAX_FACES} faces with individual outlines."
                VisionTrackMode.Object ->
                    "Detects up to ${PretextVisionLimits.MAX_OBJECTS} objects in the frame."
                VisionTrackMode.Auto ->
                    "Combines faces, bodies, and objects — up to ${PretextVisionLimits.MAX_AUTO} shapes at once."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.pretextSheetInset(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretextCameraOptions(
    trackMode: VisionTrackMode,
    onTrackModeSelected: (VisionTrackMode) -> Unit,
    showBoundingBox: Boolean,
    onShowBoundingBoxChange: (Boolean) -> Unit,
    showSpotlight: Boolean,
    onShowSpotlightChange: (Boolean) -> Unit,
    spotlightStrength: Float,
    onSpotlightStrengthChange: (Float) -> Unit,
    showBlur: Boolean,
    onShowBlurChange: (Boolean) -> Unit,
    blurRadiusDp: Float,
    onBlurRadiusDpChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PretextTrackModeSelector(
                selected = trackMode,
                onSelected = onTrackModeSelected,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            HorizontalDivider(
                modifier = Modifier.pretextSheetInset(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            SwitchListItem(
                checked = showBoundingBox,
                onCheckedChange = onShowBoundingBoxChange,
                containerColor = Color.Transparent,
                headlineContent = { Text("Show bounding box") },
                supportingContent = {
                    Text("Text still reflows when off — only the outline is hidden.")
                },
                leadingContent = {
                    DecorativeIcon(
                        Icons.Outlined.CropFree,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                },
            )

            SwitchListItem(
                checked = showSpotlight,
                onCheckedChange = onShowSpotlightChange,
                containerColor = Color.Transparent,
                headlineContent = { Text("Spotlight") },
                supportingContent = { Text("Dim outside the detected contour.") },
                leadingContent = {
                    DecorativeIcon(
                        Icons.Outlined.Waves,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                },
            )
            if (showSpotlight) {
                HapticSlider(
                    value = spotlightStrength.coerceIn(0f, 1f),
                    onValueChange = { onSpotlightStrengthChange(it.coerceIn(0f, 1f)) },
                    valueRange = 0f..1f,
                    modifier = Modifier.pretextSheetInset(),
                )
            }

            SwitchListItem(
                checked = showBlur,
                onCheckedChange = onShowBlurChange,
                containerColor = Color.Transparent,
                headlineContent = { Text("Blur background") },
                supportingContent = { Text("Blur outside the detected contour.") },
                leadingContent = {
                    DecorativeIcon(
                        Icons.Outlined.Waves,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                },
            )
            if (showBlur) {
                HapticSlider(
                    value = blurRadiusDp.coerceIn(0f, 30f),
                    onValueChange = { onBlurRadiusDpChange(it.coerceIn(0f, 30f)) },
                    valueRange = 0f..30f,
                    modifier = Modifier
                        .pretextSheetInset()
                        .padding(bottom = 8.dp),
                )
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretextMeasureSelector(
    selected: PretextMeasureMode,
    onSelected: (PretextMeasureMode) -> Unit,
    modifier: Modifier = Modifier,
    speed: PretextMeasureSpeed = PretextMeasureSpeed(),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "How text is measured",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.pretextSheetInset(),
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .pretextSheetInset(),
        ) {
            val colors = pretextSegmentedButtonColors()
            PretextMeasureMode.entries.forEachIndexed { index, mode ->
                val tag = speed.speedTag(mode)
                val timing = speed.format(mode)
                SegmentedButton(
                    selected = selected == mode,
                    onClick = { onSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, PretextMeasureMode.entries.size),
                    colors = colors,
                    icon = {
                        DecorativeIcon(
                            when (mode) {
                                PretextMeasureMode.Engine -> Icons.Outlined.Speed
                                PretextMeasureMode.ViewMeasure -> Icons.Outlined.TextFields
                            },
                            modifier = Modifier.size(18.dp),
                            tint = if (selected == mode) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    },
                    label = {
                        Text(
                            text = buildString {
                                append(mode.shortLabel)
                                if (speed.nanosFor(mode) > 0L) {
                                    append('\n')
                                    append(timing)
                                    if (tag != null) {
                                        append(" · ")
                                        append(tag)
                                    }
                                }
                            },
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 2,
                        )
                    },
                )
            }
        }
        val comparison = speed.comparisonLabel
        Text(
            text = comparison.ifEmpty { selected.hint },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .pretextSheetInset()
                .padding(bottom = 4.dp),
        )
    }
}

@Composable
fun PretextCameraLayoutSelector(
    selected: PretextCameraTextLayoutStyle,
    onSelected: (PretextCameraTextLayoutStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Paragraph layout",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.pretextSheetInset(),
        )
        Box(Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(selected.label) },
                supportingContent = {
                    Text(
                        selected.hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingContent = {
                    DecorativeIcon(
                        when (selected) {
                            PretextCameraTextLayoutStyle.ColumnWrap -> Icons.Outlined.Waves
                            PretextCameraTextLayoutStyle.DynamicFloat -> Icons.AutoMirrored.Outlined.WrapText
                            PretextCameraTextLayoutStyle.Newspaper -> Icons.Outlined.Newspaper
                            PretextCameraTextLayoutStyle.Magazine -> Icons.AutoMirrored.Outlined.Article
                            PretextCameraTextLayoutStyle.Uniform -> Icons.Outlined.ViewColumn
                        },
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                },
                trailingContent = {
                    DecorativeIcon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp),
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth().clickable { expanded = true },
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                PretextCameraTextLayoutStyle.entries.forEach { style ->
                    DropdownMenuItem(
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(style.label, fontWeight = FontWeight.Medium)
                                Text(
                                    style.hint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            onSelected(style)
                        },
                    )
                }
            }
        }
    }
}


@Composable
fun PretextMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(accent, shape = RoundedCornerShape(50)),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun PretextMetricsGrid(
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { (label, value) ->
                    PretextMetricTile(label, value, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PretextFeatureChips(
    selected: PretextFeature,
    onSelected: (PretextFeature) -> Unit,
    modifier: Modifier = Modifier,
) {
    PretextSettingsMenuRow(
        headline = "Engine showcase",
        value = selected.label,
        supporting = selected.hint,
        leadingIcon = Icons.Outlined.AutoAwesome,
        options = PretextFeature.entries.map { it.label },
        onValueSelected = { label ->
            PretextFeature.entries.firstOrNull { it.label == label }?.let(onSelected)
        },
        modifier = modifier,
    )
}


enum class PretextScreenMode(val label: String, val subtitle: String) {
    Camera("Live camera", "Point the camera — text reflows around people, faces & objects in real time."),
    Playground("Text playground", "Type your own text, tune the layout, and benchmark the engine."),
}

enum class PretextMeasureMode(val label: String, val shortLabel: String, val hint: String) {
    Engine(
        label = "Pretext engine",
        shortLabel = "Engine",
        hint = "Fast path — measure glyphs once, then reflow around the tracked shape every frame.",
    ),
    ViewMeasure(
        label = "Android View",
        shortLabel = "View",
        hint = "Classic TextView-style measure for comparison. Usually slower than the engine.",
    ),
}


enum class PretextCameraStage(
    val label: String,
    val hint: String,
    val showsLivePreview: Boolean = true,
    val usesPaperBackdrop: Boolean = false,
    val supportsMultiObstacle: Boolean = false,
    val supportsManualOrbs: Boolean = false,
    val supportsGenerativeObstacle: Boolean = false,
    val fillsSilhouetteWithType: Boolean = false,
) {
    CameraOverlay(
        label = "Live cutout",
        hint = "Your live image cuts through a wall of text that reflows around your silhouette.",
        showsLivePreview = true,
    ),
    TypePortrait(
        label = "Type portrait",
        hint = "Your silhouette is drawn with moving words while the world outside falls away.",
        showsLivePreview = true,
        fillsSilhouetteWithType = true,
    ),
    Studio(
        label = "Editorial spread",
        hint = "A calm paper composition with the live subject embedded inside flowing columns.",
        showsLivePreview = false,
        usesPaperBackdrop = true,
    ),
    Editorial(
        label = "Kinetic canvas",
        hint = "Drag expressive shapes through the page and watch every line move around them.",
        showsLivePreview = false,
        usesPaperBackdrop = true,
        supportsMultiObstacle = true,
        supportsManualOrbs = true,
    ),
    Generative(
        label = "Motion orbit",
        hint = "An autonomous shape orbits through the composition and continuously reshapes the text.",
        showsLivePreview = false,
        usesPaperBackdrop = true,
        supportsGenerativeObstacle = true,
    ),
}

fun PretextCameraStage.icon(): ImageVector = when (this) {
    PretextCameraStage.CameraOverlay -> Icons.Outlined.Videocam
    PretextCameraStage.TypePortrait -> Icons.Outlined.Person
    PretextCameraStage.Studio -> Icons.AutoMirrored.Outlined.Article
    PretextCameraStage.Editorial -> Icons.Outlined.Waves
    PretextCameraStage.Generative -> Icons.Outlined.AutoAwesome
}

enum class PretextLayoutMode(val label: String, val hint: String) {
    Uniform("Uniform", "Single width per line — lists and chat."),
    VariableWidth("Around obstacle", "Lines narrow where the obstacle intersects."),
    LineRanges("Line ranges", "Inspect line boundaries only."),
    ReflowCompare(
        "Dynamic vs column",
        "layoutDynamic() beside layoutColumn() with the same rect obstacle.",
    ),
}

enum class PretextCameraTextLayoutStyle(val label: String, val hint: String) {
    ColumnWrap(
        label = "Tight wrap",
        hint = "Lines carve around the silhouette outline — best for people and faces.",
    ),
    DynamicFloat(
        label = "Magazine float",
        hint = "Lines get shorter beside the shape, like text floating around a photo.",
    ),
    Newspaper(
        label = "Newspaper",
        hint = "Headline on top, then multi-column body that flows around detections.",
    ),
    Magazine(
        label = "Magazine spread",
        hint = "Wide headline plus two tighter columns — editorial layout on paper stages.",
    ),
    Uniform(
        label = "Straight columns",
        hint = "Fixed-width columns that ignore obstacles — useful as a baseline.",
    ),
}

enum class PretextFeature(val label: String, val hint: String) {
    Balance(
        "Balance",
        "balanceLines() — narrowest width that keeps the same line count. Like CSS text-wrap: balance.",
    ),
    Truncate(
        "Truncate",
        "truncateText() — head / middle / tail, never clips mid-grapheme.",
    ),
    FitFont(
        "Fit font",
        "fitFontSize() — binary-search the largest font that fits a box. ~12 iterations.",
    ),
    Polygon(
        "Polygon",
        "layoutColumn() now accepts polygon obstacles for star / heart / arrow exclusions.",
    ),
    Streaming(
        "Streaming",
        "prepareStreaming() — append tokens incrementally without re-measuring the head.",
    ),
    ShrinkWrap(
        "Shrink wrap",
        "measureLineStats() + layoutDynamic() — upstream variable-width line flow (chenglou/pretext).",
    ),
}

enum class TextScript(val label: String, val sample: String) {
    Latin("Latin", "The quick brown fox jumps over the lazy dog. Pretext measures glyphs once, then layouts with pure arithmetic. "),
    Cjk("CJK", "中文测试：逐字断行与 Canvas 宽度缓存，layout() 可在动画帧中调用。"),
    Arabic("Arabic", "بدأت الرحلة في الصباح الباكر مع أصدقاء نحو المدينة."),
    Emoji("Emoji", "Launch 🚀🌍 to orbit — emoji segments use a width correction factor."),
    Mixed("Mixed", "Mixed: Hello 世界 مرحبا 🎉 — Latin + CJK + Arabic + emoji in one pass."),
}


@Composable
fun PretextMeasureTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var acceptFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(acceptFocus) {
        if (acceptFocus) {
            focusRequester.requestFocus()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .focusProperties { canFocus = acceptFocus }
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        acceptFocus = false
                    }
                },
            label = { Text("Text to measure") },
            leadingIcon = {
                DecorativeIcon(
                    Icons.Outlined.Edit,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            minLines = 3,
            shape = RoundedCornerShape(20.dp),
        )
        if (!acceptFocus) {
            Spacer(
                Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { acceptFocus = true }
                    },
            )
        }
    }
}
