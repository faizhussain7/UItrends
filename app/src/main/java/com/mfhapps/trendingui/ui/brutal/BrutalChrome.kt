package com.mfhapps.trendingui.ui.brutal

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion

@Composable
fun BrutalChromeIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fill: Color = MaterialTheme.colorScheme.primaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: androidx.compose.ui.unit.Dp = 44.dp,
) {
    val ink = MaterialTheme.colorScheme.onSurface
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val pressAnim by animateFloatAsState(
        targetValue = if (pressed) 2.5f else 0f,
        animationSpec = if (reduceMotion) snap() else spring(stiffness = Spring.StiffnessHigh),
        label = "brutalChromePress",
    )

    Box(
        modifier = modifier
            .size(size)
            .minimumInteractiveComponentSize(),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .offset(3.dp, 3.dp)
                .background(ink),
        )
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationX = pressAnim
                    translationY = pressAnim
                }
                .border(3.dp, ink)
                .background(fill)
                .clickable(interactionSource = interaction, indication = null) {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                }
                .semantics {
                    role = Role.Button
                    this@semantics.contentDescription = contentDescription
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
fun BrutalSheetDragHandle(modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .offset(2.dp, 2.dp)
                .width(48.dp)
                .height(6.dp)
                .background(ink),
        )
        Box(
            Modifier
                .width(48.dp)
                .height(6.dp)
                .border(2.dp, ink)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        )
    }
}
