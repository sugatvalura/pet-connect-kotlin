package com.petconnect.ui.listings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.model.ListingKind
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

@Composable
fun NewListingScreen(onDone: () -> Unit, viewModel: NewListingViewModel = hiltViewModel()) {
    val f by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(f.saved) { if (f.saved) onDone() }

    DetailScaffold(title = "New listing", onBack = onDone) { padding ->
        ScrollBody(padding) {
            ChoiceGroup("Listing type", ListingKind.entries, f.kind, { it.label }) { v ->
                viewModel.edit("price") { it.copy(kind = v) }
            }
            SectionHeader("Photos", subtitle = "Add up to 5 photos")
            PhotoPickerRow(f.photos, f.canAddPhoto, f.importingPhoto, viewModel::addPhoto, viewModel::removePhoto)

            Field("Title", f.title, { v -> viewModel.edit("title") { it.copy(title = v) } },
                placeholder = "e.g. Playful Beagle puppy looking for a home", error = f.errors["title"])
            ChoiceGroup("Species", PetSpecies.entries, f.species, { "${it.emoji} ${it.label}" }) { v -> viewModel.edit { it.copy(species = v) } }
            Field("Breed", f.breed, { v -> viewModel.edit("breed") { it.copy(breed = v) } },
                placeholder = "e.g. Beagle", error = f.errors["breed"])
            ChoiceGroup("Gender", PetGender.entries, f.gender, { it.label }) { v -> viewModel.edit { it.copy(gender = v) } }
            Field("Date of birth", f.dateOfBirth, { v -> viewModel.edit("dateOfBirth") { it.copy(dateOfBirth = v) } },
                placeholder = "YYYY-MM-DD", error = f.errors["dateOfBirth"])
            if (f.kind == ListingKind.SELL) {
                Field("Price (₹)", f.price, { v -> viewModel.edit("price") { it.copy(price = v.filter(Char::isDigit)) } },
                    placeholder = "e.g. 15000", error = f.errors["price"], keyboardType = KeyboardType.Number)
            }
            ChoiceGroup("Vaccination", VaccinationStatus.entries, f.vaccination, { it.label }) { v -> viewModel.edit { it.copy(vaccination = v) } }
            Field("Description", f.description, { v -> viewModel.edit("description") { it.copy(description = v) } },
                placeholder = "Temperament, health, and what kind of home suits them…",
                error = f.errors["description"], singleLine = false, minLines = 4)
            PrimaryButton("Publish listing", viewModel::save, loading = f.saving)
        }
    }
}
