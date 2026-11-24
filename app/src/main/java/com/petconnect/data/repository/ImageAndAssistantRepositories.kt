package com.petconnect.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.petconnect.BuildConfig
import com.petconnect.domain.assistant.IntentMatcher
import com.petconnect.domain.assistant.LocalAnswer
import com.petconnect.domain.repository.AssistantReply
import com.petconnect.domain.repository.AssistantRepository
import com.petconnect.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Picked images come back as temporary content:// grants that die with the
 * picker. Copy them into app storage so stored pets keep their photos.
 */
@Singleton
class LocalImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImageRepository {
    override suspend fun import(sourceUri: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "photos").apply { mkdirs() }
            val target = File(dir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(Uri.parse(sourceUri))?.use { input ->
                target.outputStream().use { input.copyTo(it) }
            } ?: return@runCatching null
            Uri.fromFile(target).toString()
        }.onFailure { Log.w("Images", "import failed", it) }.getOrNull()
    }
}

/**
 * Asks the optional Agno backend; on any failure (or if the server has no
 * model key) answers from the offline knowledge base instead.
 */
@Singleton
class RemoteFirstAssistantRepository @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
    private val matcher: IntentMatcher,
) : AssistantRepository {

    @Serializable
    private data class ChatRequest(val message: String, val user_id: String?)

    @Serializable
    private data class ChatResponse(
        val reply: String = "",
        val blocked: Boolean = false,
        val intent: String = "unknown",
        val source: String = "agno",
    )

    override suspend fun ask(message: String, userId: String?): AssistantReply {
        val trimmed = message.trim()
        if (trimmed.isEmpty()) {
            return AssistantReply("Please type a question about Pet Connect or pet breeds.", false, "unknown", AssistantReply.Source.LOCAL)
        }
        // Guardrails run locally first: blocked prompts never leave the device.
        val local = matcher.answer(trimmed)
        if (local.blocked) return local.toReply(AssistantReply.Source.GUARDRAIL)

        val remote = withContext(Dispatchers.IO) {
            runCatching {
                val body = json.encodeToString(ChatRequest.serializer(), ChatRequest(trimmed, userId))
                    .toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url("${BuildConfig.AI_API_URL.trimEnd('/')}/chat").post(body).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) error("HTTP ${response.code}")
                    json.decodeFromString(ChatResponse.serializer(), response.body?.string().orEmpty())
                }
            }.getOrNull()
        }
        return if (remote == null || remote.source == "config" || remote.reply.isBlank()) {
            local.toReply(AssistantReply.Source.LOCAL)
        } else {
            AssistantReply(remote.reply, remote.blocked, remote.intent, AssistantReply.Source.REMOTE)
        }
    }

    private fun LocalAnswer.toReply(source: AssistantReply.Source) =
        AssistantReply(reply, blocked, intent.name.lowercase(), source)
}
