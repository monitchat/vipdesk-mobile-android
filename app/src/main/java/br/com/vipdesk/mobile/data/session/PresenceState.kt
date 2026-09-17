package br.com.vipdesk.mobile.data.session

import br.com.vipdesk.mobile.di.AppContainer
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Presença do agente (Disponível / Em pausa), espelhando `currentPause` de
 * GET user/{id}/info e o toggle POST user/pause/{id}. Alimenta o chip do header,
 * a gaveta "Mais" e o toggle de Configurações.
 */
object PresenceState {
    data class Presence(val paused: Boolean = false, val reason: String? = null, val loaded: Boolean = false)

    private val _state = MutableStateFlow(Presence())
    val state: StateFlow<Presence> = _state.asStateFlow()

    suspend fun refresh() {
        val userId = AppContainer.tokenManager.getUserId() ?: return
        runCatching { AppContainer.apiService.getUserInfoRaw(userId) }.onSuccess { r ->
            val body = r.body() ?: return
            val pause = body.get("currentPause")?.takeIf { it.isJsonObject }?.asJsonObject
            _state.value = Presence(
                paused = pause != null,
                reason = pause?.let { reasonName(it) },
                loaded = true
            )
        }
    }

    /** Entra em pausa com o motivo, ou sai da pausa (o backend alterna). */
    suspend fun toggle(interruptionTypeId: Int?, reasonName: String?): Result<Boolean> {
        val userId = AppContainer.tokenManager.getUserId() ?: return Result.failure(Exception("Sessão inválida"))
        return runCatching {
            val body = JsonObject().apply { interruptionTypeId?.let { addProperty("interruption_type_id", it) } }
            val r = AppContainer.apiService.pauseUser(userId, body)
            if (!r.isSuccessful) throw Exception("Não foi possível alterar a pausa (${r.code()})")
            val nowPaused = !_state.value.paused
            _state.value = Presence(paused = nowPaused, reason = if (nowPaused) reasonName else null, loaded = true)
            nowPaused
        }
    }

    fun clear() { _state.value = Presence() }

    private fun reasonName(pause: JsonObject): String? =
        pause.getAsJsonObject("interruption_type")?.get("name")?.takeIf { !it.isJsonNull }?.asString
            ?: pause.getAsJsonObject("interruptionType")?.get("name")?.takeIf { !it.isJsonNull }?.asString
}
