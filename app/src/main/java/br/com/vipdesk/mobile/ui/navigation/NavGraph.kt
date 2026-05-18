package br.com.vipdesk.mobile.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.vipdesk.mobile.ui.conversations.ConversationDetailScreen
import br.com.vipdesk.mobile.ui.conversations.ConversationDetailViewModel
import br.com.vipdesk.mobile.ui.login.LoginScreen
import br.com.vipdesk.mobile.ui.login.LoginViewModel
import br.com.vipdesk.mobile.ui.main.MainShell
import br.com.vipdesk.mobile.ui.notifications.NotificationsScreen
import br.com.vipdesk.mobile.ui.ticket.TicketDetailScreen
import br.com.vipdesk.mobile.ui.ticket.TicketDetailViewModel

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val NOTIFICATIONS = "notifications"
    const val CONVERSATION_DETAIL = "conversation/{conversationId}"
    const val TICKET_DETAIL = "ticket/{ticketId}"

    fun conversationDetail(conversationId: Int) = "conversation/$conversationId"
    fun ticketDetail(ticketId: Int) = "ticket/$ticketId"
}

@Composable
fun VipDeskNavHost() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    val startDestination = if (loginState.isLoggedIn) Routes.MAIN else Routes.LOGIN

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
    }
}
