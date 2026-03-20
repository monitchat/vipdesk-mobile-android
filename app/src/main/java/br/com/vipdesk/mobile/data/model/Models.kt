package br.com.vipdesk.mobile.data.model

import com.google.gson.annotations.SerializedName

// ============ AUTH ============

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token") val token: String,
    val user: User? = null
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String? = null,
    @SerializedName("company_id") val companyId: Int? = null,
    @SerializedName("department_id") val departmentId: Int? = null,
    val role: String? = null
)

// ============ CONVERSATION ============

data class Conversation(
    val id: Int,
    val contact: Contact,
    val source: String? = null,
    @SerializedName("last_message") val lastMessage: LastMessage? = null,
    @SerializedName("last_ticket_source") val lastTicketSource: String? = null,
    @SerializedName("unread_messages") val unreadMessages: Int = 0,
    @SerializedName("auto_reply") val autoReply: Int = 0,
    @SerializedName("activeTicket") val activeTicket: Ticket? = null,
    @SerializedName("conversationState") val conversationState: String? = null,
    @SerializedName("new") val isNew: Boolean = false
)

data class Contact(
    val id: Int,
    val name: String,
    val avatar: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val email: String? = null,
    val client: Client? = null
)

data class Client(
    val id: Int? = null,
    val name: String? = null
)

data class LastMessage(
    val message: String? = null,
    val type: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("created_at_raw") val createdAtRaw: String? = null,
    @SerializedName("last_message_sender") val lastMessageSender: Int? = null,
    @SerializedName("minutes_sice_last_message") val minutesSinceLastMessage: Int? = null
)

// ============ MESSAGE ============

data class Message(
    val id: Int,
    val message: String? = null,
    @SerializedName("message_type") val messageType: Int = 0,
    val type: String? = null,
    val sender: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("conversation_id") val conversationId: Int? = null,
    @SerializedName("ticket_id") val ticketId: Int? = null,
    @SerializedName("user_id") val userId: Int? = null,
    val user: User? = null,
    val src: String? = null,
    val media: String? = null,
    @SerializedName("media_url") val mediaUrl: String? = null,
    @SerializedName("file_name") val fileName: String? = null,
    @SerializedName("quoted_message") val quotedMessage: Message? = null,
    val status: Int? = null,
    val source: String? = null
) {
    fun getMediaUrl(cdnUrl: String): String? {
        val url = src ?: media ?: mediaUrl ?: return null
        return if (url.startsWith("http")) url
        else "$cdnUrl/app/$url"
    }
}

data class SendMessageRequest(
    val message: String,
    @SerializedName("account_number") val accountNumber: String? = null,
    @SerializedName("conversation_id") val conversationId: Int? = null,
    @SerializedName("ticket_id") val ticketId: Int? = null,
    val sender: Int = 1,
    @SerializedName("message_type") val messageType: Int = 0,
    val source: String? = null
)

// ============ TICKET ============

data class Ticket(
    val id: Int,
    val title: String? = null,
    val status: String? = null,
    @SerializedName("status_name") val statusName: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("department_id") val departmentId: Int? = null,
    @SerializedName("department_color") val departmentColor: String? = null,
    @SerializedName("department_name") val departmentName: String? = null,
    @SerializedName("conversation_id") val conversationId: Int? = null,
    @SerializedName("contact_id") val contactId: Int? = null,
    val priority: String? = null,
    val source: String? = null,
    val user: User? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class ChangeOwnerRequest(
    @SerializedName("ticket_id") val ticketId: Int,
    @SerializedName("user_id") val userId: Int
)

data class ChangeStatusRequest(
    @SerializedName("ticket_id") val ticketId: Int,
    val status: String
)

// ============ COMMENT ============

data class Comment(
    val id: Int? = null,
    val message: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("ticket_id") val ticketId: Int? = null,
    @SerializedName("conversation_id") val conversationId: Int? = null,
    val user: User? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class SendCommentRequest(
    val message: String,
    @SerializedName("ticket_id") val ticketId: Int? = null,
    @SerializedName("conversation_id") val conversationId: Int? = null
)

// ============ DEPARTMENT ============

data class Department(
    val id: Int,
    val name: String,
    val color: String? = null
)

// ============ API RESPONSES ============

data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val success: Boolean = true
)

data class PaginatedResponse<T>(
    val data: List<T> = emptyList(),
    val total: Int = 0,
    val page: Int = 1
)

data class ConversationCountResponse(
    val assigned: Int = 0,
    val waiting: Int = 0,
    val total: Int = 0
)

data class ConversationListResponse(
    val data: List<Conversation> = emptyList(),
    @SerializedName("total_records") val totalRecords: Int = 0,
    @SerializedName("total_assigned") val totalAssigned: Int = 0,
    @SerializedName("total_waiting") val totalWaiting: Int = 0,
    val whatsapp: Int = 0,
    val facebook: Int = 0,
    val instagram: Int = 0,
    val telegram: Int = 0,
    val webchat: Int = 0,
    val monitcall: Int = 0
)

data class ChannelCounts(
    val whatsapp: Int = 0,
    val facebook: Int = 0,
    val instagram: Int = 0,
    val telegram: Int = 0,
    val webchat: Int = 0,
    val monitcall: Int = 0
)
