package app.dapk.st.matrix.message.internal

import app.dapk.st.matrix.MatrixTaskRunner
import app.dapk.st.matrix.common.RoomId
import app.dapk.st.matrix.http.MatrixHttpClient
import app.dapk.st.matrix.message.BackgroundScheduler
import app.dapk.st.matrix.message.LocalEchoStore
import app.dapk.st.matrix.message.MessageEncrypter
import app.dapk.st.matrix.message.MessageService
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import java.net.SocketException
import java.net.UnknownHostException

internal class DefaultMessageService(
    httpClient: MatrixHttpClient,
    private val localEchoStore: LocalEchoStore,
    private val backgroundScheduler: BackgroundScheduler,
    messageEncrypter: MessageEncrypter,
) : MessageService, MatrixTaskRunner {

    private val sendMessageUseCase = SendMessageUseCase(httpClient, messageEncrypter)
    private val sendEventMessageUseCase = SendEventMessageUseCase(httpClient)

    override suspend fun canRun(task: MatrixTaskRunner.MatrixTask) = task.type == "text-message"

    override suspend fun run(task: MatrixTaskRunner.MatrixTask): MatrixTaskRunner.TaskResult {
        require(task.type == "text-message")
        val message = Json.decodeFromString(MessageService.Message.TextMessage.serializer(), task.jsonPayload)
        return try {
            sendMessage(message)
            MatrixTaskRunner.TaskResult.Success
        } catch (error: Throwable) {
            val canRetry = error is UnknownHostException || error is SocketException
            MatrixTaskRunner.TaskResult.Failure(canRetry)
        }
    }

    override fun localEchos(roomId: RoomId): Flow<List<MessageService.LocalEcho>> {
        return localEchoStore.observeLocalEchos(roomId)
    }

    override fun localEchos(): Flow<Map<RoomId, List<MessageService.LocalEcho>>> {
        return localEchoStore.observeLocalEchos()
    }

    override suspend fun scheduleMessage(message: MessageService.Message) {
        localEchoStore.markSending(message)
        val localId = when (message) {
            is MessageService.Message.TextMessage -> message.localId
        }
        backgroundScheduler.schedule(key = localId, message.toTask())
    }

    override suspend fun sendMessage(message: MessageService.Message) {
        localEchoStore.messageTransaction(message) {
            sendMessageUseCase.sendMessage(message)
        }
    }

    private fun MessageService.Message.toTask(): BackgroundScheduler.Task {
        return when (this) {
            is MessageService.Message.TextMessage -> {
                BackgroundScheduler.Task(type = "text-message", Json.encodeToString(MessageService.Message.TextMessage.serializer(), this))
            }
        }
    }

    override suspend fun sendEventMessage(roomId: RoomId, message: MessageService.EventMessage) {
        sendEventMessageUseCase.sendMessage(roomId, message)
    }
}