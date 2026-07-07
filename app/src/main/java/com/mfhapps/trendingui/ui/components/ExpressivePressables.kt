@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.largeIncreased,
    morphRest: RoundedPolygon = ExpressiveMorphDefaults.buttonRest,
    morphPressed: RoundedPolygon = ExpressiveMorphDefaults.buttonPressed,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val morph = rememberExpressiveMorphPress(
        interactionSource = interaction,
        rest = morphRest,
        pressed = morphPressed,
    )
    val surfaceShape = rememberDynamicPressShape(
        displayShape = shape,
        morph = morph,
        morphRest = morphRest,
        morphPressed = morphPressed,
    )
    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minWidth = ButtonDefaults.MinWidth, minHeight = ButtonDefaults.MinHeight)
            .scale(morph.scale),
        shape = surfaceShape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = if (enabled) 1.dp else 0.dp,
        shadowElevation = if (enabled && elevation != null) 2.dp else 0.dp,
        border = border,
        interactionSource = interaction,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                Modifier.padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.largeIncreased,
    morphRest: RoundedPolygon = ExpressiveMorphDefaults.buttonRest,
    morphPressed: RoundedPolygon = ExpressiveMorphDefaults.buttonPressed,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        morphRest = morphRest,
        morphPressed = morphPressed,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.largeIncreased,
    morphRest: RoundedPolygon = ExpressiveMorphDefaults.buttonRest,
    morphPressed: RoundedPolygon = ExpressiveMorphDefaults.buttonPressed,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        morphRest = morphRest,
        morphPressed = morphPressed,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.largeIncreased,
    morphRest: RoundedPolygon = ExpressiveMorphDefaults.buttonRest,
    morphPressed: RoundedPolygon = ExpressiveMorphDefaults.buttonPressed,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        morphRest = morphRest,
        morphPressed = morphPressed,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun ElevatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.largeIncreased,
    morphRest: RoundedPolygon = ExpressiveMorphDefaults.buttonRest,
    morphPressed: RoundedPolygon = ExpressiveMorphDefaults.buttonPressed,
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors(),
    elevation: ButtonElevation? = ButtonDefaults.elevatedButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        morphRest = morphRest,
        morphPressed = morphPressed,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    morphRest: RoundedPolygon = ExpressiveMorphDefaults.iconRest,
    morphPressed: RoundedPolygon = ExpressiveMorphDefaults.iconPressed,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val morph = rememberExpressiveMorphPress(
        interactionSource = interaction,
        rest = morphRest,
        pressed = morphPressed,
    )
    val surfaceShape = rememberDynamicPressShape(
        displayShape = shape,
        morph = morph,
        morphRest = morphRest,
        morphPressed = morphPressed,
    )
    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(48.dp)
            .scale(morph.scale),
        shape = surfaceShape,
        color = containerColor,
        contentColor = contentColor,
        interactionSource = interaction,
    ) {
        Box(contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

@Composable
fun FilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    morphRest: RoundedPolygon = ExpressiveMorphDefaults.iconRest,
    morphPressed: RoundedPolygon = ExpressiveMorphDefaults.iconPressed,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        morphRest = morphRest,
        morphPressed = morphPressed,
        colors = colors,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun FilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    morphRest: RoundedPolygon = ExpressiveMorphDefaults.iconRest,
    morphPressed: RoundedPolygon = ExpressiveMorphDefaults.iconPressed,
    colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        morphRest = morphRest,
        morphPressed = morphPressed,
        colors = colors,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.largeIncreased,
    morphRest: RoundedPolygon = ExpressiveMorphDefaults.buttonRest,
    morphPressed: RoundedPolygon = ExpressiveMorphDefaults.buttonPressed,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val morph = rememberExpressiveMorphPress(
        interactionSource = interaction,
        rest = morphRest,
        pressed = morphPressed,
    )
    val surfaceShape = rememberDynamicPressShape(
        displayShape = shape,
        morph = morph,
        morphRest = morphRest,
        morphPressed = morphPressed,
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .scale(morph.scale),
        shape = surfaceShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
        interactionSource = interaction,
    ) {
        Box(contentAlignment = Alignment.Center) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

@Composable
fun ExpressiveMorphIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 52.dp,
    containerColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    rest: RoundedPolygon = ExpressiveMorphDefaults.iconRest,
    pressed: RoundedPolygon = ExpressiveMorphDefaults.iconPressed,
) {
    val interaction = remember { MutableInteractionSource() }
    val morph = rememberExpressiveMorphPress(
        interactionSource = interaction,
        rest = rest,
        pressed = pressed,
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
        modifier = modifier
            .size(size)
            .scale(morph.scale),
        shape = surfaceShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 4.dp,
        interactionSource = interaction,
    ) {
        Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = LocalContentColor.current,
            )
        }
    }
}
