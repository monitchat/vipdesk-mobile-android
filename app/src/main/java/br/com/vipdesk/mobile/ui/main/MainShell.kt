package br.com.vipdesk.mobile.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.ui.components.ComingSoon
import br.com.vipdesk.mobile.ui.conversations.ConversationListScreen
import br.com.vipdesk.mobile.ui.dashboard.DashboardScreen
import br.com.vipdesk.mobile.ui.profile.ProfileScreen
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.IconMuted
import br.com.vipdesk.mobile.ui.theme.VipDeskPurple
import br.com.vipdesk.mobile.ui.theme.VipDeskPurpleContainer
import br.com.vipdesk.mobile.ui.ticket.TicketsScreen

private data class TabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainShell(
    onConversationClick: (Int) -> Unit,
    onTicketClick: (Int) -> Unit,
    onNotificationsClick: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val c = AppTheme.colors

    val tabs = listOf(
        TabItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        TabItem("Inbox", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
        TabItem("Tickets", Icons.Filled.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
        TabItem("Contatos", Icons.Filled.Contacts, Icons.Outlined.Contacts),
        TabItem("Mais", Icons.Filled.Menu, Icons.Outlined.Menu)
    )

    Scaffold(
        containerColor = c.background,
        bottomBar = {
            NavigationBar(
                containerColor = c.surface,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = selectedTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VipDeskPurple,
                            selectedTextColor = VipDeskPurple,
                            indicatorColor = if (c.isDark) VipDeskPurple.copy(alpha = 0.22f) else VipDeskPurpleContainer,
                            unselectedIconColor = IconMuted,
                            unselectedTextColor = IconMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    onTicketClick = onTicketClick,
                    onSeeAllTickets = { selectedTab = 2 },
                    onNotificationsClick = onNotificationsClick
                )
                1 -> ConversationListScreen(onConversationClick = onConversationClick)
                2 -> TicketsScreen(onTicketClick = onTicketClick)
                3 -> ComingSoon(
                    title = "Contatos",
                    subtitle = "A agenda de contatos chega em breve ao app.",
                    icon = Icons.Filled.Contacts
                )
                4 -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}
