package com.mfhapps.trendingui.ui.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp as lerpTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.BrandMark
import com.mfhapps.trendingui.ui.theme.ThemeMode
import com.mfhapps.trendingui.ui.theme.resolveDarkTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow
import com.mfhapps.trendingui.ui.components.SwitchListItem
import com.mfhapps.trendingui.ui.components.CollapsedTopAppBarBackdrop
import com.mfhapps.trendingui.ui.detail.DetailPaneGuideAction
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide

object SettingsExpressiveDefaults {
    val screenHorizontalPadding = 20.dp
    val sectionSpacing = 16.dp
    val cardCornerRadius = 24.dp
    val scrollBottomExtra = 24.dp
    val appInfoChipHorizontalPadding = 12.dp
    val appInfoChipItemSpacing = 8.dp
    val appInfoChipVerticalPadding = 8.dp
    val guideActionWidth = 48.dp
    val navIconRowWidth = 56.dp
    val topOverscrollMaxPull = 72.dp
    const val overscrollStartThreshold = 0.08f
    const val overscrollMidThreshold = 0.5f
    const val overscrollMaxThreshold = 0.95f
}

private fun settingsPullResistance(currentPullPx: Float, maxPullPx: Float): Float {
    val ratio = (currentPullPx / maxPullPx).coerceIn(0f, 1f)
    return 0.88f * (1f - 0.3f * ratio)
}

@Stable
data class SettingsTopOverscrollState(
    val stretchFraction: State<Float>,
    val followFinger: State<Boolean>,
    val pullNestedScrollConnection: NestedScrollConnection,
)

@Stable
data class SettingsAppInfoChipSizing(
    val collapsed: Dp,
    val height: Dp,
    val logoSize: Dp,
)

@Composable
fun rememberSettingsAppInfoChipSizing(): SettingsAppInfoChipSizing {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val titleSmall = MaterialTheme.typography.titleSmall
    val labelSmall = MaterialTheme.typography.labelSmall
    val horizontalPadding = SettingsExpressiveDefaults.appInfoChipHorizontalPadding * 2
    val itemSpacing = SettingsExpressiveDefaults.appInfoChipItemSpacing

    fun measureText(text: String, style: androidx.compose.ui.text.TextStyle) =
        textMeasurer.measure(
            text = text,
            style = style,
            maxLines = 1,
            softWrap = false,
        ).size.width

    val horizontalPaddingPx = with(density) { horizontalPadding.roundToPx() }
    val itemSpacingPx = with(density) { itemSpacing.roundToPx() }
    val logoSize = with(density) {
        titleSmall.lineHeight.toDp() * 0.85f
    }
    val collapsedPx = measureText("UITrends", titleSmall) + horizontalPaddingPx

    val collapsed = with(density) { collapsedPx.toDp() }
    val height = with(density) {
        maxOf(
            titleSmall.lineHeight.toDp(),
            labelSmall.lineHeight.toDp(),
        ) + SettingsExpressiveDefaults.appInfoChipVerticalPadding * 2
    }

    return remember(collapsed, height, logoSize) {
        SettingsAppInfoChipSizing(
            collapsed = collapsed,
            height = height,
            logoSize = logoSize,
        )
    }
}

private val settingsScrollToTopSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh,
)

@OptIn(ExperimentalMaterial3Api::class)
suspend fun animateSettingsAppBarExpand(state: TopAppBarState) {
    if (state.heightOffsetLimit >= -0.5f) return
    if (state.collapsedFraction < 0.02f) return
    coroutineScope {
        launch {
            animate(
                initialValue = state.heightOffset,
                targetValue = 0f,
                animationSpec = settingsScrollToTopSpring,
            ) { value, _ ->
                state.heightOffset = value
            }
        }
        if (kotlin.math.abs(state.contentOffset) > 0.5f) {
            launch {
                animate(
                    initialValue = state.contentOffset,
                    targetValue = 0f,
                    animationSpec = settingsScrollToTopSpring,
                ) { value, _ ->
                    state.contentOffset = value
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
suspend fun scrollSettingsToTop(
    listState: LazyListState,
    topAppBarState: TopAppBarState,
) {
    coroutineScope {
        launch {
            if (!listState.atSettingsContentTop()) {
                listState.animateScrollToItem(index = 0, scrollOffset = 0)
            }
        }
        launch {
            animateSettingsAppBarExpand(topAppBarState)
        }
    }
}

fun LazyListState.atSettingsContentTop(): Boolean =
    firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0

@Composable
fun rememberSettingsChipCollapseOnScroll(
    chipTapExpanded: Boolean,
    onCollapseChip: () -> Unit,
): NestedScrollConnection {
    val tapExpanded by rememberUpdatedState(chipTapExpanded)
    val collapse by rememberUpdatedState(onCollapseChip)
    return remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (tapExpanded && available.y > 0f) {
                    collapse()
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (tapExpanded && consumed.y > 0f) {
                    collapse()
                }
                return Offset.Zero
            }
        }
    }
}

@Composable
fun SettingsChipCollapseOnScrollEffect(
    listState: LazyListState,
    chipTapExpanded: Boolean,
    onCollapseChip: () -> Unit,
) {
    LaunchedEffect(listState, chipTapExpanded, onCollapseChip) {
        if (!chipTapExpanded) return@LaunchedEffect
        snapshotFlow {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
            .distinctUntilChanged()
            .collect { scrolledDown ->
                if (scrolledDown) {
                    onCollapseChip()
                }
            }
    }
}

private fun consumeSettingsTopPull(
    listState: LazyListState,
    pullOffsetPx: MutableFloatState,
    maxPullPx: Float,
    deltaY: Float,
): Float {
    if (deltaY <= 0f) return 0f
    if (!listState.atSettingsContentTop()) return 0f
    val current = pullOffsetPx.floatValue
    val room = (maxPullPx - current).coerceAtLeast(0f)
    if (room <= 0f) return 0f
    val resistance = settingsPullResistance(current, maxPullPx)
    val consume = minOf(deltaY, room) * resistance
    pullOffsetPx.floatValue = current + consume
    return consume
}

@Composable
fun rememberSettingsListNestedScroll(
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
): SettingsTopOverscrollState {
    val overscroll = rememberSettingsTopOverscroll(listState)
    val scrollConnection = scrollBehavior.nestedScrollConnection
    val combinedConnection = remember(overscroll.pullNestedScrollConnection, scrollConnection) {
        val pullConnection = overscroll.pullNestedScrollConnection
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val pullConsumed = pullConnection.onPreScroll(available, source)
                if (pullConsumed != Offset.Zero) return pullConsumed
                return scrollConnection.onPreScroll(available, source)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val pullConsumed = pullConnection.onPostScroll(consumed, available, source)
                if (pullConsumed != Offset.Zero) return pullConsumed
                return scrollConnection.onPostScroll(consumed, available, source)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                pullConnection.onPreFling(available)
                return scrollConnection.onPreFling(available)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                pullConnection.onPostFling(consumed, available)
                return scrollConnection.onPostFling(consumed, available)
            }
        }
    }
    return SettingsTopOverscrollState(
        stretchFraction = overscroll.stretchFraction,
        followFinger = overscroll.followFinger,
        pullNestedScrollConnection = combinedConnection,
    )
}

@Composable
fun rememberSettingsTopOverscroll(
    listState: LazyListState,
): SettingsTopOverscrollState {
    val pullOffsetPx = remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val maxPullPx = with(LocalDensity.current) {
        SettingsExpressiveDefaults.topOverscrollMaxPull.toPx()
    }
    val snapBackSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        )
    }
    val snapBack = remember(scope, snapBackSpec) {
        {
            scope.launch {
                val start = pullOffsetPx.floatValue
                if (start <= 0f) return@launch
                animate(
                    initialValue = start,
                    targetValue = 0f,
                    animationSpec = snapBackSpec,
                ) { value, _ ->
                    pullOffsetPx.floatValue = value
                }
            }
        }
    }
    val pullNestedScrollConnection = remember(listState, maxPullPx, snapBack) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (pullOffsetPx.floatValue > 0f && available.y < 0f) {
                    val release = minOf(-available.y, pullOffsetPx.floatValue)
                    pullOffsetPx.floatValue = (pullOffsetPx.floatValue - release).coerceAtLeast(0f)
                    return Offset(0f, -release)
                }
                val consume = consumeSettingsTopPull(
                    listState = listState,
                    pullOffsetPx = pullOffsetPx,
                    maxPullPx = maxPullPx,
                    deltaY = available.y,
                )
                return Offset(0f, consume)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val consume = consumeSettingsTopPull(
                    listState = listState,
                    pullOffsetPx = pullOffsetPx,
                    maxPullPx = maxPullPx,
                    deltaY = available.y,
                )
                return Offset(0f, consume)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (pullOffsetPx.floatValue > 0f) {
                    snapBack()
                }
                return Velocity.Zero
            }
        }
    }
    val stretchFractionState = remember {
        derivedStateOf {
            (pullOffsetPx.floatValue / maxPullPx).coerceIn(0f, 1f)
        }
    }
    val followFingerState = remember {
        derivedStateOf { pullOffsetPx.floatValue > 0f }
    }
    return SettingsTopOverscrollState(
        stretchFraction = stretchFractionState,
        followFinger = followFingerState,
        pullNestedScrollConnection = pullNestedScrollConnection,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCollapsingTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    collapsedFraction: Float,
    headerForeground: SettingsHeaderForeground,
    onNavigateBack: () -> Unit,
    appInfoRevealProgress: Float,
    followOverscrollFinger: Boolean,
    onAppInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Settings",
    guide: DemoTrendGuide? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) {
    val chipSizing = rememberSettingsAppInfoChipSizing()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val guideSlot = if (guide != null) {
        SettingsExpressiveDefaults.guideActionWidth + 4.dp
    } else {
        0.dp
    }
    val maxChipBand = screenWidth -
        SettingsExpressiveDefaults.navIconRowWidth -
        guideSlot -
        8.dp
    val reveal = appInfoRevealProgress.coerceIn(0f, 1f)
    val collapsedToolbar = collapsedFraction > 0.5f
    val compactActionsWidth = chipSizing.collapsed + guideSlot + 4.dp
    val chipExpandedTarget = if (collapsedToolbar) chipSizing.collapsed else maxChipBand
    val actionsRowWidth = if (collapsedToolbar) {
        compactActionsWidth
    } else {
        lerp(chipSizing.collapsed, maxChipBand, reveal) + guideSlot + 4.dp
    }
    val titleStyle = lerpTextStyle(
        start = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        stop = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        fraction = collapsedFraction.coerceIn(0f, 1f),
    )

    CollapsedTopAppBarBackdrop(
        collapsedFraction = collapsedFraction,
        modifier = modifier.fillMaxWidth(),
    ) {
        LargeTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            windowInsets = TopAppBarDefaults.windowInsets,
        title = {
            Text(
                text = title,
                style = titleStyle,
                color = headerForeground.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = headerForeground.icons,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to catalog",
                    tint = headerForeground.icons,
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = 4.dp,
                    alignment = Alignment.End,
                ),
                modifier = Modifier.width(actionsRowWidth),
            ) {
                SettingsAppInfoChip(
                    revealProgress = appInfoRevealProgress,
                    followOverscrollFinger = followOverscrollFinger,
                    collapsedWidth = chipSizing.collapsed,
                    expandedWidth = chipExpandedTarget,
                    height = chipSizing.height,
                    logoSize = chipSizing.logoSize,
                    onClick = onAppInfoClick,
                )
                if (guide != null) {
                    DetailPaneGuideAction(
                        guide = guide,
                        iconTint = headerForeground.icons,
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = colors,
        )
    }
}

@Composable
fun rememberSettingsBottomPadding(extra: Dp = SettingsExpressiveDefaults.scrollBottomExtra): Dp {
    val navigationBars = WindowInsets.navigationBars.asPaddingValues()
    return remember(navigationBars, extra) {
        navigationBars.calculateBottomPadding() + extra
    }
}

@Composable
fun SettingsScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        content = { content() },
    )
}

@Composable
fun SettingsSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsExpressiveDefaults.screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onBackground,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SettingsSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SettingsExpressiveDefaults.screenHorizontalPadding),
        shape = RoundedCornerShape(SettingsExpressiveDefaults.cardCornerRadius),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            content = content,
        )
    }
}

@Composable
fun SettingsSectionDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    )
}

@Composable
fun SettingsSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    SwitchListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        leadingContent = {
            SettingsIconBadge(
                icon = icon,
                containerColor = scheme.secondaryContainer,
                contentColor = scheme.onSecondaryContainer,
            )
        },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
fun SettingsSubsectionLabel(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            SettingsIconBadge(
                icon = icon,
                containerColor = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun SettingsCardContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsThemeModePicker(
    selected: ThemeMode,
    systemDarkTheme: Boolean,
    onSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val modes = ThemeMode.entries
    val activeDark = resolveDarkTheme(selected, systemDarkTheme)
    val helperText = when (selected) {
        ThemeMode.System -> {
            val follow = if (systemDarkTheme) "dark" else "light"
            "Follows device — $follow right now"
        }
        ThemeMode.Light -> "Always light · dark status icons"
        ThemeMode.Dark -> "Always dark · light status icons"
    }
    val activeLabel = if (activeDark) "Dark" else "Light"

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Active: $activeLabel",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.primary,
            )
        }
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            modes.forEachIndexed { index, mode ->
                val (icon, label) = when (mode) {
                    ThemeMode.System -> Icons.Outlined.Brightness4 to "System"
                    ThemeMode.Light -> Icons.Outlined.LightMode to "Light"
                    ThemeMode.Dark -> Icons.Outlined.DarkMode to "Dark"
                }
                SegmentedButton(
                    selected = selected == mode,
                    onClick = { onSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                )
            }
        }
        Text(
            text = helperText,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SettingsAboutPanel(
    versionName: String,
    versionCode: Int,
    modifier: Modifier = Modifier,
) {
    val deviceName = remember {
        "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    }
    val androidVersion = remember {
        "Android ${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}"
    }

    SettingsSectionCard(modifier = modifier) {
        SettingsAboutRow(
            title = "Device",
            value = deviceName,
            icon = Icons.Outlined.PhoneAndroid,
        )
        SettingsSectionDivider()
        SettingsAboutRow(
            title = "Android",
            value = androidVersion,
            icon = Icons.Outlined.Info,
        )
        SettingsSectionDivider()
        SettingsAboutRow(
            title = "App version",
            value = "$versionName ($versionCode)",
            icon = Icons.Outlined.Apps,
        )
    }
}

@Composable
fun SettingsAboutRow(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SettingsIconBadge(
            icon = icon,
            containerColor = scheme.tertiaryContainer,
            contentColor = scheme.onTertiaryContainer,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurface,
            )
        }
    }
}

@Composable
fun SettingsAppInfoOverscrollHaptics(
    stretchFraction: Float,
) {
    val haptics = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    var previousStretch by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(stretchFraction, reduceMotion) {
        if (reduceMotion) {
            previousStretch = stretchFraction
            return@LaunchedEffect
        }
        val previous = previousStretch
        val current = stretchFraction.coerceIn(0f, 1f)
        when {
            previous < SettingsExpressiveDefaults.overscrollStartThreshold &&
                current >= SettingsExpressiveDefaults.overscrollStartThreshold -> {
                haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            }
            previous < SettingsExpressiveDefaults.overscrollMidThreshold &&
                current >= SettingsExpressiveDefaults.overscrollMidThreshold -> {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
            }
            previous < SettingsExpressiveDefaults.overscrollMaxThreshold &&
                current >= SettingsExpressiveDefaults.overscrollMaxThreshold -> {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            }
            previous > SettingsExpressiveDefaults.overscrollStartThreshold &&
                current < SettingsExpressiveDefaults.overscrollStartThreshold -> {
                haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
            }
        }
        previousStretch = current
    }
}

@Composable
fun SettingsAppInfoChip(
    revealProgress: Float,
    followOverscrollFinger: Boolean,
    collapsedWidth: Dp,
    expandedWidth: Dp,
    height: Dp,
    logoSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val reduceMotion = LocalReduceMotion.current
    val scheme = MaterialTheme.colorScheme
    val chipShape = RoundedCornerShape(20.dp)
    val targetProgress = revealProgress.coerceIn(0f, 1f)
    val collapseSpec = snap<Float>()
    val expandSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    val settledProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = if (targetProgress <= 0f) collapseSpec else expandSpec,
        label = "appInfoReveal",
    )
    val progress = when {
        followOverscrollFinger -> targetProgress
        targetProgress <= 0f -> 0f
        else -> settledProgress
    }
    val expandedAlpha = ((progress - 0.04f) / 0.96f).coerceIn(0f, 1f)
    val chipWidth = lerp(collapsedWidth, expandedWidth, progress)
    val showExpandedContent = progress > if (followOverscrollFinger) 0.04f else 0.2f

    Surface(
        modifier = modifier
            .width(chipWidth)
            .height(height)
            .clip(chipShape)
            .clickable {
                if (!reduceMotion) {
                    val type = if (targetProgress > 0.5f) {
                        HapticFeedbackType.GestureEnd
                    } else {
                        HapticFeedbackType.Confirm
                    }
                    haptics.performHapticFeedback(type)
                }
                onClick()
            }
            .semantics {
                contentDescription = if (progress > 0.5f) {
                    "Collapse app info"
                } else {
                    "Expand app info"
                }
            },
        shape = chipShape,
        color = scheme.surfaceContainerHigh,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SettingsExpressiveDefaults.appInfoChipHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                SettingsExpressiveDefaults.appInfoChipItemSpacing,
            ),
        ) {
            if (showExpandedContent) {
                BrandMark(
                    size = logoSize,
                    animated = false,
                    modifier = Modifier.alpha(expandedAlpha),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "UITrends",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Material 3 pattern library",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(expandedAlpha),
                    )
                }
                Text(
                    text = "© 2026 MFH Apps",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    text = "UITrends",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun SettingsIconBadge(
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun SettingsAccentSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String,
) {
    val ringColor = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(36.dp)
            .semantics {
                role = Role.RadioButton
                this.contentDescription = contentDescription
            }
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) 30.dp else 28.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) {
                        Modifier.border(2.dp, ringColor, CircleShape)
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

@Composable
fun SettingsHomeStudioPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) = SettingsSectionCard(modifier = modifier, content = content)

@Composable
fun SettingsHomeDivider(modifier: Modifier = Modifier) = SettingsSectionDivider(modifier)

@Composable
fun SettingsHomeRowHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) = SettingsSubsectionLabel(
    title = title,
    modifier = modifier,
    subtitle = subtitle,
    icon = icon,
)
