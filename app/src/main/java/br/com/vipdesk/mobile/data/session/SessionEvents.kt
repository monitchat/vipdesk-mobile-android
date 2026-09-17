package br.com.vipdesk.mobile.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sinal global de "sessão perdida": disparado pelo interceptor HTTP (401/403 de token)
 * ou pelo evento `UserLoggedOut` do socket. O NavGraph observa e leva ao login.
 * Só o primeiro motivo é mantido até ser consumido, evitando logout em cascata
 * quando várias requisições falham ao mesmo tempo.
 */
object SessionEvents {
    private val _expired = MutableStateFlow<String?>(null)
    val expired: StateFlow<String?> = _expired.asStateFlow()

    fun expire(reason: String) {
        _expired.compareAndSet(null, reason)
    }

    fun consume() {
        _expired.value = null
    }
}
