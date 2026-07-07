package com.mfhapps.trendingui.screens.glass

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.demo.DemoAnimatedSection
import com.mfhapps.trendingui.ui.detail.DemoCollapsingScrollScaffold
import com.mfhapps.trendingui.ui.detail.DetailChromeStyle
import com.mfhapps.trendingui.ui.guide.DemoTrendGuide

private const val GlassScreenTitle = "Glassmorphism"
private const val GlassScreenSubtitle = "Frosted layers · blurred backdrop · theme-tinted glass"

@Composable
fun GlassmorphismScreen(
    onNavigateBack: () -> Unit = {},
    guide: DemoTrendGuide? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val backdrop = LocalGlassBackdrop.current

    DemoCollapsingScrollScaffold(
        title = GlassScreenTitle,
        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && backdrop?.hazeEnabled == true) {
            "Chrome and sheets sample the same live frosted backdrop"
        } else {
            GlassScreenSubtitle
        },
        chromeStyle = DetailChromeStyle.Glass,
        onNavigateBack = onNavigateBack,
        guide = guide,
        verticalSpacing = 28.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            DemoAnimatedSection(index = 1) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassSection(
                        title = "Frosted panels",
                        body = "Blur samples the full-screen backdrop behind this scroll area.",
                    )
                    GlassPanel(
                        title = "Primary glass",
                        body = "Uses surface and on-surface colors from your Material theme.",
                    )
                    GlassPanel(
                        title = "Tinted glass",
                        body = "primaryContainer tint for branded overlays.",
                        variant = GlassVariant.Tinted,
                    )
                }
            }

            DemoAnimatedSection(index = 2) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassSection(
                        title = "Thin vs thick",
                        body = "One rim, one shape — adjust frost strength only.",
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        GlassPanel(
                            title = "Thin",
                            body = "Light frost.",
                            modifier = Modifier.weight(1f),
                            variant = GlassVariant.Thin,
                        )
                        GlassPanel(
                            title = "Thick",
                            body = "Stronger blur + scrim.",
                            modifier = Modifier.weight(1f),
                            variant = GlassVariant.Thick,
                        )
                    }
                }
            }

            DemoAnimatedSection(index = 3) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassSection(title = "Glass dock", body = "Floating nav on frosted glass.")
                    GlassDock()
                }
            }

            DemoAnimatedSection(index = 4) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassSection(
                        title = "Media overlay",
                        body = "Artwork uses theme gradient; controls use thick glass.",
                    )
                    GlassMediaCard()
                }
            }

            DemoAnimatedSection(index = 5) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassSection(title = "Metrics", body = "Compact thin-glass chips.")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        GlassMetricChip(
                            label = "Sessions",
                            value = "24.8K",
                            modifier = Modifier.weight(1f),
                        )
                        GlassMetricChip(
                            label = "Retention",
                            value = "68%",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            DemoAnimatedSection(index = 6) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassSection(title = "Notifications", body = "Feed rows on glass.")
                    GlassNotificationRow(
                        title = "Delivery update",
                        subtitle = "Package arriving today · 2:40 PM",
                        dotColor = scheme.primary,
                    )
                    GlassNotificationRow(
                        title = "Team standup",
                        subtitle = "Starts in 15 minutes",
                        dotColor = scheme.tertiary,
                    )
                }
            }
        }
    }
}
