package com.example.cursy.features.chat.data.repositories

import android.util.Log
import com.example.cursy.core.di.AuthManager
import com.example.cursy.core.network.CoursyApi
import com.example.cursy.core.network.CreateConversationRequest
import com.example.cursy.features.chat.data.local.dao.ChatDao
import com.example.cursy.features.chat.data.local.mapper.toDomain
import com.example.cursy.features.chat.data.local.mapper.toEntity
import com.example.cursy.features.chat.data.remote.dto.WsMessageDto
import com.example.cursy.features.chat.data.remote.mapper.toDomain
import com.example.cursy.features.chat.domain.entities.ChatUser
import com.example.cursy.features.chat.domain.entities.Conversation
import com.example.cursy.features.chat.domain.entities.Message
import com.example.cursy.features.chat.domain.entities.MessageStatus
import com.example.cursy.features.chat.domain.repositories.ChatRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.HttpException
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val api: CoursyApi,
    private val client: OkHttpClient,
    private val authManager: AuthManager,
    private val chatDao: ChatDao
) : ChatRepository {

    private val gson = Gson()
    private val _userStatusesFlow = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private var webSocket: WebSocket? = null
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val conversationReceiverCache = java.util.concurrent.ConcurrentHashMap<String, String>()


    // Observa los mensajes desde Room (fuente de verdad)
    override fun observeMessagesFromDb(conversationId: String): Flow<List<Message>> {
        return chatDao.getMessagesByConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getConversations(): Result<List<Conversation>> {
        return try {
            val response = api.getConversations()
            val conversations = response.map { it.toDomain() }
            conversations.forEach { conv ->
                conversationReceiverCache[conv.id] = conv.otherUserId
            }
            Result.success(conversations)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtiene mensajes del servidor y los guarda en Room
    override suspend fun getMessages(conversationId: String): Result<List<Message>> {
        return try {
            val response = api.getMessages(conversationId)
            val messages = response.map { it.toDomain() }

            // Limpiar historial local del servidor antes de insertar lo nuevo
            chatDao.clearServerMessages(conversationId)
            chatDao.insertMessages(messages.map { it.toEntity() })

            val myId = authManager.getCurrentUserId()
            messages.firstOrNull { it.senderId != myId }?.let {
                conversationReceiverCache[conversationId] = it.senderId
            }

            Result.success(messages)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createConversation(otherUserId: String): Result<Conversation> {
        return try {
            val response = api.createConversation(CreateConversationRequest(otherUserId))
            val conversation = response.toDomain()
            conversationReceiverCache[conversation.id] = otherUserId
            Result.success(conversation)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String?): Result<List<ChatUser>> {
        return try {
            val response = api.getUsers(query)
            Result.success(response.users.map { userResponse ->
                ChatUser(
                    id = userResponse.id,
                    name = userResponse.name,
                    email = userResponse.email,
                    profileImage = userResponse.profileImage ?: "",
                    bio = userResponse.bio
                )
            })
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Inicia la conexión WebSocket para mensajería en tiempo real
    override fun startSession() {
        if (webSocket != null) return

        val token = authManager.getAuthToken()
        if (token.isNullOrEmpty()) return

        val request = Request.Builder()
            .url("ws://52.20.206.74:8080/api/v1/ws")
            .addHeader("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ChatWS", "Conexión WebSocket abierta")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val wsMessage = gson.fromJson(text, WsMessageDto::class.java)
                    val myId = authManager.getCurrentUserId()

                    if (wsMessage.type == "user_status") {
                        val isOnline = wsMessage.content == "online"
                        wsMessage.senderId?.let { uid ->
                            _userStatusesFlow.update { it + (uid to isOnline) }
                        }
                        return
                    }

                    val resolvedSenderId = wsMessage.senderId
                        ?: if (wsMessage.receiverId != myId) myId else (conversationReceiverCache[wsMessage.conversationId ?: ""] ?: "")

                    val convId = wsMessage.conversationId
                    if (resolvedSenderId.isNullOrEmpty() || convId.isNullOrEmpty()) return

                    // Si el mensaje lo enviamos nosotros, lo ignoramos porque ya está en Room
                    // gracias a la inserción optimista (estado PENDING).
                    if (resolvedSenderId == myId) return

                    val domainMessage = Message(
                        id = java.util.UUID.randomUUID().toString().replace("-", "").take(24),
                        conversationId = convId,
                        senderId = resolvedSenderId,
                        content = wsMessage.content,
                        createdAt = java.util.Date(),
                        type = wsMessage.type ?: "chat",
                        status = MessageStatus.SENT
                    )

                    repositoryScope.launch {
                        // Verificar si este mismo mensaje acaba de ser insertado
                        val dupes = chatDao.countDuplicates(convId, resolvedSenderId, wsMessage.content)
                        if (dupes == 0) {
                            chatDao.insertMessage(domainMessage.toEntity())
                        }
                    }

                } catch (e: Exception) {
                    Log.e("ChatWS", "Error al procesar mensaje WebSocket", e)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("ChatWS", "WebSocket cerrándose: $code / $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@ChatRepositoryImpl.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ChatWS", "Error en WebSocket: ${t.message}", t)
                this@ChatRepositoryImpl.webSocket = null
            }
        })
    }

    override fun endSession() {
        webSocket?.close(1000, "Usuario salió del chat")
        webSocket = null
    }

    override fun observeUserStatuses(): StateFlow<Map<String, Boolean>> {
        return _userStatusesFlow.asStateFlow()
    }

    override suspend fun fetchOnlineUsers() {
        try {
            val response = api.getOnlineUsers()
            val myId = authManager.getCurrentUserId()
            val onlineMap = response.onlineUsers
                .filter { it != myId }
                .associateWith { true }
            _userStatusesFlow.update { it + onlineMap }
        } catch (e: Exception) {
            Log.e("ChatWS", "Error al obtener usuarios en línea", e)
        }
    }

    // Envía un mensaje con actualización optimista y rollback
    override suspend fun sendMessage(conversationId: String, receiverId: String, content: String): Result<Unit> {
        val ws = webSocket ?: run {
            startSession()
            return Result.failure(Exception("WebSocket no conectado"))
        }

        val finalReceiverId = if (receiverId.isEmpty()) {
            conversationReceiverCache[conversationId] ?: ""
        } else {
            conversationReceiverCache[conversationId] = receiverId
            receiverId
        }

        if (finalReceiverId.isEmpty()) {
            return Result.failure(Exception("No se encontró el destinatario"))
        }

        // Actualización optimista: insertar mensaje como PENDING
        val localId = "local_${System.currentTimeMillis()}"
        val pendingMessage = Message(
            id = localId,
            conversationId = conversationId,
            senderId = authManager.getCurrentUserId() ?: "",
            content = content,
            createdAt = java.util.Date(),
            status = MessageStatus.PENDING
        )
        chatDao.insertMessage(pendingMessage.toEntity())

        // Enviar por WebSocket
        return try {
            val messageDto = WsMessageDto(
                conversationId = conversationId,
                receiverId = finalReceiverId,
                content = content
            )
            val json = gson.toJson(messageDto)
            val success = ws.send(json)

            if (success) {
                chatDao.updateMessageStatus(localId, MessageStatus.SENT.name)
                Result.success(Unit)
            } else {
                // Rollback: marcar como fallido
                chatDao.updateMessageStatus(localId, MessageStatus.FAILED.name)
                Result.failure(Exception("Error al enviar mensaje"))
            }
        } catch (e: Exception) {
            // Rollback: marcar como fallido
            chatDao.updateMessageStatus(localId, MessageStatus.FAILED.name)
            Result.failure(e)
        }
    }
}
