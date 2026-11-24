package com.petconnect.ui.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petconnect.domain.model.AvailabilityWindow
import com.petconnect.domain.model.NewPet
import com.petconnect.domain.model.PetGender
import com.petconnect.domain.model.PetSpecies
import com.petconnect.domain.model.VaccinationStatus
import com.petconnect.domain.repository.ImageRepository
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.repository.PreferencesRepository
import com.petconnect.domain.usecase.FieldErrors
import com.petconnect.domain.usecase.FormValidator
import com.petconnect.domain.usecase.ObserveViewerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewPetForm(
    val photos: List<String> = emptyList(),
    val name: String = "",
    val species: PetSpecies = PetSpecies.DOG,
    val breed: String = "",
    val gender: PetGender = PetGender.MALE,
    val dateOfBirth: String = "",
    val bio: String = "",
    val vaccination: VaccinationStatus = VaccinationStatus.VACCINATED,
    val isSpayedOrNeutered: Boolean = false,
    val availableForBreeding: Boolean = false,
    val errors: FieldErrors = emptyMap(),
    val saving: Boolean = false,
    val importingPhoto: Boolean = false,
    val saved: Boolean = false,
    val saveError: String? = null,
) {
    val canAddPhoto: Boolean get() = photos.size < MAX_PHOTOS

    companion object { const val MAX_PHOTOS = 5 }
}

@HiltViewModel
class NewPetViewModel @Inject constructor(
    private val pets: PetRepository,
    private val images: ImageRepository,
    private val location: LocationRepository,
    private val preferences: PreferencesRepository,
    private val observeViewer: ObserveViewerUseCase,
    private val validator: FormValidator,
) : ViewModel() {
    private val _form = MutableStateFlow(NewPetForm())
    val form: StateFlow<NewPetForm> = _form.asStateFlow()

    /** Field edits clear that field's error so feedback stays relevant. */
    fun edit(field: String? = null, transform: (NewPetForm) -> NewPetForm) =
        _form.update { transform(it).copy(errors = if (field == null) it.errors else it.errors - field) }

    fun addPhoto(uri: String) {
        if (!_form.value.canAddPhoto) return
        viewModelScope.launch {
            _form.update { it.copy(importingPhoto = true) }
            val stored = images.import(uri)
            _form.update { f ->
                f.copy(
                    importingPhoto = false,
                    photos = if (stored != null) (f.photos + stored).take(NewPetForm.MAX_PHOTOS) else f.photos,
                )
            }
        }
    }

    fun removePhoto(uri: String) = _form.update { it.copy(photos = it.photos - uri) }

    fun save() {
        val f = _form.value
        val errors = validator.pet(f.name, f.breed, f.dateOfBirth)
        if (errors.isNotEmpty()) {
            _form.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _form.update { it.copy(saving = true, saveError = null) }
            runCatching {
                val viewer = observeViewer().first()
                val created = pets.createPet(
                    NewPet(
                        ownerId = viewer.ownerId,
                        name = f.name.trim(),
                        photos = f.photos,
                        species = f.species,
                        breed = f.breed.trim(),
                        gender = f.gender,
                        dateOfBirth = f.dateOfBirth.trim(),
                        location = location.location.value.location,
                        bio = f.bio.trim(),
                        vaccination = f.vaccination,
                        isSpayedOrNeutered = f.isSpayedOrNeutered,
                        availableForBreeding = f.availableForBreeding,
                        availability = AvailabilityWindow.Off,
                    ),
                )
                // First pet becomes the one you match as.
                if (viewer.myPets.isEmpty()) preferences.setActivePetId(created.id)
            }.onSuccess {
                _form.update { it.copy(saving = false, saved = true) }
            }.onFailure {
                _form.update { it.copy(saving = false, saveError = "Something went wrong. Please try again.") }
            }
        }
    }

    fun clearError() = _form.update { it.copy(saveError = null) }
}
