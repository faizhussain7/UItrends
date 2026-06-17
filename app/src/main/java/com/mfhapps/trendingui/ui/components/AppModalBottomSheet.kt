package com.mfhapps.trendingui.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.brutal.BrutalSheetDragHandle
import com.mfhapps.trendingui.ui.brutal.BrutalZeroCorner
import com.mfhapps.trendingui.ui.detail.DetailChromeStyle
import com.mfhapps.trendingui.ui.neumorphism.NeuSheetDragHandle
import com.mfhapps.trendingui.ui.neumorphism.NeuSheetTopShape
import com.mfhapps.trendingui.ui.glass.GlassSheetDragHandle
import com.mfhapps.trendingui.ui.glass.GlassSheetTopShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberAppModalSheetState(
    confirmValueChange: (SheetValue) -> Boolean = { true },
): SheetState =
    rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        confirmValueChange = confirmValueChange,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberAppModalSheetState(),
    scrollable: Boolean = true,
    chromeStyle: DetailChromeStyle = DetailChromeStyle.Default,
    content: @Composable ColumnScope.() -> Unit,
) {
    val brutal = chromeStyle == DetailChromeStyle.NeoBrutal
    val neu = chromeStyle == DetailChromeStyle.Neumorphism
    val glass = chromeStyle == DetailChromeStyle.Glass
    val backdropBlur = LocalModalBackdropBlurEnabled.current
    val overlayVisible by remember {
        derivedStateOf {
            sheetState.targetValue != SheetValue.Hidden ||
                sheetState.currentValue != SheetValue.Hidden
        }
    }
    RegisterModalBackdrop(visible = overlayVisible, onDismiss = onDismiss)
    val sheetShape: Shape = when {
        brutal -> BrutalZeroCorner
        neu -> NeuSheetTopShape
        glass -> GlassSheetTopShape
        else -> BottomSheetDefaults.ExpandedShape
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        shape = sheetShape,
        containerColor = when {
            glass -> Color.Transparent
            neu -> MaterialTheme.colorScheme.surfaceContainerLow
            else -> MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (brutal || neu || glass) 0.dp else BottomSheetDefaults.Elevation,
        scrimColor = when {
            backdropBlur -> Color.Transparent
            glass -> MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
            else -> BottomSheetDefaults.ScrimColor
        },
        dragHandle = {
            when {
                brutal -> BrutalSheetDragHandle()
                neu -> NeuSheetDragHandle()
                glass -> GlassSheetDragHandle()
                else -> BottomSheetDefaults.DragHandle()
            }
        },
    ) {
        val columnModifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .then(
                if (brutal || neu || glass) Modifier.padding(top = 4.dp) else Modifier,
            )
            .then(
                if (scrollable) {
                    Modifier.verticalScroll(rememberScrollState())
                } else {
                    Modifier
                },
            )

        Column(modifier = columnModifier, content = content)
    }
}
