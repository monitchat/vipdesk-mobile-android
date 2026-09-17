package br.com.vipdesk.mobile.ui.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.ApiContact
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdHeaderIcon
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSearchField
import br.com.vipdesk.mobile.ui.components.VdSubHeader
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint
import kotlinx.coroutines.delay

/** Lista de contatos (tela 21) — GET contact real, seções alfabéticas. */
@Composable
fun ContactsListScreen(
    onBack: () -> Unit,
    onContactClick: (Int) -> Unit,
    onCreateContact: () -> Unit,
    onToast: (String) -> Unit
) {
    val c = AppTheme.colors
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Todos") }
    var contacts by remember { mutableStateOf<List<ApiContact>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(query, CrmEvents.contactsVersion) {
        loading = true
        if (query.isNotBlank()) delay(400)
        AppContainer.crmRepository.searchContacts(query, take = 100).fold(
            onSuccess = { contacts = it; error = null },
            onFailure = { error = it.message }
        )
        loading = false
    }

    val visible = when (filter) {
        "Clientes" -> contacts.filter { it.client != null }
        "Leads" -> contacts.filter { it.client == null }
        else -> contacts
    }.sortedBy { it.name.lowercase() }

    Column(Modifier.fillMaxSize().background(c.surface)) {
        VdSubHeader(
            title = "Contatos",
            subtitle = "Relacionamento · ${contacts.size} contatos",
            onBack = onBack,
            actions = {
                VdHeaderIcon(Icons.Outlined.UploadFile, "Importar", { onToast("Importação disponível na versão web") })
                VdHeaderIcon(Icons.Outlined.PersonAddAlt, "Novo contato", onCreateContact, tint = c.primary)
            },
            below = {
                VdSearchField(query, { query = it }, "Nome, telefone, e-mail, CPF/CNPJ")
                Spacer(Modifier.height(8.dp))
                VdPillRow(contentPaddingStart = 0.dp) {
                    listOf("Todos", "Clientes", "Leads").forEach { f -> VdPill(f, filter == f, { filter = f }) }
                }
            }
        )

        when {
            loading && contacts.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
            error != null -> VdEmptyState(Icons.Outlined.PersonSearch, "Não foi possível carregar", error ?: "")
            visible.isEmpty() -> VdEmptyState(Icons.Outlined.PersonSearch, "Nenhum contato encontrado", "Tente outro termo ou cadastre um novo.", ctaLabel = "Novo contato", onCta = onCreateContact)
            else -> LazyColumn(Modifier.fillMaxSize()) {
                val grouped = visible.groupBy { it.name.firstOrNull()?.uppercaseChar()?.takeIf { ch -> ch.isLetter() } ?: '#' }
                grouped.toSortedMap().forEach { (letter, list) ->
                    item(key = "h$letter") {
                        Text(
                            "$letter", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = c.muted,
                            modifier = Modifier.fillMaxWidth().background(c.surfaceSoft).padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    list.forEach { contact ->
                        item(key = contact.id) { ContactRow(contact) { onContactClick(contact.id) } }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: ApiContact, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).defaultMinSize(minHeight = 60.dp).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        VdAvatar(name = contact.name, size = 40.dp, fontSize = 12)
        Column(Modifier.weight(1f)) {
            Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(contact.client?.name, contact.phoneNumber ?: contact.email).joinToString(" · ").ifBlank { "sem telefone" },
                fontSize = 11.sp, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
        if (contact.client != null) VdTag("Cliente", color = Tint.greenFg, background = Tint.greenBg, pill = true)
        else VdTag("Lead", color = Tint.indigoFg, background = Tint.indigoBg, pill = true)
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = c.placeholder, modifier = Modifier.size(18.dp))
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(c.surfaceAlt))
}
