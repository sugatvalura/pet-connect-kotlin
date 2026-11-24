package com.petconnect.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.petconnect.domain.model.Message
import com.petconnect.domain.model.MessageKind
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun MessageBubble(message: Message, isMine: Boolean, time: String) {
    val bg = if (isMine) PcColors.Primary else PcColors.Surface
    val fg = if (isMine) Color.White else PcColors.Ink
    val shape = RoundedCornerShape(
        topStart = 18.dp, topEnd = 18.dp,
        bottomStart = if (isMine) 18.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 18.dp,
    )
    val context = LocalContext.current

    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier.widthIn(max = 280.dp).clip(shape).background(bg).padding(
                if (message.kind == MessageKind.IMAGE) 4.dp else Spacing.md,
            ),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        ) {
            when (message.kind) {
                MessageKind.TEXT -> Text(message.body, color = fg)
                MessageKind.IMAGE -> AsyncImage(
                    model = message.body,
                    contentDescription = "Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(220.dp).clip(RoundedCornerShape(14.dp)),
                )
                MessageKind.LOCATION -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        // geo: URI opens whichever maps app the user has.
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${message.body}?q=${message.body}"))
                        runCatching { context.startActivity(intent) }
                    },
                ) {
                    Icon(Icons.Filled.LocationOn, null, tint = fg)
                    Column(Modifier.padding(start = Spacing.xs)) {
                        Text("Approximate location", color = fg, style = MaterialTheme.typography.titleSmall)
                        Text("Within ~1 km · tap to open map", color = fg.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Text(
                time,
                color = fg.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 2.dp, end = if (message.kind == MessageKind.IMAGE) 6.dp else 0.dp),
            )
        }
    }
}
