package com.petconnect.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database row for a chat message.
 *
 * WHY A SEPARATE CLASS FROM domain.Message?
 * The domain model must not know about Room annotations (Clean Architecture).
 * The mapper in ChatMappers.kt converts between the two, so the database
 * schema can change without touching ViewModels or use cases.
 *
 * The index on (match_id, created_at) makes "messages of this chat, oldest
 * first" a fast index scan instead of a full-table scan.
 */
@Entity(
    tableName = "messages",
    indices = [Index(value = ["match_id", "created_at"])],
)
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "match_id") val matchId: String,
    @ColumnInfo(name = "sender_id") val senderId: String,
    /** Stored as the enum name ("TEXT", "IMAGE", "LOCATION"). */
    val kind: String,
    val body: String,
    /** ISO-8601 instant. Lexicographic order == chronological order. */
    @ColumnInfo(name = "created_at") val createdAt: String,
)

/** Owners the user has blocked. A set → primary key gives uniqueness for free. */
@Entity(tableName = "blocked_owners")
data class BlockedOwnerEntity(
    @PrimaryKey @ColumnInfo(name = "owner_id") val ownerId: String,
)
