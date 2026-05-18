package br.com.vipdesk.mobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.vipdesk.mobile.data.local.TokenManager
import br.com.vipdesk.mobile.data.model.MobileDashboard
import br.com.vipdesk.mobile.data.repository.MobileRepository
import br.com.vipdesk.mobile.data.socket.SocketEvent
import br.com.vipdesk.mobile.data.socket.SocketService
import br.com.vipdesk.mobile.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val userName: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val dashboard: MobileDashboard = MobileDashboard(),
    val unreadNotifications: Int = 0,
    val error: String? = null
)

class DashboardViewModel(
    private val mobileRepository: MobileRepository,
    private val tokenManager: TokenManager,
    private val socketService: SocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadUser()
        load()
        loadUnread()
        listenToSocket()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val rawName = tokenManager.getUserName()?.trim().orEmpty()
            val firstName = rawName.split(" ").firstOrNull().orEmpty()
            _uiState.value = _uiState.value.copy(userName = firstName.ifBlank { "agente" })
        }
    }

    private fun listenToSocket() {
        viewModelScope.launch {
            socketService.events.collect { event ->
                when (event) {
                    is SocketEvent.MessageReceived,
                    is SocketEvent.MessageAnswered,
                    is SocketEvent.NewTicketCreated,
                    is SocketEvent.TicketChangedOwner,
                    is SocketEvent.TicketChangedStatus,
                    is SocketEvent.TicketDeleted,
                    is SocketEvent.ConversationCountChanged -> {
                        fetch()
                        loadUnread()
                    }
                    else -> { }
                }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            fetch()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            fetch()
            loadUnread()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private fun loadUnread() {
        viewModelScope.launch {
            mobileRepository.getNotifications(onlyUnread = true).onSuccess { (_, count) ->
                _uiState.value = _uiState.value.copy(unreadNotifications = count)
            }
        }
    }

    private suspend fun fetch() {
        mobileRepository.getDashboard().fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(dashboard = it, error = null)
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(
                    AppContainer.mobileRepository,
                    AppContainer.tokenManager,
                    AppContainer.socketService
                ) as T
            }
        }
    }
}
