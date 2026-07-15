package com.mfhapps.trendingui.screens.expressive

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mfhapps.trendingui.R
import com.mfhapps.trendingui.ui.detail.DemoPaneHeader
import com.mfhapps.trendingui.ui.detail.demoDetailScrollBottomGap
import com.mfhapps.trendingui.ui.detail.demoDetailScrollInsets

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveUiScreen() {
    val scrollState = rememberScrollState()
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .demoDetailScrollInsets()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
            .demoDetailScrollBottomGap(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        DemoPaneHeader(
            title = "M3 Expressive",
            subtitle = "Wavy sliders, progress indicators, and carousels",
        )

        ExpressiveProgressSection()
        ExpressiveCarouselSection()
        ExpressiveShapesSection()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveProgressSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Wavy Progress", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularWavyProgressIndicator(
                progress = { 0.7f }
            )

            CircularWavyProgressIndicator()
        }

        LinearWavyProgressIndicator(
            progress = { 0.4f },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        LinearWavyProgressIndicator(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveCarouselSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Horizontal Carousel", style = MaterialTheme.typography.titleMedium)

        val colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.surfaceVariant,
        )

        val state = rememberCarouselState { colors.size }

        HorizontalMultiBrowseCarousel(
            state = state,
            preferredItemWidth = 186.dp,
            itemSpacing = 8.dp,
            modifier = Modifier.height(200.dp).fillMaxWidth()
        ) { i ->
            Box(
                modifier = Modifier
                    .maskClip(RoundedCornerShape(24.dp))
                    .background(colors[i])
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpressiveShapesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Expressive Controls", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = {},
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }

            OutlinedButton(
                onClick = {},
            ) {
                Text("Expressive Button")
            }
        }
    }
}
