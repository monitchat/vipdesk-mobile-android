package br.com.vipdesk.mobile.ui.crm

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ViewKanban
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import br.com.vipdesk.mobile.data.model.ApiContact
import br.com.vipdesk.mobile.data.model.ApiDeal
import br.com.vipdesk.mobile.data.model.ContactConversationSummary
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.common.sourceLabel
import br.com.vipdesk.mobile.ui.common.sourceVisual
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdSubHeader
import br.com.vipdesk.mobile.ui.components.VdTabs
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Brand
import br.com.vipdesk.mobile.ui.theme.Tint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val WEB_ONLY = "Disponível na versão web"

/** Contato 360 (tela 20) — contato, conversas e negócios reais. */
@Composable
fun ContactDetailScreen(
    contactId: Int,
    onBack: () -> Unit,
    onOpenConversation: (Int) -> Unit = {},
    onOpenDeal: (Int) -> Unit = {}
) {
    val c = AppTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var contact by remember { mutableStateOf<ApiContact?>(null) }
    var conversations by remember { mutableStateOf<List<ContactConversationSummary>>(emptyList()) }
    var deals by remember { mutableStateOf<List<ApiDeal>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf("Resumo") }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(contactId) {
        AppContainer.crmRepository.getContact(contactId).fold(onSuccess = { contact = it }, onFailure = { error = it.message })
        AppContainer.crmRepository.getContactConversations(contactId).onSuccess { conversations = it }
        AppContainer.crmRepository.listDeals(null, null, contactId = contactId).onSuccess { deals = it }
        loading = false
    }
    LaunchedEffect(toast) { if (toast != null) { delay(2000); toast = null } }

    fun openConversation() {
        scope.launch {
            AppContainer.crmRepository.getContactConversationId(contactId).onSuccess { id ->
                if (id != null) onOpenConversation(id) else toast = "Contato ainda não possui conversa"
            }
        }
    }

    val p = contact
    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize()) {
            VdSubHeader(
                title = "Contatos › ${p?.name ?: ""}",
                breadcrumb = true,
                onBack = onBack,
                below = if (p == null) null else {
                    {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            VdAvatar(name = p.name, size = 60.dp, fontSize = 18, ring = true)
                            Column(Modifier.weight(1f)) {
                                Text(p.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                                Text(listOfNotNull(p.client?.name, p.city).joinToString(" · ").ifBlank { "Sem empresa vinculada" }, fontSize = 12.sp, color = c.muted)
                                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (p.client != null) VdTag("Cliente", color = Tint.greenFg, background = Tint.greenBg, pill = true)
                                    else VdTag("Lead", color = Tint.indigoFg, background = Tint.indigoBg, pill = true)
                                    p.source?.let { VdTag(sourceLabel(it), color = c.primary, background = c.primarySurface, pill = true) }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            QuickAction(Icons.Outlined.Forum, "WhatsApp", Brand.whatsapp, Modifier.weight(1f)) { openConversation() }
                            QuickAction(Icons.Outlined.Forum, "Conversa", c.primary, Modifier.weight(1f)) { openConversation() }
                            QuickAction(Icons.Outlined.Email, "E-mail", Brand.messenger, Modifier.weight(1f)) {
                                if (p.email.isNullOrBlank()) toast = "Contato sem e-mail"
                                else try { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${p.email}"))) } catch (_: Exception) { toast = "Nenhum app de e-mail" }
                            }
                            QuickAction(Icons.Outlined.Phone, "Ligar", c.textTertiary, Modifier.weight(1f)) {
                                if (p.phoneNumber.isNullOrBlank()) toast = "Contato sem telefone"
                                else try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${p.phoneNumber}"))) } catch (_: Exception) {}
                            }
                            QuickAction(Icons.Outlined.CalendarMonth, "Agendar", c.textTertiary, Modifier.weight(1f)) { toast = WEB_ONLY }
                        }
                        Spacer(Modifier.height(6.dp))
                        VdTabs(
                            listOf("Resumo", "Conversas · ${conversations.size}", "Negócios · ${deals.size}", "Docs", "LGPD"),
                            tab, { tab = it }, scrollable = true
                        )
                    }
                }
            )

            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
                p == null -> VdEmptyState(Icons.Outlined.Person, "Contato não encontrado", error ?: "")
                else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when {
                        tab.startsWith("Resumo") -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MiniKpi("${deals.count { it.status == "won" }}", "ganhos", Modifier.weight(1f))
                                MiniKpi("${deals.count { it.status == "open" }}", "abertos", Modifier.weight(1f))
                                MiniKpi("${conversations.size}", "conversas", Modifier.weight(1f))
                                MiniKpi(p.createdAt?.let { relativeTime(it) } ?: "—", "contato há", Modifier.weight(1f))
                            }
                            VdCard(padding = 0.dp) {
                                InfoRow("Telefone", p.phoneNumber ?: "—")
                                InfoRow("E-mail", p.email ?: "—")
                                InfoRow("Empresa", p.client?.name ?: "—", accent = p.client != null)
                                InfoRow("CPF/CNPJ", p.cpf ?: "—")
                                InfoRow("Endereço", listOfNotNull(p.address, p.city).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "—" })
                                InfoRow("Origem", p.source?.let { sourceLabel(it) } ?: "—", last = true)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Em aberto", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                            }
                            val open = deals.filter { it.status == "open" }
                            if (open.isEmpty() && conversations.isEmpty()) Text("Nada em aberto para este contato.", fontSize = 12.sp, color = c.muted)
                            open.take(3).forEach { d -> OpenCard(Icons.Outlined.ViewKanban, "${d.title} · ${formatDealValue(d.value, d.currency)}", "${d.stage?.name ?: ""} · ${d.owner?.name ?: ""}") { onOpenDeal(d.id) } }
                            conversations.take(2).forEach { cv -> OpenCard(sourceVisual(cv.source).icon, "Conversa ${sourceLabel(cv.source)}", cv.preview ?: cv.updatedAt?.let { relativeTime(it) } ?: "") { onOpenConversation(cv.id) } }
                        }
                        tab.startsWith("Conversas") -> {
                            if (conversations.isEmpty()) Text("Nenhuma conversa registrada.", fontSize = 12.sp, color = c.muted)
                            conversations.forEach { cv -> OpenCard(sourceVisual(cv.source).icon, "Conversa ${sourceLabel(cv.source)}", listOfNotNull(cv.preview, cv.updatedAt?.let { relativeTime(it) }).joinToString(" · ")) { onOpenConversation(cv.id) } }
                        }
                        tab.startsWith("Negócios") -> {
                            if (deals.isEmpty()) Text("Nenhum negócio para este contato.", fontSize = 12.sp, color = c.muted)
                            deals.forEach { d ->
                                val (stLabel, stBg, stFg) = dealStatusVisual(d.status)
                                VdCard(onClick = { onOpenDeal(d.id) }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(d.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                                        VdTag(stLabel, color = stFg, background = stBg)
                                    }
                                    Text("${formatDealValue(d.value, d.currency)} · ${d.stage?.name ?: ""} · ${d.owner?.name ?: ""}", fontSize = 11.sp, color = c.muted)
                                }
                            }
                        }
                        else -> Text("Documentos e consentimentos LGPD ficam na versão web.", fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(8.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
        toast?.let { Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp)) { VdToast(it) } }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    val c = AppTheme.colors
    Column(
        modifier.height(42.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, c.border, RoundedCornerShape(8.dp)).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Text(label, fontSize = 10.sp, color = c.text)
    }
}

@Composable
private fun MiniKpi(value: String, label: String, modifier: Modifier) {
    val c = AppTheme.colors
    Column(
        modifier.clip(RoundedCornerShape(8.dp)).background(c.surface).border(1.dp, c.divider, RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text, maxLines = 1)
        Text(label, fontSize = 9.sp, color = c.muted)
    }
}

@Composable
private fun InfoRow(label: String, value: String, accent: Boolean = false, last: Boolean = false) {
    val c = AppTheme.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
        Text(label, fontSize = 12.sp, color = c.muted, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, fontWeight = if (accent) FontWeight.Medium else FontWeight.Normal, color = if (accent) c.primary else c.text)
    }
    if (!last) Box(Modifier.fillMaxWidth().height(1.dp).background(c.surfaceAlt))
}

@Composable
private fun OpenCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val c = AppTheme.colors
    VdCard(onClick = onClick, padding = 10.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = c.primary, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.text, maxLines = 1)
                Text(subtitle, fontSize = 12.sp, color = c.muted, maxLines = 1)
            }
        }
    }
}
