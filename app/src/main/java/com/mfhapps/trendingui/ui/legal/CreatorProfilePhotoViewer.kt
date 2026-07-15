@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.mfhapps.trendingui.ui.legal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.mfhapps.trendingui.legal.CREATOR_PROFILE_PHOTO_KEY
import com.mfhapps.trendingui.navigation.demoSharedElement
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.components.ExpressiveMorphTempo
import com.mfhapps.trendingui.ui.components.ExpressiveShapeCatalogTier
import com.mfhapps.trendingui.ui.components.FilledTonalIconButton
import com.mfhapps.trendingui.ui.components.LocalModalBackdropStyle
import com.mfhapps.trendingui.ui.components.ZoomableImage
import com.mfhapps.trendingui.ui.components.activeAppHazeState
import com.mfhapps.trendingui.ui.components.modalBackdropHazeEffect
import com.mfhapps.trendingui.ui.components.rememberExpressiveMorphLoopShape
import com.mfhapps.trendingui.ui.motion.expressiveEffectsSpec
import kotlinx.coroutines.delay

@Composable
private fun rememberFullscreenPhotoFrameShape(
    expressiveShapesEnabled: Boolean,
    reduceMotion: Boolean,
) = rememberExpressiveMorphLoopShape(
    enabled = expressiveShapesEnabled && !reduceMotion,
    tier = ExpressiveShapeCatalogTier.Fullscreen,
    tempo = ExpressiveMorphTempo.Fullscreen,
    introFrom = MaterialShapes.Square,
    introMillis = 720,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CreatorProfilePhotoFullscreen(
    photoUrl: String,
    displayName: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val reduceMotion = LocalReduceMotion.current
    val hazeState = activeAppHazeState()
    val backdropStyle = LocalModalBackdropStyle.current
    var expressiveShapesEnabled by remember { mutableStateOf(reduceMotion) }
    val chromeAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = expressiveEffectsSpec(),
        label = "fullscreenChromeAlpha",
    )
    val frameShape = rememberFullscreenPhotoFrameShape(
        expressiveShapesEnabled = expressiveShapesEnabled,
        reduceMotion = reduceMotion,
    )
    val dismissInteraction = remember { MutableInteractionSource() }

    LaunchedEffect(reduceMotion) {
        if (!reduceMotion) {
            delay(460)
            expressiveShapesEnabled = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (hazeState != null) {
                        Modifier.modalBackdropHazeEffect(
                            state = hazeState,
                            style = backdropStyle,
                        )
                    } else {
                        Modifier
                    },
                )
                .clickable(
                    indication = null,
                    interactionSource = dismissInteraction,
                    onClick = onDismiss,
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .graphicsLayer { alpha = chromeAlpha },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.72f)
                    .clip(frameShape),
            ) {
                ZoomableImage(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(photoUrl)
                            .scale(Scale.FIT)
                            .crossfade(false)
                            .build(),
                        contentDescription = "Full profile photo of $displayName",
                        modifier = Modifier
                            .fillMaxSize()
                            .demoSharedElement(
                                sharedTransitionScope = sharedTransitionScope,
                                key = CREATOR_PROFILE_PHOTO_KEY,
                                animatedVisibilityScope = animatedVisibilityScope,
                            ),
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
        animatedVisibilityScope: AnimatedVisibilityScope,
        onPhotoClick: () -> Unit,
    ) -> Unit,
) {
    val effects = expressiveEffectsSpec<Float>()
    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = photoExpanded,
            transitionSpec = {
                fadeIn(animationSpec = effects) togetherWith fadeOut(animationSpec = effects)
            },
            label = "creator-photo-expand",
        ) { expanded ->
            if (expanded) {
                CreatorProfilePhotoFullscreen(
                    photoUrl = photoUrlFullscreen,
                    displayName = displayName,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                    onDismiss = { onPhotoExpandedChange(false) },
                )
            } else {
                listContent(
                    this@SharedTransitionLayout,
                    this@AnimatedContent,
                ) { onPhotoExpandedChange(true) }
            }
        }
    }
}
