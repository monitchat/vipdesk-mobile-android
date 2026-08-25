package br.com.vipdesk.mobile.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.ApiContact
import br.com.vipdesk.mobile.data.model.CreateDealRequest
import br.com.vipdesk.mobile.data.model.CreateTicketRequest
import br.com.vipdesk.mobile.data.model.DealPipeline
import br.com.vipdesk.mobile.data.model.Department
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdOutlineButton
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSearchField
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.kanban.KanbanStore
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Fluxo de criação aberto a partir do FAB "Criar novo". */
enum class CreateFlow { CONVERSA, CONTATO, NEGOCIO, TICKET, TAREFA }

/**
 * Hospeda os formulários de criação. Conversa/Contato/Ticket usam a API
 * real; Tarefa cria no quadro demo (KanbanStore) até existir endpoint.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFlowSheets(
    flow: CreateFlow?,
    onDismiss: () -> Unit,
    onOpenConversation: (Int) -> Unit,
    onOpenTicket: (Int) -> Unit,
    onOpenKanban: () -> Unit,
    onOpenCrm: () -> Unit = {},
    onToast: (String) -> Unit
) {
    val c = AppTheme.colors
    if (flow == null) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface
    ) {
        when (flow) {
            CreateFlow.CONVERSA -> NovaConversaContent(
                onDismiss = onDismiss,
                onOpenConversation = onOpenConversation,
                onToast = onToast
            )
            CreateFlow.CONTATO -> NovoContatoContent(
                onDismiss = onDismiss,
                onToast = onToast
            )
            CreateFlow.NEGOCIO -> NovoDealContent(
                onDismiss = onDismiss,
                onOpenCrm = onOpenCrm,
                onToast = onToast
            )
            CreateFlow.TICKET -> NovoTicketContent(
                onDismiss = onDismiss,
                onOpenTicket = onOpenTicket,
                onToast = onToast
            )
            CreateFlow.TAREFA -> NovaTarefaContent(
                onDismiss = onDismiss,
                onOpenKanban = onOpenKanban,
                onToast = onToast
            )
        }
    }
}

// ————— Nova conversa: escolher contato real e abrir a conversa dele —————

@Composable
private fun NovaConversaContent(
    onDismiss: () -> Unit,
    onOpenConversation: (Int) -> Unit,
    onToast: (String) -> Unit
) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    Column(Modifier.padding(horizontal = 16.dp)) {
        SheetTitle("Nova conversa", "Escolha o contato para abrir a conversa")
        ContactPicker(enabled = !busy) { contact ->
            busy = true
            scope.launch {
                AppContainer.crmRepository.getContactConversationId(contact.id).fold(
                    onSuccess = { conversationId ->
                        busy = false
                        if (conversationId != null) {
                            onDismiss()
                            onOpenConversation(conversationId)
                        } else {
                            onToast("${contact.name} ainda não possui conversa")
                        }
                    },
                    onFailure = {
                        busy = false
                        onToast(it.message ?: "Erro ao abrir conversa")
                    }
                )
            }
        }
        if (busy) {
            Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.accent, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

// ————— Novo contato: POST contact —————

@Composable
private fun NovoContatoContent(
    onDismiss: () -> Unit,
    onToast: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.padding(horizontal = 16.dp)) {
        SheetTitle("Novo contato", "Adicionar pessoa ao CRM")
        FormField("Nome", name, { name = it }, "Nome completo")
        Spacer(Modifier.height(10.dp))
        FormField("Telefone", phone, { phone = it }, "(11) 99999-9999")
        Spacer(Modifier.height(10.dp))
        FormField("E-mail", email, { email = it }, "email@empresa.com.br")

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, fontSize = 12.5.sp, color = VdDanger)
        }

        Spacer(Modifier.height(16.dp))
        VdOutlineButton(
            label = if (busy) "Criando…" else "Criar contato",
            enabled = !busy,
            onClick = {
                error = null
                when {
                    name.isBlank() -> error = "Informe o nome do contato"
                    phone.isBlank() && email.isBlank() ->
                        error = "Informe telefone ou e-mail"
                    else -> {
                        busy = true
                        scope.launch {
                            AppContainer.crmRepository.createContact(name.trim(), phone, email).fold(
                                onSuccess = {
                                    busy = false
                                    onDismiss()
                                    onToast("Contato criado no CRM")
                                },
                                onFailure = {
                                    busy = false
                                    error = it.message ?: "Erro ao criar contato"
                                }
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(28.dp))
    }
}

// ————— Novo negócio: POST deal (pipeline/etapa reais do CRM) —————

@Composable
private fun NovoDealContent(
    onDismiss: () -> Unit,
    onOpenCrm: () -> Unit,
    onToast: (String) -> Unit
) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var contact by remember { mutableStateOf<ApiContact?>(null) }
    var skipContact by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var valueText by remember { mutableStateOf("") }
    var pipelines by remember { mutableStateOf<List<DealPipeline>>(emptyList()) }
    var pipelineId by remember { mutableStateOf<Int?>(null) }
    var stageId by remember { mutableStateOf<Int?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        AppContainer.crmRepository.getPipelines().fold(
            onSuccess = { list ->
                pipelines = list
                val default = list.find { it.isDefault } ?: list.firstOrNull()
                pipelineId = default?.id
            },
            onFailure = { error = it.message }
        )
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        SheetTitle("Novo negócio", "Criar oportunidade no funil de vendas")

        val chosen = contact
        if (chosen == null && !skipContact) {
            VdSectionLabel("Contato", Modifier.padding(bottom = 6.dp))
            ContactPicker(enabled = true) { contact = it }
            Spacer(Modifier.height(6.dp))
            Text(
                "Continuar sem contato",
                fontSize = 13.sp,
                color = c.accent,
                modifier = Modifier
                    .clickable { skipContact = true }
                    .padding(vertical = 6.dp)
            )
        } else {
            if (chosen != null) {
                VdSectionLabel("Contato", Modifier.padding(bottom = 6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .border(1.dp, c.accentBorder, RoundedCornerShape(11.dp))
                        .clickable {
                            contact = null
                            skipContact = false
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VdAvatar(name = chosen.name, size = 32.dp, fontSize = 12)
                    Column(Modifier.weight(1f)) {
                        Text(chosen.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
                        Text(
                            chosen.phoneNumber ?: chosen.email ?: "",
                            fontSize = 11.5.sp,
                            color = c.textSecondary
                        )
                    }
                    Text("trocar", fontSize = 12.sp, color = c.accent)
                }
                Spacer(Modifier.height(12.dp))
            }

            FormField("Título", title, { title = it }, "Ex.: Plano Pro anual — Padaria Estrela")
            Spacer(Modifier.height(10.dp))
            FormField("Valor (R$)", valueText, { valueText = it }, "Ex.: 4890,00")

            if (pipelines.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                VdSectionLabel("Funil", Modifier.padding(bottom = 6.dp))
                VdPillRow(contentPaddingStart = 0.dp) {
                    pipelines.forEach { pipeline ->
                        VdPill(
                            label = pipeline.name,
                            selected = pipelineId == pipeline.id,
                            onClick = {
                                pipelineId = pipeline.id
                                stageId = null
                            }
                        )
                    }
                }
                val stages = pipelines.find { it.id == pipelineId }
                    ?.stages?.filter { !it.isWon && !it.isLost }
                    .orEmpty()
                if (stages.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    VdSectionLabel("Etapa", Modifier.padding(bottom = 6.dp))
                    VdPillRow(contentPaddingStart = 0.dp) {
                        stages.forEach { stage ->
                            VdPill(
                                label = stage.name,
                                selected = stageId == stage.id,
                                onClick = { stageId = stage.id }
                            )
                        }
                    }
                }
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, fontSize = 12.5.sp, color = VdDanger)
            }

            Spacer(Modifier.height(16.dp))
            VdOutlineButton(
                label = if (busy) "Criando…" else "Criar negócio",
                enabled = !busy,
                onClick = {
                    error = null
                    if (title.isBlank()) {
                        error = "Informe o título do negócio"
                        return@VdOutlineButton
                    }
                    val value = valueText
                        .replace(".", "")
                        .replace(",", ".")
                        .toDoubleOrNull()
                    if (valueText.isNotBlank() && value == null) {
                        error = "Valor inválido"
                        return@VdOutlineButton
                    }
                    busy = true
                    scope.launch {
                        AppContainer.crmRepository.createDeal(
                            CreateDealRequest(
                                title = title.trim(),
                                value = value,
                                contactId = chosen?.id,
                                pipelineId = pipelineId,
                                stageId = stageId
                            )
                        ).fold(
                            onSuccess = {
                                busy = false
                                br.com.vipdesk.mobile.ui.crm.CrmEvents.dealsVersion++
                                onDismiss()
                                onToast("Negócio criado no funil")
                                onOpenCrm()
                            },
                            onFailure = {
                                busy = false
                                error = it.message ?: "Erro ao criar negócio"
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(28.dp))
    }
}

// ————— Novo ticket: contato real + título + prioridade + departamento —————

private val TICKET_PRIORITIES = listOf(
    "Baixa" to "low", "Normal" to "normal", "Alta" to "high", "Urgente" to "very_high"
)

@Composable
private fun NovoTicketContent(
    onDismiss: () -> Unit,
    onOpenTicket: (Int) -> Unit,
    onToast: (String) -> Unit
) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var contact by remember { mutableStateOf<ApiContact?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("normal") }
    var departments by remember { mutableStateOf<List<Department>>(emptyList()) }
    var departmentId by remember { mutableStateOf<Int?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        AppContainer.crmRepository.getDepartments().onSuccess { departments = it }
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        SheetTitle("Novo ticket", "Abrir chamado de suporte")

        val chosen = contact
        if (chosen == null) {
            VdSectionLabel("Contato", Modifier.padding(bottom = 6.dp))
            ContactPicker(enabled = true) { contact = it }
        } else {
            VdSectionLabel("Contato", Modifier.padding(bottom = 6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .border(1.dp, c.accentBorder, RoundedCornerShape(11.dp))
                    .clickable { contact = null }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                VdAvatar(name = chosen.name, size = 32.dp, fontSize = 12)
                Column(Modifier.weight(1f)) {
                    Text(chosen.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
                    Text(
                        chosen.phoneNumber ?: chosen.email ?: "",
                        fontSize = 11.5.sp,
                        color = c.textSecondary
                    )
                }
                Text("trocar", fontSize = 12.sp, color = c.accent)
            }

            Spacer(Modifier.height(12.dp))
            FormField("Título", title, { title = it }, "Resumo do problema")
            Spacer(Modifier.height(10.dp))
            FormField("Descrição (opcional)", description, { description = it }, "Detalhes do chamado", minLines = 3)

            Spacer(Modifier.height(12.dp))
            VdSectionLabel("Prioridade", Modifier.padding(bottom = 6.dp))
            VdPillRow(contentPaddingStart = 0.dp) {
                TICKET_PRIORITIES.forEach { (label, key) ->
                    VdPill(label = label, selected = priority == key, onClick = { priority = key })
                }
            }

            if (departments.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                VdSectionLabel("Departamento", Modifier.padding(bottom = 6.dp))
                VdPillRow(contentPaddingStart = 0.dp) {
                    departments.forEach { dept ->
                        VdPill(
                            label = dept.name,
                            selected = departmentId == dept.id,
                            onClick = {
                                departmentId = if (departmentId == dept.id) null else dept.id
                            }
                        )
                    }
                }
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, fontSize = 12.5.sp, color = VdDanger)
            }

            Spacer(Modifier.height(16.dp))
            VdOutlineButton(
                label = if (busy) "Criando…" else "Criar ticket",
                enabled = !busy,
                onClick = {
                    error = null
                    if (title.isBlank()) {
                        error = "Informe o título do ticket"
                        return@VdOutlineButton
                    }
                    busy = true
                    scope.launch {
                        AppContainer.crmRepository.createTicket(
                            CreateTicketRequest(
                                contactId = chosen.id,
                                title = title.trim(),
                                description = description.trim().ifBlank { null },
                                priority = priority,
                                departmentId = departmentId
                            )
                        ).fold(
                            onSuccess = { ticket ->
                                busy = false
                                onDismiss()
                                onToast("Ticket criado com sucesso")
                                ticket?.id?.let(onOpenTicket)
                            },
                            onFailure = {
                                busy = false
                                error = it.message ?: "Erro ao criar ticket"
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(28.dp))
    }
}

// ————— Nova tarefa: cria cartão no quadro (local) —————

@Composable
private fun NovaTarefaContent(
    onDismiss: () -> Unit,
    onOpenKanban: () -> Unit,
    onToast: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var column by remember { mutableStateOf(KanbanStore.columns.getOrNull(1)?.name ?: KanbanStore.columns.first().name) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.padding(horizontal = 16.dp)) {
        SheetTitle("Nova tarefa", "Criar cartão no quadro Operações CS")
        FormField("Título", title, { title = it }, "O que precisa ser feito?")
        Spacer(Modifier.height(10.dp))
        FormField("Cliente (opcional)", customer, { customer = it }, "Nome do cliente")
        Spacer(Modifier.height(10.dp))
        FormField("Etiqueta (opcional)", label, { label = it }, "Ex.: Urgente, Comercial")

        Spacer(Modifier.height(12.dp))
        VdSectionLabel("Coluna", Modifier.padding(bottom = 6.dp))
        VdPillRow(contentPaddingStart = 0.dp) {
            KanbanStore.columns.forEach { col ->
                VdPill(label = col.name, selected = column == col.name, onClick = { column = col.name })
            }
        }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, fontSize = 12.5.sp, color = VdDanger)
        }

        Spacer(Modifier.height(16.dp))
        VdOutlineButton(
            label = "Criar tarefa",
            onClick = {
                if (title.isBlank()) {
                    error = "Informe o título da tarefa"
                } else {
                    KanbanStore.addCard(title.trim(), column, customer.trim(), label.trim())
                    onDismiss()
                    onToast("Tarefa criada em \"$column\"")
                    onOpenKanban()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(28.dp))
    }
}

// ————— Peças compartilhadas —————

@Composable
private fun SheetTitle(title: String, subtitle: String) {
    val c = AppTheme.colors
    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
    Text(
        subtitle,
        fontSize = 12.sp,
        color = c.textSecondary,
        modifier = Modifier.padding(top = 1.dp, bottom = 12.dp)
    )
}

/** Busca de contatos reais (GET contact?search=) com debounce. */
@Composable
private fun ContactPicker(enabled: Boolean, onPick: (ApiContact) -> Unit) {
    val c = AppTheme.colors
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<ApiContact>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(query) {
        loading = true
        delay(if (query.isBlank()) 0 else 400)
        AppContainer.crmRepository.searchContacts(query).fold(
            onSuccess = {
                results = it
                loading = false
            },
            onFailure = { loading = false }
        )
    }

    VdSearchField(
        value = query,
        onValueChange = { query = it },
        placeholder = "Buscar contato…"
    )
    Spacer(Modifier.height(8.dp))
    when {
        loading -> Box(
            Modifier.fillMaxWidth().padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = c.accent, modifier = Modifier.size(20.dp))
        }
        results.isEmpty() -> Row(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.PersonSearch, null,
                tint = c.textFaint, modifier = Modifier.size(18.dp)
            )
            Text("Nenhum contato encontrado", fontSize = 13.sp, color = c.textSecondary)
        }
        else -> LazyColumn(Modifier.heightIn(max = 300.dp)) {
            items(results, key = { it.id }) { contact ->
                Column(
                    Modifier.clickable(enabled = enabled) { onPick(contact) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VdAvatar(name = contact.name, size = 36.dp, fontSize = 13)
                        Column(Modifier.weight(1f)) {
                            Text(
                                contact.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = c.textPrimary
                            )
                            Text(
                                contact.phoneNumber ?: contact.email ?: "",
                                fontSize = 11.5.sp,
                                color = c.textSecondary
                            )
                        }
                    }
                    VdDivider()
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    val c = AppTheme.colors
    Column {
        VdSectionLabel(label)
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(c.background)
                .border(1.dp, c.divider, RoundedCornerShape(10.dp))
                .padding(horizontal = 13.dp, vertical = 11.dp)
        ) {
            if (value.isEmpty()) {
                Text(placeholder, fontSize = 14.sp, color = c.textFaint)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                minLines = minLines,
                textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
