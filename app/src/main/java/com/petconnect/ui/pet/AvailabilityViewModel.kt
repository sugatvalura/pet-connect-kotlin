package com.petconnect.ui.pet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.petconnect.domain.model.AvailabilityVisibility
import com.petconnect.domain.model.AvailabilityWindow
import com.petconnect.domain.model.PetId
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.usecase.FieldErrors
import com.petconnect.domain.usecase.FormValidator
import com.petconnect.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvailabilityForm(
    val loaded: Boolean = false,
    val petName: String = "",
    val enabled: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val visibility: AvailabilityVisibility = AvailabilityVisibility.PRIVATE,
    val notes: String = "",
    val errors: FieldErrors = emptyMap(),
    val saving: Boolean = false,
    val saved: Boolean = false,
)

@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val pets: PetRepository,
    private val validator: FormValidator,
) : ViewModel() {
    private val petId = PetId(savedState.toRoute<Route.Availability>().petId)

    private val _form = MutableStateFlow(AvailabilityForm())
    val form: StateFlow<AvailabilityForm> = _form.asStateFlow()

    init {
        // Seed the form once; later repository updates must not clobber edits.
        viewModelScope.launch {
            val pet = pets.pets.first().firstOrNull { it.id == petId } ?: return@launch
            val a = pet.availability
            _form.value = AvailabilityForm(
                loaded = true,
                petName = pet.name,
                enabled = a.enabled,
                startDate = a.startDate.orEmpty(),
                endDate = a.endDate.orEmpty(),
                visibility = a.visibility,
                notes = a.notes.orEmpty(),
            )
        }
    }

    fun edit(field: String? = null, transform: (AvailabilityForm) -> AvailabilityForm) =
        _form.update { transform(it).copy(errors = if (field == null) it.errors else it.errors - field) }

    fun save() {
        val f = _form.value
        val errors = validator.availability(f.enabled, f.startDate, f.endDate)
        if (errors.isNotEmpty()) {
            _form.update { it.copy(errors = errors) }
            return
        }
        viewModelScope.launch {
            _form.update { it.copy(saving = true) }
            val window = AvailabilityWindow(
                enabled = f.enabled,
                startDate = f.startDate.trim().ifEmpty { null },
                endDate = f.endDate.trim().ifEmpty { null },
                visibility = f.visibility,
                notes = f.notes.trim().ifEmpty { null },
            )
            pets.updatePet(petId) { it.copy(availability = window, availableForBreeding = f.enabled) }
            _form.update { it.copy(saving = false, saved = true) }
        }
    }
}
