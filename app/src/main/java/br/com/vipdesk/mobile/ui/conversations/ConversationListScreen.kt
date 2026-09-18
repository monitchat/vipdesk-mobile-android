package br.com.vipdesk.mobile.ui.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.Conversation
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.common.sourceVisual
import br.com.vipdesk.mobile.ui.components.VdAppHeader
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCountBadge
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdHeaderIcon
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdSearchField
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Brand
import br.com.vipdesk.mobile.ui.theme.Tint

private val CHANNELS = listOf(
    "whatsapp" to Brand.whatsapp,
    "instagram" to Brand.instagram,
    "facebook" to Brand.messenger,
    "telegram" to Brand.telegram,
    "email" to Brand.email,
    "webchat" to Brand.webchat,
    "sms" to Brand.sms
)

@Composable
fun ConversationListScreen(
    onConversationClick: (Int) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onNewConversation: () -> Unit = {},
    unreadNotifications: Int = 0,
    viewModel: ConversationListViewModel = viewModel(factory = ConversationListViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    // Volta do segundo plano: enquanto o app está parado não chegam eventos do socket
    // (conversas arquivadas/atribuídas no web ficavam na lista até puxar para atualizar).
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) viewModel.refreshSilently()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var onlyUnread by remember { mutableStateOf(false) }

    val list = if (onlyUnread) state.filteredConversations.filter { it.unreadMessages > 0 }
    else state.filteredConversations

    Column(Modifier.fillMaxSize().background(c.surface)) {
        VdAppHeader(title = "Conversas") {
            VdHeaderIcon(Icons.Outlined.Notifications, "Notificações", onNotificationsClick, badge = unreadNotifications)
            VdHeaderIcon(Icons.Outlined.EditNote, "Nova conversa", onNewConversation, tint = c.primary)
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(c.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VdSearchField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Buscar contato, telefone ou mensagem"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                VdPill("Minhas", state.currentStatus == "assigned", { viewModel.onStatusChange("assigned") }, count = state.counts.assigned)
                VdPill("Fila", state.currentStatus == "waiting", { viewModel.onStatusChange("waiting") }, count = state.counts.waiting)
                VdPill("Todas", state.currentStatus == "all", { viewModel.onStatusChange("all") })
            }
            // Faixa de canais
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(c.primarySurface, RoundedCornerShape(8.dp))
                        .border(1.dp, c.primaryLight, RoundedCornerShape(8.dp))
                        .clickable { viewModel.onMediaChange("") },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.FilterAlt, null, tint = c.primary, modifier = Modifier.size(16.dp)) }
                CHANNELS.forEach { (key, color) ->
                    val on = state.currentMedia == key
                    Box(
                        Modifier
                            .size(32.dp)
                            .background(if (on) c.primarySurface else c.surface, RoundedCornerShape(8.dp))
                            .border(1.dp, if (on) c.primary else c.divider, RoundedCornerShape(8.dp))
                            .clickable { viewModel.onMediaChange(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(sourceVisual(key).icon, null, tint = color, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    if (onlyUnread) "Todas · @" else "Não lidas · @",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.primary,
                    modifier = Modifier.clickable { onlyUnread = !onlyUnread }
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))

        when {
            state.isLoading && list.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.primary)
            }
            list.isEmpty() -> VdEmptyState(
                icon = Icons.Outlined.Forum,
                title = "Nenhuma conversa por aqui",
                subtitle = "Você está em dia. Novas mensagens dos seus canais aparecem em tempo real.",
                ctaLabel = "Nova conversa",
                onCta = onNewConversation
            )
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(list, key = { it.id }) { conv ->
                    ConversationRow(conv, state.currentUserId) { onConversationClick(conv.id) }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, currentUserId: Int?, onClick: () -> Unit) {
    val c = AppTheme.colors
    val ticket = conversation.activeTicket
    val source = conversation.source ?: conversation.lastTicketSource
    val last = conversation.lastMessage
    val fromAgent = last?.lastMessageSender == 1
    val waiting = ticket?.userId == null && conversation.unreadMessages > 0
    val agentName = ticket?.user?.name
    val (stLabel, stColor) = ticketStatusVisual(ticket?.statusName ?: ticket?.status, null)
    val stBg = when (stLabel) {
        "Resolvido", "Finalizado" -> Tint.greenBg
        "Pendente" -> Tint.yellowBg
        "Aberto", "Em andamento" -> Tint.blueBg
        else -> c.surfaceAlt
    }
    val stFg = when (stLabel) {
        "Resolvido", "Finalizado" -> Tint.greenFg
        "Pendente" -> Tint.yellowFg
        "Aberto", "Em andamento" -> Tint.blueFg
        else -> Tint.grayFg
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (waiting) Color(0xFFFDF2F2) else c.surface)
            .clickable(onClick = onClick)
            .then(if (waiting) Modifier.border(0.dp, Color.Transparent) else Modifier)
            .padding(start = if (waiting) 9.dp else 12.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (waiting) {
            Box(Modifier.width(3.dp).height(44.dp).background(c.danger, RoundedCornerShape(2.dp)))
        }
        VdAvatar(name = conversation.contact.name, size = 44.dp, source = source)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    conversation.contact.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                val timeLabel = last?.createdAtRaw?.let { relativeTime(it) } ?: last?.createdAt.orEmpty()
                Text(
                    timeLabel,
                    fontSize = 11.sp,
                    fontWeight = if (waiting) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (waiting) c.danger else c.muted
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (fromAgent) {
                    Icon(Icons.Default.DoneAll, null, tint = c.info, modifier = Modifier.size(14.dp))
                }
                Text(
                    buildString {
                        if (fromAgent) append("Você: ")
                        append(last?.message?.replace(Regex("<[^>]*>"), "") ?: "Sem mensagens")
                    },
                    fontSize = 12.sp,
                    color = if (conversation.unreadMessages > 0) c.textTertiary else c.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (conversation.unreadMessages > 0) {
                    VdCountBadge(conversation.unreadMessages, background = if (waiting) c.danger else c.success)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (ticket != null) VdTag(stLabel, color = stFg, background = stBg)
                if (conversation.autoReply == 1) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Outlined.SmartToy, null, tint = c.info, modifier = Modifier.size(12.dp))
                        Text("bot ativo", fontSize = 10.sp, color = c.muted)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (ticket != null) {
                    Text(
                        buildString {
                            append("Ticket #${ticket.id}")
                            ticket.departmentName?.let { append(" · $it") }
                        },
                        fontSize = 10.sp, color = c.muted, maxLines = 1
                    )
                }
                if (agentName != null) {
                    VdAvatar(name = agentName, size = 18.dp, fontSize = 8, agent = true)
                } else {
                    Text("sem atendente", fontSize = 10.sp, color = c.muted)
                }
            }
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(c.surfaceAlt))
}
