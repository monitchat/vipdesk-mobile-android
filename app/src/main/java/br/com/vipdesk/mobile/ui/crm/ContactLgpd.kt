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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.ui.components.VdButton
import br.com.vipdesk.mobile.ui.components.VdButtonStyle
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.modules.apiGet
import br.com.vipdesk.mobile.ui.modules.apiPost
import br.com.vipdesk.mobile.ui.modules.arr
import br.com.vipdesk.mobile.ui.modules.fmtDate
import br.com.vipdesk.mobile.ui.modules.int
import br.com.vipdesk.mobile.ui.modules.json
import br.com.vipdesk.mobile.ui.modules.obj
import br.com.vipdesk.mobile.ui.modules.objects
import br.com.vipdesk.mobile.ui.modules.str
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

private val PURPOSE_LABELS = mapOf(
    "marketing" to "Marketing", "whatsapp_optin" to "Opt-in WhatsApp", "transactional" to "Mensagens transacionais",
    "data_sharing" to "Compartilhamento de dados", "profiling" to "Perfilamento"
)
private val DOC_LABELS = mapOf("rg" to "RG", "cpf" to "CPF", "cnh" to "CNH", "proof_of_address" to "Comprovante de endereço", "contract" to "Contrato")

/** Aba Docs do Contato 360: documentos (GET contact/{id}/documents) com URL assinada. */
@Composable
fun ContactDocsTab(contactId: Int, onToast: (String) -> Unit) {
    val c = AppTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var docs by remember { mutableStateOf<List<JsonObject>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(contactId) {
        apiGet("contact/$contactId/documents").fold(
            { docs = it.asJsonObject.arr("data")?.objects().orEmpty() }, { error = it.message; docs = emptyList() }
        )
    }
    when {
        docs == null -> Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
        error != null -> Text(error ?: "", fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(8.dp))
        docs!!.isEmpty() -> Text("Nenhum documento anexado a este contato.", fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(8.dp))
        else -> docs!!.forEach { d ->
            VdCard(onClick = {
                scope.launch {
                    apiGet("contact/$contactId/documents/${d.int("id")}/url").fold(
                        { r -> r.asJsonObject.obj("data")?.str("url")?.let { url -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } } ?: onToast("Link indisponível") },
                        { onToast(it.message ?: "Não foi possível abrir") }
                    )
                }
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(DOC_LABELS[d.str("doc_type")] ?: d.str("doc_type") ?: "Documento", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                    d.str("file_type")?.let { VdTag(it.uppercase(), color = Tint.grayFg, background = Tint.grayBg, pill = true) }
                }
                Text(listOfNotNull(fmtDate(d.str("created_at"), "dd/MM/yyyy"), d.str("legal_basis")?.let { "base: $it" }, d.str("expires_at")?.let { "expira ${fmtDate(it, "dd/MM/yyyy")}" }, "toque para abrir").joinToString(" · "), fontSize = 11.sp, color = c.muted, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

/** Aba LGPD do Contato 360: estado por finalidade + registrar concedido/negado. */
@Composable
fun ContactLgpdTab(contactId: Int, onToast: (String) -> Unit) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<JsonObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var version by remember { mutableStateOf(0) }
    var busy by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(contactId, version) {
        apiGet("contact/$contactId/lgpd/consents").fold({ data = it.asJsonObject.obj("data"); error = null }, { error = it.message })
    }
    when {
        error != null && data == null -> Text(error ?: "", fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(8.dp))
        data == null -> Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
        else -> {
            // `current` é {purpose: estado}; sem registros o PHP serializa como [] (array vazio)
            val current: Map<String?, JsonObject> = data!!.obj("current")?.entrySet()?.mapNotNull { (k, v) -> if (v.isJsonObject) k to v.asJsonObject else null }?.toMap()
                ?: data!!.arr("current")?.objects().orEmpty().associateBy { it.str("purpose") }
            val purposes = data!!.arr("purposes")?.map { it.asString }.orEmpty()
            purposes.forEach { p ->
                val st = current[p]?.str("status")
                val (label, bg, fg) = when (st) { "granted" -> Triple("Concedido", Tint.greenBg, Tint.greenFg); "denied" -> Triple("Negado", Tint.redBg, Tint.redFg); "withdrawn" -> Triple("Revogado", Tint.redBg, Tint.redFg); else -> Triple("Pendente", Tint.grayBg, Tint.grayFg) }
                VdCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(PURPOSE_LABELS[p] ?: p, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
                        VdTag(label, color = fg, background = bg, pill = true)
                    }
                    current[p]?.let { cur -> Text(listOfNotNull(cur.str("legal_basis")?.let { "base: $it" }, cur.str("source")?.let { "origem: $it" }, fmtDate(cur.str("collected_at"), "dd/MM/yyyy")).joinToString(" · "), fontSize = 11.sp, color = c.muted, modifier = Modifier.padding(top = 2.dp)) }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (busy == p) CircularProgressIndicator(color = c.primary, modifier = Modifier.size(20.dp))
                        else {
                            if (st != "granted") VdButton("Registrar concedido", onClick = {
                                busy = p
                                scope.launch { apiPost("contact/$contactId/lgpd/consents", json("purpose" to p, "action" to "granted", "legal_basis" to "consent", "source" to "mobile_app")).fold({ onToast("Consentimento registrado"); version++ }, { onToast(it.message ?: "Falha") }); busy = null }
                            }, style = VdButtonStyle.Secondary, height = 36.dp, modifier = Modifier.weight(1f))
                            if (st == "granted") VdButton("Revogar", onClick = {
                                busy = p
                                scope.launch { apiPost("contact/$contactId/lgpd/consents/$p/withdraw").fold({ onToast("Consentimento revogado"); version++ }, { onToast(it.message ?: "Falha") }); busy = null }
                            }, style = VdButtonStyle.Destructive, height = 36.dp, modifier = Modifier.weight(1f))
                            else if (st != "denied") VdButton("Registrar negado", onClick = {
                                busy = p
                                scope.launch { apiPost("contact/$contactId/lgpd/consents", json("purpose" to p, "action" to "denied", "source" to "mobile_app")).fold({ onToast("Recusa registrada"); version++ }, { onToast(it.message ?: "Falha") }); busy = null }
                            }, style = VdButtonStyle.Ghost, height = 36.dp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            val events = data!!.arr("events")?.objects().orEmpty()
            if (events.isNotEmpty()) {
                Text("HISTÓRICO", fontSize = 10.sp, color = c.muted, modifier = Modifier.padding(top = 8.dp))
                events.take(20).forEach { e ->
                    Text("${fmtDate(e.str("created_at"), "dd/MM/yy HH:mm")} · ${PURPOSE_LABELS[e.str("purpose")] ?: e.str("purpose")} · ${e.str("action")}${e.str("source")?.let { " ($it)" } ?: ""}", fontSize = 11.sp, color = c.textTertiary, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}
