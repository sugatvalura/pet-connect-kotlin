package com.petconnect.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.ui.components.Field
import com.petconnect.ui.components.PrimaryButton
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

/** Shared by login and signup — signup just adds a name field. */
@Composable
fun AuthScreen(
    isSignup: Boolean,
    onSwitchMode: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(PcColors.SurfaceAlt)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(PcColors.Primary), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Pets, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
        }
        Text("Pet Connect", style = MaterialTheme.typography.headlineMedium)
        Text(
            if (isSignup) "Create an account to start matching." else "Find the perfect match for your pet.",
            color = PcColors.InkMuted,
        )

        Card(colors = CardDefaults.cardColors(containerColor = PcColors.Surface), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                if (isSignup) {
                    Field("Your name", state.name, viewModel::onName, error = state.errors["name"])
                }
                Field(
                    "Email", state.email, viewModel::onEmail,
                    placeholder = "you@example.com", error = state.errors["email"], keyboardType = KeyboardType.Email,
                )
                Field(
                    "Password", state.password, viewModel::onPassword,
                    error = state.errors["password"], keyboardType = KeyboardType.Password, isPassword = true,
                )
                PrimaryButton(
                    label = if (isSignup) "Create account" else "Log in",
                    onClick = { viewModel.submit(requireName = isSignup) },
                    loading = state.submitting,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (isSignup) "Already have an account?" else "New to Pet Connect?", color = PcColors.InkMuted)
            TextButton(onClick = onSwitchMode) { Text(if (isSignup) "Log in" else "Create an account") }
        }
    }
}
