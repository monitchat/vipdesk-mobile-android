package br.com.vipdesk.mobile.ui.mais

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.demo.DEMO_TEAM
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import br.com.vipdesk.mobile.ui.theme.VdWarning

@Composable
fun MaisScreen(
    onOpenKanban: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val c = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
    ) {
        Text(
            "Mais",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.4).sp,
            color = c.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MaisItem(Icons.Outlined.TableChart, "Quadros", Modifier.weight(1f), onOpenKanban)
                MaisItem(Icons.Outlined.TrendingUp, "Relatórios", Modifier.weight(1f), onOpenReports)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MaisItem(Icons.Outlined.Notifications, "Notificações", Modifier.weight(1f), onOpenNotifications)
                MaisItem(Icons.Outlined.Search, "Busca global", Modifier.weight(1f), onOpenSearch)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MaisItem(Icons.Outlined.Settings, "Configurações", Modifier.weight(1f), onOpenSettings)
                MaisItem(Icons.Outlined.AccountCircle, "Meu perfil", Modifier.weight(1f), onOpenSettings)
            }

            VdCard {
                VdSectionLabel("Equipe online", Modifier.padding(bottom = 8.dp))
                DEMO_TEAM.forEachIndexed { i, member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box {
                            VdAvatar(name = member.name, size = 34.dp, fontSize = 12)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(9.dp)
                                    .background(
                                        if (member.online) VdSuccess else VdWarning,
                                        CircleShape
                                    )
                                    .border(2.dp, c.surface, CircleShape)
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                member.name,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = c.textPrimary
                            )
                            Text(member.role, fontSize = 11.sp, color = c.textSecondary)
                        }
                        Text(member.load, fontSize = 11.5.sp, color = c.textSecondary)
                    }
                    if (i < DEMO_TEAM.lastIndex) VdDivider()
                }
            }

            Spacer(Modifier.height(56.dp))
        }
    }
}

@Composable
private fun MaisItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val c = AppTheme.colors
    VdCard(modifier = modifier, padding = 16.dp, onClick = onClick) {
        Icon(icon, null, tint = c.accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(9.dp))
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = c.textPrimary
        )
    }
}
