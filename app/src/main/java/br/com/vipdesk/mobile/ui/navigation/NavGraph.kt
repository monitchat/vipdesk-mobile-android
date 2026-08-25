package br.com.vipdesk.mobile.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.vipdesk.mobile.data.notifications.PendingNav
import br.com.vipdesk.mobile.ui.conversations.ConversationDetailScreen
import br.com.vipdesk.mobile.ui.conversations.ConversationDetailViewModel
import br.com.vipdesk.mobile.ui.crm.ContactDetailScreen
import br.com.vipdesk.mobile.ui.kanban.KanbanCardScreen
import br.com.vipdesk.mobile.ui.kanban.KanbanScreen
import br.com.vipdesk.mobile.ui.login.LoginScreen
import br.com.vipdesk.mobile.ui.login.LoginViewModel
import br.com.vipdesk.mobile.ui.main.MainShell
import br.com.vipdesk.mobile.ui.notifications.NotificationsScreen
import br.com.vipdesk.mobile.ui.profile.ProfileScreen
import br.com.vipdesk.mobile.ui.reports.ReportsScreen
import br.com.vipdesk.mobile.ui.search.SearchScreen
import br.com.vipdesk.mobile.ui.ticket.TicketDetailScreen
import br.com.vipdesk.mobile.ui.ticket.TicketDetailViewModel

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val NOTIFICATIONS = "notifications"
    const val CONVERSATION_DETAIL = "conversation/{conversationId}"
    const val TICKET_DETAIL = "ticket/{ticketId}"
    const val CONTACT_DETAIL = "contact/{contactId}"
    const val KANBAN = "kanban"
    const val KANBAN_CARD = "kanban/card/{cardId}"
    const val REPORTS = "reports"
    const val SEARCH = "search"
    const val SETTINGS = "settings"

    fun conversationDetail(conversationId: Int) = "conversation/$conversationId"
    fun ticketDetail(ticketId: Int) = "ticket/$ticketId"
    fun contactDetail(contactId: Int) = "contact/$contactId"
    fun kanbanCard(cardId: String) = "kanban/card/$cardId"
}

@Composable
fun VipDeskNavHost() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    val startDestination = if (loginState.isLoggedIn) Routes.MAIN else Routes.LOGIN

    // Toque em notificação de mensagem → navega para a conversa.
    LaunchedEffect(PendingNav.conversationId, loginState.isLoggedIn) {
        val conversationId = PendingNav.conversationId ?: return@LaunchedEffect
        if (loginState.isLoggedIn) {
            PendingNav.conversationId = null
            navController.navigate(Routes.conversationDetail(conversationId))
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainShell(
                onConversationClick = { conversationId ->
                    navController.navigate(Routes.conversationDetail(conversationId))
                },
                onTicketClick = { ticketId ->
                    navController.navigate(Routes.ticketDetail(ticketId))
                },
                onNotificationsClick = {
                    navController.navigate(Routes.NOTIFICATIONS)
                },
                onContactClick = { contactId ->
                    navController.navigate(Routes.contactDetail(contactId))
                },
                onOpenKanban = { navController.navigate(Routes.KANBAN) },
                onOpenReports = { navController.navigate(Routes.REPORTS) },
                onOpenSearch = { navController.navigate(Routes.SEARCH) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.CONVERSATION_DETAIL,
            arguments = listOf(navArgument("conversationId") { type = NavType.IntType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getInt("conversationId") ?: return@composable
            val detailViewModel: ConversationDetailViewModel = viewModel(
                factory = ConversationDetailViewModel.Factory(conversationId)
            )
            ConversationDetailScreen(
                onBack = { navController.popBackStack() },
                viewModel = detailViewModel
            )
        }

        composable(
            route = Routes.TICKET_DETAIL,
            arguments = listOf(navArgument("ticketId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: return@composable
            val ticketViewModel: TicketDetailViewModel = viewModel(
                factory = TicketDetailViewModel.Factory(ticketId)
            )
            TicketDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenContact = { },
                viewModel = ticketViewModel
            )
        }

        composable(
            route = Routes.CONTACT_DETAIL,
            arguments = listOf(navArgument("contactId") { type = NavType.IntType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getInt("contactId") ?: return@composable
            ContactDetailScreen(
                contactId = contactId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.KANBAN) {
            KanbanScreen(
                onBack = { navController.popBackStack() },
                onCardClick = { cardId -> navController.navigate(Routes.kanbanCard(cardId)) }
            )
        }

        composable(
            route = Routes.KANBAN_CARD,
            arguments = listOf(navArgument("cardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
            KanbanCardScreen(
                cardId = cardId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REPORTS) {
            ReportsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onContactClick = { contactId ->
                    navController.navigate(Routes.contactDetail(contactId))
                },
                onTicketClick = { ticketId ->
                    navController.navigate(Routes.ticketDetail(ticketId))
                }
            )
        }

        composable(Routes.SETTINGS) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
