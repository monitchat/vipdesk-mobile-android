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

data class ConversationDetail(
    val id: Int,
    val contact: Contact,
    val source: String? = null,
    @SerializedName("last_message") val lastMessage: LastMessage? = null,
    @SerializedName("last_ticket_source") val lastTicketSource: String? = null,
    @SerializedName("unread_messages") val unreadMessages: Int = 0,
    @SerializedName("auto_reply") val autoReply: Int = 0,
    @SerializedName("activeTicket") val activeTicket: Ticket? = null,
    val messages: List<Message> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val tickets: List<Ticket> = emptyList(),
    @SerializedName("account_number") val accountNumber: String? = null
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

// ============ MOBILE (endpoints /mobile/*) ============

data class MobileDashboardEnvelope(val data: MobileDashboard = MobileDashboard())

data class MobileDashboard(
    @SerializedName("tickets_open") val ticketsOpen: StatMetric = StatMetric(),
    val sla: SlaMetric = SlaMetric(),
    @SerializedName("avg_response") val avgResponse: AvgResponseMetric = AvgResponseMetric(),
    @SerializedName("agents_online") val agentsOnline: AgentsMetric = AgentsMetric(),
    @SerializedName("recent_tickets") val recentTickets: List<RecentTicket> = emptyList()
)

data class StatMetric(
    val value: Int = 0,
    @SerializedName("delta_pct") val deltaPct: Int = 0,
    @SerializedName("delta_label") val deltaLabel: String? = null
)

data class SlaMetric(
    val value: Int = 0,
    val unit: String = "%",
    val target: Int = 90,
    val within: Boolean = true,
    val status: String = ""
)

data class AvgResponseMetric(
    val seconds: Int? = null,
    val label: String = "—",
    @SerializedName("delta_label") val deltaLabel: String? = null,
    val improved: Boolean? = null
)

data class AgentsMetric(
    val value: Int = 0,
    val status: String = ""
)

data class RecentTicket(
    val id: Int,
    @SerializedName("ticket_number") val ticketNumber: String? = null,
    val title: String? = null,
    val priority: String? = null,
    val status: String? = null,
    @SerializedName("contact_name") val contactName: String = "Contato",
    @SerializedName("contact_avatar") val contactAvatar: String? = null,
    @SerializedName("owner_name") val ownerName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class MobileNotificationsEnvelope(
    val data: List<MobileNotification> = emptyList(),
    @SerializedName("unread_count") val unreadCount: Int = 0
)

data class MobileNotification(
    val id: Int,
    val message: String = "",
    val type: Int = 0,
    val kind: String = "general",
    val read: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

data class MobileTicketEnvelope(val data: MobileTicket? = null)

data class MobileTicket(
    val id: Int,
    @SerializedName("ticket_number") val ticketNumber: String? = null,
    val title: String? = null,
    val status: String? = null,
    @SerializedName("status_id") val statusId: Int? = null,
    @SerializedName("is_open") val isOpen: Boolean = true,
    val priority: String? = null,
    val sla: TicketSla? = null,
    val channel: String? = null,
    val department: String? = null,
    val responsible: PersonRef? = null,
    val contact: PersonRef? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("closed_at") val closedAt: String? = null,
    val timeline: List<TimelineEvent> = emptyList()
)

data class TicketSla(
    val name: String? = null,
    val within: Boolean = true,
    val label: String = ""
)

data class PersonRef(
    val id: Int? = null,
    val name: String = "",
    val avatar: String? = null
)

data class TimelineEvent(
    val type: String = "log",
    val message: String = "",
    val user: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// ============ CRM / CRIAÇÃO ============

data class ApiContact(
    val id: Int,
    val name: String = "",
    val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val city: String? = null
)

data class ContactListResponse(
    @SerializedName("total_records") val totalRecords: Int = 0,
    val data: List<ApiContact> = emptyList()
)

data class CreateContactRequest(
    val name: String,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val email: String? = null
)

data class CreateContactResponse(
    val data: ApiContact? = null,
    val message: String? = null
)

data class CreateTicketRequest(
    @SerializedName("contact_id") val contactId: Int,
    val title: String,
    val description: String? = null,
    val priority: String? = null,
    @SerializedName("department_id") val departmentId: Int? = null,
    val source: String = "whatsapp"
)

data class CreateTicketResponse(
    val ticket: Ticket? = null,
    val message: String? = null
)

// ============ CRM / NEGÓCIOS (deals) ============

data class DealStage(
    val id: Int,
    val name: String = "",
    @SerializedName("is_won") val isWon: Boolean = false,
    @SerializedName("is_lost") val isLost: Boolean = false,
    val color: String? = null
)

data class DealPipeline(
    val id: Int,
    val name: String = "",
    @SerializedName("is_default") val isDefault: Boolean = false,
    val stages: List<DealStage> = emptyList()
)

data class PipelineListResponse(val data: List<DealPipeline> = emptyList())

data class ApiDeal(
    val id: Int,
    val title: String = "",
    val value: Double? = null,
    val currency: String? = "BRL",
    val status: String? = "open",
    val stage: DealStage? = null,
    val pipeline: DealPipeline? = null,
    val contact: ApiContact? = null,
    val owner: User? = null,
    @SerializedName("expected_close_date") val expectedCloseDate: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class DealListResponse(
    val data: List<ApiDeal> = emptyList(),
    @SerializedName("total_records") val totalRecords: Int = 0
)

data class CreateDealRequest(
    val title: String,
    val value: Double? = null,
    @SerializedName("contact_id") val contactId: Int? = null,
    @SerializedName("pipeline_id") val pipelineId: Int? = null,
    @SerializedName("stage_id") val stageId: Int? = null,
    val description: String? = null,
    val source: String? = "mobile"
)

data class DealEnvelope(
    val data: ApiDeal? = null,
    val message: String? = null
)

data class MoveDealStageRequest(@SerializedName("stage_id") val stageId: Int)

data class CloseDealRequest(
    val result: String, // won | lost
    @SerializedName("lost_reason_id") val lostReasonId: Int? = null
)

data class LossReason(val id: Int, val name: String = "")

data class LossReasonListResponse(val data: List<LossReason> = emptyList())

data class MobileTicketsEnvelope(val data: List<TicketListItem> = emptyList())

data class TicketListItem(
    val id: Int,
    @SerializedName("ticket_number") val ticketNumber: String? = null,
    val title: String? = null,
    val priority: String? = null,
    val status: String? = null,
    @SerializedName("is_open") val isOpen: Boolean = true,
    @SerializedName("contact_name") val contactName: String = "Contato",
    @SerializedName("contact_avatar") val contactAvatar: String? = null,
    @SerializedName("owner_name") val ownerName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)
