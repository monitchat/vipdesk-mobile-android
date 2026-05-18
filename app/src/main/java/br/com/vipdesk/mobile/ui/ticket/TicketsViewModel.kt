package br.com.vipdesk.mobile.ui.ticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.vipdesk.mobile.data.model.TicketListItem
import br.com.vipdesk.mobile.data.repository.MobileRepository
import br.com.vipdesk.mobile.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TicketsUiState(
    val items: List<TicketListItem> = emptyList(),
    val status: String = "open",
    val query: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class TicketsViewModel(
    private val mobileRepository: MobileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsUiState())
    val uiState: StateFlow<TicketsUiState> = _uiState.asStateFlow()

    init {
        load()
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
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun setStatus(status: String) {
        if (_uiState.value.status == status) return
        _uiState.value = _uiState.value.copy(status = status)
        load()
    }

    fun onQueryChange(q: String) {
        _uiState.value = _uiState.value.copy(query = q)
    }

    fun search() = load()

    private suspend fun fetch() {
        mobileRepository.getTickets(_uiState.value.status, _uiState.value.query).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(items = it, error = null) },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TicketsViewModel(AppContainer.mobileRepository) as T
            }
        }
    }
}
