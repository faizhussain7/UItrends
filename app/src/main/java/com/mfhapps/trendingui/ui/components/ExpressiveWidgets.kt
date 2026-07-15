package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ContainedLoadingIndicator as MaterialContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator as MaterialLoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ButtonGroup(
    modifier: Modifier = Modifier,
    content: @Composable ButtonGroupScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalAlignment = Alignment.CenterVertically,
        content = { ButtonGroupScopeImpl(this).content() },
    )
}

interface ButtonGroupScope {
    @Composable
    fun ToggleButton(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable RowScope.() -> Unit,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private class ButtonGroupScopeImpl(
    private val rowScope: RowScope,
) : ButtonGroupScope, RowScope by rowScope {
    @Composable
    override fun ToggleButton(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier,
        content: @Composable RowScope.() -> Unit,
    ) {
        ToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            shapes = ToggleButtonDefaults.shapes(),
            colors = ToggleButtonDefaults.toggleButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    indicatorSize: Dp = 40.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    MaterialLoadingIndicator(
        modifier = modifier.size(indicatorSize),
        color = color,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContainedLoadingIndicator(
    modifier: Modifier = Modifier,
) {
    MaterialContainedLoadingIndicator(
        modifier = modifier.size(56.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        indicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}
