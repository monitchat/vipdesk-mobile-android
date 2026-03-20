package br.com.vipdesk.mobile.data.repository

import android.util.Base64
import br.com.vipdesk.mobile.data.api.ApiService
import br.com.vipdesk.mobile.data.local.TokenManager
import br.com.vipdesk.mobile.data.model.LoginRequest
import br.com.vipdesk.mobile.data.model.LoginResponse
import br.com.vipdesk.mobile.data.model.User
import org.json.JSONObject

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                if (loginResponse.token.isBlank()) {
                    return Result.failure(Exception("Resposta invalida do servidor"))
                }
                tokenManager.saveToken(loginResponse.token)

                // Extract user info from JWT payload
                try {
                    val payload = decodeJwtPayload(loginResponse.token)
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

                Result.success(loginResponse)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "E-mail ou senha incorretos"
                    403 -> "Acesso negado"
                    422 -> "Dados invalidos"
                    500 -> "Erro interno do servidor"
                    525 -> "Erro de conexao com o servidor. Tente novamente"
                    else -> response.message().ifBlank { "Erro ao fazer login (${response.code()})" }
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Sem conexao com a internet"))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Tempo de conexao esgotado"))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erro ao fazer login"))
        }
    }

    suspend fun logout() {
        try {
            apiService.logout()
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
