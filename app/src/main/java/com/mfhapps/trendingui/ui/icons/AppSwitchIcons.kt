package com.mfhapps.trendingui.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppSwitchIcons {
    val On: ImageVector
        get() = onMark

    val Off: ImageVector
        get() = offMark
}

private var _onMark: ImageVector? = null

private val onMark: ImageVector
    get() = _onMark ?: ImageVector.Builder(
        name = "AppSwitch.On",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(12f, 2.5f)
            lineTo(13.7f, 9.2f)
            lineTo(20.5f, 6.2f)
            lineTo(16.3f, 11.7f)
            lineTo(21.5f, 12f)
            lineTo(16.3f, 12.3f)
            lineTo(20.5f, 17.8f)
            lineTo(13.7f, 14.8f)
            lineTo(12f, 21.5f)
            lineTo(10.3f, 14.8f)
            lineTo(3.5f, 17.8f)
            lineTo(7.7f, 12.3f)
            lineTo(2.5f, 12f)
            lineTo(7.7f, 11.7f)
            lineTo(3.5f, 6.2f)
            lineTo(10.3f, 9.2f)
            close()
        }
    }.build().also { _onMark = it }

private var _offMark: ImageVector? = null

private val offMark: ImageVector
    get() = _offMark ?: ImageVector.Builder(
        name = "AppSwitch.Off",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(12f, 2.4f)
            lineTo(19.2f, 5.4f)
            lineTo(21.6f, 12f)
            lineTo(19.2f, 18.6f)
            lineTo(12f, 21.6f)
            lineTo(4.8f, 18.6f)
            lineTo(2.4f, 12f)
            lineTo(4.8f, 5.4f)
            close()
            moveTo(12f, 5.2f)
            lineTo(6.7f, 7.4f)
            lineTo(4.95f, 12f)
            lineTo(6.7f, 16.6f)
            lineTo(12f, 18.8f)
            lineTo(17.3f, 16.6f)
            lineTo(19.05f, 12f)
            lineTo(17.3f, 7.4f)
            close()
        }
    }.build().also { _offMark = it }
