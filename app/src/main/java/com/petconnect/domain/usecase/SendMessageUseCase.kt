package com.petconnect.domain.usecase

import com.petconnect.domain.model.Coordinates
import com.petconnect.domain.model.MessageKind
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.repository.ChatRepository
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.util.Geo
import javax.inject.Inject

/** Sends a message and keeps the conversation-list preview in sync. */
class SendMessageUseCase @Inject constructor(
    private val chat: ChatRepository,
    private val matches: MatchRepository,
) {
    suspend fun text(matchId: String, sender: OwnerId, text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        send(matchId, sender, MessageKind.TEXT, trimmed)
        return true
    }

    suspend fun image(matchId: String, sender: OwnerId, uri: String) =
        send(matchId, sender, MessageKind.IMAGE, uri)

    /** Location is always blurred to ~1 km before it leaves the device. */
    suspend fun approximateLocation(matchId: String, sender: OwnerId, exact: Coordinates) {
        val blurred = Geo.approximate(exact, radiusKm = 1.0)
        send(matchId, sender, MessageKind.LOCATION, "${blurred.latitude},${blurred.longitude}")
    }

    private suspend fun send(matchId: String, sender: OwnerId, kind: MessageKind, body: String) {
        chat.send(matchId, sender, kind, body)
        matches.updatePreview(matchId, kind.preview(body))
    }
}
