package br.com.vipdesk.mobile.ui.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.Conversation
import br.com.vipdesk.mobile.ui.common.initialsOf
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.common.sourceVisual
import br.com.vipdesk.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onConversationClick: (Int) -> Unit,
    viewModel: ConversationListViewModel = viewModel(factory = ConversationListViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        // Header
        Text(
            text = "Conversas",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = c.textPrimary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search pill
        SearchPill(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status filter chips
        StatusChips(
            currentStatus = uiState.currentStatus,
            counts = uiState.counts,
            onStatusChange = viewModel::onStatusChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.conversations.isEmpty() -> {
                    CenterBox { CircularProgressIndicator(color = VipDeskPurple) }
                }

                uiState.error != null && uiState.conversations.isEmpty() -> {
                    CenterBox {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: "Erro desconhecido",
                                color = VipDeskRed,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.loadConversations() }) {
                                Text("Tentar novamente", color = VipDeskPurple)
                            }
                        }
                    }
                }

                uiState.filteredConversations.isEmpty() -> {
                    CenterBox {
                        Text(
                            text = "Nenhuma conversa encontrada",
                            color = c.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.filteredConversations,
                            key = { it.id }
                        ) { conversation ->
                            ConversationRow(
                                conversation = conversation,
                                currentUserId = uiState.currentUserId,
                                onClick = { onConversationClick(conversation.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPill(query: String, onQueryChange: (String) -> Unit) {
    val c = AppTheme.colors
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar conversa...", fontSize = 14.sp) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, "Limpar", modifier = Modifier.size(20.dp))
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .heightIn(min = 54.dp),
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = c.textPrimary,
            unfocusedTextColor = c.textPrimary,
            cursorColor = VipDeskPurple,
            focusedBorderColor = VipDeskPurple,
            unfocusedBorderColor = c.fieldBorder,
            focusedLeadingIconColor = VipDeskPurple,
            unfocusedLeadingIconColor = c.iconMuted,
            focusedContainerColor = c.surface,
            unfocusedContainerColor = c.surface,
            focusedPlaceholderColor = c.textSecondary,
            unfocusedPlaceholderColor = c.textSecondary
        )
    )
}

@Composable
private fun StatusChips(
    currentStatus: String,
    counts: br.com.vipdesk.mobile.data.model.ConversationCountResponse,
    onStatusChange: (String) -> Unit
) {
    val c = AppTheme.colors
    val tabs = listOf(
        Triple("assigned", "Meus", counts.assigned),
        Triple("waiting", "Aguardando", counts.waiting),
        Triple("all", "Todos", counts.total)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        tabs.forEach { (status, label, count) ->
            val selected = currentStatus == status
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) VipDeskPurple else c.surface)
                    .clickable { onStatusChange(status) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$label · $count",
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Color.White else c.textSecondary
                )
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
    val state = conversation.conversationState
    val statusColor = when (state) {
        "available", "composing" -> OnlineGreen
        else -> AwayGray
    }
    val source = conversation.source ?: conversation.lastTicketSource
    val visual = sourceVisual(source)
    val isMine = conversation.activeTicket?.userId == currentUserId

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = c.surface,
        shadowElevation = if (c.isDark) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PurpleGradientLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsOf(conversation.contact.name),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.contact.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        visual.icon,
                        contentDescription = source,
                        tint = visual.color,
                        modifier = Modifier.size(14.dp)
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ConfirmationNumber,
                            contentDescription = "Meu atendimento",
                            tint = VipDeskPurple,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                conversation.contact.client?.name?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = VipDeskPurple,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                val previewText: String
                val previewColor: Color
                val previewItalic: Boolean
                when (state) {
                    "composing" -> {
                        previewText = "digitando..."; previewColor = OnlineGreen; previewItalic = true
                    }
                    "available" -> {
                        previewText = "online"; previewColor = OnlineGreen; previewItalic = true
                    }
                    else -> {
                        val lastMsg = conversation.lastMessage
                        previewText = when (lastMsg?.type) {
                            "4" -> "📷 Imagem"
                            "5", "2" -> "🎤 Áudio"
                            "1" -> "🎥 Vídeo"
                            "3" -> "📄 Documento"
                            else -> lastMsg?.message?.replace(Regex("<[^>]*>"), "")?.trim().orEmpty()
                        }.ifBlank { "Sem mensagens" }
                        previewColor = c.textSecondary; previewItalic = false
                    }
                }
                Text(
                    text = previewText,
                    fontSize = 12.sp,
                    color = previewColor,
                    fontStyle = if (previewItalic) FontStyle.Italic else FontStyle.Normal,
                    fontWeight = if (previewItalic) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                conversation.lastMessage?.createdAtRaw?.let {
                    Text(
                        text = relativeTime(it),
                        fontSize = 11.sp,
                        color = c.textSecondary
                    )
                }
                if (conversation.unreadMessages > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(VipDeskGreen)
                            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conversation.unreadMessages.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { content() }
}
