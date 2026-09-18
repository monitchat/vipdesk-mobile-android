package br.com.vipdesk.mobile.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.vipdesk.mobile.data.model.CompanyOption
import br.com.vipdesk.mobile.data.model.MfaChallenge
import br.com.vipdesk.mobile.data.repository.AuthRepository
import br.com.vipdesk.mobile.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    // Tela 06: desafio MFA pendente (código TOTP, e-mail ou recuperação)
    val mfa: MfaChallenge? = null,
    val mfaCode: String = "",
    val mfaError: String? = null,
    val mfaInfo: String? = null,
    // Tela 05: credencial válida em mais de uma empresa
    val companies: List<CompanyOption>? = null,
    val selectedCompanyId: Int? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            _uiState.value = _uiState.value.copy(isLoggedIn = loggedIn)
        }
    }

    /** Sessão derrubada fora do fluxo de login: volta ao formulário limpo. */
    fun onSessionLost() {
        _uiState.value = LoginUiState()
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null, selectedCompanyId = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun login() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Preencha todos os campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = authRepository.login(state.email, state.password, state.selectedCompanyId)
            result.fold(
                onSuccess = { outcome -> applyOutcome(outcome) },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Erro ao fazer login"
                    )
                }
            )
        }
    }

    private fun applyOutcome(outcome: AuthRepository.LoginOutcome) {
        _uiState.value = when (outcome) {
            is AuthRepository.LoginOutcome.LoggedIn ->
                _uiState.value.copy(isLoading = false, isLoggedIn = true, mfa = null, mfaCode = "", mfaError = null)
            is AuthRepository.LoginOutcome.MfaRequired ->
                _uiState.value.copy(isLoading = false, mfa = outcome.challenge, mfaCode = "", mfaError = null, mfaInfo = null)
            is AuthRepository.LoginOutcome.SelectCompany ->
                _uiState.value.copy(isLoading = false, companies = outcome.companies, selectedCompanyId = null)
        }
    }

    /** Tela 05: reenvia o login com a empresa escolhida (pode ainda cair em MFA). */
    fun selectCompany(companyId: Int) {
        _uiState.value = _uiState.value.copy(selectedCompanyId = companyId, companies = null, error = null)
        login()
    }

    fun cancelCompanySelection() {
        _uiState.value = _uiState.value.copy(companies = null, selectedCompanyId = null, password = "")
    }

    fun onMfaCodeChange(code: String) {
        // TOTP/e-mail = 6 dígitos; código de recuperação pode ter até 9 caracteres alfanuméricos
        val cleaned = code.filter { it.isLetterOrDigit() }.take(9).uppercase()
        _uiState.value = _uiState.value.copy(mfaCode = cleaned, mfaError = null)
    }

    fun verifyMfa() {
        val state = _uiState.value
        val challenge = state.mfa ?: return
        if (state.mfaCode.length < 6) {
            _uiState.value = state.copy(mfaError = "Digite o código de 6 dígitos")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, mfaError = null)
            authRepository.verifyMfa(challenge, state.mfaCode, state.email).fold(
                onSuccess = { applyOutcome(it) },
                onFailure = {
                    val msg = it.message ?: "Código inválido"
                    // Token MFA expirado/estourou tentativas: volta ao formulário
                    val expired = msg.contains("expirado", true) || msg.contains("Faca login", true) || msg.contains("Faça login", true)
                    _uiState.value = if (expired) _uiState.value.copy(isLoading = false, mfa = null, mfaCode = "", error = msg)
                    else _uiState.value.copy(isLoading = false, mfaError = msg)
                }
            )
        }
    }

    fun resendMfaEmail() {
        val challenge = _uiState.value.mfa ?: return
        viewModelScope.launch {
            authRepository.resendMfaEmail(challenge).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(mfaInfo = "Novo código enviado para o seu e-mail") },
                onFailure = { _uiState.value = _uiState.value.copy(mfaError = it.message) }
            )
        }
    }

    fun cancelMfa() {
        _uiState.value = _uiState.value.copy(mfa = null, mfaCode = "", mfaError = null, mfaInfo = null, password = "", selectedCompanyId = null)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(AppContainer.authRepository) as T
            }
        }
    }
}
