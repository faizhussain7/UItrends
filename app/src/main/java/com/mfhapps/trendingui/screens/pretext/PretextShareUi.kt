@file:OptIn(ExperimentalLayoutApi::class)

package com.mfhapps.trendingui.screens.pretext

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.mfhapps.trendingui.ui.accessibility.DecorativeIcon
import kotlin.math.floor
import kotlin.math.min

internal data class QuickShareLayout(
    val buttonSizePx: Float,
    val columns: Int,
    val rows: Int,
    val visibleTargetCount: Int,
)

internal fun visibleQuickShareTargetCount(
    rowWidthPx: Float,
    buttonSizePx: Float,
    spacingPx: Float,
    reserveMoreSlot: Boolean = true,
): Int {
    if (rowWidthPx <= 0f || buttonSizePx <= 0f) return 0
    val slots = ((rowWidthPx + spacingPx) / (buttonSizePx + spacingPx))
        .toInt()
        .coerceAtLeast(1)
    val available = if (reserveMoreSlot) slots - 1 else slots
    return available.coerceAtLeast(0)
}

internal fun computeAdaptiveQuickShareLayout(
    targetCount: Int,
    widthPx: Float,
    heightPx: Float,
    spacingPx: Float,
    minButtonPx: Float,
    maxButtonPx: Float,
): QuickShareLayout {
    if (widthPx <= 0f || heightPx <= 0f) {
        return QuickShareLayout(
            buttonSizePx = minButtonPx,
            columns = 1,
            rows = 1,
            visibleTargetCount = 0,
        )
    }

    val maxColumns = ((widthPx + spacingPx) / (minButtonPx + spacingPx))
        .toInt()
        .coerceAtLeast(1)
    var best: QuickShareLayout? = null

    for (rows in 1..2) {
        for (columns in 1..maxColumns) {
            val capacity = rows * columns
            if (capacity < 2) continue

            val buttonWidth = (widthPx - spacingPx * (columns - 1)) / columns
            val buttonHeight = (heightPx - spacingPx * (rows - 1)) / rows
            val buttonSizePx = min(buttonWidth, buttonHeight).coerceIn(minButtonPx, maxButtonPx)
            val fittedColumns = floor((widthPx + spacingPx) / (buttonSizePx + spacingPx))
                .toInt()
                .coerceIn(1, columns)
            val fittedCapacity = rows * fittedColumns
            if (fittedCapacity < 2) continue

            val visibleTargetCount = (fittedCapacity - 1).coerceAtMost(targetCount)
            val candidate = QuickShareLayout(
                buttonSizePx = buttonSizePx,
                columns = fittedColumns,
                rows = rows,
                visibleTargetCount = visibleTargetCount,
            )
            val currentBest = best
            if (
                currentBest == null ||
                candidate.buttonSizePx > currentBest.buttonSizePx + 0.5f ||
                (
                    candidate.buttonSizePx >= currentBest.buttonSizePx - 0.5f &&
                        candidate.visibleTargetCount > currentBest.visibleTargetCount
                    )
            ) {
                best = candidate
            }
        }
    }

    return best ?: QuickShareLayout(
        buttonSizePx = minButtonPx,
        columns = 1,
        rows = 1,
        visibleTargetCount = visibleQuickShareTargetCount(
            rowWidthPx = widthPx,
            buttonSizePx = minButtonPx,
            spacingPx = spacingPx,
        ).coerceAtMost(targetCount),
    )
}

@Composable
internal fun PretextQuickShareGrid(
    targets: List<PretextShareTarget>,
    videoUri: Uri,
    onOpenShareSheet: () -> Unit,
    style: PretextShareSurfaceStyle,
    modifier: Modifier = Modifier,
    onInteraction: () -> Unit = {},
) {
    val context = LocalContext.current
    val colors = style.colors()
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val spacing = PretextRecordingConfig.SHARE_ICON_BUTTON_SPACING
        val spacingPx = with(density) { spacing.toPx() }
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val minButtonPx = with(density) { PretextRecordingConfig.SHARE_ICON_BUTTON_MIN_SIZE.toPx() }

        val layout = remember(targets.size, widthPx, heightPx) {
            computeAdaptiveQuickShareLayout(
                targetCount = targets.size,
                widthPx = widthPx,
                heightPx = heightPx,
                spacingPx = spacingPx,
                minButtonPx = minButtonPx,
                maxButtonPx = heightPx.coerceAtLeast(minButtonPx),
            )
        }
        val buttonSize = with(density) { layout.buttonSizePx.toDp() }
        val shareButtons = rememberShareButtons(
            context = context,
            targets = targets,
            videoUri = videoUri,
            visibleCount = layout.visibleTargetCount,
            onOpenShareSheet = onOpenShareSheet,
            onInteraction = onInteraction,
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(
                space = spacing,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            shareButtons.chunked(layout.columns.coerceAtLeast(1)).forEach { rowButtons ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    rowButtons.forEach { button ->
                        PretextShareIconButton(
                            icon = button.icon,
                            label = button.label,
                            colors = colors,
                            buttonSize = buttonSize,
                            onClick = button.onClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PretextQuickShareRow(
    targets: List<PretextShareTarget>,
    videoUri: Uri,
    onOpenShareSheet: () -> Unit,
    style: PretextShareSurfaceStyle,
    modifier: Modifier = Modifier,
    onInteraction: () -> Unit = {},
) {
    val context = LocalContext.current
    val colors = style.colors()
    val spacing = PretextRecordingConfig.SHARE_ICON_BUTTON_SPACING
    val buttonSize = PretextRecordingConfig.SHARE_ICON_BUTTON_SIZE

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val spacingPx = with(density) { spacing.toPx() }
        val buttonSizePx = with(density) { buttonSize.toPx() }
        val rowWidthPx = with(density) { maxWidth.toPx() }
        val visibleCount = visibleQuickShareTargetCount(
            rowWidthPx = rowWidthPx,
            buttonSizePx = buttonSizePx,
            spacingPx = spacingPx,
        ).coerceAtMost(targets.size)

        val shareButtons = rememberShareButtons(
            context = context,
            targets = targets,
            videoUri = videoUri,
            visibleCount = visibleCount,
            onOpenShareSheet = onOpenShareSheet,
            onInteraction = onInteraction,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            shareButtons.forEach { button ->
                PretextShareIconButton(
                    icon = button.icon,
                    label = button.label,
                    colors = colors,
                    buttonSize = buttonSize,
                    onClick = button.onClick,
                )
            }
        }
    }
}

@Composable
private fun rememberShareButtons(
    context: android.content.Context,
    targets: List<PretextShareTarget>,
    videoUri: Uri,
    visibleCount: Int,
    onOpenShareSheet: () -> Unit,
    onInteraction: () -> Unit,
): List<ShareButtonSpec> {
    return remember(context, targets, videoUri, visibleCount, onOpenShareSheet, onInteraction) {
        buildList {
            targets.take(visibleCount).forEach { target ->
                add(
                    ShareButtonSpec(
                        icon = resolveShareAppIcon(context, target.packageName!!),
                        label = target.label,
                        onClick = {
                            onInteraction()
                            shareRecordingToTarget(context, videoUri, target)
                        },
                    ),
                )
            }
            add(
                ShareButtonSpec(
                    icon = null,
                    label = PretextMoreShareTarget.label,
                    onClick = {
                        onInteraction()
                        onOpenShareSheet()
                    },
                ),
            )
        }
    }
}

private data class ShareButtonSpec(
    val icon: Drawable?,
    val label: String,
    val onClick: () -> Unit,
)

@Composable
internal fun PretextShareSheetChip(
    target: PretextShareTarget,
    icon: Drawable?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PretextShareSurfaceStyle.Sheet.colors()
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = colors.container,
        contentColor = colors.content,
    ) {
        PretextShareTargetRow(
            label = target.label,
            icon = icon,
            colors = colors,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
internal fun PretextShareTargetRow(
    label: String,
    icon: Drawable?,
    colors: PretextShareColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon != null) {
            Image(
                bitmap = icon.toBitmap(
                    PretextRecordingConfig.SHARE_APP_ICON_BITMAP_PX,
                    PretextRecordingConfig.SHARE_APP_ICON_BITMAP_PX,
                ).asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape),
            )
        } else {
            DecorativeIcon(
                Icons.Outlined.IosShare,
                tint = colors.accent,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PretextShareIconButton(
    icon: Drawable?,
    label: String,
    colors: PretextShareColors,
    buttonSize: Dp,
    onClick: () -> Unit,
) {
    val iconSize = buttonSize * PretextRecordingConfig.SHARE_ICON_INNER_RATIO
    Surface(
        onClick = onClick,
        modifier = Modifier.size(buttonSize),
        shape = CircleShape,
        color = colors.container,
        contentColor = colors.content,
        tonalElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Image(
                    bitmap = icon.toBitmap(
                        PretextRecordingConfig.SHARE_APP_ICON_BITMAP_PX,
                        PretextRecordingConfig.SHARE_APP_ICON_BITMAP_PX,
                    ).asImageBitmap(),
                    contentDescription = label,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape),
                )
            } else {
                DecorativeIcon(
                    Icons.Outlined.IosShare,
                    tint = colors.accent,
                    modifier = Modifier.size(iconSize * 0.82f),
                )
            }
        }
    }
}

@Composable
internal fun PretextShareSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}
