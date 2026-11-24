package com.petconnect.data.repository

import com.petconnect.data.db.BlockedOwnerEntity
import com.petconnect.data.db.ChatDao
import com.petconnect.data.db.toDomain
import com.petconnect.data.db.toEntity
import com.petconnect.data.local.IdGenerator
import com.petconnect.data.local.JsonStore
import com.petconnect.domain.model.Message
import com.petconnect.domain.model.MessageKind
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.repository.ChatRepository
import com.petconnect.domain.util.Dates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ChatRepository backed by Room.
 *
 * This class implements the SAME domain interface as the old DataStore
 * version, so no ViewModel or use case changed when we swapped it in —
 * only one line in di/AppModule.kt. That is Dependency Inversion paying off.
 */
@Singleton
class RoomChatRepository @Inject constructor(
    private val dao: ChatDao,
    private val legacy: JsonStore,
    private val ids: IdGenerator,
    private val dates: Dates,
) : ChatRepository {

    override fun messages(matchId: String): Flow<List<Message>> =
        dao.observeMessages(matchId)
            .onStart { migrateLegacyOnce() }
            .map { rows -> rows.map { it.toDomain() } }
            .distinctUntilChanged()

    override suspend fun send(matchId: String, sender: OwnerId, kind: MessageKind, body: String): Message {
        val message = Message(ids.next("msg"), matchId, sender, kind, body, dates.nowIso())
        dao.insert(message.toEntity())
        return message
    }

    override val blockedOwners: Flow<Set<OwnerId>> =
        dao.observeBlockedOwnerIds()
            .onStart { migrateLegacyOnce() }
            .map { rows -> rows.map(::OwnerId).toSet() }
            .distinctUntilChanged()

    override suspend fun blockOwner(ownerId: OwnerId) {
        dao.block(BlockedOwnerEntity(ownerId.value))
    }

    // ── One-time migration from the old DataStore JSON storage ──────────────

    private val migrationLock = Mutex()
    @Volatile private var migrated = false

    /**
     * Copies messages/blocks saved by the previous app version into Room, then
     * deletes the old keys. Mutex + flag make it run at most once even when
     * several screens start collecting at the same time.
     */
    private suspend fun migrateLegacyOnce() {
        if (migrated) return
        migrationLock.withLock {
            if (migrated) return
            val oldMessages = legacy.get(JsonStore.Keys.MESSAGES, ListSerializer(Message.serializer())) { emptyList() }
            if (oldMessages.isNotEmpty()) dao.insertAll(oldMessages.map { it.toEntity() })

            val oldBlocked = legacy.get(JsonStore.Keys.BLOCKED, SetSerializer(OwnerId.serializer())) { emptySet() }
            oldBlocked.forEach { dao.block(BlockedOwnerEntity(it.value)) }

            legacy.remove(JsonStore.Keys.MESSAGES)
            legacy.remove(JsonStore.Keys.BLOCKED)
            migrated = true
        }
    }
}
