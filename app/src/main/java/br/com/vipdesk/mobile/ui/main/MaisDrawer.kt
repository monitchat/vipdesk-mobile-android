package br.com.vipdesk.mobile.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.BuildConfig
import br.com.vipdesk.mobile.data.session.PresenceState
import br.com.vipdesk.mobile.di.AppContainer
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdSheetHandle
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint

private data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val gray: Boolean = false,
    val addon: Boolean = false,
    val action: DrawerAction
)

private sealed class DrawerAction {
    data object DASHBOARD : DrawerAction(); data object REPORTS : DrawerAction(); data object SETTINGS : DrawerAction()
    data class MODULE(val key: String) : DrawerAction()
}


/** Gaveta "Mais" (tela 02): cabeçalho do usuário, grade de atalhos e lista. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaisDrawer(
    onDismiss: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onLogout: () -> Unit,
    onToast: (String) -> Unit,
    onOpenModule: (String) -> Unit = {}
) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    val presence by PresenceState.state.collectAsState()
    var showPause by remember { mutableStateOf(false) }
    if (showPause) PauseReasonSheet(onDismiss = { showPause = false }, onResult = onToast)

    LaunchedEffect(Unit) {
        userName = AppContainer.tokenManager.getUserName().orEmpty()
        userEmail = AppContainer.tokenManager.getUserEmail().orEmpty()
    }

    val items = listOf(
        DrawerItem("Dashboard", Icons.Outlined.Speed, action = DrawerAction.DASHBOARD),
        DrawerItem("Campanhas", Icons.Outlined.Campaign, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.CAMPAIGNS)),
        DrawerItem("DeskFlow", Icons.Outlined.AccountTree, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.DESKFLOW)),
        DrawerItem("Relatórios", Icons.Outlined.BarChart, action = DrawerAction.REPORTS),
        DrawerItem("Confirmações", Icons.Outlined.CheckBox, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.CONFIRMATIONS)),
        DrawerItem("Cobranças", Icons.Outlined.MonetizationOn, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.BILLING)),
        DrawerItem("Msgs. Classif.", Icons.Outlined.Sell, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.SENTIMENT)),
        DrawerItem("Base Legal", Icons.Outlined.Balance, addon = true, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.BASE_LEGAL)),
        DrawerItem("Pesquisas", Icons.Outlined.Star, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.SURVEYS)),
        DrawerItem("Chat interno", Icons.Outlined.ChatBubbleOutline, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.INTERNAL_CHAT)),
        DrawerItem("Reuniões", Icons.Outlined.Videocam, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.MEETINGS)),
        DrawerItem("Arena", Icons.Outlined.EmojiEvents, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.ARENA)),
        DrawerItem("Configurações", Icons.Outlined.Settings, gray = true, action = DrawerAction.SETTINGS),
        DrawerItem("Administração", Icons.Outlined.Business, gray = true, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.ADMIN)),
        DrawerItem("Observab.", Icons.Outlined.MonitorHeart, gray = true, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.OBSERVABILITY)),
        DrawerItem("Plano", Icons.Outlined.WorkspacePremium, gray = true, action = DrawerAction.MODULE(br.com.vipdesk.mobile.ui.modules.ModuleKeys.PLAN))
    )

    fun run(action: DrawerAction) {
        onDismiss()
        when (action) {
            DrawerAction.DASHBOARD -> onOpenDashboard()
            DrawerAction.REPORTS -> onOpenReports()
            DrawerAction.SETTINGS -> onOpenSettings()
            is DrawerAction.MODULE -> onOpenModule(action.key)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        dragHandle = { VdSheetHandle() }
    ) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 20.dp)) {
            // Cabeçalho do usuário
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VdAvatar(name = userName.ifBlank { "U" }, size = 40.dp, fontSize = 13, agent = true)
                Column(Modifier.weight(1f)) {
                    Text(userName.ifBlank { "Usuário" }, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = c.text)
                    Text(userEmail, fontSize = 12.sp, color = c.muted, maxLines = 1)
                }
                Row(
                    modifier = Modifier
                        .background(Tint.yellowBg, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.EmojiEvents, null, tint = Tint.yellowFg, modifier = Modifier.size(12.dp))
                    Text("XP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Tint.yellowFg)
                }
            }
            VdDivider()
            Spacer(Modifier.height(12.dp))

            // Grade 4 colunas
            items.chunked(4).forEach { row ->
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    row.forEach { item ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { run(item.action) },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (item.gray) c.surfaceAlt else c.primarySurface,
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        item.icon, null,
                                        tint = if (item.gray) c.textTertiary else c.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                if (item.addon) {
                                    Text(
                                        "ADD-ON",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .background(c.info, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                item.label,
                                fontSize = 11.sp,
                                color = Color(0xFF374151),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            VdDivider()
            Spacer(Modifier.height(6.dp))
            if (presence.paused) {
                DrawerRow(Icons.Outlined.PlayCircle, "Sair da pausa", trailing = presence.reason ?: "em pausa") {
                    scope.launch {
                        PresenceState.toggle(null, null).fold(
                            onSuccess = { onToast("Você está disponível novamente") },
                            onFailure = { onToast(it.message ?: "Erro ao sair da pausa") }
                        )
                    }
                }
            } else {
                DrawerRow(Icons.Outlined.PauseCircle, "Entrar em pausa", trailing = "motivo ›") { showPause = true }
            }
            DrawerRow(Icons.Outlined.StickyNote2, "Post-its") { onDismiss(); onOpenModule(br.com.vipdesk.mobile.ui.modules.ModuleKeys.NOTES) }
            DrawerRow(Icons.Outlined.AccountCircle, "Perfil, MFA, treinamentos", caret = true) {
                onDismiss(); onOpenSettings()
            }
            DrawerRow(Icons.AutoMirrored.Outlined.HelpOutline, "Ajuda e tour", trailing = "v${BuildConfig.VERSION_NAME}", muted = true) {
                onDismiss(); onOpenModule(br.com.vipdesk.mobile.ui.modules.ModuleKeys.KB)
            }
            DrawerRow(Icons.AutoMirrored.Outlined.Logout, "Sair da conta", danger = true) {
                onDismiss(); onLogout()
            }
        }
    }
}

@Composable
private fun DrawerRow(
    icon: ImageVector,
    label: String,
    trailing: String? = null,
    caret: Boolean = false,
    muted: Boolean = false,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val c = AppTheme.colors
    val fg = when {
        danger -> Tint.redFg
        muted -> c.muted
        else -> c.text
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = if (danger) Tint.redFg else c.textTertiary, modifier = Modifier.size(20.dp))
        Text(label, fontSize = if (muted) 12.sp else 14.sp, color = fg, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, fontSize = 12.sp, color = c.muted)
        if (caret) Icon(Icons.Outlined.KeyboardArrowRight, null, tint = c.placeholder, modifier = Modifier.size(18.dp))
    }
}
