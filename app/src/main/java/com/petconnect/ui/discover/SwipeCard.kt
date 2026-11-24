package com.petconnect.ui.discover

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.petconnect.domain.model.InterestDecision
import com.petconnect.domain.model.PetWithContext
import com.petconnect.domain.util.Geo
import com.petconnect.ui.components.ScorePill
import com.petconnect.ui.components.primaryPhoto
import com.petconnect.ui.components.symbol
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * A draggable card. Past [threshold] it flies off and reports the decision;
 * otherwise it springs back. [key] the call site on the pet id so state resets.
 */
@Composable
fun SwipeCard(
    item: PetWithContext,
    ageLabel: String,
    onDecision: (InterestDecision) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val threshold = with(LocalDensity.current) { 120.dp.toPx() }
    val flyOut = with(LocalDensity.current) { 600.dp.toPx() }

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = modifier
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
            }
            .rotate(offsetX.value / 40f)
            .pointerInput(item.pet.id) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (abs(offsetX.value) > threshold) {
                                val right = offsetX.value > 0
                                offsetX.animateTo(if (right) flyOut else -flyOut, tween(200))
                                onDecision(if (right) InterestDecision.INTERESTED else InterestDecision.SKIPPED)
                            } else {
                                launch { offsetX.animateTo(0f) }
                                offsetY.animateTo(0f)
                            }
                        }
                    },
                ) { change, drag ->
                    change.consume()
                    scope.launch {
                        offsetX.snapTo(offsetX.value + drag.x)
                        offsetY.snapTo(offsetY.value + drag.y)
                    }
                }
            },
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.pet.photos.primaryPhoto(),
                contentDescription = item.pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color(0xD9000000))))

            // Live intent stamps while dragging.
            val progress = (offsetX.value / threshold).coerceIn(-1f, 1f)
            if (progress > 0.15f) Stamp("LIKE", PcColors.Success, progress, Modifier.align(Alignment.TopStart))
            if (progress < -0.15f) Stamp("NOPE", PcColors.Danger, -progress, Modifier.align(Alignment.TopEnd))

            ScorePill(
                item.compatibility.score,
                Modifier.align(Alignment.TopEnd).padding(Spacing.lg).graphicsLayer { alpha = if (progress < -0.15f) 0f else 1f },
                large = true,
            )

            Column(Modifier.align(Alignment.BottomStart).padding(Spacing.xl)) {
                Text(
                    "${item.pet.name}, $ageLabel",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    "${item.pet.breed} · ${item.pet.gender.symbol()}",
                    color = Color.White.copy(alpha = 0.9f),
                )
                Text(
                    "📍 ${Geo.formatDistance(item.distanceKm)}" + (item.owner?.let { " · ${it.name}" } ?: ""),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
                if (item.pet.bio.isNotBlank()) {
                    Text(
                        item.pet.bio,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        modifier = Modifier.padding(top = Spacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
private fun Stamp(text: String, color: Color, alpha: Float, modifier: Modifier) {
    Text(
        text,
        color = color,
        fontWeight = FontWeight.ExtraBold,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
            .padding(Spacing.xl)
            .graphicsLayer { this.alpha = alpha }
            .background(Color.White.copy(alpha = 0.85f), MaterialTheme.shapes.small)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
    )
}
