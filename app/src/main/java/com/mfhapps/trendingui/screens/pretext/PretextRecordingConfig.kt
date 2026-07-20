package com.mfhapps.trendingui.screens.pretext

import androidx.compose.ui.unit.dp

internal object PretextRecordingConfig {
    const val MAX_LONG_EDGE_PX_DEFAULT = 1080
    const val TARGET_FPS_DEFAULT = 30
    const val VIDEO_BITRATE_DEFAULT = 8_000_000
    const val ASPECT_WIDTH = 9
    const val ASPECT_HEIGHT = 16
    const val ASPECT_RATIO = ASPECT_WIDTH.toFloat() / ASPECT_HEIGHT.toFloat()
    const val ASPECT_LABEL = "9:16"

    val CARD_THUMB_WIDTH = 92.dp
    val CARD_THUMB_HEIGHT = CARD_THUMB_WIDTH / ASPECT_RATIO
    val CARD_SECTION_SPACING = 8.dp
    val SHARE_ICON_BUTTON_SIZE = 36.dp
    val SHARE_ICON_BUTTON_MIN_SIZE = 28.dp
    val SHARE_ICON_BUTTON_SPACING = 6.dp
    const val SHARE_ICON_INNER_RATIO = 22f / 36f
    val SHARE_APP_ICON_BITMAP_PX = 96

    const val PLAYBACK_SKIP_MS = 10_000L
    const val PLAYBACK_POSITION_POLL_MS = 200L
    const val RECORDING_ELAPSED_TICK_MS = 250L

    const val OVERLAY_HIDE_CAMERA_MS = 2_600L
    const val OVERLAY_HIDE_RECORDING_MS = 2_000L
    const val OVERLAY_HIDE_PLAYING_MS = 3_000L
    const val OVERLAY_HIDE_PAUSED_MS = 5_000L

    const val PLAY_STORE_PACKAGE = "com.mfhapps.trendingui"
    const val APP_MARKETING_NAME = "UITrends"

    val WATERMARK_BRAND_ICON_SIZE = 15.dp
    val WATERMARK_PLAY_BADGE_HEIGHT = 20.dp
    val WATERMARK_DIVIDER_WIDTH = 1.dp
    val WATERMARK_DIVIDER_HEIGHT = 12.dp
    val WATERMARK_HORIZONTAL_PADDING = 8.dp
    val WATERMARK_VERTICAL_PADDING = 5.dp
    val WATERMARK_ITEM_SPACING = 5.dp
    val WATERMARK_EDGE_PADDING = 12.dp
    val WATERMARK_DOCK_CLEARANCE = 100.dp
}
