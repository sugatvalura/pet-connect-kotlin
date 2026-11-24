package com.petconnect.data

import com.petconnect.data.db.toDomain
import com.petconnect.data.db.toEntity
import com.petconnect.domain.model.Message
import com.petconnect.domain.model.MessageKind
import com.petconnect.domain.model.OwnerId
import org.junit.Assert.assertEquals
import org.junit.Test

/** The entity <-> domain mapping is pure Kotlin, so it is tested without Room or a device. */
class ChatMappersTest {

    private val message = Message("m1", "match-1", OwnerId("me"), MessageKind.LOCATION, "28.6,77.2", "2026-11-19T10:00:00Z")

    @Test fun `round trip keeps every field`() {
        assertEquals(message, message.toEntity().toDomain())
    }

    @Test fun `enum is stored by name`() {
        assertEquals("LOCATION", message.toEntity().kind)
    }

    @Test fun `unknown kind from a newer app version falls back to TEXT instead of crashing`() {
        val future = message.toEntity().copy(kind = "VOICE_NOTE")
        assertEquals(MessageKind.TEXT, future.toDomain().kind)
    }
}
