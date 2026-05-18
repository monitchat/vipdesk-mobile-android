package br.com.vipdesk.mobile.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
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
import br.com.vipdesk.mobile.ui.common.initialsOf
import br.com.vipdesk.mobile.ui.theme.*

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = VipDeskRed) },
            title = { Text("Sair da conta") },
            text = { Text("Deseja realmente encerrar a sua sessão?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout { onLogout() }
                }) { Text("Sair", color = VipDeskRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(PurpleGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initialsOf(uiState.name),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = uiState.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = c.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (uiState.email.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = uiState.email,
                fontSize = 13.sp,
                color = c.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = c.surface,
            shadowElevation = if (c.isDark) 0.dp else 2.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                ProfileItem(Icons.Default.Notifications, "Notificações", "Em breve")
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(start = 64.dp))
                ProfileItem(Icons.Default.Shield, "Privacidade e segurança", "Em breve")
                HorizontalDivider(color = c.divider, modifier = Modifier.padding(start = 64.dp))
                ProfileItem(Icons.Default.Info, "Sobre o VipDesk", "Versão 1.0")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showLogoutDialog = true },
            enabled = !uiState.loggingOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VipDeskRed.copy(alpha = 0.12f),
                contentColor = VipDeskRed
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            if (uiState.loggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = VipDeskRed,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sair da conta", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ProfileItem(icon: ImageVector, title: String, hint: String) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(VipDeskPurple.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = VipDeskPurple, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = c.textPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(text = hint, fontSize = 12.sp, color = c.textSecondary)
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = c.iconMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
