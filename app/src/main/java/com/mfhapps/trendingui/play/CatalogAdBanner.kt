@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.mfhapps.trendingui.play

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mfhapps.trendingui.ui.platform.catalogAdDockHorizontalInsets
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.mfhapps.trendingui.ui.components.collapsedHeaderBlur
import com.mfhapps.trendingui.ui.components.collapsedHeaderBlurActive
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val RetryDelayMs = 45_000L

private enum class BannerAdState { Loading, Loaded, Failed }

@Composable
fun CatalogAdDock(
    modifier: Modifier = Modifier,
    onDockHeightChanged: (Dp) -> Unit = {},
) {
    if (!AdsConfig.adsEnabled) return

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() ?: context }
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scheme = MaterialTheme.colorScheme
    val landscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val contentGutter = if (landscape) 8.dp else 12.dp

    var adState by remember { mutableStateOf(BannerAdState.Loading) }
    var retryToken by remember { mutableIntStateOf(0) }
    var adViewRef by remember { mutableStateOf<AdView?>(null) }

    LaunchedEffect(adState) {
        if (adState == BannerAdState.Failed) {
            delay(RetryDelayMs)
            retryToken++
            adState = BannerAdState.Loading
        }
    }
    LaunchedEffect(retryToken) {
        if (retryToken > 0) {
            adViewRef?.loadAd(AdRequest.Builder().build())
        }
    }

    val dockVisible = adState != BannerAdState.Failed
    LaunchedEffect(dockVisible) {
        if (!dockVisible) onDockHeightChanged(0.dp)
    }

    AnimatedVisibility(
        visible = dockVisible,
        enter = fadeIn(tween(220)) + expandVertically(tween(260)),
        exit = fadeOut(tween(160)) + shrinkVertically(tween(220)),
        modifier = modifier,
    ) {
        val blurActive = collapsedHeaderBlurActive(collapsedFraction = 1f)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(catalogAdDockHorizontalInsets())
                .onSizeChanged { size ->
                    onDockHeightChanged(with(density) { size.height.toDp() })
                }
                .collapsedHeaderBlur(collapsedFraction = 1f),
            color = if (blurActive) Color.Transparent else scheme.surfaceContainer,
            tonalElevation = if (blurActive) 0.dp else 3.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = contentGutter)
                    .padding(top = 10.dp)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                    )
                    .padding(bottom = 10.dp),
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val adWidthDp = maxWidth.value.roundToInt().coerceAtLeast(300)
                    val adSize = remember(adWidthDp, configuration.orientation) {
                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
                    }
                    val adHeight = adSize.height.dp
                    LaunchedEffect(adWidthDp, configuration.orientation) {
                        adState = BannerAdState.Loading
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = scheme.surfaceContainerHigh,
                        contentColor = scheme.onSurface,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(adHeight),
                        ) {
                            key(adWidthDp, configuration.orientation) {
                                AndroidView(
                                    factory = {
                                        AdView(activity).apply {
                                            adUnitId = AdsConfig.bannerUnitId
                                            setAdSize(adSize)
                                            adListener = object : AdListener() {
                                                override fun onAdLoaded() {
                                                    adState = BannerAdState.Loaded
                                                }

                                                override fun onAdFailedToLoad(error: LoadAdError) {
                                                    adState = BannerAdState.Failed
                                                }
                                            }
                                            adViewRef = this
                                            AdsConfig.initialize(context.applicationContext) {
                                                loadAd(AdRequest.Builder().build())
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .alpha(if (adState == BannerAdState.Loaded) 1f else 0f),
                                )
                            }
                            Crossfade(
                                targetState = adState == BannerAdState.Loaded,
                                animationSpec = tween(280),
                                label = "adPlaceholder",
                            ) { loaded ->
                                if (!loaded) {
                                    BannerAdLoadingPlaceholder(
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, adViewRef) {
        val adView = adViewRef
        if (adView == null) {
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                adView.resume()
            }
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                adView.destroy()
                adViewRef = null
            }
        }
    }
}

@Composable
private fun BannerAdLoadingPlaceholder(
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = scheme.secondaryContainer,
                contentColor = scheme.onSecondaryContainer,
            ) {
                Text(
                    text = "Ad",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
            Text(
                text = "Sponsored content loading…",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        LinearWavyProgressIndicator(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .width(160.dp),
            color = scheme.primary,
            trackColor = scheme.surfaceContainerHighest,
        )
    }
}
