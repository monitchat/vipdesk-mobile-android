package br.com.vipdesk.mobile.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.com.vipdesk.mobile.MainActivity
import br.com.vipdesk.mobile.R
import br.com.vipdesk.mobile.data.socket.SocketEvent
import br.com.vipdesk.mobile.data.socket.SocketService
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Conversa aberta na tela agora — mensagens dela não geram notificação. */
object ActiveConversation {
    @Volatile
    var id: Int? = null
}

/** Navegação pendente disparada por toque em notificação. */
object PendingNav {
    var conversationId by mutableStateOf<Int?>(null)
}

/**
 * Notificações do sistema para mensagens recebidas via socket (Pusher).
 * Funciona enquanto o processo do app estiver vivo; push com o app
 * encerrado exigiria FCM no backend.
 */
object MessageNotifier {

    const val EXTRA_CONVERSATION_ID = "conversation_id"
    private const val CHANNEL_ID = "messages"
    private var started = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start(context: Context, socketService: SocketService) {
        if (started) return
        started = true
        createChannel(context)
        scope.launch {
            socketService.events.collect { event ->
                if (event is SocketEvent.MessageReceived) {
                    try {
                        handle(context.applicationContext, event.data)
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mensagens",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Novas mensagens de clientes"
                enableVibration(true)
            }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    private fun handle(context: Context, json: JsonObject) {
        val msg = json.objOrNull("data") ?: json
        val conversation = msg.objOrNull("conversation") ?: json.objOrNull("conversation")

        val convId = msg.intOrNull("conversation_id")
            ?: conversation?.intOrNull("id")
            ?: return

        // Mensagem de agente (sender=1) não notifica — só as do cliente.
        if (msg.intOrNull("sender") == 1) return

        // Usuário já está com essa conversa aberta: a tela atualiza sozinha.
        if (ActiveConversation.id == convId) return

        val contactName = conversation?.objOrNull("contact")?.strOrNull("name")
            ?: conversation?.strOrNull("name")
            ?: "Nova mensagem"
        val text = msg.strOrNull("message")
            ?.replace(Regex("<[^>]*>"), "")
            ?.trim()
            ?.ifBlank { null }
            ?: "Você recebeu uma nova mensagem"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CONVERSATION_ID, convId)
        }
        val pending = PendingIntent.getActivity(
            context,
            convId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contactName)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        NotificationManagerCompat.from(context).notify(convId, notification)
    }

    fun cancel(context: Context, conversationId: Int) {
        NotificationManagerCompat.from(context).cancel(conversationId)
    }

    // ————— Acesso defensivo ao JSON do evento —————

    private fun JsonObject.objOrNull(key: String): JsonObject? =
        get(key)?.takeIf { it.isJsonObject }?.asJsonObject

    private fun JsonObject.intOrNull(key: String): Int? =
        get(key)?.takeIf { it.isJsonPrimitive }?.let {
            try {
                it.asInt
            } catch (_: Exception) {
                it.asString.toIntOrNull()
            }
        }

    private fun JsonObject.strOrNull(key: String): String? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString
}
