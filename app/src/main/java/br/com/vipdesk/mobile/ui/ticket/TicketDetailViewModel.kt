package br.com.vipdesk.mobile.ui.ticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.vipdesk.mobile.data.model.Comment
import br.com.vipdesk.mobile.data.model.MobileTicket
import br.com.vipdesk.mobile.data.model.TicketStatusOption
import br.com.vipdesk.mobile.data.model.User
import br.com.vipdesk.mobile.data.repository.MobileRepository
import br.com.vipdesk.mobile.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TicketDetailUiState(
    val ticket: MobileTicket? = null,
    val comments: List<Comment> = emptyList(),
    val commentText: String = "",
    val isLoading: Boolean = false,
    val isActing: Boolean = false,
    val showTransferDialog: Boolean = false,
    val agents: List<User> = emptyList(),
    val statuses: List<TicketStatusOption> = emptyList(),
    val message: String? = null,
    val error: String? = null
)

class TicketDetailViewModel(
    private val ticketId: Int,
    private val mobileRepository: MobileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketDetailUiState())
    val uiState: StateFlow<TicketDetailUiState> = _uiState.asStateFlow()

    init {
        load()
        loadComments()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            mobileRepository.getTicket(ticketId).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(ticket = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun loadComments() {
        viewModelScope.launch {
            try {
                val response = AppContainer.apiService.getTicketComments(ticketId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(comments = response.body()?.data ?: emptyList())
                }
            } catch (_: Exception) {
            }
        }
    }

    fun onCommentTextChange(text: String) {
        _uiState.value = _uiState.value.copy(commentText = text)
    }

    /** Comentário interno no ticket (não vai ao cliente). */
    fun sendComment() {
        val text = _uiState.value.commentText.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            AppContainer.conversationRepository.sendComment(
                message = text,
                ticketId = ticketId
            ).fold(
                onSuccess = { comment ->
                    _uiState.value = _uiState.value.copy(
                        comments = _uiState.value.comments + comment,
                        commentText = "",
                        message = "Comentário adicionado"
                    )
                    load()
                },
                onFailure = { _uiState.value = _uiState.value.copy(message = it.message ?: "Erro ao comentar") }
            )
        }
    }

    fun resolve() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActing = true)
            mobileRepository.resolveTicket(ticketId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isActing = false, message = "Ticket resolvido")
                    load()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isActing = false, message = it.message ?: "Falha ao resolver")
                }
            )
        }
    }

    fun assignToMe() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActing = true)
            mobileRepository.claimTicket(ticketId, null).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isActing = false, message = "Atendimento atribuído a você")
                    load()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isActing = false, message = it.message ?: "Falha ao atribuir")
                }
            )
        }
    }

    fun openTransferDialog() {
        _uiState.value = _uiState.value.copy(showTransferDialog = true)
        if (_uiState.value.agents.isEmpty()) {
            viewModelScope.launch {
                mobileRepository.getAgents().onSuccess { list ->
                    _uiState.value = _uiState.value.copy(agents = list)
                }
            }
        }
    }

    fun dismissTransferDialog() {
        _uiState.value = _uiState.value.copy(showTransferDialog = false)
    }

    fun transferTo(userId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActing = true, showTransferDialog = false)
            mobileRepository.claimTicket(ticketId, userId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isActing = false, message = "Atendimento transferido")
                    load()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isActing = false, message = it.message ?: "Falha ao transferir")
                }
            )
        }
    }

    /** Lista de status da empresa para o sheet de troca (tela 15 · Propriedades). */
    fun loadStatuses() {
        if (_uiState.value.statuses.isNotEmpty()) return
        viewModelScope.launch {
            runCatching { AppContainer.apiService.getTicketStatuses() }.onSuccess { r ->
                if (r.isSuccessful) _uiState.value = _uiState.value.copy(
                    statuses = r.body()?.data.orEmpty()
                        .filter { (it.active ?: 1) != 0 }
                        .sortedWith(compareBy({ it.menuOrder ?: Int.MAX_VALUE }, { it.progressPercentage ?: 0 }))
                )
            }
        }
    }

    /** POST ticket/setTicketStatus — `pendingReason` é exigido quando o status pausa o SLA. */
    fun changeStatus(status: TicketStatusOption, pendingReason: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActing = true)
            AppContainer.conversationRepository.changeTicketStatus(ticketId, status.id.toString(), pendingReason).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isActing = false, message = "Status alterado para ${status.description ?: ""}")
                    load()
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isActing = false, message = it.message ?: "Falha ao alterar status")
                }
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    class Factory(private val ticketId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TicketDetailViewModel(ticketId, AppContainer.mobileRepository) as T
        }
    }
}
