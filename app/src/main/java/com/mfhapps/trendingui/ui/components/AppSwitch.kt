package com.mfhapps.trendingui.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mfhapps.trendingui.ui.accessibility.LocalReduceMotion
import com.mfhapps.trendingui.ui.icons.AppSwitchIcons

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val reduceMotion = LocalReduceMotion.current
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        thumbContent = {
            AnimatedContent(
                targetState = checked,
                transitionSpec = {
                    if (reduceMotion) {
                        fadeIn() togetherWith fadeOut()
                    } else {
                        (fadeIn() + scaleIn(initialScale = 0.72f)) togetherWith
                            (fadeOut() + scaleOut(targetScale = 0.72f))
                    }
                },
                label = "app-switch-thumb",
            ) { isOn ->
                Icon(
                    imageVector = if (isOn) AppSwitchIcons.On else AppSwitchIcons.Off,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        },
        colors = SwitchDefaults.colors(),
    )
}
