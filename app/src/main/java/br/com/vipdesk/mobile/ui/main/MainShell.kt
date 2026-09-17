package br.com.vipdesk.mobile.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.ConversationCountResponse
import br.com.vipdesk.mobile.data.socket.SocketEvent
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.agenda.AgendaScreen
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.conversations.ConversationListScreen
import br.com.vipdesk.mobile.ui.create.CreateFlow
import br.com.vipdesk.mobile.ui.create.CreateFlowSheets
import br.com.vipdesk.mobile.ui.crm.CrmHubScreen
import br.com.vipdesk.mobile.ui.helpdesk.HelpdeskHubScreen
import br.com.vipdesk.mobile.ui.theme.AppTheme
import kotlinx.coroutines.delay

/** Destinos que a shell pode abrir a partir das abas e da gaveta "Mais". */
class ShellNav(
    val onConversationClick: (Int) -> Unit,
    val onTicketClick: (Int) -> Unit,
    val onTicketsList: () -> Unit,
    val onNotificationsClick: () -> Unit,
    val onContactClick: (Int) -> Unit,
    val onContactsList: () -> Unit,
    val onDealsKanban: () -> Unit,
    val onDealClick: (Int) -> Unit,
    val onOpenDashboard: () -> Unit,
    val onOpenKanbanBoard: () -> Unit,
    val onOpenReports: () -> Unit,
    val onOpenSearch: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onLogout: () -> Unit
)

private data class TabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainShell(nav: ShellNav) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showMore by remember { mutableStateOf(false) }
    var createFlow by remember { mutableStateOf<CreateFlow?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    var counts by remember { mutableStateOf(ConversationCountResponse()) }
    var unreadNotifications by remember { mutableIntStateOf(0) }
    val c = AppTheme.colors

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(2200)
            toast = null
        }
    }

    // Socket em tempo real desde a entrada no app (alimenta listas, badges e
    // notificações). Contadores da aba Conversas seguem os eventos.
    LaunchedEffect(Unit) {
        val userId = AppContainer.tokenManager.getUserId()
        val companyId = AppContainer.tokenManager.getCompanyId()
        if (userId != null && companyId != null && !AppContainer.socketService.isConnected) {
            AppContainer.socketService.connect(companyId, userId)
        }
        refreshCounts { counts = it }
        br.com.vipdesk.mobile.data.session.PresenceState.refresh()
        AppContainer.mobileRepository.getNotifications(onlyUnread = true)
            .onSuccess { (_, n) -> unreadNotifications = n }
        AppContainer.socketService.events.collect { event ->
            when (event) {
                is SocketEvent.MessageReceived,
                is SocketEvent.MessageAnswered,
                is SocketEvent.ConversationCountChanged,
                is SocketEvent.TicketChangedOwner,
                is SocketEvent.TicketChangedStatus -> refreshCounts { counts = it }
                else -> {}
            }
        }
    }

    val tabs = listOf(
        TabItem("Conversas", Icons.Filled.Forum, Icons.Outlined.Forum),
        TabItem("Tickets", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
        TabItem("CRM", Icons.Filled.ViewKanban, Icons.Outlined.ViewKanban),
        TabItem("Agenda", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        TabItem("Mais", Icons.Filled.GridView, Icons.Outlined.GridView)
    )

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> ConversationListScreen(
                        onConversationClick = nav.onConversationClick,
                        onNotificationsClick = nav.onNotificationsClick,
                        onNewConversation = { createFlow = CreateFlow.CONVERSA },
                        unreadNotifications = unreadNotifications
                    )
                    1 -> HelpdeskHubScreen(
                        onOpenTickets = nav.onTicketsList,
                        onOpenKanban = nav.onOpenKanbanBoard,
                        onOpenDashboard = nav.onOpenDashboard,
                        onOpenReports = nav.onOpenReports,
                        onSearch = nav.onOpenSearch,
                        onNotifications = nav.onNotificationsClick,
                        onCreateTicket = { createFlow = CreateFlow.TICKET },
                        onToast = { toast = it },
                        unreadNotifications = unreadNotifications
                    )
                    2 -> CrmHubScreen(
                        onOpenDeals = nav.onDealsKanban,
                        onOpenContacts = nav.onContactsList,
                        onOpenKanban = nav.onOpenKanbanBoard,
                        onSearch = nav.onOpenSearch,
                        onNotifications = nav.onNotificationsClick,
                        onToast = { toast = it },
                        unreadNotifications = unreadNotifications
                    )
                    3 -> AgendaScreen(
                        onConversationClick = nav.onConversationClick,
                        onToast = { toast = it }
                    )
                }
            }

            // Menu inferior (tela 01)
            Column(Modifier.background(c.surface)) {
                VdDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 6.dp)
                        .navigationBarsPadding()
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isMore = index == 4
                        val selected = if (isMore) showMore else selectedTab == index
                        val tint = if (selected) c.primary else c.muted
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (isMore) showMore = true else selectedTab = index
                                }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box {
                                Icon(
                                    if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label,
                                    tint = tint,
                                    modifier = Modifier.size(24.dp)
                                )
                                if (index == 0) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 14.dp, y = (-4).dp),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        if (counts.waiting > 0) MiniBadge(counts.waiting, c.danger)
                                        if (counts.assigned > 0) MiniBadge(counts.assigned, c.success)
                                    }
                                }
                            }
                            Text(tab.label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = tint)
                        }
                    }
                }
            }
        }

        toast?.let {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp)
            ) { VdToast(it) }
        }
    }

    if (showMore) {
        MaisDrawer(
            onDismiss = { showMore = false },
            onOpenDashboard = nav.onOpenDashboard,
            onOpenReports = nav.onOpenReports,
            onOpenSettings = nav.onOpenSettings,
            onOpenNotifications = nav.onNotificationsClick,
            onLogout = nav.onLogout,
            onToast = { toast = it }
        )
    }

    CreateFlowSheets(
        flow = createFlow,
        onDismiss = { createFlow = null },
        onOpenConversation = nav.onConversationClick,
        onOpenTicket = nav.onTicketClick,
        onOpenKanban = nav.onOpenKanbanBoard,
        onOpenCrm = { selectedTab = 2 },
        onToast = { toast = it }
    )
}

@Composable
private fun MiniBadge(n: Int, color: Color) {
    Box(
        modifier = Modifier
            .background(color, androidx.compose.foundation.shape.RoundedCornerShape(7.dp))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (n > 99) "99+" else "$n",
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 13.sp
        )
    }
}

private suspend fun refreshCounts(onResult: (ConversationCountResponse) -> Unit) {
    try {
        val response = AppContainer.apiService.getConversationCount()
        if (response.isSuccessful) response.body()?.let(onResult)
    } catch (_: Exception) {
    }
}
