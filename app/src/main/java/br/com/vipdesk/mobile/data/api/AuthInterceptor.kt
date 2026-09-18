package br.com.vipdesk.mobile.data.api

import br.com.vipdesk.mobile.data.local.TokenManager
import br.com.vipdesk.mobile.data.session.SessionEvents
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenManager.getToken() }
        // Sem cache em nenhuma camada: o app sempre mostra o estado atual da API
        // (OkHttp não tem cache configurado; os headers barram proxies/CDN).
        val builder = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Cache-Control", "no-cache, no-store, max-age=0")
            .addHeader("Pragma", "no-cache")
        if (token != null) builder.addHeader("Authorization", "Bearer $token")
        val request = builder.build()
        val response = chain.proceed(request)
        if (token != null && isSessionLost(request.url.encodedPath, response)) {
            SessionEvents.expire("Sua sessão expirou. Entre novamente.")
        }
        return response
    }

    // Backend: 401 = sem token válido; 403 com "token" na mensagem = expirado/blacklist.
    // Permissão negada é 422, então não cai aqui.
    private fun isSessionLost(path: String, response: Response): Boolean {
        if (path.contains("auth/login") || path.contains("auth/logout")) return false
        return when (response.code) {
            401 -> true
            403 -> runCatching { response.peekBody(2048).string() }
                .getOrDefault("").contains("token", ignoreCase = true)
            else -> false
        }
    }
}
