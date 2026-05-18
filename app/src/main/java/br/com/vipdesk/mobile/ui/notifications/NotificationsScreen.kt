package br.com.vipdesk.mobile.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.MobileNotification
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(factory = NotificationsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors

    Scaffold(
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text("Notificações", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    if (uiState.unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllRead() }) {
                            Icon(
                                Icons.Default.DoneAll,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = VipDeskPurple
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Marcar todas", fontSize = 12.sp, color = VipDeskPurple)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.surface,
                    titleContentColor = c.textPrimary,
                    navigationIconContentColor = c.textPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterPill(
                    text = "Todas",
                    selected = !uiState.onlyUnread,
                    onClick = { viewModel.setFilter(false) },
                    modifier = Modifier.weight(1f)
                )
                FilterPill(
                    text = if (uiState.unreadCount > 0) "Não lidas · ${uiState.unreadCount}" else "Não lidas",
                    selected = uiState.onlyUnread,
                    onClick = { viewModel.setFilter(true) },
                    modifier = Modifier.weight(1f)
                )
            }

            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = VipDeskPurple)
                    }
                }
                uiState.items.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text(
                            if (uiState.error != null) uiState.error!!
                            else "Nenhuma notificação",
                            color = c.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.items, key = { it.id }) { n ->
                            NotificationRow(n) { viewModel.markRead(n.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = AppTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) VipDeskPurple else c.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else c.textSecondary
        )
    }
}

private fun kindVisual(kind: String): Pair<ImageVector, Color> = when (kind) {
    "message" -> Icons.AutoMirrored.Filled.Chat to WhatsAppGreen
    "assignment" -> Icons.Default.SwapHoriz to VipDeskPurple
    "sla" -> Icons.Default.Schedule to VipDeskRed
    "comment" -> Icons.Default.ModeComment to VipDeskOrange
    else -> Icons.Default.Notifications to VipDeskBlue
}

@Composable
private fun NotificationRow(n: MobileNotification, onClick: () -> Unit) {
    val c = AppTheme.colors
    val (icon, color) = kindVisual(n.kind)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (!n.read && !c.isDark) VipDeskPurpleContainer.copy(alpha = 0.35f) else c.surface,
        shadowElevation = if (c.isDark) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (c.isDark) 0.24f else 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = n.message,
                    fontSize = 13.sp,
                    fontWeight = if (n.read) FontWeight.Normal else FontWeight.SemiBold,
                    color = c.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                n.createdAt?.let {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = relativeTime(it), fontSize = 11.sp, color = c.textSecondary)
                }
            }
            if (!n.read) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(VipDeskPurple)
                )
            }
        }
    }
}
