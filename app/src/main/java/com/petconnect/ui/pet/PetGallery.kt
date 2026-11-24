package com.petconnect.ui.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.petconnect.ui.components.PLACEHOLDER_PHOTO

/** Swipeable photo pager with page dots and a floating back button. */
@Composable
fun PetGallery(photos: List<String>, contentDescription: String, onBack: () -> Unit) {
    val gallery = photos.ifEmpty { listOf(PLACEHOLDER_PHOTO) }
    val pager = rememberPagerState { gallery.size }

    Box(Modifier.fillMaxWidth().height(340.dp)) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { page ->
            AsyncImage(
                model = gallery[page],
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (gallery.size > 1) {
            Row(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(gallery.size) { i ->
                    Box(
                        Modifier
                            .size(if (i == pager.currentPage) 18.dp else 7.dp, 7.dp)
                            .clip(CircleShape)
                            .background(if (i == pager.currentPage) Color.White else Color.White.copy(alpha = 0.5f)),
                    )
                }
            }
        }
        FilledTonalIconButton(
            onClick = onBack,
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color.White.copy(alpha = 0.9f)),
            modifier = Modifier.statusBarsPadding().padding(12.dp),
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back") }
    }
}
