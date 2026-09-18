package br.com.vipdesk.mobile.ui.crm

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.modules.apiGet
import br.com.vipdesk.mobile.ui.modules.arr
import br.com.vipdesk.mobile.ui.modules.dbl
import br.com.vipdesk.mobile.ui.modules.fmtDate
import br.com.vipdesk.mobile.ui.modules.fmtMoney
import br.com.vipdesk.mobile.ui.modules.fmtRelative
import br.com.vipdesk.mobile.ui.modules.int
import br.com.vipdesk.mobile.ui.modules.obj
import br.com.vipdesk.mobile.ui.modules.objects
import br.com.vipdesk.mobile.ui.modules.rows
import br.com.vipdesk.mobile.ui.modules.statusTint
import br.com.vipdesk.mobile.ui.modules.str
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint
import com.google.gson.JsonObject

/** Dados extras do negócio vindos do payload cru (atividades, histórico de etapas, orçamentos). */
class DealExtrasState {
    var raw by mutableStateOf<JsonObject?>(null)
    var quotes by mutableStateOf<List<JsonObject>?>(null)
    var items by mutableStateOf<List<JsonObject>?>(null)

    suspend fun load(dealId: Int) {
        apiGet("deal/$dealId").onSuccess { raw = it.asJsonObject.obj("data") }
        apiGet("quote", mapOf("deal_id" to dealId.toString(), "take" to "20")).onSuccess { body ->
            val qs = body.rows().first.filter { it.int("deal_id") == dealId }
            quotes = qs
            // Produtos do negócio = itens dos orçamentos (os 3 mais recentes)
            items = qs.take(3).flatMap { q ->
                apiGet("quote/${q.int("id")}").getOrNull()?.let { r -> (r.asJsonObject.obj("data") ?: r.asJsonObject).arr("items")?.objects() }.orEmpty()
                    .onEach { it.addProperty("_quote_number", q.str("number") ?: "") }
            }
        }.onFailure { quotes = emptyList(); items = emptyList() }
    }
}

@Composable
fun rememberDealExtras(dealId: Int, version: Int): DealExtrasState {
    val state = remember(dealId) { DealExtrasState() }
    LaunchedEffect(dealId, version) { state.load(dealId) }
    return state
}

private val activityLabels = mapOf("call" to "Ligação", "meeting" to "Reunião", "task" to "Tarefa", "note" to "Nota", "email" to "E-mail")

@Composable
fun DealTimelineExtras(extras: DealExtrasState) {
    val c = AppTheme.colors
    val raw = extras.raw ?: return
    val activities = raw.arr("activities")?.objects().orEmpty()
    val history = raw.arr("stage_history")?.objects().orEmpty()
    data class Ev(val date: String?, val title: String, val sub: String, val tag: String?)
    val events = activities.map { a ->
        Ev(a.str("completed_at") ?: a.str("created_at"), a.str("title") ?: activityLabels[a.str("type")] ?: "Atividade",
            listOfNotNull(a.str("description")?.take(140), a.obj("user")?.str("name")).joinToString(" · "),
            (activityLabels[a.str("type")] ?: a.str("type")) + if (a.str("completed_at") == null && a.str("type") == "task") " · pendente" else "")
    } + history.map { h ->
        Ev(h.str("changed_at") ?: h.str("created_at"), "Mudou para ${h.obj("to_stage")?.str("name") ?: "etapa ${h.int("to_stage_id")}"}",
            listOfNotNull(h.obj("from_stage")?.str("name")?.let { "de $it" }, h.obj("user")?.str("name")).joinToString(" · "), "Etapa")
    }
    events.sortedByDescending { br.com.vipdesk.mobile.ui.common.parseApiDate(it.date)?.time ?: 0L }.forEach { e ->
        VdCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(e.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                Text(fmtRelative(e.date) ?: "", fontSize = 11.sp, color = c.muted)
            }
            if (e.sub.isNotBlank()) Text(e.sub, fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 2.dp))
            e.tag?.let { Box(Modifier.padding(top = 6.dp)) { VdTag(it, color = Tint.indigoFg, background = Tint.indigoBg, pill = true) } }
        }
    }
}

@Composable
fun DealProductsTab(extras: DealExtrasState) {
    val c = AppTheme.colors
    val items = extras.items
    when {
        items == null -> Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
        items.isEmpty() -> Text("Nenhum produto: os itens aparecem quando o negócio tem orçamento.", fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(8.dp))
        else -> {
            VdCard {
                Row { Text("Total dos itens", fontSize = 12.sp, color = c.muted, modifier = Modifier.weight(1f)); Text(fmtMoney(items.sumOf { it.dbl("total") ?: 0.0 }), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text) }
            }
            items.forEach { it ->
                VdCard {
                    Text(it.obj("product")?.str("name") ?: it.str("description") ?: "Item", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                    if (it.obj("product") != null) it.str("description")?.let { d -> Text(d, fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 2.dp)) }
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${(it.dbl("quantity") ?: 0.0).let { q -> if (q % 1.0 == 0.0) q.toInt().toString() else q.toString() }} × ${fmtMoney(it.dbl("unit_price"))}" + (if (it.str("billing_cycle") == "monthly") " /mês" else ""), fontSize = 12.sp, color = c.muted)
                        Text(fmtMoney(it.dbl("total")), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text)
                    }
                    Text("Orçamento ${it.str("_quote_number")}", fontSize = 10.sp, color = c.placeholder, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

@Composable
fun DealDocsTab(extras: DealExtrasState) {
    val c = AppTheme.colors
    val context = LocalContext.current
    val quotes = extras.quotes
    val links = extras.raw?.arr("payment_links")?.objects().orEmpty()
    when {
        quotes == null -> Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
        quotes.isEmpty() && links.isEmpty() -> Text("Nenhum orçamento ou cobrança neste negócio.", fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(8.dp))
        else -> {
            quotes.forEach { q ->
                val st = q.str("status"); val (bg, fg) = statusTint(st)
                val label = when (st) { "draft" -> "Rascunho"; "sent" -> "Enviado"; "accepted" -> "Aceito"; "rejected" -> "Rejeitado"; else -> st ?: "—" }
                VdCard(onClick = q.str("pdf_url")?.let { url -> { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }; Unit } }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Orçamento ${q.str("number") ?: q.int("id")}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                        VdTag(label, color = fg, background = bg, pill = true)
                    }
                    Text(listOfNotNull(fmtMoney(q.dbl("total")), q.str("valid_until")?.let { "válido até ${fmtDate(it, "dd/MM/yyyy")}" }, if (q.str("pdf_url") != null) "toque para abrir o PDF" else null).joinToString(" · "), fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 4.dp))
                }
            }
            links.forEach { l ->
                val (bg, fg) = statusTint(l.str("status"))
                VdCard(onClick = (l.str("url") ?: l.str("payment_url"))?.let { url -> { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }; Unit } }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(l.str("description") ?: "Link de pagamento", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                        l.str("status")?.let { st -> VdTag(when (st) { "paid" -> "Pago"; "pending", "waiting" -> "Pendente"; "canceled", "cancelled" -> "Cancelado"; "expired" -> "Expirado"; else -> st }, color = fg, background = bg, pill = true) }
                    }
                    Text(fmtMoney(l.dbl("amount") ?: l.dbl("value")), fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
