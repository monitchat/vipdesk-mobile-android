package br.com.vipdesk.mobile.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.components.VdSheetRow
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.conversations.ConversationListScreen
import br.com.vipdesk.mobile.ui.create.CreateFlow
import br.com.vipdesk.mobile.ui.create.CreateFlowSheets
import br.com.vipdesk.mobile.ui.crm.CrmScreen
import br.com.vipdesk.mobile.ui.dashboard.DashboardScreen
import br.com.vipdesk.mobile.ui.mais.MaisScreen
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.ticket.TicketsScreen
import kotlinx.coroutines.delay

private data class TabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShell(
    onConversationClick: (Int) -> Unit,
    onTicketClick: (Int) -> Unit,
    onNotificationsClick: () -> Unit,
    onContactClick: (Int) -> Unit,
    onOpenKanban: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var createFlow by remember { mutableStateOf<CreateFlow?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    val c = AppTheme.colors

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1900)
            toast = null
        }
    }

    // Socket em tempo real ativo desde a entrada no app (não só na aba Inbox):
    // alimenta atualização das conversas e as notificações de mensagem.
    LaunchedEffect(Unit) {
        val userId = AppContainer.tokenManager.getUserId()
        val companyId = AppContainer.tokenManager.getCompanyId()
        if (userId != null && companyId != null && !AppContainer.socketService.isConnected) {
            AppContainer.socketService.connect(companyId, userId)
        }
    }

    val tabs = listOf(
        TabItem("Início", Icons.Filled.Home, Icons.Outlined.Home),
        TabItem("Inbox", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
        TabItem("CRM", Icons.Filled.Contacts, Icons.Outlined.Contacts),
        TabItem("Tickets", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
        TabItem("Mais", Icons.Filled.GridView, Icons.Outlined.GridView)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        Box(Modifier.fillMaxSize().padding(bottom = 64.dp)) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    onTicketClick = onTicketClick,
                    onSeeAllTickets = { selectedTab = 3 },
                    onNotificationsClick = onNotificationsClick,
                    onSearchClick = onOpenSearch,
                    onSettingsClick = onOpenSettings,
                    onOpenInbox = { selectedTab = 1 }
                )
                1 -> ConversationListScreen(onConversationClick = onConversationClick)
                2 -> CrmScreen(onContactClick = onContactClick)
                3 -> TicketsScreen(onTicketClick = onTicketClick)
                4 -> MaisScreen(
                    onOpenKanban = onOpenKanban,
                    onOpenReports = onOpenReports,
                    onOpenNotifications = onNotificationsClick,
                    onOpenSearch = onOpenSearch,
                    onOpenSettings = onOpenSettings
                )
            }
        }

        // FAB "Criar novo" (oculto na aba Mais, como no design)
        if (selectedTab != 4) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 84.dp)
                    .size(52.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(c.accentStrong)
                    .border(1.dp, c.accentBorder, RoundedCornerShape(17.dp))
                    .clickable { showCreateSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add, "Criar novo",
                    tint = c.onAccentStrong,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Barra de navegação inferior do design
        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(c.navBg)
                    .padding(horizontal = 6.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = selectedTab == index
                    val tint = if (selected) c.accent else c.textSecondary
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedTab = index }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            if (selected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(23.dp)
                        )
                        Text(
                            tab.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = tint
                        )
                    }
                }
            }
        }

        toast?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 108.dp)
            ) { VdToast(it) }
        }
    }

    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(
                    "Criar novo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                VdSheetRow(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = "Nova conversa",
                    subtitle = "Abrir a conversa de um contato",
                    iconBoxed = true,
                    onClick = {
                        showCreateSheet = false
                        createFlow = CreateFlow.CONVERSA
                    }
                )
                VdSheetRow(
                    icon = Icons.Outlined.PersonAddAlt,
                    label = "Novo contato",
                    subtitle = "Adicionar pessoa ou empresa ao CRM",
                    iconBoxed = true,
                    onClick = {
                        showCreateSheet = false
                        createFlow = CreateFlow.CONTATO
                    }
                )
                VdSheetRow(
                    icon = Icons.Outlined.MonetizationOn,
                    label = "Novo negócio",
                    subtitle = "Criar oportunidade no funil do CRM",
                    iconBoxed = true,
                    onClick = {
                        showCreateSheet = false
                        createFlow = CreateFlow.NEGOCIO
                    }
                )
                VdSheetRow(
                    icon = Icons.Outlined.ConfirmationNumber,
                    label = "Novo ticket",
                    subtitle = "Abrir chamado de suporte",
                    iconBoxed = true,
                    onClick = {
                        showCreateSheet = false
                        createFlow = CreateFlow.TICKET
                    }
                )
                VdSheetRow(
                    icon = Icons.Outlined.CheckBox,
                    label = "Nova tarefa",
                    subtitle = "Criar cartão em um quadro",
                    iconBoxed = true,
                    onClick = {
                        showCreateSheet = false
                        createFlow = CreateFlow.TAREFA
                    }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    CreateFlowSheets(
        flow = createFlow,
        onDismiss = { createFlow = null },
        onOpenConversation = onConversationClick,
        onOpenTicket = onTicketClick,
        onOpenKanban = onOpenKanban,
        onOpenCrm = { selectedTab = 2 },
        onToast = { toast = it }
    )
}
