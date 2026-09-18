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
import kotlinx.coroutines.launch

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
    var total by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val pageSize = 50
    val importScope = androidx.compose.runtime.rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var importing by remember { mutableStateOf(false) }
    // Importação em 2 passos, igual ao web: file/excel/getArrayFromExcel (base64 → tmp_file no Spaces)
    // e depois contact/import com o tmp_file (processado em fila no backend).
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        importing = true
        onToast("Enviando planilha…")
        importScope.launch {
            try {
                val resolver = context.contentResolver
                var name = "contatos.xlsx"
                resolver.query(uri, null, null, null, null)?.use { cur -> if (cur.moveToFirst()) { val i = cur.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME); if (i >= 0) name = cur.getString(i) } }
                val ext = name.substringAfterLast('.', "xlsx").lowercase()
                if (ext !in listOf("xlsx", "xls", "csv")) { onToast("Use uma planilha .xlsx, .xls ou .csv"); return@launch }
                val bytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { resolver.openInputStream(uri)?.use { it.readBytes() } } ?: throw Exception("Não foi possível ler o arquivo")
                val mime = resolver.getType(uri) ?: "application/octet-stream"
                val dataUrl = "data:$mime;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                val up = br.com.vipdesk.mobile.ui.modules.apiPost("file/excel/getArrayFromExcel", br.com.vipdesk.mobile.ui.modules.json("data" to dataUrl, "ext" to ext, "actual_file" to "")).getOrThrow()
                val tmp = up.asJsonObject.get("tmp_file")?.takeIf { !it.isJsonNull }?.asString ?: throw Exception("Upload sem arquivo temporário")
                br.com.vipdesk.mobile.ui.modules.apiPost("contact/import", br.com.vipdesk.mobile.ui.modules.json(
                    "tmp_file" to tmp, "actual_file" to tmp, "excel_import_src" to tmp, "excel_import_name" to name, "ext" to ext
                )).getOrThrow()
                onToast("Importação iniciada — os contatos aparecem em alguns minutos")
            } catch (e: Exception) {
                onToast(e.message ?: "Falha na importação")
            } finally { importing = false }
        }
    }

    LaunchedEffect(query, CrmEvents.contactsVersion) {
        loading = true
        if (query.isNotBlank()) delay(400)
        AppContainer.crmRepository.searchContactsPage(query, take = pageSize).fold(
            onSuccess = { contacts = it.items; total = it.total; error = null },
            onFailure = { error = it.message }
        )
        loading = false
    }
    // Scroll infinito: próxima página quando o fim da lista aparece
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val nearEnd by remember {
        androidx.compose.runtime.derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= listState.layoutInfo.totalItemsCount - 4
        }
    }
    // Carrega em escopo próprio: um LaunchedEffect re-executado cancelaria a página em voo
    // e deixaria `loadingMore` preso em true.
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    LaunchedEffect(nearEnd, contacts.size) {
        if (nearEnd && !loading && !loadingMore && contacts.size < total) {
            loadingMore = true
            scope.launch {
                try {
                    AppContainer.crmRepository.searchContactsPage(query, take = pageSize, skip = contacts.size).onSuccess { page ->
                        val known = contacts.map { it.id }.toSet()
                        contacts = contacts + page.items.filter { it.id !in known }
                        total = page.total
                    }
                } finally { loadingMore = false }
            }
        }
    }

    val visible = when (filter) {
        "Clientes" -> contacts.filter { it.client != null }
        "Leads" -> contacts.filter { it.client == null }
        else -> contacts
    }.sortedBy { it.name.lowercase() }

    Column(Modifier.fillMaxSize().background(c.surface)) {
        VdSubHeader(
            title = "Contatos",
            subtitle = "Relacionamento · $total contatos",
            onBack = onBack,
            actions = {
                VdHeaderIcon(Icons.Outlined.UploadFile, "Importar", { if (!importing) importLauncher.launch("*/*") })
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
            else -> LazyColumn(Modifier.fillMaxSize(), state = listState) {
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
                if (loadingMore) item("loading-more") {
                    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
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
