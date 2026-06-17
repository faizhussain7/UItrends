package com.mfhapps.trendingui.screens.glass

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.ui.demo.DemoAnimatedSection
@Composable
fun GlassmorphismScreen() {
    val scheme = MaterialTheme.colorScheme
    val backdrop = LocalGlassBackdrop.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 56.dp, bottom = 28.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        DemoAnimatedSection(index = 0) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Glassmorphism",
                    style = MaterialTheme.typography.headlineLarge,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && backdrop?.hazeEnabled == true) {
                        "Theme orbs fill the screen · chrome and sheets use the same frosted glass"
                    } else {
                        "Theme orbs fill the screen · tonal glass when blur is unavailable"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                )
            }
        }

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
