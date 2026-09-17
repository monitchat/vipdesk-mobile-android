package br.com.vipdesk.mobile.data.repository

import android.util.Base64
import br.com.vipdesk.mobile.data.api.ApiService
import br.com.vipdesk.mobile.data.local.TokenManager
import br.com.vipdesk.mobile.data.model.LoginRequest
import br.com.vipdesk.mobile.data.model.LoginResponse
import br.com.vipdesk.mobile.data.model.MfaChallenge
import br.com.vipdesk.mobile.data.model.MfaResendRequest
import br.com.vipdesk.mobile.data.model.MfaVerifyRequest
import br.com.vipdesk.mobile.data.model.User
import org.json.JSONObject

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    /** Resultado do login: sessão criada ou desafio MFA pendente. */
    sealed class LoginOutcome {
        data object LoggedIn : LoginOutcome()
        data class MfaRequired(val challenge: MfaChallenge) : LoginOutcome()
    }

    suspend fun login(email: String, password: String): Result<LoginOutcome> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            handleAuthResponse(response, email)
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Sem conexao com a internet"))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Tempo de conexao esgotado"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao fazer login"))
        }
    }

    /** Segundo passo do login: código TOTP, código por e-mail ou código de recuperação. */
    suspend fun verifyMfa(challenge: MfaChallenge, code: String, email: String): Result<LoginOutcome> {
        return try {
            val response = apiService.verifyMfa(MfaVerifyRequest(challenge.token, code.trim()))
            handleAuthResponse(response, email)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao verificar código"))
        }
    }

    suspend fun resendMfaEmail(challenge: MfaChallenge): Result<Unit> {
        return try {
            val response = apiService.resendMfaEmail(MfaResendRequest(challenge.token))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(errorMessage(response, "Não foi possível reenviar o código")))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Não foi possível reenviar o código"))
        }
    }

    private suspend fun handleAuthResponse(response: retrofit2.Response<LoginResponse>, email: String): Result<LoginOutcome> {
        val body = response.body()
        if (response.isSuccessful && body != null) {
            if (body.mfaRequired && !body.mfaToken.isNullOrBlank()) {
                return Result.success(LoginOutcome.MfaRequired(MfaChallenge(body.mfaToken, body.mfaMethod ?: "totp", body.emailHint)))
            }
            val token = body.token
            if (token.isNullOrBlank()) return Result.failure(Exception("Resposta invalida do servidor"))
            tokenManager.saveToken(token)
            try {
                val payload = decodeJwtPayload(token)
                val userId = payload.optInt("id", -1)
                val name = payload.optString("name", "")
                val userEmail = payload.optString("email", email)
                val companyId = payload.optInt("company_id", -1)
                if (userId > 0) {
                    tokenManager.saveUserInfo(
                        userId = userId,
                        name = name,
                        email = userEmail,
                        companyId = if (companyId > 0) companyId else null
                    )
                }
            } catch (_: Exception) { }
            return Result.success(LoginOutcome.LoggedIn)
        }
        val fallback = when (response.code()) {
            401 -> "E-mail ou senha incorretos"
            403 -> "Acesso negado"
            422 -> "Dados invalidos"
            429 -> "Muitas tentativas. Aguarde e tente novamente"
            500 -> "Erro interno do servidor"
            525 -> "Erro de conexao com o servidor. Tente novamente"
            else -> response.message().ifBlank { "Erro ao fazer login (${response.code()})" }
        }
        return Result.failure(Exception(errorMessage(response, fallback)))
    }

    // Mensagem do backend ({message} ou {errors.global.message}) quando existir.
    private fun errorMessage(response: retrofit2.Response<*>, fallback: String): String {
        val raw = runCatching { response.errorBody()?.string() }.getOrNull() ?: return fallback
        return runCatching {
            val obj = JSONObject(raw)
            obj.optJSONObject("errors")?.optJSONObject("global")?.optString("message")?.takeIf { it.isNotBlank() }
                ?: obj.optString("message").takeIf { it.isNotBlank() }
        }.getOrNull() ?: fallback
    }

    /** Revoga o token no servidor (com limite de tempo) e limpa a sessão local. */
    suspend fun logout() {
        try {
            kotlinx.coroutines.withTimeoutOrNull(5_000) { apiService.logout() }
        } catch (_: Exception) { }
        tokenManager.clearAll()
    }

    suspend fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    suspend fun getCurrentUserId(): Int? = tokenManager.getUserId()

    suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception("Erro ao buscar usuarios"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decodeJwtPayload(jwt: String): JSONObject {
        val parts = jwt.split(".")
        if (parts.size < 2) throw IllegalArgumentException("Invalid JWT")
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
        return JSONObject(payload)
    }
}
