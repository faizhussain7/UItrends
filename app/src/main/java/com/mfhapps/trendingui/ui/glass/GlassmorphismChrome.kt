package com.mfhapps.trendingui.ui.glass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.screens.glass.GlassSurface
import com.mfhapps.trendingui.screens.glass.GlassVariant
import com.mfhapps.trendingui.screens.glass.LocalGlassBackdrop
import com.mfhapps.trendingui.screens.glass.requireGlassBackdrop

val GlassSheetTopShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@Composable
fun GlassChromeIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val backdrop = requireGlassBackdrop()
    GlassSurface(
        backdrop = backdrop,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(44.dp)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        shape = CircleShape,
        variant = GlassVariant.Standard,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun GlassSheetDragHandle(modifier: Modifier = Modifier) {
    val backdrop = LocalGlassBackdrop.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (backdrop != null) {
            GlassSurface(
                backdrop = backdrop,
                modifier = Modifier.width(52.dp),
                shape = RoundedCornerShape(8.dp),
                variant = GlassVariant.Thin,
            ) {
                Box(Modifier.padding(vertical = 4.dp))
            }
        } else {
            Box(
                Modifier
                    .width(48.dp)
                    .padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
fun GlassGuideSheetFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val backdrop = requireGlassBackdrop()
    GlassSurface(
        backdrop = backdrop,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        variant = GlassVariant.Thick,
    ) {
        Box(Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
            content()
        }
    }
}
