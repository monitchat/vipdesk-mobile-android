package br.com.vipdesk.mobile.data.api

import br.com.vipdesk.mobile.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ============ AUTH ============

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(@Body body: Map<String, Boolean> = mapOf("ignore_revoke" to false)): Response<Any>

    @POST("auth/ping")
    suspend fun ping(): Response<Any>

    // ============ USER ============

    @GET("user")
    suspend fun getUsers(): Response<ApiResponse<List<User>>>

    // ============ CONTACTS (CRM) ============

    @GET("contact")
    suspend fun getContacts(
        @Query("search") search: String? = null,
        @Query("take") take: Int = 25,
        @Query("skip") skip: Int = 0
    ): Response<ContactListResponse>

    @POST("contact")
    suspend fun createContact(@Body body: CreateContactRequest): Response<CreateContactResponse>

    // Conversa existente do contato (para abrir chat a partir do CRM).
    // Resource do Laravel: pode vir embrulhado em "data" ou não.
    @GET("contact/{id}/conversation")
    suspend fun getContactConversation(@Path("id") contactId: Int): Response<com.google.gson.JsonObject>

    @POST("ticket")
    suspend fun createTicket(@Body body: CreateTicketRequest): Response<CreateTicketResponse>

    // ============ CRM / NEGÓCIOS (deals) ============

    @GET("deal")
    suspend fun getDeals(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("take") take: Int = 30,
        @Query("skip") skip: Int = 0
    ): Response<DealListResponse>

    @POST("deal")
    suspend fun createDeal(@Body body: CreateDealRequest): Response<DealEnvelope>

    @PUT("deal/{id}/move-stage")
    suspend fun moveDealStage(
        @Path("id") dealId: Int,
        @Body body: MoveDealStageRequest
    ): Response<DealEnvelope>

    @PUT("deal/{id}/close")
    suspend fun closeDeal(
        @Path("id") dealId: Int,
        @Body body: CloseDealRequest
    ): Response<DealEnvelope>

    @GET("pipeline")
    suspend fun getPipelines(): Response<PipelineListResponse>

    @GET("loss-reason")
    suspend fun getLossReasons(): Response<LossReasonListResponse>

    @GET("user/{id}/info/")
    suspend fun getUserInfo(@Path("id") userId: Int): Response<User>

    // ============ CONVERSATIONS ============

    @GET("conversation")
    suspend fun getConversations(
        @QueryMap params: Map<String, String> = emptyMap()
    ): Response<ConversationListResponse>

    @GET("conversation/{id}")
    suspend fun getConversation(
        @Path("id") conversationId: Int,
        @QueryMap params: Map<String, String> = emptyMap()
    ): Response<ApiResponse<ConversationDetail>>

    @GET("conversation/{id}/read")
    suspend fun markAsRead(@Path("id") conversationId: Int): Response<Any>

    @GET("conversation/count")
    suspend fun getConversationCount(): Response<ConversationCountResponse>

    @GET("conversation/{id}/messages")
    suspend fun getMessages(
        @Path("id") conversationId: Int,
        @Query("take") take: Int = 15,
        @Query("skip") skip: Int = 0
    ): Response<ApiResponse<List<Message>>>

    @GET("conversation/{id}/comments")
    suspend fun getComments(
        @Path("id") conversationId: Int,
        @Query("take") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<ApiResponse<List<Comment>>>

    @GET("conversation/{id}/tickets")
    suspend fun getConversationTickets(
        @Path("id") conversationId: Int,
        @Query("take") take: Int = 30,
        @Query("skip") skip: Int = 0
    ): Response<List<Ticket>>

    @GET("conversation/{id}/stats")
    suspend fun getConversationStats(
        @Path("id") conversationId: Int
    ): Response<Any>

    // ============ MESSAGES ============

    @POST("conversation/{id}/message")
    suspend fun sendTextMessage(
        @Path("id") conversationId: Int,
        @Body message: SendMessageRequest
    ): Response<ApiResponse<Message>>

    @POST("conversation/{id}/imageMessage")
    suspend fun sendImageMessage(
        @Path("id") conversationId: Int,
        @Body message: Map<String, Any>
    ): Response<ApiResponse<Message>>

    @POST("conversation/{id}/documentMessage")
    suspend fun sendDocumentMessage(
        @Path("id") conversationId: Int,
        @Body message: Map<String, Any>
    ): Response<ApiResponse<Message>>

    @POST("conversation/{id}/voiceMessage")
    suspend fun sendVoiceMessage(
        @Path("id") conversationId: Int,
        @Body message: Map<String, Any>
    ): Response<ApiResponse<Message>>

    @Multipart
    @POST("conversation-file/{id}")
    suspend fun uploadFile(
        @Path("id") conversationId: Int,
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody? = null
    ): Response<ApiResponse<Any>>

    // ============ COMMENTS ============

    @POST("comment")
    suspend fun sendComment(@Body comment: SendCommentRequest): Response<ApiResponse<Comment>>

    @GET("comment")
    suspend fun getTicketComments(
        @Query("ticket_id") ticketId: Int
    ): Response<ApiResponse<List<Comment>>>

    // ============ TICKETS ============

    @GET("ticket")
    suspend fun getTickets(
        @QueryMap params: Map<String, String> = emptyMap()
    ): Response<ApiResponse<List<Ticket>>>

    @GET("ticket/{id}")
    suspend fun getTicket(@Path("id") ticketId: Int): Response<ApiResponse<Ticket>>

    @PUT("ticket/{id}")
    suspend fun updateTicket(
        @Path("id") ticketId: Int,
        @Body ticket: Map<String, Any>
    ): Response<ApiResponse<Ticket>>

    @POST("ticket/setTicketOwner")
    suspend fun changeTicketOwner(
        @Body request: ChangeOwnerRequest
    ): Response<ApiResponse<Ticket>>

    @POST("ticket/setTicketStatus")
    suspend fun changeTicketStatus(
        @Body request: ChangeStatusRequest
    ): Response<ApiResponse<Ticket>>

    // ============ DEPARTMENTS ============

    @GET("department")
    suspend fun getDepartments(): Response<ApiResponse<List<Department>>>

    // ============ SEARCH ============

    @GET("conversation/{id}/searchMessages")
    suspend fun searchMessages(
        @Path("id") conversationId: Int,
        @Query("search") search: String,
        @Query("take") take: Int = 20,
        @Query("page") page: Int = 1
    ): Response<ApiResponse<List<Message>>>

    // ============ MOBILE ============

    @GET("mobile/dashboard")
    suspend fun getMobileDashboard(): Response<MobileDashboardEnvelope>

    @GET("mobile/tickets")
    suspend fun getMobileTickets(
        @Query("status") status: String = "open",
        @Query("q") q: String? = null
    ): Response<MobileTicketsEnvelope>

    @GET("mobile/notifications")
    suspend fun getMobileNotifications(
        @Query("filter") filter: String? = null
    ): Response<MobileNotificationsEnvelope>

    @POST("mobile/notifications/read-all")
    suspend fun readAllNotifications(): Response<Any>

    @POST("mobile/notifications/{id}/read")
    suspend fun readNotification(@Path("id") id: Int): Response<Any>

    @GET("mobile/ticket/{id}")
    suspend fun getMobileTicket(@Path("id") id: Int): Response<MobileTicketEnvelope>

    @POST("mobile/ticket/{id}/resolve")
    suspend fun resolveTicket(@Path("id") id: Int): Response<Any>

    @POST("ticket/{id}/claim")
    suspend fun claimTicket(
        @Path("id") id: Int,
        @Body body: Map<String, String> = emptyMap()
    ): Response<Any>
}
