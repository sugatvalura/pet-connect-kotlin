package com.petconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.petconnect.domain.model.CompatibilityResult
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(48.dp), shape = MaterialTheme.shapes.medium) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

/** Labelled text field with inline error, used by every form. */
@Composable
fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = PcColors.InkFaint) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun PcChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PcColors.Primary,
            selectedLabelColor = Color.White,
        ),
    )
}

enum class BadgeTone(val fg: Color, val bg: Color) {
    BRAND(PcColors.Primary, PcColors.PrimarySoft),
    ACCENT(PcColors.Warning, PcColors.AccentSoft),
    SUCCESS(PcColors.Success, PcColors.SuccessSoft),
    WARNING(PcColors.Warning, PcColors.WarningSoft),
    DANGER(PcColors.Danger, PcColors.DangerSoft),
    NEUTRAL(PcColors.InkMuted, PcColors.SurfaceAlt),
}

@Composable
fun Badge(label: String, tone: BadgeTone = BadgeTone.NEUTRAL) {
    Text(
        label,
        color = tone.fg,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(CircleShape)
            .background(tone.bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
fun Avatar(url: String?, name: String, size: Dp = 44.dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(PcColors.PrimarySoft),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(model = url, contentDescription = name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Text(name.take(1).uppercase(), color = PcColors.Primary, fontWeight = FontWeight.Bold)
        }
    }
}

/** Score pill whose colour follows the compatibility band. */
@Composable
fun ScorePill(score: Int, modifier: Modifier = Modifier, large: Boolean = false) {
    val tone = when (CompatibilityResult(score, emptyList()).band) {
        CompatibilityResult.Band.HIGH -> BadgeTone.SUCCESS
        CompatibilityResult.Band.MEDIUM -> BadgeTone.WARNING
        CompatibilityResult.Band.LOW -> BadgeTone.NEUTRAL
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(if (large) 18.dp else 12.dp))
            .background(tone.bg)
            .padding(horizontal = if (large) 14.dp else 8.dp, vertical = if (large) 8.dp else 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("$score%", color = tone.fg, fontWeight = FontWeight.ExtraBold, fontSize = if (large) 22.sp else 13.sp)
        if (large) Text("match", color = tone.fg, fontSize = 11.sp)
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth().padding(bottom = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = PcColors.InkMuted) }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(PcColors.PrimarySoft), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = PcColors.Primary, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(Spacing.sm))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = PcColors.InkMuted, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.sm))
            PrimaryButton(actionLabel, onAction, Modifier.padding(horizontal = Spacing.xl))
        }
        if (secondaryLabel != null && onSecondary != null) {
            TextButton(onClick = onSecondary) { Text(secondaryLabel) }
        }
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PcColors.Primary)
    }
}
