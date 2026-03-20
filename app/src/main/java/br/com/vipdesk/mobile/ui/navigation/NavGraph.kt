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
import br.com.vipdesk.mobile.ui.conversations.ConversationListScreen
import br.com.vipdesk.mobile.ui.login.LoginScreen
import br.com.vipdesk.mobile.ui.login.LoginViewModel

object Routes {
    const val LOGIN = "login"
    const val CONVERSATIONS = "conversations"
    const val CONVERSATION_DETAIL = "conversation/{conversationId}"

    fun conversationDetail(conversationId: Int) = "conversation/$conversationId"
}

@Composable
fun VipDeskNavHost() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    val startDestination = if (loginState.isLoggedIn) Routes.CONVERSATIONS else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.CONVERSATIONS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CONVERSATIONS) {
            ConversationListScreen(
                onConversationClick = { conversationId ->
                    navController.navigate(Routes.conversationDetail(conversationId))
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.CONVERSATION_DETAIL,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.IntType }
            )
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
    }
}
