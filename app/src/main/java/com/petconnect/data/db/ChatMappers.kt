package com.petconnect.data.db

import com.petconnect.domain.model.Message
import com.petconnect.domain.model.MessageKind
import com.petconnect.domain.model.OwnerId

/*
 * Mappers between the database layer and the domain layer.
 * Kept as extension functions: pure, tiny, and trivially unit-testable.
 */

fun MessageEntity.toDomain(): Message = Message(
    id = id,
    matchId = matchId,
    senderId = OwnerId(senderId),
    // Unknown kinds (e.g. written by a newer app version) degrade to TEXT instead of crashing.
    kind = MessageKind.entries.firstOrNull { it.name == kind } ?: MessageKind.TEXT,
    body = body,
    createdAt = createdAt,
)

fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    matchId = matchId,
    senderId = senderId.value,
    kind = kind.name,
    body = body,
    createdAt = createdAt,
)
