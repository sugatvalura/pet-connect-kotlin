package com.petconnect.ui.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petconnect.domain.model.ListingKind
import com.petconnect.domain.model.NewListing
import com.petconnect.domain.model.PetGender
import com.petconnect.domain.model.PetSpecies
import com.petconnect.domain.model.VaccinationStatus
import com.petconnect.domain.repository.ImageRepository
import com.petconnect.domain.repository.ListingRepository
import com.petconnect.domain.repository.LocationRepository
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

data class NewListingForm(
    val kind: ListingKind = ListingKind.ADOPT,
    val photos: List<String> = emptyList(),
    val title: String = "",
    val species: PetSpecies = PetSpecies.DOG,
    val breed: String = "",
    val gender: PetGender = PetGender.MALE,
    val dateOfBirth: String = "",
    val price: String = "",
    val description: String = "",
    val vaccination: VaccinationStatus = VaccinationStatus.VACCINATED,
    val errors: FieldErrors = emptyMap(),
    val importingPhoto: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
) {
    val canAddPhoto: Boolean get() = photos.size < MAX_PHOTOS

    companion object { const val MAX_PHOTOS = 5 }
}

@HiltViewModel
class NewListingViewModel @Inject constructor(
    private val listings: ListingRepository,
    private val images: ImageRepository,
    private val location: LocationRepository,
    private val observeViewer: ObserveViewerUseCase,
    private val validator: FormValidator,
) : ViewModel() {
    private val _form = MutableStateFlow(NewListingForm())
    val form: StateFlow<NewListingForm> = _form.asStateFlow()

    fun edit(field: String? = null, transform: (NewListingForm) -> NewListingForm) =
        _form.update { transform(it).copy(errors = if (field == null) it.errors else it.errors - field) }

    fun addPhoto(uri: String) {
        if (!_form.value.canAddPhoto) return
        viewModelScope.launch {
            _form.update { it.copy(importingPhoto = true) }
            val stored = images.import(uri)
            _form.update { f ->
                f.copy(importingPhoto = false, photos = if (stored != null) (f.photos + stored).take(NewListingForm.MAX_PHOTOS) else f.photos)
            }
        }
    }

    fun removePhoto(uri: String) = _form.update { it.copy(photos = it.photos - uri) }

    fun save() {
        val f = _form.value
        val errors = validator.listing(f.kind, f.title, f.breed, f.description, f.dateOfBirth, f.price)
        if (errors.isNotEmpty()) {
            _form.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _form.update { it.copy(saving = true) }
            listings.create(
                NewListing(
                    ownerId = observeViewer().first().ownerId,
                    kind = f.kind,
                    title = f.title.trim(),
                    photos = f.photos,
                    species = f.species,
                    breed = f.breed.trim(),
                    gender = f.gender,
                    dateOfBirth = f.dateOfBirth.trim(),
                    location = location.location.value.location,
                    price = if (f.kind == ListingKind.SELL) f.price.trim().toIntOrNull() else null,
                    description = f.description.trim(),
                    vaccination = f.vaccination,
                ),
            )
            _form.update { it.copy(saving = false, saved = true) }
        }
    }
}
