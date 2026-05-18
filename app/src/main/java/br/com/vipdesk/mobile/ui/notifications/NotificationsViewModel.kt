package br.com.vipdesk.mobile.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.vipdesk.mobile.data.model.MobileNotification
import br.com.vipdesk.mobile.data.repository.MobileRepository
import br.com.vipdesk.mobile.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val items: List<MobileNotification> = emptyList(),
    val unreadCount: Int = 0,
    val onlyUnread: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationsViewModel(
    private val mobileRepository: MobileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            mobileRepository.getNotifications(_uiState.value.onlyUnread).fold(
                onSuccess = { (items, unread) ->
                    _uiState.value = _uiState.value.copy(
                        items = items,
                        unreadCount = unread,
                        isLoading = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
            )
        }
    }

    fun setFilter(onlyUnread: Boolean) {
        if (_uiState.value.onlyUnread == onlyUnread) return
        _uiState.value = _uiState.value.copy(onlyUnread = onlyUnread)
        load()
    }

    fun markRead(id: Int) {
        viewModelScope.launch {
            mobileRepository.markNotificationRead(id)
            _uiState.value = _uiState.value.copy(
                items = _uiState.value.items.map {
                    if (it.id == id) it.copy(read = true) else it
                },
                unreadCount = (_uiState.value.unreadCount - 1).coerceAtLeast(0)
            )
            if (_uiState.value.onlyUnread) load()
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            mobileRepository.markAllNotificationsRead()
            _uiState.value = _uiState.value.copy(
                items = _uiState.value.items.map { it.copy(read = true) },
                unreadCount = 0
            )
            if (_uiState.value.onlyUnread) load()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NotificationsViewModel(AppContainer.mobileRepository) as T
            }
        }
    }
}
