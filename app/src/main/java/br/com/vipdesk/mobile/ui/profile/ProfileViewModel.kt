package br.com.vipdesk.mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.vipdesk.mobile.data.local.TokenManager
import br.com.vipdesk.mobile.data.repository.AuthRepository
import br.com.vipdesk.mobile.data.socket.SocketService
import br.com.vipdesk.mobile.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val loggingOut: Boolean = false
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager,
    private val socketService: SocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                name = tokenManager.getUserName()?.takeIf { it.isNotBlank() } ?: "Usuário",
                email = tokenManager.getUserEmail().orEmpty()
            )
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loggingOut = true)
            socketService.disconnect()
            authRepository.logout()
            onDone()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(
                    AppContainer.authRepository,
                    AppContainer.tokenManager,
                    AppContainer.socketService
                ) as T
            }
        }
    }
}
