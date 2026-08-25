package br.com.vipdesk.mobile.ui.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.Conversation
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.common.sourceLabel
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCountBadge
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdIconButton
import br.com.vipdesk.mobile.ui.components.VdOutlineButton
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSearchField
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme

private data class SegDef(val label: String, val status: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onConversationClick: (Int) -> Unit,
    viewModel: ConversationListViewModel = viewModel(factory = ConversationListViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    var showFilters by remember { mutableStateOf(false) }

    val segs = listOf(
        SegDef("Minhas", "assigned"),
        SegDef("Aguardando", "waiting"),
        SegDef("Todas", "all")
    )
    fun segCount(status: String) = when (status) {
        "assigned" -> state.counts.assigned
        "waiting" -> state.counts.waiting
        else -> state.counts.total
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Inbox",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.4).sp,
                color = c.textPrimary,
                modifier = Modifier.weight(1f)
            )
            VdIconButton(
                Icons.Outlined.FilterAlt,
                onClick = { showFilters = true },
                size = 36.dp
            )
        }

        VdSearchField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = "Buscar conversas, contatos…",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(11.dp))
        VdPillRow {
            segs.forEach { seg ->
                VdPill(
                    label = seg.label,
                    count = segCount(seg.status),
                    selected = state.currentStatus == seg.status,
                    onClick = { viewModel.onStatusChange(seg.status) }
                )
            }
        }
        Spacer(Modifier.height(11.dp))

        when {
            state.isLoading && state.filteredConversations.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.accent)
                }
            }
            state.filteredConversations.isEmpty() -> {
                VdEmptyState(
                    icon = Icons.Outlined.Forum,
                    title = "Nenhuma conversa aqui",
                    subtitle = "Tente outro filtro ou limpe a busca."
                )
            }
            else -> {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.filteredConversations, key = { it.id }) { conv ->
                        ConversationRow(
                            conversation = conv,
                            currentUserId = state.currentUserId,
                            onClick = { onConversationClick(conv.id) }
                        )
                    }
                    item { Spacer(Modifier.height(60.dp)) }
                }
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Filtros",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                VdSectionLabel("Canal")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(
                        "whatsapp" to "WhatsApp",
                        "instagram" to "Instagram",
                        "facebook" to "Messenger",
                        "webchat" to "Web Chat"
                    ).forEach { (key, label) ->
                        VdPill(
                            label = label,
                            selected = state.currentMedia == key,
                            onClick = { viewModel.onMediaChange(key) }
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                VdOutlineButton(
                    label = "Aplicar filtros",
                    onClick = { showFilters = false },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    currentUserId: Int?,
    onClick: () -> Unit
) {
    val c = AppTheme.colors
    val ticket = conversation.activeTicket
    val (stLabel, _) = ticketStatusVisual(
        ticket?.statusName ?: ticket?.status ?: conversation.conversationState, null
    )
    val (_, priColor) = ticketPriorityVisual(ticket?.priority)
    val source = conversation.source ?: conversation.lastTicketSource
    val agentName = ticket?.user?.name
    val agentLabel = when {
        agentName == null -> "Não atribuída"
        ticket.userId != null && ticket.userId == currentUserId -> "Você"
        else -> agentName.split(" ").firstOrNull() ?: agentName
    }

    Column(Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            VdAvatar(name = conversation.contact.name, size = 46.dp, source = source)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conversation.contact.name,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        conversation.lastMessage?.createdAtRaw?.let { relativeTime(it) }
                            ?: conversation.lastMessage?.createdAt.orEmpty(),
                        fontSize = 11.sp,
                        color = c.textSecondary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        conversation.lastMessage?.message ?: "Sem mensagens",
                        fontSize = 13.sp,
                        color = c.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (conversation.unreadMessages > 0) {
                        VdCountBadge(conversation.unreadMessages)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.size(6.dp).background(priColor, CircleShape))
                    VdTag(stLabel)
                    source?.let {
                        VdTag(
                            sourceLabel(it),
                            color = c.accentTint,
                            background = c.accentSoft
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(agentLabel, fontSize = 10.5.sp, color = c.textFaint)
                }
            }
        }
        VdDivider(Modifier.padding(start = 73.dp))
    }
}
