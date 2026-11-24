package com.petconnect.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object: the SQL lives here and nowhere else.
 *
 * - Functions returning Flow are OBSERVABLE: Room re-emits whenever the
 *   underlying table changes, so the chat screen updates automatically.
 * - `suspend` functions run off the main thread (Room handles the dispatcher).
 */
@Dao
interface ChatDao {

    @Query("SELECT * FROM messages WHERE match_id = :matchId ORDER BY created_at ASC")
    fun observeMessages(matchId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("SELECT owner_id FROM blocked_owners")
    fun observeBlockedOwnerIds(): Flow<List<String>>

    /** IGNORE: blocking twice is a no-op, not an error. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun block(owner: BlockedOwnerEntity)
}
