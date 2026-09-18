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
    val isLoadingMore: Boolean = false,
    // Backend retorna `total` quando suporta paginação; null = versão antiga (sem "carregar mais")
    val total: Int? = null,
    val error: String? = null
) {
    val hasMore: Boolean get() = total != null && items.size < total
}

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

    /** Puxar para atualizar. */
    suspend fun refreshAwait() = fetch()

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
            onSuccess = { _uiState.value = _uiState.value.copy(items = it.items, total = it.total, error = null) },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    /** Scroll infinito: próxima página com skip = itens já carregados. */
    fun loadMore() {
        val st = _uiState.value
        if (st.isLoading || st.isLoadingMore || !st.hasMore) return
        viewModelScope.launch {
            _uiState.value = st.copy(isLoadingMore = true)
            mobileRepository.getTickets(st.status, st.query, skip = st.items.size).fold(
                onSuccess = { page ->
                    val known = st.items.map { it.id }.toSet()
                    _uiState.value = _uiState.value.copy(
                        items = st.items + page.items.filter { it.id !in known },
                        total = page.total ?: _uiState.value.total,
                        isLoadingMore = false
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoadingMore = false, error = it.message) }
            )
        }
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
