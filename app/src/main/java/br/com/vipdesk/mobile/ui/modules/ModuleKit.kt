package br.com.vipdesk.mobile.ui.modules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.common.parseApiDate
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdButton
import br.com.vipdesk.mobile.ui.components.VdButtonStyle
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSearchField
import br.com.vipdesk.mobile.ui.components.VdSheetHandle
import br.com.vipdesk.mobile.ui.components.VdSubHeader
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// ————— Helpers de JSON (as respostas dos módulos variam: {data:[]}, [], {total_records, data}) —————

// O backend às vezes devolve texto com entidades HTML (emojis "&#128590;", quebras "&#10;").
fun decodeEntities(s: String): String =
    if (s.contains('&') && Regex("&(#\\d+|#x[0-9a-fA-F]+|[a-z]+);").containsMatchIn(s))
        android.text.Html.fromHtml(s.replace("\n", "<br>"), android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
    else s

fun JsonObject.str(key: String): String? = get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }?.let { decodeEntities(it) }
fun JsonObject.int(key: String): Int? = get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.let { runCatching { it.asInt }.getOrNull() }
fun JsonObject.dbl(key: String): Double? = get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.let { runCatching { it.asDouble }.getOrNull() }
// Gson trata o número 1 como `false` em asBoolean; o backend mistura 0/1, "1" e true.
fun JsonObject.bool(key: String): Boolean? = get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asJsonPrimitive?.let { p ->
    when {
        p.isBoolean -> p.asBoolean
        p.isNumber -> p.asInt != 0
        else -> when (p.asString.lowercase()) { "1", "true", "yes", "sim" -> true; "0", "false", "no", "nao", "não", "" -> false; else -> null }
    }
}
fun JsonObject.obj(key: String): JsonObject? = get(key)?.takeIf { it.isJsonObject }?.asJsonObject
fun JsonObject.arr(key: String): JsonArray? = get(key)?.takeIf { it.isJsonArray }?.asJsonArray
fun JsonArray.objects(): List<JsonObject> = mapNotNull { it.takeIf { e -> e.isJsonObject }?.asJsonObject }

/** Extrai a lista principal e o total de qualquer envelope conhecido. */
fun JsonElement?.rows(vararg keys: String = arrayOf("data", "items", "chats", "results")): Pair<List<JsonObject>, Int?> {
    if (this == null) return emptyList<JsonObject>() to null
    if (isJsonArray) return asJsonArray.objects() to null
    if (!isJsonObject) return emptyList<JsonObject>() to null
    val o = asJsonObject
    val total = o.int("total_records") ?: o.int("total") ?: o.obj("meta")?.int("total")
    for (k in keys) o.arr(k)?.let { return it.objects() to total }
    o.obj("data")?.let { d -> for (k in keys) d.arr(k)?.let { return it.objects() to (d.int("total") ?: total) } }
    return emptyList<JsonObject>() to total
}

fun fmtMoney(v: Double?): String = if (v == null) "—" else runCatching { NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(v) }.getOrDefault("R$ $v")
fun fmtDate(s: String?, pattern: String = "dd/MM/yyyy HH:mm"): String? = s?.let { parseApiDate(it) }?.let { SimpleDateFormat(pattern, Locale("pt", "BR")).format(it) } ?: s
fun fmtRelative(s: String?): String? = s?.let { relativeTime(it) }

// ————— Modelo de linha genérica —————

data class ModuleRow(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val tag: String? = null,
    val tagBg: Color = Tint.grayBg,
    val tagFg: Color = Tint.grayFg,
    val meta: String? = null,
    val avatar: String? = null,
    val leftBorder: Color? = null,
    val details: List<Pair<String, String>> = emptyList(),
    val body: String? = null,
    val raw: JsonObject? = null
)

data class ModulePage(val rows: List<ModuleRow>, val total: Int? = null)

/** Ação disponível no detalhe de uma linha; retorna mensagem para o toast. */
data class ModuleAction(
    val label: String,
    val style: VdButtonStyle = VdButtonStyle.Secondary,
    val run: suspend (ModuleRow) -> Result<String>
)

/** Chamada crua da API relativa a `api/v1/`. */
suspend fun apiGet(path: String, params: Map<String, String> = emptyMap()): Result<JsonElement> = runCatching {
    val r = AppContainer.apiService.getRaw(path, params)
    if (!r.isSuccessful) throw Exception(apiError(r.errorBody()?.string(), "Erro ${r.code()}"))
    r.body() ?: JsonObject()
}

suspend fun apiPost(path: String, body: JsonObject = JsonObject()): Result<JsonElement> = runCatching {
    val r = AppContainer.apiService.postRaw(path, body)
    if (!r.isSuccessful) throw Exception(apiError(r.errorBody()?.string(), "Erro ${r.code()}"))
    r.body() ?: JsonObject()
}

suspend fun apiPut(path: String, body: JsonObject = JsonObject()): Result<JsonElement> = runCatching {
    val r = AppContainer.apiService.putRaw(path, body)
    if (!r.isSuccessful) throw Exception(apiError(r.errorBody()?.string(), "Erro ${r.code()}"))
    r.body() ?: JsonObject()
}

suspend fun apiDelete(path: String): Result<JsonElement> = runCatching {
    val r = AppContainer.apiService.deleteRaw(path)
    if (!r.isSuccessful) throw Exception(apiError(r.errorBody()?.string(), "Erro ${r.code()}"))
    r.body() ?: JsonObject()
}

fun apiError(raw: String?, fallback: String): String = runCatching {
    val o = com.google.gson.JsonParser.parseString(raw ?: return fallback).asJsonObject
    o.obj("errors")?.obj("global")?.str("message")
        ?: o.str("message") ?: o.str("error")
        ?: o.obj("errors")?.entrySet()?.firstOrNull()?.value?.let { v -> if (v.isJsonArray) v.asJsonArray.firstOrNull()?.asString else v.asString }
}.getOrNull() ?: fallback

fun json(vararg pairs: Pair<String, Any?>): JsonObject = JsonObject().apply {
    pairs.forEach { (k, v) ->
        when (v) {
            null -> {}
            is Number -> addProperty(k, v)
            is Boolean -> addProperty(k, v)
            is JsonElement -> add(k, v)
            else -> addProperty(k, v.toString())
        }
    }
}

/** Navegação disponível para as ações dos módulos ("Abrir ticket" etc.). */
val LocalModuleNav = androidx.compose.runtime.compositionLocalOf<ModuleNav?> { null }

/** Resultados especiais das ações: "open-ticket:123" navega em vez de mostrar toast. */
fun handleActionResult(msg: String, nav: ModuleNav?): String? {
    val parts = msg.split(":", limit = 2)
    if (parts.size != 2 || nav == null) return msg
    val id = parts[1].toIntOrNull() ?: return msg
    when (parts[0]) {
        "open-ticket" -> nav.onOpenTicket(id)
        "open-conversation" -> nav.onOpenConversation(id)
        "open-contact" -> nav.onOpenContact(id)
        "open-deal" -> nav.onOpenDeal(id)
        else -> return msg
    }
    return null
}

/** Sheet com um campo de texto e confirmação (justificativas, notas, post-its). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextPromptSheet(
    title: String,
    hint: String,
    confirmLabel: String = "Confirmar",
    allowEmpty: Boolean = false,
    minLines: Int = 3,
    onDismiss: () -> Unit,
    onConfirm: suspend (String) -> Result<String>
) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    val nav = LocalModuleNav.current
    var text by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(done) { if (done != null) { delay(1600); onDismiss() } }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = c.surface, dragHandle = { VdSheetHandle() }) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 28.dp).imePadding()) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.padding(bottom = 8.dp))
            br.com.vipdesk.mobile.ui.components.VdInput(label = null, value = text, onValueChange = { text = it; error = null }, placeholder = hint, minLines = minLines, error = error)
            Spacer(Modifier.height(12.dp))
            when {
                done != null -> Text(done ?: "", fontSize = 13.sp, color = c.success, fontWeight = FontWeight.SemiBold)
                busy -> Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
                else -> VdButton(confirmLabel, enabled = allowEmpty || text.isNotBlank(), onClick = {
                    busy = true
                    scope.launch {
                        onConfirm(text.trim()).fold(
                            { m -> handleActionResult(m, nav)?.let { done = it } ?: onDismiss() },
                            { error = it.message ?: "Falha" }
                        )
                        busy = false
                    }
                }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ————— Tela de lista genérica —————

/**
 * Lista paginada de um módulo: busca (server-side via `load(query)`), filtros em pílulas,
 * detalhe em bottom sheet com campos e ações. Todos os módulos "somente web" do design
 * que têm endpoint passam por aqui.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleListScreen(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    searchHint: String? = null,
    filters: List<String> = emptyList(),
    pageSize: Int = 30,
    emptyTitle: String = "Nada por aqui",
    emptySubtitle: String = "Nenhum registro encontrado.",
    headerActions: @Composable RowScope.() -> Unit = {},
    actions: (ModuleRow) -> List<ModuleAction> = { emptyList() },
    onRowClick: ((ModuleRow) -> Unit)? = null,
    reloadKey: Any? = null,
    load: suspend (query: String, filter: String, skip: Int, take: Int) -> Result<ModulePage>
) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(filters.firstOrNull() ?: "") }
    var rows by remember { mutableStateOf<List<ModuleRow>>(emptyList()) }
    var total by remember { mutableStateOf<Int?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<ModuleRow?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    var version by remember { mutableStateOf(0) }
    LaunchedEffect(toast) { if (toast != null) { delay(2600); toast = null } }

    LaunchedEffect(query, filter, version, reloadKey) {
        loading = true
        if (query.isNotBlank()) delay(350)
        load(query, filter, 0, pageSize).fold(
            onSuccess = { rows = it.rows; total = it.total; error = null },
            onFailure = { error = it.message ?: "Erro ao carregar" }
        )
        loading = false
    }

    val listState = rememberLazyListState()
    val nearEnd by remember { derivedStateOf { (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1) >= listState.layoutInfo.totalItemsCount - 3 } }
    LaunchedEffect(nearEnd, rows.size) {
        val t = total
        if (nearEnd && !loading && !loadingMore && t != null && rows.size < t) {
            loadingMore = true
            scope.launch {
                try {
                    load(query, filter, rows.size, pageSize).onSuccess { page ->
                        val known = rows.map { it.id }.toSet()
                        rows = rows + page.rows.filter { it.id !in known }
                        total = page.total ?: total
                    }
                } finally { loadingMore = false }
            }
        }
    }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize()) {
            VdSubHeader(
                title = title,
                subtitle = subtitle ?: total?.let { "$it registros" },
                onBack = onBack,
                actions = headerActions,
                below = if (searchHint != null || filters.isNotEmpty()) ({
                    if (searchHint != null) { VdSearchField(query, { query = it }, searchHint); Spacer(Modifier.height(8.dp)) }
                    if (filters.isNotEmpty()) VdPillRow(contentPaddingStart = 0.dp) { filters.forEach { f -> VdPill(f, filter == f, { filter = f }) } }
                }) else null
            )
            when {
                loading && rows.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary) }
                error != null && rows.isEmpty() -> VdEmptyState(Icons.Outlined.Inbox, "Não foi possível carregar", error ?: "", ctaLabel = "Tentar novamente", onCta = { version++ })
                rows.isEmpty() -> VdEmptyState(Icons.Outlined.Inbox, emptyTitle, emptySubtitle)
                else -> LazyColumn(
                    state = listState, modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 32.dp)
                ) {
                    items(rows, key = { it.id }) { row ->
                        ModuleRowCard(row) { if (onRowClick != null) onRowClick(row) else selected = row }
                    }
                    if (loadingMore) item("more") { Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) } }
                }
            }
        }
        toast?.let { Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)) { VdToast(it) } }
    }

    selected?.let { row ->
        ModalBottomSheet(
            onDismissRequest = { selected = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface, dragHandle = { VdSheetHandle() }
        ) {
            val nav = LocalModuleNav.current
            ModuleDetailSheet(row, actions(row)) { msg, changed ->
                selected = null
                handleActionResult(msg, nav)?.let { if (it.isNotBlank()) toast = it }
                if (changed) version++
            }
        }
    }
}

@Composable
fun ModuleRowCard(row: ModuleRow, onClick: () -> Unit) {
    val c = AppTheme.colors
    VdCard(onClick = onClick, leftBorder = row.leftBorder) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            row.avatar?.let { VdAvatar(name = it, size = 36.dp, fontSize = 12) }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(row.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    row.meta?.let { Text(it, fontSize = 11.sp, color = c.muted, modifier = Modifier.padding(start = 8.dp)) }
                }
                row.subtitle?.let { Text(it, fontSize = 12.sp, color = c.muted, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp)) }
                row.tag?.let { Box(Modifier.padding(top = 6.dp)) { VdTag(it, color = row.tagFg, background = row.tagBg, pill = true) } }
            }
        }
    }
}

@Composable
fun ModuleDetailSheet(row: ModuleRow, actions: List<ModuleAction>, onDone: (String, Boolean) -> Unit) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 28.dp).imePadding().heightIn(max = 640.dp).verticalScroll(rememberScrollState())) {
        Text(row.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text)
        row.subtitle?.let { Text(it, fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 2.dp)) }
        row.tag?.let { Box(Modifier.padding(top = 6.dp)) { VdTag(it, color = row.tagFg, background = row.tagBg, pill = true) } }
        row.body?.let { Text(it, fontSize = 13.sp, color = c.text, lineHeight = 19.sp, modifier = Modifier.padding(top = 10.dp)) }
        if (row.details.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            row.details.forEach { (k, v) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                    Text(k, fontSize = 12.sp, color = c.muted, modifier = Modifier.weight(0.4f))
                    Text(v, fontSize = 13.sp, color = c.text, modifier = Modifier.weight(0.6f))
                }
                HorizontalDivider(color = c.divider)
            }
        }
        if (actions.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            if (busy) Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
            else actions.forEach { a ->
                VdButton(a.label, onClick = {
                    busy = true
                    scope.launch { a.run(row).fold({ onDone(it, true) }, { onDone(it.message ?: "Falha", false) }); busy = false }
                }, style = a.style, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
            }
        }
    }
}

/** Sheet de escolha (motivos, responsáveis, departamentos…). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerSheet(title: String, options: List<Pair<String, String>>, selectedId: String? = null, onDismiss: () -> Unit, onPick: (String) -> Unit) {
    val c = AppTheme.colors
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = c.surface, dragHandle = { VdSheetHandle() }) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 28.dp).heightIn(max = 560.dp).verticalScroll(rememberScrollState())) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.padding(bottom = 6.dp))
            if (options.isEmpty()) Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
            options.forEach { (id, label) ->
                val sel = id == selectedId
                Text(
                    label, fontSize = 14.sp, color = if (sel) c.primary else c.text, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth().clickable { onPick(id) }.padding(vertical = 13.dp)
                )
                HorizontalDivider(color = c.divider)
            }
        }
    }
}

fun statusTint(status: String?): Pair<Color, Color> = when (status?.lowercase()) {
    "active", "ativo", "ativa", "approved", "aprovado", "done", "concluída", "concluido", "finalizada", "confirmed", "confirmado", "paid", "pago", "won", "ganho", "sent", "enviada", "completed", "success" -> Tint.greenBg to Tint.greenFg
    "pending", "pendente", "scheduled", "agendada", "waiting", "aguardando", "in_progress", "running", "enviando", "draft", "rascunho" -> Tint.yellowBg to Tint.yellowFg
    "rejected", "rejeitado", "canceled", "cancelled", "cancelada", "failed", "falhou", "lost", "perdido", "error", "overdue", "vencido", "inactive", "inativo", "paused", "pausada" -> Tint.redBg to Tint.redFg
    else -> Tint.grayBg to Tint.grayFg
}

