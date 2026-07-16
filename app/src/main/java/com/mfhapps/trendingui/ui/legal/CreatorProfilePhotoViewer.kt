package com.mfhapps.trendingui.ui.legal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.mfhapps.trendingui.legal.CREATOR_PROFILE_PHOTO_KEY
import com.mfhapps.trendingui.navigation.photoSharedElementManaged
import com.mfhapps.trendingui.ui.components.ExpressiveMorphTempo
import com.mfhapps.trendingui.ui.components.ExpressiveShapeCatalogTier
import com.mfhapps.trendingui.ui.components.FilledTonalIconButton
import com.mfhapps.trendingui.ui.components.LocalModalBackdropStyle
import com.mfhapps.trendingui.ui.components.ZoomableImage
import com.mfhapps.trendingui.ui.components.activeAppHazeState
import com.mfhapps.trendingui.ui.components.modalBackdropHazeEffect
import com.mfhapps.trendingui.ui.components.rememberExpressiveAccentShape
import com.mfhapps.trendingui.ui.motion.expressiveEffectsSpec

internal const val CreatorPhotoShapeSeed = 17

private val FullscreenPhotoShape = RoundedCornerShape(28.dp)

@Composable
internal fun rememberCreatorPhotoMorphShape(
    enabled: Boolean = true,
) = rememberExpressiveAccentShape(
    seed = CreatorPhotoShapeSeed,
    tier = ExpressiveShapeCatalogTier.Contained,
    tempo = ExpressiveMorphTempo(
        segmentMillis = 3_000,
        dwellFraction = 0.18f,
    ),
    enabled = enabled,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CreatorProfilePhotoFullscreen(
    photoUrl: String,
    displayName: String,
    sharedTransitionScope: SharedTransitionScope,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val hazeState = activeAppHazeState()
    val backdropStyle = LocalModalBackdropStyle.current
    val dismissInteraction = remember { MutableInteractionSource() }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (hazeState != null) {
                        Modifier.modalBackdropHazeEffect(
                            state = hazeState,
                            style = backdropStyle,
                            collapsedHeader = true,
                        )
                    } else {
                        Modifier.background(scheme.scrim.copy(alpha = 0.72f))
                    },
                )
                .background(scheme.scrim.copy(alpha = 0.22f))
                .clickable(
                    indication = null,
                    interactionSource = dismissInteraction,
                    onClick = onDismiss,
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.72f)
                    .photoSharedElementManaged(
                        sharedTransitionScope = sharedTransitionScope,
                        key = CREATOR_PROFILE_PHOTO_KEY,
                        visible = true,
                    )
                    .clip(FullscreenPhotoShape)
                    .background(scheme.surfaceContainerHighest.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                ZoomableImage(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUrl)
                            .scale(Scale.FIT)
                            .crossfade(false)
                            .build(),
                        contentDescription = "Full profile photo of $displayName",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            FilledTonalIconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close photo",
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CreatorProfilePhotoTransitionHost(
    photoExpanded: Boolean,
    photoUrlFullscreen: String,
    displayName: String,
    onPhotoExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    listContent: @Composable (
        sharedTransitionScope: SharedTransitionScope,
        photoExpanded: Boolean,
        onPhotoClick: () -> Unit,
    ) -> Unit,
) {
    val effects = expressiveEffectsSpec<Float>()

    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            listContent(
                this@SharedTransitionLayout,
                photoExpanded,
            ) { onPhotoExpandedChange(true) }

            AnimatedVisibility(
                visible = photoExpanded,
                enter = fadeIn(animationSpec = effects),
                exit = fadeOut(animationSpec = effects),
                modifier = Modifier.fillMaxSize(),
            ) {
                CreatorProfilePhotoFullscreen(
                    photoUrl = photoUrlFullscreen,
                    displayName = displayName,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    onDismiss = { onPhotoExpandedChange(false) },
                )
            }
        }
    }
}
