package br.com.vipdesk.mobile.ui.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.components.VdToggle
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.ThemeController
import br.com.vipdesk.mobile.ui.theme.VdDanger
import br.com.vipdesk.mobile.ui.theme.VdNeutral
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onBack: (() -> Unit)? = null,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val context = LocalContext.current
    var online by remember { mutableStateOf(true) }
    val prefs = remember {
        mutableStateMapOf(
            "Novas mensagens e conversas atribuídas" to true,
            "Tickets atribuídos a mim" to true,
            "Alertas de SLA" to true,
            "Menções em quadros e notas" to false
        )
    }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1900)
            toast = null
        }
    }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = c.textPrimary)
                    }
                } else {
                    Spacer(Modifier.size(12.dp))
                }
                Text(
                    "Perfil e configurações",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cartão do perfil
                VdCard(padding = 16.dp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        Box {
                            VdAvatar(name = state.name, size = 54.dp, fontSize = 19)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(13.dp)
                                    .background(if (online) VdSuccess else VdNeutral, CircleShape)
                                    .border(2.5.dp, c.surface, CircleShape)
                            )
                        }
                        Column {
                            Text(
                                state.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = c.textPrimary
                            )
                            Text(
                                state.email,
                                fontSize = 12.5.sp,
                                color = c.textSecondary
                            )
                        }
                    }
                }

                // Disponibilidade + tema
                VdCard(padding = 0.dp) {
                    Column(Modifier.padding(horizontal = 14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Verified, null,
                                tint = c.textSecondary, modifier = Modifier.size(19.dp)
                            )
                            Column(Modifier.weight(1f)) {
                                Text("Disponível para atendimento", fontSize = 14.sp, color = c.textPrimary)
                                Text(
                                    if (online) "Recebendo novas conversas"
                                    else "Pausado — não recebe novas conversas",
                                    fontSize = 11.5.sp,
                                    color = c.textSecondary
                                )
                            }
                            VdToggle(checked = online, onToggle = { online = !online })
                        }
                        VdDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            Icon(
                                if (c.isDark) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                null,
                                tint = c.textSecondary, modifier = Modifier.size(19.dp)
                            )
                            Text("Tema", fontSize = 14.sp, color = c.textPrimary, modifier = Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .border(1.dp, c.divider, RoundedCornerShape(9.dp))
                            ) {
                                ThemeSeg("Escuro", ThemeController.dark) {
                                    ThemeController.set(context, true)
                                }
                                ThemeSeg("Claro", !ThemeController.dark) {
                                    ThemeController.set(context, false)
                                }
                            }
                        }
                    }
                }

                // Preferências de notificação
                VdCard(padding = 0.dp) {
                    Column(Modifier.padding(horizontal = 14.dp)) {
                        VdSectionLabel("Notificações", Modifier.padding(top = 12.dp, bottom = 4.dp))
                        listOf(
                            "Novas mensagens e conversas atribuídas",
                            "Tickets atribuídos a mim",
                            "Alertas de SLA",
                            "Menções em quadros e notas"
                        ).forEachIndexed { i, key ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(11.dp)
                            ) {
                                Text(
                                    key,
                                    fontSize = 13.5.sp,
                                    color = c.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                VdToggle(
                                    checked = prefs[key] == true,
                                    onToggle = { prefs[key] = !(prefs[key] ?: false) }
                                )
                            }
                            if (i < 3) VdDivider()
                        }
                    }
                }

                // Outras configurações
                VdCard(padding = 0.dp) {
                    Column(Modifier.padding(horizontal = 14.dp)) {
                        SettingRow(Icons.Outlined.Bolt, "Respostas rápidas", "12 salvas") {
                            toast = "Disponível na versão web"
                        }
                        SettingRow(Icons.Outlined.Draw, "Assinatura de mensagens", "Ativa") {
                            toast = "Disponível na versão web"
                        }
                        SettingRow(Icons.Outlined.Translate, "Idioma", "Português (BR)") {
                            toast = "Disponível na versão web"
                        }
                        SettingRow(Icons.Outlined.Info, "Sobre o VIPdesk", "v2.4.1", last = true) {
                            toast = "Disponível na versão web"
                        }
                    }
                }

                // Sair
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, VdDanger.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable(enabled = !state.loggingOut) {
                            viewModel.logout(onLogout)
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ExitToApp, null,
                        tint = VdDanger, modifier = Modifier.size(17.dp)
                    )
                    Text(
                        if (state.loggingOut) "Saindo…" else "Sair da conta",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = VdDanger
                    )
                }

                Spacer(Modifier.height(40.dp))
            }
        }

        toast?.let {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
            ) { VdToast(it) }
        }
    }
}

@Composable
private fun ThemeSeg(label: String, selected: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    Text(
        label,
        fontSize = 12.5.sp,
        color = if (selected) c.accent else c.textSecondary,
        modifier = Modifier
            .background(if (selected) c.accentSoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    label: String,
    value: String,
    last: Boolean = false,
    onClick: () -> Unit
) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Icon(icon, null, tint = c.textSecondary, modifier = Modifier.size(19.dp))
        Text(label, fontSize = 14.sp, color = c.textPrimary, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, color = c.textSecondary)
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = c.textFaint, modifier = Modifier.size(16.dp)
        )
    }
    if (!last) VdDivider()
}
