package br.com.vipdesk.mobile.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.vipdesk.mobile.data.notifications.PendingNav
import br.com.vipdesk.mobile.data.session.SessionEvents
import br.com.vipdesk.mobile.di.AppContainer
import kotlinx.coroutines.launch
import br.com.vipdesk.mobile.ui.conversations.ConversationDetailScreen
import br.com.vipdesk.mobile.ui.conversations.ConversationDetailViewModel
import br.com.vipdesk.mobile.ui.create.CreateFlow
import br.com.vipdesk.mobile.ui.create.CreateFlowSheets
import br.com.vipdesk.mobile.ui.crm.ContactDetailScreen
import br.com.vipdesk.mobile.ui.crm.ContactsListScreen
import br.com.vipdesk.mobile.ui.crm.DealDetailScreen
import br.com.vipdesk.mobile.ui.crm.DealsKanbanScreen
import br.com.vipdesk.mobile.ui.dashboard.DashboardScreen
import br.com.vipdesk.mobile.ui.kanban.KanbanCardScreen
import br.com.vipdesk.mobile.ui.kanban.KanbanScreen
import br.com.vipdesk.mobile.ui.login.LoginScreen
import br.com.vipdesk.mobile.ui.login.LoginViewModel
import br.com.vipdesk.mobile.ui.main.MainShell
import br.com.vipdesk.mobile.ui.main.ShellNav
import br.com.vipdesk.mobile.ui.notifications.NotificationsScreen
import br.com.vipdesk.mobile.ui.profile.ProfileScreen
import br.com.vipdesk.mobile.ui.reports.ReportsScreen
import br.com.vipdesk.mobile.ui.search.SearchScreen
import br.com.vipdesk.mobile.ui.ticket.TicketDetailScreen
import br.com.vipdesk.mobile.ui.ticket.TicketDetailViewModel
import br.com.vipdesk.mobile.ui.ticket.TicketsScreen

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val NOTIFICATIONS = "notifications"
    const val CONVERSATION_DETAIL = "conversation/{conversationId}"
    const val TICKETS = "tickets"
    const val TICKET_DETAIL = "ticket/{ticketId}"
    const val CONTACTS = "contacts"
    const val CONTACT_DETAIL = "contact/{contactId}"
    const val DEALS = "deals"
    const val DEAL_DETAIL = "deal/{dealId}"
    const val KANBAN = "kanban"
    const val KANBAN_CARD = "kanban/card/{cardId}"
    const val DASHBOARD = "dashboard"
    const val REPORTS = "reports"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val MODULE = "module/{key}"
    const val INTERNAL_CHAT = "internalchat/{chatId}?title={title}"

    fun conversationDetail(conversationId: Int) = "conversation/$conversationId"
    fun ticketDetail(ticketId: Int) = "ticket/$ticketId"
    fun contactDetail(contactId: Int) = "contact/$contactId"
    fun dealDetail(dealId: Int) = "deal/$dealId"
    fun kanbanCard(cardId: String) = "kanban/card/$cardId"
    fun module(key: String) = "module/$key"
    fun internalChat(chatId: Int, title: String) = "internalchat/$chatId?title=${android.net.Uri.encode(title)}"
}

@Composable
fun VipDeskNavHost() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()
    var createFlow by remember { mutableStateOf<CreateFlow?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toast) { if (toast != null) { kotlinx.coroutines.delay(3000); toast = null } }

    val startDestination = if (loginState.isLoggedIn) Routes.MAIN else Routes.LOGIN

    // Toque em notificação de mensagem → navega para a conversa.
    LaunchedEffect(PendingNav.conversationId, loginState.isLoggedIn) {
        val conversationId = PendingNav.conversationId ?: return@LaunchedEffect
        if (loginState.isLoggedIn) {
            PendingNav.conversationId = null
            navController.navigate(Routes.conversationDetail(conversationId))
        }
    }

    val openConversation: (Int) -> Unit = { navController.navigate(Routes.conversationDetail(it)) }
    val openTicket: (Int) -> Unit = { navController.navigate(Routes.ticketDetail(it)) }
    val openContact: (Int) -> Unit = { navController.navigate(Routes.contactDetail(it)) }
    val openDeal: (Int) -> Unit = { navController.navigate(Routes.dealDetail(it)) }
    val back: () -> Unit = { navController.popBackStack() }
    val scope = rememberCoroutineScope()
    // Logout real: desconecta o socket, revoga/limpa a sessão e só então volta ao login
    // (navegar sem limpar o token fazia a tela de login "voltar" para dentro do app).
    val logout: () -> Unit = {
        scope.launch {
            AppContainer.socketService.disconnect()
            AppContainer.authRepository.logout()
            br.com.vipdesk.mobile.data.session.PresenceState.clear()
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }

    // Heartbeat de sessão: o backend desloga por inatividade (`last_seen`) via
    // monitchat:logout_user; o web pinga a cada 5 min e o app faz o mesmo em qualquer
    // tela autenticada enquanto está em primeiro plano.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val onLoginScreen = backStackEntry?.destination?.route == Routes.LOGIN
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(onLoginScreen) {
        if (onLoginScreen) return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                runCatching { AppContainer.apiService.ping() }
                kotlinx.coroutines.delay(5 * 60 * 1000L)
            }
        }
    }

    // Sessão perdida (401/403 de token na API ou UserLoggedOut do socket): sem isso o app
    // ficava na tela com tudo zerado e todas as chamadas falhando em silêncio.
    val sessionExpired by SessionEvents.expired.collectAsStateWithLifecycle()
    LaunchedEffect(sessionExpired) {
        val reason = sessionExpired ?: return@LaunchedEffect
        AppContainer.socketService.disconnect()
        AppContainer.tokenManager.clearAll()
        br.com.vipdesk.mobile.data.session.PresenceState.clear()
        loginViewModel.onSessionLost()
        if (navController.currentDestination?.route != Routes.LOGIN) {
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
        toast = reason
        SessionEvents.consume()
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Routes.MAIN) { popUpTo(Routes.LOGIN) { inclusive = true } }
            })
        }

        composable(Routes.MAIN) {
            MainShell(
                ShellNav(
                    onConversationClick = openConversation,
                    onTicketClick = openTicket,
                    onTicketsList = { navController.navigate(Routes.TICKETS) },
                    onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                    onContactClick = openContact,
                    onContactsList = { navController.navigate(Routes.CONTACTS) },
                    onDealsKanban = { navController.navigate(Routes.DEALS) },
                    onDealClick = openDeal,
                    onOpenDashboard = { navController.navigate(Routes.DASHBOARD) },
                    onOpenKanbanBoard = { navController.navigate(Routes.KANBAN) },
                    onOpenReports = { navController.navigate(Routes.REPORTS) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onLogout = logout,
                    onOpenModule = { navController.navigate(Routes.module(it)) }
                )
            )
        }

        composable(Routes.NOTIFICATIONS) { NotificationsScreen(onBack = back) }

        composable(
            route = Routes.CONVERSATION_DETAIL,
            arguments = listOf(navArgument("conversationId") { type = NavType.IntType })
        ) { entry ->
            val conversationId = entry.arguments?.getInt("conversationId") ?: return@composable
            val vm: ConversationDetailViewModel = viewModel(factory = ConversationDetailViewModel.Factory(conversationId))
            ConversationDetailScreen(onBack = back, viewModel = vm, onOpenTicket = openTicket, onOpenContact = openContact)
        }

        composable(Routes.TICKETS) {
            TicketsScreen(onTicketClick = openTicket, onBack = back, onCreateTicket = { createFlow = CreateFlow.TICKET })
        }

        composable(
            route = Routes.TICKET_DETAIL,
            arguments = listOf(navArgument("ticketId") { type = NavType.IntType })
        ) { entry ->
            val ticketId = entry.arguments?.getInt("ticketId") ?: return@composable
            val vm: TicketDetailViewModel = viewModel(factory = TicketDetailViewModel.Factory(ticketId))
            TicketDetailScreen(onBack = back, onOpenContact = openContact, viewModel = vm, onOpenConversation = openConversation)
        }

        composable(Routes.CONTACTS) {
            ContactsListScreen(onBack = back, onContactClick = openContact, onCreateContact = { createFlow = CreateFlow.CONTATO }, onToast = { toast = it })
        }

        composable(
            route = Routes.CONTACT_DETAIL,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { entry ->
            val contactId = entry.arguments?.getInt("contactId") ?: return@composable
            ContactDetailScreen(contactId = contactId, onBack = back, onOpenConversation = openConversation, onOpenDeal = openDeal)
        }

        composable(Routes.DEALS) {
            DealsKanbanScreen(onBack = back, onDealClick = openDeal, onCreateDeal = { createFlow = CreateFlow.NEGOCIO })
        }

        composable(
            route = Routes.DEAL_DETAIL,
            arguments = listOf(navArgument("dealId") { type = NavType.IntType })
        ) { entry ->
            val dealId = entry.arguments?.getInt("dealId") ?: return@composable
            DealDetailScreen(dealId = dealId, onBack = back, onOpenContact = openContact, onOpenConversation = openConversation)
        }

        composable(Routes.KANBAN) {
            KanbanScreen(onBack = back, onCardClick = { navController.navigate(Routes.kanbanCard(it)) })
        }

        composable(
            route = Routes.KANBAN_CARD,
            arguments = listOf(navArgument("cardId") { type = NavType.StringType })
        ) { entry ->
            val cardId = entry.arguments?.getString("cardId") ?: return@composable
            KanbanCardScreen(cardId = cardId, onBack = back)
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onBack = back,
                onTicketClick = openTicket,
                onSeeAllTickets = { navController.navigate(Routes.TICKETS) },
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) }
            )
        }

        composable(Routes.REPORTS) { ReportsScreen(onBack = back) }

        composable(Routes.SEARCH) {
            SearchScreen(onBack = back, onContactClick = openContact, onTicketClick = openTicket)
        }

        composable(Routes.SETTINGS) { ProfileScreen(onBack = back, onLogout = logout, onOpenModule = { navController.navigate(Routes.module(it)) }) }

        // Módulos "somente web" do design que têm endpoint (campanhas, KB, aprovações, ...)
        composable(Routes.MODULE, arguments = listOf(navArgument("key") { type = NavType.StringType })) { entry ->
            val key = entry.arguments?.getString("key") ?: return@composable
            br.com.vipdesk.mobile.ui.modules.ModuleScreen(
                key,
                br.com.vipdesk.mobile.ui.modules.ModuleNav(
                    onBack = back, onOpenTicket = openTicket, onOpenConversation = openConversation,
                    onOpenContact = openContact, onOpenDeal = openDeal,
                    onOpenChat = { id, title -> navController.navigate(Routes.internalChat(id, title)) }
                )
            )
        }
        composable(
            Routes.INTERNAL_CHAT,
            arguments = listOf(navArgument("chatId") { type = NavType.IntType }, navArgument("title") { type = NavType.StringType; defaultValue = "Chat" })
        ) { entry ->
            val chatId = entry.arguments?.getInt("chatId") ?: return@composable
            br.com.vipdesk.mobile.ui.modules.InternalChatRoomScreen(chatId, entry.arguments?.getString("title") ?: "Chat", back)
        }
    }

    // Fluxos de criação acionados por telas empilhadas (lista de tickets, contatos, kanban de negócios)
    CreateFlowSheets(
        flow = createFlow,
        onDismiss = { createFlow = null },
        onOpenConversation = openConversation,
        onOpenTicket = openTicket,
        onOpenKanban = { navController.navigate(Routes.KANBAN) },
        onOpenCrm = { navController.navigate(Routes.DEALS) },
        onToast = { toast = it }
    )
    toast?.let {
        androidx.compose.foundation.layout.Box(
            androidx.compose.ui.Modifier.fillMaxSize().padding(bottom = 96.dp),
            contentAlignment = androidx.compose.ui.Alignment.BottomCenter
        ) { br.com.vipdesk.mobile.ui.components.VdToast(it) }
    }
}
