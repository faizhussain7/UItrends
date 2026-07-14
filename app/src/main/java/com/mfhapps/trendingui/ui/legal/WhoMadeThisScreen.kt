@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.mfhapps.trendingui.ui.legal

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import com.mfhapps.trendingui.ui.platform.appBarTopWindowInsets
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.toShape
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp as lerpTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpDp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.mfhapps.trendingui.legal.CREATOR_PROFILE_PHOTO_KEY
import com.mfhapps.trendingui.legal.CreatorLinks
import com.mfhapps.trendingui.legal.CreatorProfileUiState
import com.mfhapps.trendingui.legal.CreatorProfileViewModel
import com.mfhapps.trendingui.legal.github.GitHubUserProfile
import com.mfhapps.trendingui.navigation.demoSharedElement
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.CollapsingBlurTopBarLayout
import com.mfhapps.trendingui.ui.components.IconButton
import com.mfhapps.trendingui.ui.components.LoadingIndicator
import com.mfhapps.trendingui.ui.components.appHazeSource
import com.mfhapps.trendingui.ui.components.collapsingTopBarContentPadding
import com.mfhapps.trendingui.ui.components.rememberCollapsedTopAppBarColors
import com.mfhapps.trendingui.ui.components.rememberMorphShape
import com.mfhapps.trendingui.ui.settings.SettingsExpressiveDefaults
import com.mfhapps.trendingui.ui.settings.SettingsSectionCard
import com.mfhapps.trendingui.ui.settings.SettingsSectionDivider
import com.mfhapps.trendingui.ui.settings.SettingsSectionTitle

private val AvatarDisplaySize = 136.dp

@Immutable
private data class CreatorHeroMorphLayout(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val sectionSpacing: Dp,
    val textBlockSpacing: Dp,
    val avatarSize: Dp,
    val nameStyle: TextStyle,
    val headlineStyle: TextStyle,
    val hintStyle: TextStyle,
    val chipStyle: TextStyle,
    val chipHorizontalPadding: Dp,
    val chipVerticalPadding: Dp,
    val contentMaxWidthFraction: Float,
    val tagSpacing: Dp,
    val contentScale: Float,
)

private fun smoothMorph(progress: Float): Float {
    val t = progress.coerceIn(0f, 1f)
    val step = t * t * (3f - 2f * t)
    return step * step * (3f - 2f * step)
}

@Composable
private fun rememberCreatorHeroMorphLayout(morph: Float): CreatorHeroMorphLayout {
    val smooth = smoothMorph(morph)
    val typography = MaterialTheme.typography
    val nameScale = lerp(1f, 0.97f, smooth)
    val headlineScale = lerp(1f, 0.96f, smooth)
    val hintScale = lerp(1f, 0.97f, smooth)
    val chipScale = lerp(1f, 0.96f, smooth)
    val contentScale = lerp(1f, 0.985f, smooth)

    return CreatorHeroMorphLayout(
        horizontalPadding = lerpDp(22.dp, 26.dp, smooth),
        verticalPadding = lerpDp(24.dp, 22.dp, smooth),
        sectionSpacing = lerpDp(14.dp, 13.dp, smooth),
        textBlockSpacing = lerpDp(6.dp, 5.5.dp, smooth),
        avatarSize = lerpDp(AvatarDisplaySize, 132.dp, smooth),
        nameStyle = typography.headlineMedium.scaled(nameScale),
        headlineStyle = typography.bodyLarge.scaled(headlineScale),
        hintStyle = typography.labelMedium.scaled(hintScale),
        chipStyle = typography.labelLarge.scaled(chipScale),
        chipHorizontalPadding = lerpDp(12.dp, 11.dp, smooth),
        chipVerticalPadding = lerpDp(6.dp, 5.5.dp, smooth),
        contentMaxWidthFraction = lerp(0.94f, 0.9f, smooth),
        tagSpacing = lerpDp(8.dp, 7.5.dp, smooth),
        contentScale = contentScale,
    )
}

private fun TextStyle.scaled(scale: Float): TextStyle = copy(
    fontSize = fontSize * scale,
    lineHeight = lineHeight * scale,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun WhoMadeThisScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatorProfileViewModel = viewModel(),
) {
    var photoExpanded by rememberSaveable { mutableStateOf(false) }
    BackHandler {
        if (photoExpanded) {
            photoExpanded = false
        } else {
            onNavigateBack()
        }
    }
    val scheme = MaterialTheme.colorScheme
    val uriHandler = LocalUriHandler.current
    val state = viewModel.uiState

    Surface(
        modifier = modifier.fillMaxSize(),
        color = scheme.background,
    ) {
        CreatorProfilePhotoTransitionHost(
            photoExpanded = photoExpanded,
            photoUrlFullscreen = state.photoUrlFullscreen,
            displayName = state.displayName,
            onPhotoExpandedChange = { photoExpanded = it },
        ) { sharedTransitionScope, animatedVisibilityScope, onPhotoClick ->
            val listState = rememberLazyListState()
            val topAppBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
            val collapsedFraction by remember {
                derivedStateOf { scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f) }
            }
            val topBarColors = rememberCollapsedTopAppBarColors(
                collapsedFraction = collapsedFraction,
                containerColor = Color.Transparent,
                scrolledContainerColor = scheme.surface,
                titleContentColor = scheme.onSurface,
                navigationIconContentColor = scheme.onSurface,
                actionIconContentColor = scheme.onSurface,
            )

            CollapsingBlurTopBarLayout(
                scrollBehavior = scrollBehavior,
                collapsedFraction = collapsedFraction,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                topBar = { barModifier ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        WhoMadeThisCollapsingTopBar(
                            scrollBehavior = scrollBehavior,
                            collapsedFraction = collapsedFraction,
                            colors = topBarColors,
                            onNavigateBack = onNavigateBack,
                            onRefresh = viewModel::refresh,
                            refreshEnabled = !state.loading,
                            modifier = barModifier,
                        )
                        CreatorProfileLoadingBar(loading = state.loading)
                    }
                },
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .appHazeSource(),
                    contentPadding = collapsingTopBarContentPadding(
                        extra = PaddingValues(
                            start = SettingsExpressiveDefaults.screenHorizontalPadding,
                            end = SettingsExpressiveDefaults.screenHorizontalPadding,
                            bottom = 28.dp,
                        ),
                    ),
                    verticalArrangement = Arrangement.spacedBy(SettingsExpressiveDefaults.sectionSpacing),
                ) {
                    item(key = "hero") {
                        CreatorHeroCard(
                            state = state,
                            onPhotoClick = onPhotoClick,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }

                    state.errorMessage?.let { message ->
                        item(key = "error") {
                            CreatorErrorCard(
                                message = message,
                                onRetry = viewModel::refresh,
                            )
                        }
                    }

                    state.github?.let { github ->
                        item(key = "stats") {
                            CreatorStatsRow(profile = github)
                        }
                    }

                    item(key = "connect-title") {
                        SettingsSectionTitle(
                            title = "Contact",
                            subtitle = "Professional profiles and correspondence",
                        )
                    }

                    item(key = "social") {
                        CreatorSocialCard(
                            state = state,
                            onOpenLink = uriHandler::openUri,
                        )
                    }

                    item(key = "whatsapp-title") {
                        SettingsSectionTitle(
                            title = "WhatsApp",
                            subtitle = "Regional contact numbers",
                        )
                    }

                    item(key = "whatsapp") {
                        CreatorWhatsAppCard(onOpenLink = uriHandler::openUri)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhoMadeThisCollapsingTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    collapsedFraction: Float,
    colors: TopAppBarColors,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    refreshEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val titleStyle = lerpTextStyle(
        start = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
        stop = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        fraction = collapsedFraction,
    )
    val subtitleAlpha = (1f - collapsedFraction * 1.4f).coerceIn(0f, 1f)

    LargeTopAppBar(
        modifier = modifier,
        windowInsets = appBarTopWindowInsets(),
        scrollBehavior = scrollBehavior,
        colors = colors,
        title = {
            Column(
                modifier = Modifier.padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Who made this",
                    style = titleStyle,
                    color = scheme.onSurface,
                    maxLines = if (collapsedFraction > 0.85f) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitleAlpha > 0.01f) {
                    Text(
                        text = "Application creator and contact information",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer { alpha = subtitleAlpha },
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to settings",
                )
            }
        },
        actions = {
            IconButton(
                onClick = onRefresh,
                enabled = refreshEnabled,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh profile",
                )
            }
        },
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CreatorHeroCard(
    state: CreatorProfileUiState,
    onPhotoClick: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val density = LocalDensity.current
    val reduceMotion = LocalReduceMotion.current
    val infinite = rememberInfiniteTransition(label = "creatorHero")
    val cardMorph by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cardMorph",
    )
    val avatarMorph by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "avatarMorph",
    )
    val gradientPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "gradientPhase",
    )
    val effectiveCardMorph = if (reduceMotion) 0f else cardMorph
    val effectiveAvatarMorph = if (reduceMotion) 0f else avatarMorph
    val morphLayout = rememberCreatorHeroMorphLayout(effectiveCardMorph)
    val heroShape = rememberMorphShape(
        rest = MaterialShapes.Gem,
        pressed = MaterialShapes.Sunny,
        progress = effectiveCardMorph,
    )
    val avatarShape = rememberMorphShape(
        rest = MaterialShapes.Circle,
        pressed = MaterialShapes.Cookie4Sided,
        progress = effectiveAvatarMorph,
    )
    val avatarPixels = with(density) { morphLayout.avatarSize.roundToPx() }
    val isDark = scheme.background.luminance() < 0.45f
    val hasPhoto = !state.loading || state.github != null || state.linkedIn != null
    val heroWash = Brush.linearGradient(
        colors = listOf(
            scheme.primary.copy(alpha = if (isDark) 0.34f else 0.22f),
            scheme.tertiary.copy(alpha = if (isDark) 0.28f else 0.18f),
            scheme.secondary.copy(alpha = if (isDark) 0.24f else 0.14f),
            scheme.primaryContainer.copy(alpha = if (isDark) 0.72f else 0.88f),
        ),
        start = Offset(x = 900f * gradientPhase, y = 0f),
        end = Offset(x = 200f * (1f - gradientPhase), y = 900f),
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = heroShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroWash)
                .background(scheme.primaryContainer.copy(alpha = if (isDark) 0.18f else 0.08f)),
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = morphLayout.horizontalPadding,
                    vertical = morphLayout.verticalPadding,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(morphLayout.sectionSpacing),
        ) {
            Box(
                modifier = Modifier
                    .size(morphLayout.avatarSize)
                    .demoSharedElement(
                        sharedTransitionScope = sharedTransitionScope,
                        key = CREATOR_PROFILE_PHOTO_KEY,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                    .clip(avatarShape)
                    .background(scheme.surface)
                    .clickable(
                        enabled = hasPhoto,
                        onClick = onPhotoClick,
                        role = Role.Button,
                    )
                    .semantics {
                        contentDescription = "Profile photo of ${state.displayName}. Opens enlarged view."
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (hasPhoto) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(state.photoUrl)
                            .size(avatarPixels)
                            .scale(Scale.FILL)
                            .crossfade(false)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    LoadingIndicator(
                        indicatorSize = 32.dp,
                        color = scheme.primary,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(morphLayout.contentMaxWidthFraction)
                    .graphicsLayer {
                        val scale = morphLayout.contentScale
                        scaleX = scale
                        scaleY = scale
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(morphLayout.textBlockSpacing),
            ) {
                Text(
                    text = state.displayName,
                    style = morphLayout.nameStyle,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = state.headline,
                    style = morphLayout.headlineStyle,
                    color = scheme.onPrimaryContainer.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
                state.github?.let { github ->
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(morphLayout.tagSpacing, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(morphLayout.tagSpacing),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        github.location?.takeIf { it.isNotBlank() }?.let { location ->
                            CreatorMetaChip(
                                text = location,
                                style = morphLayout.chipStyle,
                                horizontalPadding = morphLayout.chipHorizontalPadding,
                                verticalPadding = morphLayout.chipVerticalPadding,
                            )
                        }
                        github.company?.takeIf { it.isNotBlank() }?.let { company ->
                            CreatorMetaChip(
                                text = company,
                                style = morphLayout.chipStyle,
                                horizontalPadding = morphLayout.chipHorizontalPadding,
                                verticalPadding = morphLayout.chipVerticalPadding,
                            )
                        }
                        CreatorMetaChip(
                            text = "@${github.login}",
                            style = morphLayout.chipStyle,
                            horizontalPadding = morphLayout.chipHorizontalPadding,
                            verticalPadding = morphLayout.chipVerticalPadding,
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun CreatorProfileLoadingBar(
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val infinite = rememberInfiniteTransition(label = "creatorRefreshWave")
    val waveProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "creatorRefreshWaveProgress",
    )
    val barAlpha by animateFloatAsState(
        targetValue = if (loading) 1f else 0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy,
        ),
        label = "creatorLoadingBarAlpha",
    )

    if (barAlpha > 0f) {
        LinearWavyProgressIndicator(
            progress = { waveProgress },
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = barAlpha },
            color = scheme.primary,
            trackColor = scheme.surfaceContainerHighest,
        )
    }
}

@Composable
private fun CreatorMetaChip(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    horizontalPadding: Dp = 12.dp,
    verticalPadding: Dp = 6.dp,
) {
    val scheme = MaterialTheme.colorScheme
    val chipShape = MaterialShapes.Pill.toShape()
    Surface(
        modifier = modifier,
        shape = chipShape,
        color = scheme.surface.copy(alpha = 0.42f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
            style = style,
            color = scheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CreatorStatsRow(
    profile: GitHubUserProfile,
    modifier: Modifier = Modifier,
) {
    val statShape = MaterialShapes.Cookie4Sided.toShape()
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CreatorStatChip(label = "Repos", value = profile.publicRepos.toString(), shape = statShape, modifier = Modifier.weight(1f))
        CreatorStatChip(label = "Followers", value = profile.followers.toString(), shape = statShape, modifier = Modifier.weight(1f))
        CreatorStatChip(label = "Following", value = profile.following.toString(), shape = statShape, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun CreatorStatChip(
    label: String,
    value: String,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = shape,
        color = scheme.surfaceContainerHigh,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CreatorSocialCard(
    state: CreatorProfileUiState,
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val linkedInSubtitle = state.linkedIn?.headline?.takeIf { it.isNotBlank() }
        ?: "in/${CreatorLinks.LINKEDIN_HANDLE}"

    SettingsSectionCard(modifier = modifier) {
        CreatorSocialRow(
            title = "Email",
            subtitle = CreatorLinks.EMAIL,
            brand = CreatorSocialBrand.Gmail,
            onClick = { onOpenLink(CreatorLinks.EMAIL_URL) },
        )
        SettingsSectionDivider()
        CreatorSocialRow(
            title = "GitHub",
            subtitle = CreatorLinks.GITHUB_USERNAME,
            brand = CreatorSocialBrand.GitHub,
            onClick = { onOpenLink(state.github?.htmlUrl ?: CreatorLinks.GITHUB_PROFILE) },
        )
        SettingsSectionDivider()
        CreatorSocialRow(
            title = "LinkedIn",
            subtitle = linkedInSubtitle,
            brand = CreatorSocialBrand.LinkedIn,
            onClick = { onOpenLink(CreatorLinks.LINKEDIN_PROFILE) },
        )
        SettingsSectionDivider()
        CreatorSocialRow(
            title = "X",
            subtitle = state.twitterHandle,
            brand = CreatorSocialBrand.X,
            onClick = { onOpenLink(CreatorLinks.X_PROFILE) },
        )
    }
}

@Composable
private fun CreatorSocialRow(
    title: String,
    subtitle: String,
    brand: CreatorSocialBrand,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val badgeShape = MaterialShapes.Gem.toShape()
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CreatorBrandIcon(
            brand = brand,
            shape = badgeShape,
            background = scheme.surfaceContainerHighest,
            modifier = Modifier.size(40.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLargeEmphasized,
                color = scheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun CreatorWhatsAppCard(
    onOpenLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSectionCard(modifier = modifier) {
        CreatorWhatsAppRow(
            region = "India",
            number = CreatorLinks.WHATSAPP_INDIA_DISPLAY,
            onClick = { onOpenLink(CreatorLinks.WHATSAPP_INDIA_URL) },
        )
        SettingsSectionDivider()
        CreatorWhatsAppRow(
            region = "Saudi Arabia",
            number = CreatorLinks.WHATSAPP_SAUDI_DISPLAY,
            onClick = { onOpenLink(CreatorLinks.WHATSAPP_SAUDI_URL) },
        )
    }
}

@Composable
private fun CreatorWhatsAppRow(
    region: String,
    number: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CreatorWhatsAppBrandIcon(modifier = Modifier.size(40.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = region,
                style = MaterialTheme.typography.bodyLargeEmphasized,
                color = scheme.onSurface,
            )
            Text(
                text = number,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            tint = scheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun CreatorErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = MaterialShapes.Slanted.toShape()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(scheme.errorContainer)
            .clickable(onClick = onRetry)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Could not refresh profile",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onErrorContainer,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onErrorContainer,
        )
        Text(
            text = "Select to retry",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onErrorContainer,
        )
    }
}
