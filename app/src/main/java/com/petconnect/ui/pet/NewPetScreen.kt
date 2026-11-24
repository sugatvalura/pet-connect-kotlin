package com.petconnect.ui.pet

import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.model.PetGender
import com.petconnect.domain.model.PetSpecies
import com.petconnect.domain.model.VaccinationStatus
import com.petconnect.ui.components.ChoiceGroup
import com.petconnect.ui.components.DetailScaffold
import com.petconnect.ui.components.Field
import com.petconnect.ui.components.PhotoPickerRow
import com.petconnect.ui.components.PrimaryButton
import com.petconnect.ui.components.ScrollBody
import com.petconnect.ui.components.SectionHeader
import com.petconnect.ui.components.SwitchRow

@Composable
fun NewPetScreen(onDone: () -> Unit, viewModel: NewPetViewModel = hiltViewModel()) {
    val f by viewModel.form.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(f.saved) { if (f.saved) onDone() }
    LaunchedEffect(f.saveError) {
        f.saveError?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    DetailScaffold(title = "Add a pet", onBack = onDone, snackbar = snackbar) { padding ->
        ScrollBody(padding) {
            SectionHeader("Photos", subtitle = "Add up to 5 photos")
            PhotoPickerRow(f.photos, f.canAddPhoto, f.importingPhoto, viewModel::addPhoto, viewModel::removePhoto)

            Field("Pet name", f.name, { v -> viewModel.edit("name") { it.copy(name = v) } }, placeholder = "e.g. Bruno", error = f.errors["name"])
            ChoiceGroup("Species", PetSpecies.entries, f.species, { "${it.emoji} ${it.label}" }) { v -> viewModel.edit { it.copy(species = v) } }
            Field("Breed", f.breed, { v -> viewModel.edit("breed") { it.copy(breed = v) } }, placeholder = "e.g. Golden Retriever", error = f.errors["breed"])
            ChoiceGroup("Gender", PetGender.entries, f.gender, { it.label }) { v -> viewModel.edit { it.copy(gender = v) } }
            Field(
                "Date of birth", f.dateOfBirth, { v -> viewModel.edit("dateOfBirth") { it.copy(dateOfBirth = v) } },
                placeholder = "YYYY-MM-DD", error = f.errors["dateOfBirth"],
            )
            Field(
                "Bio / personality", f.bio, { v -> viewModel.edit { it.copy(bio = v) } },
                placeholder = "Playful, loves long walks, great with kids…", singleLine = false, minLines = 3,
            )
            ChoiceGroup("Vaccination", VaccinationStatus.entries, f.vaccination, { it.label }) { v -> viewModel.edit { it.copy(vaccination = v) } }
            SwitchRow("Spayed / neutered", f.isSpayedOrNeutered, { v -> viewModel.edit { it.copy(isSpayedOrNeutered = v) } })
            SwitchRow(
                "Available for breeding", f.availableForBreeding, { v -> viewModel.edit { it.copy(availableForBreeding = v) } },
                hint = "Set exact dates later from your profile.",
            )
            PrimaryButton("Save pet", viewModel::save, Modifier.imePadding(), loading = f.saving)
        }
    }
}
