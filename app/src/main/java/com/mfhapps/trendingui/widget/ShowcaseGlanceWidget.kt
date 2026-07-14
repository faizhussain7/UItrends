package com.mfhapps.trendingui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider as GlanceColorProvider
import com.mfhapps.trendingui.MainActivity
import com.mfhapps.trendingui.R
import com.mfhapps.trendingui.deeplink.AppDeepLinks

class ShowcaseGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                PretextEngineWidget()
            }
        }
    }
}

@Composable
private fun PretextEngineWidget() {
    val context = LocalContext.current
    val colors = GlanceTheme.colors

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_panel_solid)),
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_expressive_pattern),
            contentDescription = null,
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(14.dp),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_brand_logo_monochrome),
                    contentDescription = null,
                    modifier = GlanceModifier.size(22.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(colors.primary),
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = context.getString(R.string.app_name),
                        style = TextStyle(
                            color = colors.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                    )
                    Text(
                        text = context.getString(R.string.widget_showcase_header),
                        style = TextStyle(
                            color = colors.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        maxLines = 1,
                    )
                }
            }
            Spacer(modifier = GlanceModifier.height(10.dp))

            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .background(ImageProvider(R.drawable.widget_hero_pretext_bg))
                    .clickable(
                        actionStartActivity(
                            openDestination(context, AppDeepLinks.DEST_PRETEXT),
                        ),
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_demo_pretext),
                    contentDescription = null,
                    modifier = GlanceModifier.size(22.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(WidgetColors.onHero),
                )
                Text(
                    text = context.getString(R.string.widget_pretext_title),
                    style = TextStyle(
                        color = WidgetColors.onHero,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.padding(top = 10.dp),
                )
                Text(
                    text = context.getString(R.string.widget_pretext_body),
                    style = TextStyle(
                        color = WidgetColors.onHeroMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    maxLines = 2,
                    modifier = GlanceModifier.padding(top = 4.dp),
                )
                Spacer(modifier = GlanceModifier.height(12.dp))
                Box(
                    modifier = GlanceModifier
                        .background(ImageProvider(R.drawable.widget_hero_cta_bg))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = context.getString(R.string.widget_pretext_cta),
                        style = TextStyle(
                            color = WidgetColors.onHero,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                    )
                }
            }

            Text(
                text = context.getString(R.string.widget_more_label),
                style = TextStyle(
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
                modifier = GlanceModifier.padding(top = 10.dp, bottom = 6.dp),
            )

            Row(modifier = GlanceModifier.fillMaxWidth().height(44.dp)) {
                DestinationChip(
                    label = context.getString(R.string.shortcut_glass_short),
                    iconRes = R.drawable.ic_demo_glass,
                    backgroundRes = R.drawable.widget_chip_glass_bg,
                    textColor = WidgetColors.slate,
                    destination = AppDeepLinks.DEST_GLASS,
                    modifier = GlanceModifier.defaultWeight(),
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                DestinationChip(
                    label = context.getString(R.string.shortcut_masonry_short),
                    iconRes = R.drawable.ic_demo_masonry,
                    backgroundRes = R.drawable.widget_chip_masonry_bg,
                    textColor = WidgetColors.honey,
                    destination = AppDeepLinks.DEST_MASONRY,
                    modifier = GlanceModifier.defaultWeight(),
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                DestinationChip(
                    label = context.getString(R.string.shortcut_settings_short),
                    iconRes = R.drawable.ic_demo_settings,
                    backgroundRes = R.drawable.widget_chip_settings_bg,
                    textColor = WidgetColors.neutral,
                    destination = AppDeepLinks.DEST_SETTINGS,
                    modifier = GlanceModifier.defaultWeight(),
                )
            }
        }
    }
}

@Composable
private fun DestinationChip(
    label: String,
    iconRes: Int,
    backgroundRes: Int,
    textColor: GlanceColorProvider,
    destination: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .height(44.dp)
            .background(ImageProvider(backgroundRes))
            .clickable(actionStartActivity(openDestination(context, destination))),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = null,
                modifier = GlanceModifier.size(16.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(textColor),
            )
            Spacer(modifier = GlanceModifier.width(5.dp))
            Text(
                text = label,
                style = TextStyle(
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
        }
    }
}

private object WidgetColors {
    val onHero = ColorProvider(day = Color.White, night = Color.White)
    val onHeroMuted = ColorProvider(day = Color(0xE8FFFFFF), night = Color(0xE8FFFFFF))
    val slate = ColorProvider(day = Color(0xFF0A1929), night = Color(0xFFD9E2EC))
    val honey = ColorProvider(day = Color(0xFF261A00), night = Color(0xFFFFE8A3))
    val neutral = ColorProvider(day = Color(0xFF1F1C1A), night = Color(0xFFF5F0EB))
}

private fun openDestination(context: Context, destination: String): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = AppDeepLinks.ACTION_OPEN_DESTINATION
        putExtra(AppDeepLinks.EXTRA_DESTINATION, destination)
        flags = AppDeepLinks.intentFlags()
    }

class ShowcaseGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ShowcaseGlanceWidget()
}
