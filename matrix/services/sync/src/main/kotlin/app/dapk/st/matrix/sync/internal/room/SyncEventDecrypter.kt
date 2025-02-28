package app.dapk.st.matrix.sync.internal.room

import app.dapk.st.matrix.common.DecryptionResult
import app.dapk.st.matrix.common.EncryptedMessageContent
import app.dapk.st.matrix.common.MatrixLogger
import app.dapk.st.matrix.common.matrixLog
import app.dapk.st.matrix.sync.internal.request.ApiEncryptedContent
import app.dapk.st.matrix.sync.internal.request.ApiTimelineEvent
import app.dapk.st.matrix.sync.internal.request.DecryptedContent
import kotlinx.serialization.json.Json

internal class SyncEventDecrypter(
    private val messageDecrypter: MessageDecrypter,
    private val json: Json,
    private val logger: MatrixLogger,
) {

    suspend fun decryptTimelineEvents(events: List<ApiTimelineEvent>) = events.map { event ->
        when (event) {
            is ApiTimelineEvent.Encrypted -> {
                event.encryptedContent.export(event.senderId)?.let { encryptedContent ->
                    decrypt(encryptedContent, event)
                } ?: event
            }
            else -> event
        }
    }

    private suspend fun decrypt(it: EncryptedMessageContent, event: ApiTimelineEvent.Encrypted) = messageDecrypter.decrypt(it).let {
        when (it) {
            is DecryptionResult.Failed -> event
            is DecryptionResult.Success -> json.decodeFromString(DecryptedContent.serializer(), it.payload.value).let {
                val relation = when (event.encryptedContent) {
                    is ApiEncryptedContent.MegOlmV1 -> event.encryptedContent.relation
                    is ApiEncryptedContent.OlmV1 -> null
                    ApiEncryptedContent.Unknown -> null
                }
                when (it) {
                    is DecryptedContent.TimelineText -> ApiTimelineEvent.TimelineText(
                        event.eventId,
                        event.senderId,
                        it.content.copy(relation = relation),
                        event.utcTimestamp,
                    ).also { logger.matrixLog("decrypted to timeline text: $it") }
                    DecryptedContent.Ignored -> event
                }
            }
        }
    }
}