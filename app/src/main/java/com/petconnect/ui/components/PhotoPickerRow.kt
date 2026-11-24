package com.petconnect.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

/**
 * Uses the system Photo Picker, which needs no storage permission. The first
 * photo is the cover.
 */
@Composable
fun PhotoPickerRow(
    photos: List<String>,
    canAdd: Boolean,
    importing: Boolean,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onAdd(it.toString()) }
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        items(photos, key = { it }) { uri ->
            Box {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(92.dp).clip(RoundedCornerShape(14.dp)),
                )
                if (uri == photos.first()) {
                    Text(
                        "Cover",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .background(PcColors.Scrim, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Remove photo",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .background(PcColors.Scrim, CircleShape)
                        .clickable { onRemove(uri) }
                        .padding(3.dp),
                )
            }
        }
        if (canAdd) {
            item {
                Column(
                    Modifier
                        .size(92.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, PcColors.Border, RoundedCornerShape(14.dp))
                        .background(PcColors.Surface)
                        .clickable(enabled = !importing) {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (importing) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.AddAPhoto, null, tint = PcColors.Primary)
                        Text("Add", style = MaterialTheme.typography.labelSmall, color = PcColors.Primary)
                    }
                }
            }
        }
    }
}
