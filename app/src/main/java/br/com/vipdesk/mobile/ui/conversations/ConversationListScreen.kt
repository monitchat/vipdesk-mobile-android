package br.com.vipdesk.mobile.ui.conversations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.Conversation
import coil.compose.AsyncImage
import br.com.vipdesk.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onConversationClick: (Int) -> Unit,
    onLogout: () -> Unit,
    viewModel: ConversationListViewModel = viewModel(factory = ConversationListViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sair") },
            text = { Text("Deseja realmente sair da sua conta?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout { onLogout() }
                }) {
                    Text("Sair", color = VipDeskRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AsyncImage(
                        model = "${br.com.vipdesk.mobile.BuildConfig.CDN_URL}/logo/logo-vipdesk-mobile.png",
                        contentDescription = "VipDesk",
                        modifier = Modifier.height(32.dp),
                        contentScale = ContentScale.Fit
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VipDeskPurple
                ),
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Atualizar", tint = Color.White)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Sair", tint = Color.White)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange
            )

            // Channel filter buttons
            ChannelFilterBar(
                channelCounts = uiState.channelCounts,
                currentMedia = uiState.currentMedia,
                onMediaChange = viewModel::onMediaChange
            )

            // Status filter tabs
            StatusFilterTabs(
                currentStatus = uiState.currentStatus,
                counts = uiState.counts,
                onStatusChange = viewModel::onStatusChange
            )

            // Conversation list
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.conversations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VipDeskPurple)
                        }
                    }

                    uiState.error != null && uiState.conversations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = uiState.error ?: "Erro desconhecido",
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { viewModel.loadConversations() }) {
                                    Text("Tentar novamente")
                                }
                            }
                        }
                    }

                    uiState.filteredConversations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma conversa encontrada",
                                color = TextSecondary
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.filteredConversations,
                                key = { it.id }
                            ) { conversation ->
                                ConversationCardItem(
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
}

@Composable
private fun ChannelFilterBar(
    channelCounts: br.com.vipdesk.mobile.data.model.ChannelCounts,
    currentMedia: String,
    onMediaChange: (String) -> Unit
) {
    val channels = listOf(
        Triple("whatsapp", channelCounts.whatsapp, WhatsAppGreen),
        Triple("facebook", channelCounts.facebook, Color(0xFF1877F2)),
        Triple("instagram", channelCounts.instagram, Color(0xFFE4405F)),
        Triple("telegram", channelCounts.telegram, Color(0xFF0088CC)),
        Triple("webchat", channelCounts.webchat, WebChatGray),
        Triple("monitcall", channelCounts.monitcall, PhoneOrange)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        channels.forEach { (media, count, color) ->
            val isSelected = currentMedia == media
            val icon = when (media) {
                "whatsapp" -> Icons.Default.Forum
                "facebook" -> Icons.Default.Facebook
                "instagram" -> Icons.Default.CameraAlt
                "telegram" -> Icons.AutoMirrored.Filled.Send
                "webchat" -> Icons.Default.Language
                "monitcall" -> Icons.Default.Phone
                else -> Icons.Default.Forum
            }
            OutlinedButton(
                onClick = { onMediaChange(media) },
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) color else Color.Transparent,
                    contentColor = if (isSelected) Color.White else color
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) color else color.copy(alpha = 0.4f)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(icon, contentDescription = media, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(count.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatusFilterTabs(
    currentStatus: String,
    counts: br.com.vipdesk.mobile.data.model.ConversationCountResponse,
    onStatusChange: (String) -> Unit
) {
    val tabs = listOf(
        Triple("assigned", "Meus", counts.assigned),
        Triple("waiting", "Aguardando", counts.waiting),
        Triple("all", "Todos", counts.total)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEach { (status, label, count) ->
            val isSelected = currentStatus == status

            OutlinedButton(
                onClick = { onStatusChange(status) },
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) VipDeskPurple else Color.Transparent,
                    contentColor = if (isSelected) Color.White else VipDeskPurple
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) VipDeskPurple else VipDeskPurple.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "$label $count",
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar conversa...", fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
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
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VipDeskPurple,
            unfocusedBorderColor = CardBorder
        )
    )
}

@Composable
private fun ConversationCardItem(
    conversation: Conversation,
    currentUserId: Int?,
    onClick: () -> Unit
) {
    val departmentColor = conversation.activeTicket?.departmentColor?.let {
        try {
            Color(android.graphics.Color.parseColor(it))
        } catch (_: Exception) {
            VipDeskPurple
        }
    } ?: VipDeskPurple

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Source indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(departmentColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AvatarGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(conversation.contact.name),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.contact.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Icons row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Source icon
                        SourceIcon(conversation.source ?: conversation.lastTicketSource)

                        // Unread badge
                        if (conversation.unreadMessages > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Badge(
                                containerColor = VipDeskGreen,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = conversation.unreadMessages.toString(),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // My ticket indicator
                        if (conversation.activeTicket?.userId == currentUserId) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.ConfirmationNumber,
                                contentDescription = "Meu atendimento",
                                modifier = Modifier.size(14.dp),
                                tint = VipDeskPurple
                            )
                        }
                    }
                }

                // Client name
                if (conversation.contact.client?.name != null) {
                    Text(
                        text = conversation.contact.client.name,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Last message or status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (conversation.conversationState) {
                        "composing" -> {
                            Text(
                                text = "digitando...",
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                color = VipDeskGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        "available" -> {
                            Text(
                                text = "online",
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                color = VipDeskGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        else -> {
                            val lastMsg = conversation.lastMessage
                            val messagePreview = when (lastMsg?.type) {
                                "4" -> "\uD83D\uDCF7 Imagem"
                                "5", "2" -> "\uD83C\uDFA4 Audio"
                                "1" -> "\uD83C\uDFA5 Video"
                                "3" -> "\uD83D\uDCC4 Documento"
                                else -> lastMsg?.message?.replace(Regex("<[^>]*>"), "") ?: ""
                            }
                            Text(
                                text = messagePreview,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Time
                    conversation.lastMessage?.createdAtRaw?.let { rawDate ->
                        Text(
                            text = formatRelativeTime(rawDate),
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceIcon(source: String?) {
    val icon = when (source) {
        "whatsapp" -> Icons.Default.Forum
        "email" -> Icons.Default.Email
        "monitcall", "phone" -> Icons.Default.Phone
        "webchat" -> Icons.Default.Language
        "campaing" -> Icons.Default.Campaign
        else -> Icons.Default.Forum
    }
    val tint = when (source) {
        "whatsapp" -> WhatsAppGreen
        "email" -> EmailBlue
        "monitcall", "phone" -> PhoneOrange
        "webchat" -> WebChatGray
        else -> TextSecondary
    }
    Icon(
        imageVector = icon,
        contentDescription = source,
        modifier = Modifier.size(16.dp),
        tint = tint
    )
}

private fun getInitials(fullName: String): String {
    return fullName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
}

private fun formatRelativeTime(dateStr: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale("pt", "BR"))
        val date = sdf.parse(dateStr) ?: return dateStr
        val now = System.currentTimeMillis()
        val diff = now - date.time
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        when {
            minutes < 1 -> "agora"
            minutes < 60 -> "${minutes}min"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> {
                val fmt = java.text.SimpleDateFormat("dd/MM", java.util.Locale("pt", "BR"))
                fmt.format(date)
            }
        }
    } catch (_: Exception) {
        dateStr
    }
}
