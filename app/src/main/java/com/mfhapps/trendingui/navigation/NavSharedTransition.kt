package com.mfhapps.trendingui.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DemoSharedIcon(
    contentKey: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = LocalSharedTransitionScope.current,
    animatedVisibilityScope: AnimatedVisibilityScope? = LocalNavAnimatedVisibilityScope.current,
) {
    val scope = sharedTransitionScope
    val visibilityScope = animatedVisibilityScope
    Surface(
        modifier = modifier
            .size(42.dp)
            .then(
                if (scope != null && visibilityScope != null) {
                    Modifier.demoSharedElement(
                        sharedTransitionScope = scope,
                        key = contentKey,
                        animatedVisibilityScope = visibilityScope,
                    )
                } else {
                    Modifier
                },
            ),
        shape = RoundedCornerShape(13.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            DecorativeIcon(
                icon,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
