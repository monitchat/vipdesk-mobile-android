package br.com.vipdesk.mobile.ui.conversations

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.vipdesk.mobile.data.local.TokenManager
import br.com.vipdesk.mobile.data.model.*
import br.com.vipdesk.mobile.data.repository.AuthRepository
import br.com.vipdesk.mobile.data.repository.ConversationRepository
import br.com.vipdesk.mobile.data.socket.SocketEvent
import br.com.vipdesk.mobile.data.socket.SocketService
import br.com.vipdesk.mobile.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class ConversationDetailUiState(
    val conversation: ConversationDetail? = null,
    val messages: List<Message> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val isLoadingMessages: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isSending: Boolean = false,
    val messageText: String = "",
    val commentText: String = "",
    val error: String? = null,
    val currentUserId: Int? = null,
    val users: List<User> = emptyList(),
    val departments: List<Department> = emptyList(),
    val showTransferDialog: Boolean = false,
    val showAssignDialog: Boolean = false,
    val activeTab: Int = 0,
    val canLoadMore: Boolean = true,
    val tickets: List<Ticket> = emptyList(),
    val actionSuccess: String? = null
)

class ConversationDetailViewModel(
    private val conversationId: Int,
    private val conversationRepository: ConversationRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val socketService: SocketService,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationDetailUiState())
    val uiState: StateFlow<ConversationDetailUiState> = _uiState.asStateFlow()

    private var messageSkip = 0
    private val messageTake = 30

    init {
        loadCurrentUser()
        loadAll()
        markAsRead()
        listenToSocketEvents()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            _uiState.value = _uiState.value.copy(currentUserId = userId)
        }
    }

    private fun loadConversation() {
        viewModelScope.launch {
            conversationRepository.getConversation(conversationId).onSuccess { detail ->
                _uiState.value = _uiState.value.copy(
                    conversation = detail,
                    messages = detail.messages,
                    comments = detail.comments,
                    tickets = detail.tickets,
                    isLoadingMessages = false
                )
            }
        }
    }

    /** Load conversation detail + messages */
    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMessages = true)

            // Load conversation detail (includes initial messages)
            conversationRepository.getConversation(conversationId).onSuccess { detail ->
                _uiState.value = _uiState.value.copy(
                    conversation = detail,
                    tickets = detail.tickets
                )
            }

            // Load messages from paginated endpoint (most recent first)
            conversationRepository.getMessages(conversationId, 50, 0).fold(
                onSuccess = { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages, // API returns in chronological order
                        isLoadingMessages = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMessages = false,
                        error = it.message
                    )
                }
            )
        }
    }

    fun loadComments() {
        viewModelScope.launch {
            conversationRepository.getComments(conversationId).onSuccess { comments ->
                _uiState.value = _uiState.value.copy(comments = comments)
            }
        }
    }

    fun onMessageTextChange(text: String) {
        _uiState.value = _uiState.value.copy(messageText = text)
    }

    fun onCommentTextChange(text: String) {
        _uiState.value = _uiState.value.copy(commentText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.messageText.trim()
        if (text.isEmpty() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            val ticketId = _uiState.value.conversation?.activeTicket?.id
            conversationRepository.sendTextMessage(conversationId, text, ticketId).fold(
                onSuccess = { message ->
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + message,
                        messageText = "",
                        isSending = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = it.message
                    )
                }
            )
        }
    }

    fun sendComment() {
        val text = _uiState.value.commentText.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            val ticketId = _uiState.value.conversation?.activeTicket?.id
            conversationRepository.sendComment(text, conversationId, ticketId).fold(
                onSuccess = { comment ->
                    _uiState.value = _uiState.value.copy(
                        comments = _uiState.value.comments + comment,
                        commentText = ""
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(error = it.message)
                }
            )
        }
    }

    fun uploadFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

                var fileName = "file"
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) fileName = cursor.getString(nameIndex)
                    }
                }

                val tempFile = File(context.cacheDir, fileName)
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                conversationRepository.uploadFile(conversationId, tempFile, mimeType).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isSending = false)
                        loadAll()
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            error = "Erro ao enviar arquivo"
                        )
                    }
                )

                tempFile.delete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = "Erro ao processar arquivo"
                )
            }
        }
    }

    fun showTransferDialog() {
        viewModelScope.launch {
            authRepository.getUsers().onSuccess { users ->
                _uiState.value = _uiState.value.copy(users = users)
            }
            conversationRepository.getDepartments().onSuccess { departments ->
                _uiState.value = _uiState.value.copy(departments = departments)
            }
            _uiState.value = _uiState.value.copy(showTransferDialog = true)
        }
    }

    fun hideTransferDialog() {
        _uiState.value = _uiState.value.copy(showTransferDialog = false)
    }

    fun showAssignDialog() {
        viewModelScope.launch {
            authRepository.getUsers().onSuccess { users ->
                _uiState.value = _uiState.value.copy(users = users)
            }
            _uiState.value = _uiState.value.copy(showAssignDialog = true)
        }
    }

    fun hideAssignDialog() {
        _uiState.value = _uiState.value.copy(showAssignDialog = false)
    }

    fun transferToUser(userId: Int) {
        val ticketId = _uiState.value.conversation?.activeTicket?.id ?: return
        viewModelScope.launch {
            conversationRepository.changeTicketOwner(ticketId, userId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        showTransferDialog = false,
                        actionSuccess = "Atendimento transferido com sucesso"
                    )
                    loadAll()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "Erro ao transferir atendimento"
                    )
                }
            )
        }
    }

    fun assignToUser(userId: Int) {
        val ticketId = _uiState.value.conversation?.activeTicket?.id ?: return
        viewModelScope.launch {
            conversationRepository.changeTicketOwner(ticketId, userId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        showAssignDialog = false,
                        actionSuccess = "Atendente atribuido com sucesso"
                    )
                    loadAll()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "Erro ao atribuir atendente"
                    )
                }
            )
        }
    }

    fun setActiveTab(tab: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
        if (tab == 1 && _uiState.value.comments.isEmpty()) {
            loadComments()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearActionSuccess() {
        _uiState.value = _uiState.value.copy(actionSuccess = null)
    }

    private fun markAsRead() {
        viewModelScope.launch {
            conversationRepository.markAsRead(conversationId)
        }
    }

    private fun listenToSocketEvents() {
        viewModelScope.launch {
            socketService.events.collect { event ->
                when (event) {
                    is SocketEvent.MessageReceived, is SocketEvent.MessageAnswered -> {
                        // Check if message belongs to this conversation
                        val data = if (event is SocketEvent.MessageReceived) event.data else (event as SocketEvent.MessageAnswered).data
                        val msgConversationId = data.get("conversation_id")?.asInt
                            ?: data.get("data")?.asJsonObject?.get("conversation_id")?.asInt
                        if (msgConversationId == conversationId) {
                            loadAll()
                            markAsRead()
                        }
                    }
                    is SocketEvent.MessageUpdated -> {
                        val data = event.data
                        val msgConversationId = data.get("conversation_id")?.asInt
                            ?: data.get("data")?.asJsonObject?.get("conversation_id")?.asInt
                        if (msgConversationId == conversationId) {
                            loadAll()
                        }
                    }
                    is SocketEvent.TicketChangedOwner, is SocketEvent.TicketChangedStatus -> {
                        loadAll()
                    }
                    else -> { }
                }
            }
        }
    }

    class Factory(private val conversationId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConversationDetailViewModel(
                conversationId = conversationId,
                conversationRepository = AppContainer.conversationRepository,
                authRepository = AppContainer.authRepository,
                tokenManager = AppContainer.tokenManager,
                socketService = AppContainer.socketService,
                context = AppContainer.appContext
            ) as T
        }
    }
}
