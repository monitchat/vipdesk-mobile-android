package br.com.vipdesk.mobile.data.repository

import br.com.vipdesk.mobile.data.api.ApiService
import br.com.vipdesk.mobile.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

private const val SPACES_CDN = "https://monitchat.nyc3.digitaloceanspaces.com/app"

class ConversationRepository(private val apiService: ApiService) {

    var lastCounts: ConversationCountResponse? = null
        private set

    var lastChannelCounts: ChannelCounts? = null
        private set

    suspend fun getConversations(
        status: String = "assigned",
        media: String = ""
    ): Result<List<Conversation>> {
        return try {
            val params = mutableMapOf<String, String>()
            params["filter"] = """[["archived","=",0]]"""
            params["status"] = status.ifEmpty { "all" }
            if (media.isNotEmpty()) params["media"] = media
            params["take"] = "100"
            val response = apiService.getConversations(params)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                lastCounts = ConversationCountResponse(
                    assigned = body.totalAssigned,
                    waiting = body.totalWaiting,
                    total = body.totalRecords
                )
                lastChannelCounts = ChannelCounts(
                    whatsapp = body.whatsapp,
                    facebook = body.facebook,
                    instagram = body.instagram,
                    telegram = body.telegram,
                    webchat = body.webchat,
                    monitcall = body.monitcall
                )
                Result.success(body.data)
            } else {
                Result.failure(Exception("Erro ao buscar conversas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConversation(conversationId: Int): Result<ConversationDetail> {
        return try {
            val response = apiService.getConversation(conversationId)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao buscar conversa"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(
        conversationId: Int,
        take: Int = 15,
        skip: Int = 0
    ): Result<List<Message>> {
        return try {
            val response = apiService.getMessages(conversationId, take, skip)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao buscar mensagens"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendTextMessage(
        conversationId: Int,
        message: String,
        ticketId: Int? = null
    ): Result<Message> {
        return try {
            val request = SendMessageRequest(
                message = message,
                conversationId = conversationId,
                ticketId = ticketId,
                sender = 1,
                messageType = 0
            )
            val response = apiService.sendTextMessage(conversationId, request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao enviar mensagem"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Envio de anexo em dois passos, como o web: `conversation-file/{id}` só guarda o
     * arquivo e devolve `src`; a mensagem é criada por `imageMessage` (message_type 4,
     * src com CDN) ou `documentMessage` (message_type 3, src cru).
     */
    suspend fun sendFileMessage(
        conversationId: Int,
        file: File,
        mimeType: String,
        ticketId: Int? = null,
        accountNumber: String? = null,
        userName: String? = null
    ): Result<Unit> {
        return try {
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val typePart = mimeType.toRequestBody("text/plain".toMediaTypeOrNull())
            val upload = apiService.uploadFile(conversationId, filePart, typePart)
            if (!upload.isSuccessful) return Result.failure(Exception(errorMessage(upload, "Erro ao enviar arquivo")))
            val src = upload.body()?.get("src")?.takeIf { !it.isJsonNull }?.asString
                ?: return Result.failure(Exception("Upload sem src"))

            val isImage = mimeType.startsWith("image/")
            val payload = mutableMapOf<String, Any>(
                "message" to "",
                "conversation_id" to conversationId,
                "mime_type" to mimeType,
                "file_name" to file.name,
                "ext" to file.extension,
                "timestamp" to System.currentTimeMillis() / 1000,
                "source" to "message",
                "human_date" to "agora mesmo",
                "status" to 0,
                "sender" to 1,
                "message_token" to java.util.UUID.randomUUID().toString().replace("-", ""),
                "type" to if (isImage) "image" else mimeType,
                "src" to if (isImage) "$SPACES_CDN/$src" else src,
                "message_type" to if (isImage) 4 else 3
            )
            ticketId?.let { payload["ticket_id"] = it }
            accountNumber?.let { payload["account_number"] = it }
            userName?.let { payload["user"] = it }

            val body = com.google.gson.Gson().toJsonTree(payload).asJsonObject
            val response = if (isImage) apiService.sendImageMessage(conversationId, body)
            else apiService.sendDocumentMessage(conversationId, body)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response, "Erro ao enviar arquivo")))
        } catch (e: Exception) {
            android.util.Log.e("VipDeskUpload", "sendFileMessage failed", e)
            Result.failure(e)
        }
    }

    suspend fun getComments(
        conversationId: Int,
        take: Int = 20,
        skip: Int = 0
    ): Result<List<Comment>> {
        return try {
            val response = apiService.getComments(conversationId, take, skip)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                val arr = when {
                    body.isJsonArray -> body.asJsonArray
                    body.isJsonObject && body.asJsonObject.get("data")?.isJsonArray == true -> body.asJsonObject.getAsJsonArray("data")
                    else -> com.google.gson.JsonArray()
                }
                val type = object : com.google.gson.reflect.TypeToken<List<Comment>>() {}.type
                Result.success(br.com.vipdesk.mobile.di.AppContainer.gson.fromJson(arr, type))
            } else {
                Result.failure(Exception("Erro ao carregar comentários"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendComment(
        message: String,
        conversationId: Int? = null,
        ticketId: Int? = null
    ): Result<Comment> {
        return try {
            val request = SendCommentRequest(
                message = message,
                conversationId = conversationId,
                ticketId = ticketId,
                userId = br.com.vipdesk.mobile.di.AppContainer.tokenManager.getUserId()
            )
            val response = apiService.sendComment(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val detail = try {
                    val body = response.errorBody()?.string().orEmpty()
                    com.google.gson.JsonParser.parseString(body).asJsonObject
                        .getAsJsonObject("errors")?.getAsJsonObject("global")?.get("message")?.asString
                } catch (_: Exception) { null }
                Result.failure(Exception(detail?.take(160) ?: "Erro ao enviar comentário (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(conversationId: Int) {
        try {
            apiService.markAsRead(conversationId)
        } catch (_: Exception) { }
    }

    suspend fun getConversationCount(): Result<ConversationCountResponse> {
        return try {
            val response = apiService.getConversationCount()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao buscar contagem"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // setTicketOwner/setTicketStatus respondem {title, message, status} sem `data`;
    // exigir `data` fazia a transferência "falhar" mesmo com HTTP 200.
    suspend fun changeTicketOwner(ticketId: Int, userId: Int): Result<Unit> {
        return try {
            val response = apiService.changeTicketOwner(ChangeOwnerRequest(ticketId, userId))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response, "Erro ao transferir atendimento")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeTicketStatus(ticketId: Int, status: String, pendingReason: String? = null): Result<Unit> {
        return try {
            val response = apiService.changeTicketStatus(ChangeStatusRequest(ticketId.toString(), status, pendingReason))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response, "Erro ao alterar status")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun errorMessage(response: retrofit2.Response<*>, fallback: String): String {
        val raw = runCatching { response.errorBody()?.string() }.getOrNull() ?: return fallback
        return runCatching {
            val obj = com.google.gson.JsonParser.parseString(raw).asJsonObject
            obj.getAsJsonObject("errors")?.getAsJsonObject("global")?.get("message")?.asString
                ?: obj.get("message")?.takeIf { it.isJsonPrimitive }?.asString
        }.getOrNull() ?: fallback
    }

    suspend fun getDepartments(): Result<List<Department>> {
        return try {
            val response = apiService.getDepartments()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao buscar departamentos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConversationTickets(conversationId: Int): Result<List<Ticket>> {
        return try {
            val response = apiService.getConversationTickets(conversationId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao buscar tickets"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
