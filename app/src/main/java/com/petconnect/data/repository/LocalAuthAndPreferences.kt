package com.petconnect.data.repository

import com.petconnect.data.local.JsonStore
import com.petconnect.domain.model.PetFilters
import com.petconnect.domain.model.PetId
import com.petconnect.domain.model.Session
import com.petconnect.domain.repository.AuthRepository
import com.petconnect.domain.repository.PreferencesRepository
import com.petconnect.domain.usecase.DemoAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.nullable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Demo auth: persists a session record locally. Replace with a token exchange
 * once a backend exists; the interface (and every ViewModel) stays the same.
 */
@Singleton
class LocalAuthRepository @Inject constructor(private val store: JsonStore) : AuthRepository {
    private val serializer = Session.serializer().nullable

    override val session: Flow<Session?> = store.observe(JsonStore.Keys.SESSION, serializer) { null }

    override suspend fun signIn(email: String, name: String?): Session {
        val session = Session(DemoAccount.OWNER_ID, email.trim().lowercase(), name?.trim()?.ifBlank { null })
        store.update(JsonStore.Keys.SESSION, serializer, { null }) { session }
        return session
    }

    override suspend fun signOut() = store.remove(JsonStore.Keys.SESSION)
}

@Singleton
class LocalPreferencesRepository @Inject constructor(private val store: JsonStore) : PreferencesRepository {
    private val serializer = PetId.serializer().nullable

    override val activePetId: Flow<PetId?> = store.observe(JsonStore.Keys.ACTIVE_PET, serializer) { null }

    override suspend fun setActivePetId(id: PetId?) {
        store.update(JsonStore.Keys.ACTIVE_PET, serializer, { null }) { id }
    }

    // Filters are session-scoped (like the RN app), so in-memory is enough.
    private val _filters = MutableStateFlow(PetFilters.DEFAULT)
    override val filters: StateFlow<PetFilters> = _filters.asStateFlow()
    override fun setFilters(filters: PetFilters) {
        _filters.value = filters
    }
}
