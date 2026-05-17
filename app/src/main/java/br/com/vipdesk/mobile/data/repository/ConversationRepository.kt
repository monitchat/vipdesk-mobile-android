package br.com.vipdesk.mobile.data.repository

import br.com.vipdesk.mobile.data.api.ApiService
import br.com.vipdesk.mobile.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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

    suspend fun uploadFile(conversationId: Int, file: File, mimeType: String): Result<Any> {
        return try {
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val typePart = mimeType.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = apiService.uploadFile(conversationId, filePart, typePart)
            if (response.isSuccessful) {
                Result.success(response.body() ?: Any())
            } else {
                Result.failure(Exception("Erro ao enviar arquivo"))
            }
        } catch (e: Exception) {
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
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao buscar comentarios"))
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
                ticketId = ticketId
            )
            val response = apiService.sendComment(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao enviar comentario"))
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

    suspend fun changeTicketOwner(ticketId: Int, userId: Int): Result<Ticket> {
        return try {
            val response = apiService.changeTicketOwner(ChangeOwnerRequest(ticketId, userId))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao transferir atendimento"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changeTicketStatus(ticketId: Int, status: String): Result<Ticket> {
        return try {
            val response = apiService.changeTicketStatus(ChangeStatusRequest(ticketId, status))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao alterar status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
