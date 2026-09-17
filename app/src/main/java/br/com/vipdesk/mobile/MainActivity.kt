package br.com.vipdesk.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.vipdesk.mobile.data.notifications.MessageNotifier
import br.com.vipdesk.mobile.data.notifications.PendingNav
import br.com.vipdesk.mobile.ui.navigation.VipDeskNavHost
import br.com.vipdesk.mobile.ui.theme.ThemeController
import br.com.vipdesk.mobile.ui.theme.VipDeskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeController.init(this)
        requestNotificationPermission()
        consumeNotificationIntent(intent)
        setContent {
            // Barras do sistema seguem o tema do app (não o do sistema): claro = ícones escuros.
            val dark = ThemeController.dark
            LaunchedEffect(dark) {
                val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            VipDeskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VipDeskNavHost()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeNotificationIntent(intent)
    }

    /** Toque em notificação de mensagem → abre a conversa correspondente. */
    private fun consumeNotificationIntent(intent: Intent?) {
        val conversationId = intent
            ?.getIntExtra(MessageNotifier.EXTRA_CONVERSATION_ID, -1)
            ?.takeIf { it > 0 } ?: return
        intent.removeExtra(MessageNotifier.EXTRA_CONVERSATION_ID)
        PendingNav.conversationId = conversationId
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100
            )
        }
    }
}
