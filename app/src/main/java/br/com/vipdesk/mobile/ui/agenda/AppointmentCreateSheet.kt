package br.com.vipdesk.mobile.ui.agenda

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.ui.components.VdButton
import br.com.vipdesk.mobile.ui.components.VdInput
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSheetHandle
import br.com.vipdesk.mobile.ui.modules.apiGet
import br.com.vipdesk.mobile.ui.modules.apiPost
import br.com.vipdesk.mobile.ui.modules.bool
import br.com.vipdesk.mobile.ui.modules.int
import br.com.vipdesk.mobile.ui.modules.json
import br.com.vipdesk.mobile.ui.modules.obj
import br.com.vipdesk.mobile.ui.modules.rows
import br.com.vipdesk.mobile.ui.modules.str
import br.com.vipdesk.mobile.ui.theme.AppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Novo agendamento (tela 22 · FAB "+"): serviço → profissional → dia → horário livre
 * (GET appointments/slots) → POST appointments. Contato opcional (vindo do chat / 360).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppointmentCreateSheet(
    onDismiss: () -> Unit,
    onCreated: (String) -> Unit,
    contactId: Int? = null,
    contactName: String? = null,
    phoneNumber: String? = null
) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var services by remember { mutableStateOf<List<Pair<Int, String>>?>(null) }
    var professionals by remember { mutableStateOf<List<Pair<Int, String>>?>(null) }
    var serviceId by remember { mutableStateOf<Int?>(null) }
    var professionalId by remember { mutableStateOf<Int?>(null) }
    val dayFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val days = remember { (0 until 14).map { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, it) } } }
    var day by remember { mutableStateOf(dayFmt.format(days.first().time)) }
    var slots by remember { mutableStateOf<List<String>?>(null) }
    var time by remember { mutableStateOf<String?>(null) }
    var phone by remember { mutableStateOf(phoneNumber.orEmpty()) }
    var name by remember { mutableStateOf(contactName.orEmpty()) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        apiGet("services", mapOf("take" to "100")).fold(
            { b -> services = b.rows().first.filter { it.bool("is_active") != false }.map { it.int("id")!! to (it.str("name") ?: "Serviço") }; if (services?.size == 1) serviceId = services!!.first().first },
            { error = it.message; services = emptyList() }
        )
        apiGet("professional", mapOf("take" to "100")).fold(
            { b -> professionals = b.rows().first.map { it.int("id")!! to (it.str("name") ?: "Profissional") }; if (professionals?.size == 1) professionalId = professionals!!.first().first },
            { professionals = emptyList() }
        )
    }
    LaunchedEffect(serviceId, professionalId, day) {
        time = null
        val s = serviceId; val p = professionalId
        if (s == null || p == null) { slots = null; return@LaunchedEffect }
        slots = null
        apiGet("appointments/slots", mapOf("professional_id" to p.toString(), "service_id" to s.toString(), "date_from" to day, "date_to" to day)).fold(
            { b -> slots = b.asJsonObject.obj("data")?.getAsJsonArray(day)?.map { it.asString }.orEmpty() },
            { error = it.message; slots = emptyList() }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = c.surface, dragHandle = { VdSheetHandle() }) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 28.dp).imePadding().heightIn(max = 680.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Novo agendamento", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            contactName?.let { Text("Para $it", fontSize = 12.sp, color = c.muted) }

            Label("SERVIÇO")
            if (services == null) Loading() else if (services!!.isEmpty()) Text("Nenhum serviço cadastrado.", fontSize = 12.sp, color = c.muted)
            else FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { services!!.forEach { (id, n) -> VdPill(n, serviceId == id, { serviceId = id }) } }

            Label("PROFISSIONAL")
            if (professionals == null) Loading() else if (professionals!!.isEmpty()) Text("Nenhum profissional cadastrado.", fontSize = 12.sp, color = c.muted)
            else FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { professionals!!.forEach { (id, n) -> VdPill(n, professionalId == id, { professionalId = id }) } }

            Label("DIA")
            VdPillRow(contentPaddingStart = 0.dp) {
                days.forEach { d ->
                    val key = dayFmt.format(d.time)
                    VdPill(SimpleDateFormat("EEE dd/MM", Locale("pt", "BR")).format(d.time).replace(".", ""), day == key, { day = key })
                }
            }

            Label("HORÁRIO")
            when {
                serviceId == null || professionalId == null -> Text("Escolha serviço e profissional.", fontSize = 12.sp, color = c.muted)
                slots == null -> Loading()
                // O backend recusa horário fora da disponibilidade (409 out_of_availability), então não há "horário livre"
                slots!!.isEmpty() -> Text("Sem horários livres neste dia. Verifique a disponibilidade do profissional ou escolha outro dia.", fontSize = 12.sp, color = c.muted)
                else -> FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { slots!!.forEach { t -> VdPill(t, time == t, { time = t }) } }
            }

            if (contactId == null) {
                VdInput(label = "Nome do cliente", value = name, onValueChange = { name = it }, placeholder = "Nome")
                VdInput(label = "Telefone (WhatsApp)", value = phone, onValueChange = { phone = it }, placeholder = "5527999999999")
            }
            VdInput(label = "Observações", value = notes, onValueChange = { notes = it }, placeholder = "Opcional", minLines = 2)
            error?.let { Text(it, fontSize = 12.sp, color = c.danger) }
            Spacer(Modifier.height(4.dp))
            if (saving) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
            else VdButton("Agendar", enabled = serviceId != null && professionalId != null && time != null && (contactId != null || phone.isNotBlank()), onClick = {
                saving = true; error = null
                scope.launch {
                    val body = json(
                        "service_id" to serviceId, "professional_id" to professionalId,
                        "start_date" to "$day $time:00", "status" to "pending",
                        "contact_id" to contactId, "phone_number" to (if (contactId == null) phone.filter { it.isDigit() } else phoneNumber),
                        "contact_name" to name.ifBlank { null }, "notes" to notes.ifBlank { null }, "type" to "service"
                    )
                    apiPost("appointments", body).fold(
                        { onCreated("Agendado para ${day.substring(8, 10)}/${day.substring(5, 7)} às $time"); onDismiss() },
                        { error = it.message ?: "Não foi possível agendar" }
                    )
                    saving = false
                }
            }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.muted, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun Loading() {
    Row(Modifier.fillMaxWidth().padding(6.dp)) { CircularProgressIndicator(color = AppTheme.colors.primary, modifier = Modifier.size(18.dp)) }
}
