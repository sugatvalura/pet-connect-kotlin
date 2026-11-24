package com.petconnect.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petconnect.domain.repository.AuthRepository
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.usecase.FieldErrors
import com.petconnect.domain.usecase.FormValidator
import com.petconnect.ui.common.stateInVm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Session gate for the whole app: Loading → SignedOut / SignedIn. */
sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data object SignedIn : SessionState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    auth: AuthRepository,
    private val location: LocationRepository,
) : ViewModel() {
    val state: StateFlow<SessionState> = auth.session
        .map { if (it == null) SessionState.SignedOut else SessionState.SignedIn }
        .stateInVm(this, SessionState.Loading)

    /** Called once location permission has been answered (either way). */
    fun refreshLocation() {
        viewModelScope.launch { location.refresh() }
    }
}

data class AuthFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val errors: FieldErrors = emptyMap(),
    val submitting: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val validator: FormValidator,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthFormState())
    val state: StateFlow<AuthFormState> = _state.asStateFlow()

    fun onName(v: String) = _state.update { it.copy(name = v, errors = it.errors - "name") }
    fun onEmail(v: String) = _state.update { it.copy(email = v, errors = it.errors - "email") }
    fun onPassword(v: String) = _state.update { it.copy(password = v, errors = it.errors - "password") }

    /** [requireName] is true on signup. Navigation reacts to the session flow. */
    fun submit(requireName: Boolean) {
        val s = _state.value
        val errors = validator.login(s.email, s.password).toMutableMap()
        if (requireName && s.name.isBlank()) errors["name"] = "Tell us what to call you."
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            auth.signIn(s.email, s.name.takeIf { requireName })
            _state.update { it.copy(submitting = false) }
        }
    }
}
