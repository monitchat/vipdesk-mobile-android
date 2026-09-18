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

    @POST("auth/mfa/verify")
    suspend fun verifyMfa(@Body request: MfaVerifyRequest): Response<LoginResponse>

    @POST("auth/mfa/resend-email")
    suspend fun resendMfaEmail(@Body request: MfaResendRequest): Response<com.google.gson.JsonObject>

    @POST("auth/logout")
    suspend fun logout(@Body body: Map<String, Boolean> = mapOf("ignore_revoke" to false)): Response<Any>

    @POST("auth/ping")
    suspend fun ping(): Response<Any>

    // ============ USER ============

    @GET("user")
    suspend fun getUsers(): Response<ApiResponse<List<User>>>

    /** Mensagens rápidas da empresa (o web usa only_fast_menu=1 no composer). */
    @GET("fast-message")
    suspend fun getFastMessages(
        @Query("only_fast_menu") onlyFastMenu: Int = 1,
        @Query("take") take: Int = 100
    ): Response<ApiResponse<List<FastMessage>>>

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
        @Query("skip") skip: Int = 0,
        @Query("pipeline_id") pipelineId: Int? = null,
        @Query("contact_id") contactId: Int? = null,
        @Query("view") view: String? = null
    ): Response<DealListResponse>

    @GET("deal/{id}")
    suspend fun getDeal(@Path("id") dealId: Int): Response<DealEnvelope>

    @GET("contact/{id}")
    suspend fun getContact(@Path("id") contactId: Int): Response<com.google.gson.JsonObject>

    @GET("contact/{id}/conversations")
    suspend fun getContactConversations(@Path("id") contactId: Int): Response<com.google.gson.JsonElement>

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

    /** Estado de pausa atual: `currentPause` é null quando disponível. */
    @GET("user/{id}/info/")
    suspend fun getUserInfoRaw(@Path("id") userId: Int): Response<com.google.gson.JsonObject>

    // ============ GENÉRICO (módulos de lista: campanhas, KB, aprovações, etc.) ============

    @GET
    suspend fun getRaw(@Url path: String, @QueryMap params: Map<String, String> = emptyMap()): Response<com.google.gson.JsonElement>

    @POST
    suspend fun postRaw(@Url path: String, @Body body: com.google.gson.JsonObject = com.google.gson.JsonObject()): Response<com.google.gson.JsonElement>

    @PUT
    suspend fun putRaw(@Url path: String, @Body body: com.google.gson.JsonObject = com.google.gson.JsonObject()): Response<com.google.gson.JsonElement>

    @DELETE
    suspend fun deleteRaw(@Url path: String): Response<com.google.gson.JsonElement>

    // ============ KANBAN (quadros de tarefas) ============

    @GET("kanban/boards")
    suspend fun getKanbanBoards(): Response<com.google.gson.JsonObject>

    @GET("kanban/boards/{id}")
    suspend fun getKanbanBoard(@Path("id") boardId: Int): Response<com.google.gson.JsonObject>

    @GET("kanban/boards/{id}/tasks")
    suspend fun getKanbanTasks(@Path("id") boardId: Int, @Query("limit") limit: Int = 200): Response<com.google.gson.JsonElement>

    @POST("kanban/boards/{id}/tasks")
    suspend fun createKanbanTask(@Path("id") boardId: Int, @Body body: com.google.gson.JsonObject): Response<com.google.gson.JsonObject>

    @POST("kanban/boards/{id}/tasks/{taskId}/move")
    suspend fun moveKanbanTask(@Path("id") boardId: Int, @Path("taskId") taskId: Int, @Body body: com.google.gson.JsonObject): Response<com.google.gson.JsonObject>

    @POST("kanban/boards/{id}/tasks/{taskId}/archive")
    suspend fun archiveKanbanTask(@Path("id") boardId: Int, @Path("taskId") taskId: Int, @Body body: com.google.gson.JsonObject): Response<com.google.gson.JsonObject>

    @GET("kanban/boards/{id}/tasks/{taskId}/checklist")
    suspend fun getKanbanChecklist(@Path("id") boardId: Int, @Path("taskId") taskId: Int): Response<com.google.gson.JsonObject>

    @POST("kanban/boards/{id}/tasks/{taskId}/checklist")
    suspend fun createKanbanChecklistItem(@Path("id") boardId: Int, @Path("taskId") taskId: Int, @Body body: com.google.gson.JsonObject): Response<com.google.gson.JsonObject>

    @PUT("kanban/boards/{id}/tasks/{taskId}/checklist/{itemId}")
    suspend fun updateKanbanChecklistItem(@Path("id") boardId: Int, @Path("taskId") taskId: Int, @Path("itemId") itemId: Int, @Body body: com.google.gson.JsonObject): Response<com.google.gson.JsonObject>

    @GET("kanban/boards/{id}/tasks/{taskId}/comments")
    suspend fun getKanbanComments(@Path("id") boardId: Int, @Path("taskId") taskId: Int): Response<com.google.gson.JsonObject>

    @POST("kanban/boards/{id}/tasks/{taskId}/comments")
    suspend fun createKanbanComment(@Path("id") boardId: Int, @Path("taskId") taskId: Int, @Body body: com.google.gson.JsonObject): Response<com.google.gson.JsonObject>

    @GET("ticket-status")
    suspend fun getTicketStatuses(@Query("take") take: Int = 100): Response<ApiResponse<List<TicketStatusOption>>>

    @GET("interruption-type")
    suspend fun getInterruptionTypes(@Query("take") take: Int = 100): Response<ApiResponse<List<InterruptionType>>>

    /** Alterna a pausa do usuário (entra com o motivo; sai se já estiver pausado). */
    @POST("user/pause/{id}")
    suspend fun pauseUser(@Path("id") userId: Int, @Body body: com.google.gson.JsonObject): Response<com.google.gson.JsonObject>

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

    // Responde array puro (sem envelope `data`); o repositório aceita os dois formatos.
    @GET("conversation/{id}/comments")
    suspend fun getComments(
        @Path("id") conversationId: Int,
        @Query("take") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<com.google.gson.JsonElement>

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
        @Body message: com.google.gson.JsonObject
    ): Response<ApiResponse<Message>>

    @POST("conversation/{id}/documentMessage")
    suspend fun sendDocumentMessage(
        @Path("id") conversationId: Int,
        @Body message: com.google.gson.JsonObject
    ): Response<ApiResponse<Message>>

    @POST("conversation/{id}/voiceMessage")
    suspend fun sendVoiceMessage(
        @Path("id") conversationId: Int,
        @Body message: com.google.gson.JsonObject
    ): Response<ApiResponse<Message>>

    @Multipart
    @POST("conversation-file/{id}")
    suspend fun uploadFile(
        @Path("id") conversationId: Int,
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody? = null
    ): Response<com.google.gson.JsonObject>

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

    // ============ AGENDA ============

    // O ResourceCollection do Laravel pode vir como array puro ou {"data":[...]}
    @GET("appointments/range")
    suspend fun getAppointmentsRange(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("professional_id") professionalId: Int? = null
    ): Response<com.google.gson.JsonElement>

    @PUT("appointments/{id}/confirm")
    suspend fun confirmAppointment(@Path("id") id: Int): Response<Any>

    // ============ ESTATÍSTICAS (dashboard — mesmos endpoints da web) ============

    @GET("statistic")
    suspend fun getStatistics(
        @Query("filter") filterJson: String,
        // no_cache=1: o backend cacheia períodos longos por 5 min; o app sempre quer o estado atual
        @Query("no_cache") noCache: Int
    ): Response<StatisticsResponse>

    @GET("statistic/statisticsCount")
    suspend fun getStatisticsCount(
        @Query("filter") filterJson: String,
        @Query("no_cache") noCache: Int
    ): Response<StatisticsCountResponse>

    // ============ MOBILE ============

    @GET("mobile/dashboard")
    suspend fun getMobileDashboard(): Response<MobileDashboardEnvelope>

    @GET("mobile/tickets")
    suspend fun getMobileTickets(
        @Query("status") status: String = "open",
        @Query("q") q: String? = null,
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 80
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
