package br.com.vipdesk.mobile.ui.conversations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.vipdesk.mobile.data.model.ChannelCounts
import br.com.vipdesk.mobile.data.model.Conversation
import br.com.vipdesk.mobile.data.model.ConversationCountResponse
import br.com.vipdesk.mobile.data.repository.AuthRepository
import br.com.vipdesk.mobile.data.repository.ConversationRepository
import br.com.vipdesk.mobile.data.local.TokenManager
import br.com.vipdesk.mobile.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConversationListUiState(
    val conversations: List<Conversation> = emptyList(),
    val filteredConversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val currentStatus: String = "assigned",
    val currentMedia: String = "",
    val counts: ConversationCountResponse = ConversationCountResponse(),
    val channelCounts: ChannelCounts = ChannelCounts(),
    val currentUserId: Int? = null
)

class ConversationListViewModel(
    private val conversationRepository: ConversationRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
        loadConversations()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = tokenManager.getUserId()
            _uiState.value = _uiState.value.copy(currentUserId = userId)
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = conversationRepository.getConversations(
                status = _uiState.value.currentStatus,
                media = _uiState.value.currentMedia
            )
            result.fold(
                onSuccess = { conversations ->
                    val filtered = applyFilters(conversations, _uiState.value.currentMedia, _uiState.value.searchQuery)
                    _uiState.value = _uiState.value.copy(
                        conversations = conversations,
                        filteredConversations = filtered,
                        isLoading = false,
                        counts = conversationRepository.lastCounts ?: _uiState.value.counts,
                        channelCounts = conversationRepository.lastChannelCounts ?: _uiState.value.channelCounts
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message
                    )
                }
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val result = conversationRepository.getConversations(
                status = _uiState.value.currentStatus,
                media = _uiState.value.currentMedia
            )
            result.fold(
                onSuccess = { conversations ->
                    val filtered = applyFilters(conversations, _uiState.value.currentMedia, _uiState.value.searchQuery)
                    _uiState.value = _uiState.value.copy(
                        conversations = conversations,
                        filteredConversations = filtered,
                        isRefreshing = false,
                        counts = conversationRepository.lastCounts ?: _uiState.value.counts,
                        channelCounts = conversationRepository.lastChannelCounts ?: _uiState.value.channelCounts
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredConversations = applyFilters(_uiState.value.conversations, _uiState.value.currentMedia, query)
        )
    }

    fun onStatusChange(status: String) {
        if (_uiState.value.currentStatus == status) return
        _uiState.value = _uiState.value.copy(currentStatus = status)
        loadConversations()
    }

    fun onMediaChange(media: String) {
        val newMedia = if (_uiState.value.currentMedia == media) "" else media
        _uiState.value = _uiState.value.copy(currentMedia = newMedia)
        // Apply client-side filter immediately, then also reload from API
        _uiState.value = _uiState.value.copy(
            filteredConversations = applyFilters(_uiState.value.conversations, newMedia, _uiState.value.searchQuery)
        )
        loadConversations()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    private fun applyFilters(
        conversations: List<Conversation>,
        media: String,
        query: String
    ): List<Conversation> {
        var result = conversations

        // Filter by channel (client-side, same as frontend)
        if (media.isNotBlank()) {
            result = result.filter { conv ->
                conv.source?.equals(media, ignoreCase = true) == true ||
                        conv.lastTicketSource?.equals(media, ignoreCase = true) == true
            }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            result = result.filter { conv ->
                conv.contact.name.lowercase().contains(lowerQuery) ||
                        (conv.contact.client?.name?.lowercase()?.contains(lowerQuery) == true) ||
                        (conv.contact.phoneNumber?.contains(lowerQuery) == true) ||
                        (conv.lastMessage?.message?.lowercase()?.contains(lowerQuery) == true)
            }
        }

        return result
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ConversationListViewModel(
                    AppContainer.conversationRepository,
                    AppContainer.authRepository,
                    AppContainer.tokenManager
                ) as T
            }
        }
    }
}
