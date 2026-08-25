package br.com.vipdesk.mobile.ui.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.demo.DEMO_CONTACTS
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger
import br.com.vipdesk.mobile.ui.theme.VdInfo
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import kotlinx.coroutines.delay

private val CRM_TABS = listOf("Visão geral", "Conversas", "Tickets", "Atividades", "Notas", "Arquivos")

@Composable
fun ContactDetailScreen(contactId: Int, onBack: () -> Unit) {
    val c = AppTheme.colors
    val contact = DEMO_CONTACTS.find { it.id == contactId } ?: DEMO_CONTACTS.first()
    var tab by remember { mutableStateOf(CRM_TABS.first()) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1900)
            toast = null
        }
    }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // Cabeçalho
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = c.textPrimary)
                }
                Text(
                    "Perfil do contato",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { toast = "Edição de contato" }) {
                    Icon(Icons.Outlined.Edit, "Editar", tint = c.textSecondary, modifier = Modifier.size(19.dp))
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Identidade
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VdAvatar(name = contact.name, size = 74.dp, fontSize = 26)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            contact.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = c.textPrimary
                        )
                        Text(contact.company, fontSize = 13.sp, color = c.textSecondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val lc = leadColor(contact.lead)
                        VdTag(contact.lead, color = lc, background = lc.copy(alpha = 0.14f))
                        VdTag("Resp.: ${contact.owner}")
                    }
                }

                // Ações rápidas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)
                ) {
                    QuickAction(Icons.AutoMirrored.Outlined.Chat, "Conversar") { toast = "Abrindo conversa…" }
                    QuickAction(Icons.Outlined.PhoneInTalk, "Ligar") { toast = "Ligando para ${contact.name}…" }
                    QuickAction(Icons.Outlined.Forum, "WhatsApp") { toast = "Abrindo WhatsApp…" }
                    QuickAction(Icons.Outlined.ConfirmationNumber, "Ticket") { toast = "Ticket criado para ${contact.name}" }
                    QuickAction(Icons.Outlined.CheckBox, "Tarefa") { toast = "Tarefa criada" }
                }

                // Abas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CRM_TABS.forEach { t ->
                        val selected = tab == t
                        Column(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .clickable { tab = t }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                t,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selected) c.accent else c.textSecondary
                            )
                            Spacer(Modifier.height(6.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(if (selected) c.accent else Color.Transparent)
                            )
                        }
                    }
                }
                VdDivider()
                Spacer(Modifier.height(14.dp))

                when (tab) {
                    "Visão geral" -> OverviewTab(contact.phone, contact.email, contact.company, contact.origin, contact.owner, contact.plan, contact.tags)
                    "Conversas" -> ConvsTab(contact.origin)
                    "Tickets" -> TicketsTab()
                    "Atividades" -> ActivitiesTab()
                    "Notas" -> NotesTab()
                    "Arquivos" -> FilesTab()
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
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    val c = AppTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .border(1.dp, c.accentBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = c.accent, modifier = Modifier.size(20.dp))
        }
        Text(label, fontSize = 10.5.sp, color = c.textSecondary)
    }
}

@Composable
private fun OverviewTab(
    phone: String, email: String, company: String,
    origin: String, owner: String, plan: String, tags: List<String>
) {
    val c = AppTheme.colors
    VdCard(padding = 0.dp) {
        Column(Modifier.padding(horizontal = 14.dp)) {
            FieldRow(Icons.Outlined.Phone, "Telefone", phone)
            FieldRow(Icons.Outlined.Email, "E-mail", email)
            FieldRow(Icons.Outlined.Business, "Empresa", company)
            FieldRow(Icons.Outlined.Flag, "Origem", origin)
            FieldRow(Icons.Outlined.Person, "Responsável", owner)
            FieldRow(Icons.Outlined.Inventory2, "Plano", plan, last = true)
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.forEach { VdTag(it, color = c.accentTint, background = c.accentSoft) }
    }
}

@Composable
private fun FieldRow(icon: ImageVector, label: String, value: String, last: Boolean = false) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Icon(icon, null, tint = c.textSecondary, modifier = Modifier.size(17.dp))
        Column {
            VdSectionLabel(label)
            Text(value, fontSize = 14.sp, color = c.textPrimary, modifier = Modifier.padding(top = 1.dp))
        }
    }
    if (!last) VdDivider()
}

@Composable
private fun ConvsTab(origin: String) {
    val c = AppTheme.colors
    listOf(
        Triple("Oi! Meu pedido #3412 ainda não chegou, podem verificar?", "09:42", "Aguardando"),
        Triple("Obrigada pelo atendimento, resolvido!", "Ontem", "Resolvida")
    ).forEachIndexed { i, (preview, time, status) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Outlined.Forum, null, tint = c.accent, modifier = Modifier.size(19.dp))
            Column(Modifier.weight(1f)) {
                Text(preview, fontSize = 13.5.sp, color = c.textPrimary, maxLines = 1)
                Text("$time · $status · $origin", fontSize = 11.sp, color = c.textSecondary)
            }
        }
        VdDivider()
    }
}

@Composable
private fun TicketsTab() {
    val c = AppTheme.colors
    listOf(
        Triple("#4821", "Erro na emissão de NF-e", "Aberto" to VdInfo),
        Triple("#4801", "Relatório mensal não abre no app", "Resolvido" to VdSuccess)
    ).forEach { (id, title, st) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(id, fontSize = 11.5.sp, color = c.textSecondary)
            Text(title, fontSize = 13.5.sp, color = c.textPrimary, modifier = Modifier.weight(1f))
            VdTag(st.first, color = st.second, background = st.second.copy(alpha = 0.15f))
        }
        VdDivider()
    }
}

@Composable
private fun ActivitiesTab() {
    val c = AppTheme.colors
    listOf(
        Icons.Outlined.Forum to ("Conversa iniciada no WhatsApp" to "hoje, 09:38"),
        Icons.Outlined.NoteAlt to ("Nota interna adicionada por Você" to "hoje, 09:41"),
        Icons.Outlined.PhoneInTalk to ("Ligação realizada · 4m12s" to "sexta, 14:20"),
        Icons.Outlined.ConfirmationNumber to ("Ticket #4763 resolvido" to "12 ago")
    ).forEach { (icon, item) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(c.chip, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = c.textSecondary, modifier = Modifier.size(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(item.first, fontSize = 13.5.sp, color = c.textPrimary)
                Text(item.second, fontSize = 11.sp, color = c.textSecondary, modifier = Modifier.padding(top = 1.dp))
            }
        }
        VdDivider()
    }
}

@Composable
private fun NotesTab() {
    val c = AppTheme.colors
    listOf(
        "Cliente prefere contato por WhatsApp no período da manhã. Pedidos recorrentes toda sexta." to "Você · hoje, 09:41",
        "Negociou desconto de 10% na renovação anual em julho." to "Ana Beatriz · 14 jul"
    ).forEach { (text, meta) ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(c.noteBg)
                .border(1.dp, c.noteBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(text, fontSize = 13.5.sp, color = c.textPrimary, lineHeight = 19.sp)
            Text(meta, fontSize = 10.5.sp, color = c.textSecondary, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@Composable
private fun FilesTab() {
    val c = AppTheme.colors
    listOf(
        Triple("contrato-2026.pdf", "PDF · 1,2 MB · 02 fev", VdDanger),
        Triple("comprovante-pedido.jpg", "Imagem · 418 KB · hoje", VdInfo)
    ).forEach { (name, meta, tint) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(c.chip, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (tint == VdDanger) Icons.Outlined.Description else Icons.Outlined.AttachFile,
                    null, tint = tint, modifier = Modifier.size(18.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
                Text(meta, fontSize = 11.sp, color = c.textSecondary)
            }
        }
        VdDivider()
    }
}
