package com.mfhapps.trendingui.screens.pretext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.R
import com.mfhapps.trendingui.launcher.AppLauncherIcon
import com.mfhapps.trendingui.ui.accessibility.DecorativeImage
import com.mfhapps.trendingui.ui.components.BrandMark

private const val GOOGLE_PLAY_BADGE_ASPECT = 646f / 250f

internal fun Modifier.pretextRecordingWatermarkPlacement(
    chromeVisible: Boolean,
): Modifier = navigationBarsPadding()
    .padding(
        start = PretextRecordingConfig.WATERMARK_EDGE_PADDING,
        bottom = if (chromeVisible) {
            PretextRecordingConfig.WATERMARK_DOCK_CLEARANCE
        } else {
            PretextRecordingConfig.WATERMARK_EDGE_PADDING
        },
    )

@Composable
internal fun PretextRecordingWatermark(
    launcherIcon: AppLauncherIcon,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = PretextRecordingTheme.overlayScrim.copy(alpha = 0.74f),
        contentColor = PretextRecordingTheme.overlayContent,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = PretextRecordingConfig.WATERMARK_HORIZONTAL_PADDING,
                vertical = PretextRecordingConfig.WATERMARK_VERTICAL_PADDING,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PretextRecordingConfig.WATERMARK_ITEM_SPACING),
        ) {
            BrandMark(
                size = PretextRecordingConfig.WATERMARK_BRAND_ICON_SIZE,
                animated = false,
                launcherIcon = launcherIcon,
                contentColor = PretextRecordingTheme.overlayContent,
                useMonochromeAsset = true,
            )
            Text(
                text = PretextRecordingConfig.APP_MARKETING_NAME,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Box(
                modifier = Modifier
                    .width(PretextRecordingConfig.WATERMARK_DIVIDER_WIDTH)
                    .height(PretextRecordingConfig.WATERMARK_DIVIDER_HEIGHT)
                    .background(PretextRecordingTheme.overlayContent.copy(alpha = 0.24f)),
            )
            DecorativeImage(
                painter = painterResource(R.drawable.ic_google_play_badge_en),
                modifier = Modifier
                    .height(PretextRecordingConfig.WATERMARK_PLAY_BADGE_HEIGHT)
                    .aspectRatio(GOOGLE_PLAY_BADGE_ASPECT),
            )
        }
    }
}
