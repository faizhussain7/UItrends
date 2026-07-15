package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveMorphSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    rest: androidx.graphics.shapes.RoundedPolygon = ExpressiveMorphDefaults.buttonRest,
    pressed: androidx.graphics.shapes.RoundedPolygon = ExpressiveMorphDefaults.buttonPressed,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    tonalElevation: Dp = 2.dp,
    shadowElevation: Dp = 0.dp,
    hapticOnPress: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val morph = rememberExpressiveMorphPress(
        interactionSource = interaction,
        rest = rest,
        pressed = pressed,
        hapticOnPress = hapticOnPress,
    )
    val displayShape = rest.toShape()
    val surfaceShape = rememberDynamicPressShape(
        displayShape = displayShape,
        morph = morph,
        morphRest = rest,
        morphPressed = pressed,
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.scale(morph.scale),
        shape = surfaceShape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        interactionSource = interaction,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressivePolygonIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    polygon: androidx.graphics.shapes.RoundedPolygon = MaterialShapes.Clover4Leaf,
    ambientLoop: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconSize: Dp = 22.dp,
) {
    val loopShape = rememberExpressiveBadgeShape(seed = System.identityHashCode(polygon))
    val shape = if (ambientLoop) loopShape else polygon.toShape()
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = contentColor,
            )
        }
    }
}
