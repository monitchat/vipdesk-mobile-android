package br.com.vipdesk.mobile.ui.crm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonetizationOn
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
import br.com.vipdesk.mobile.data.model.ApiDeal
import br.com.vipdesk.mobile.data.model.DealStage
import br.com.vipdesk.mobile.data.model.LossReason
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdOutlineButton
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger
import br.com.vipdesk.mobile.ui.theme.VdInfo
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/** Sinaliza às listas de negócios que algo mudou (criação/fechamento). */
object CrmEvents {
    var dealsVersion by mutableStateOf(0)
}

fun formatDealValue(value: Double?, currency: String?): String {
    if (value == null || value == 0.0) return "—"
    val fmt = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return try {
        fmt.format(value)
    } catch (_: Exception) {
        "${currency ?: "R$"} $value"
    }
}

fun dealStatusVisual(status: String?) = when (status) {
    "won" -> "Ganho" to VdSuccess
    "lost" -> "Perdido" to VdDanger
    else -> "Aberto" to VdInfo
}

private val DEAL_FILTERS = listOf(
    "Abertos" to "open", "Ganhos" to "won", "Perdidos" to "lost", "Todos" to ""
)

@Composable
fun DealsContent(query: String, onToast: (String) -> Unit) {
    val c = AppTheme.colors
    var status by remember { mutableStateOf("open") }
    var deals by remember { mutableStateOf<List<ApiDeal>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableStateOf<ApiDeal?>(null) }

    LaunchedEffect(query, status, CrmEvents.dealsVersion) {
        loading = true
        error = null
        AppContainer.crmRepository.listDeals(query, status).fold(
            onSuccess = {
                deals = it
                loading = false
            },
            onFailure = {
                error = it.message
                loading = false
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        VdPillRow {
            DEAL_FILTERS.forEach { (label, key) ->
                VdPill(label = label, selected = status == key, onClick = { status = key })
            }
        }
        Spacer(Modifier.height(11.dp))

        when {
            loading && deals.isEmpty() -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = c.accent)
            }
            error != null -> VdEmptyState(
                icon = Icons.Outlined.MonetizationOn,
                title = "Não foi possível carregar",
                subtitle = error ?: ""
            )
            deals.isEmpty() -> VdEmptyState(
                icon = Icons.Outlined.MonetizationOn,
                title = "Nenhum negócio aqui",
                subtitle = "Crie um negócio pelo botão + ou mude o filtro."
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 70.dp)
            ) {
                items(deals, key = { it.id }) { deal ->
                    DealCard(deal) { selected = deal }
                }
            }
        }
    }

    selected?.let { deal ->
        DealDetailSheet(
            deal = deal,
            onDismiss = { selected = null },
            onChanged = {
                selected = null
                CrmEvents.dealsVersion++
            },
            onToast = onToast
        )
    }
}

@Composable
private fun DealCard(deal: ApiDeal, onClick: () -> Unit) {
    val c = AppTheme.colors
    val (stLabel, stColor) = dealStatusVisual(deal.status)
    VdCard(onClick = onClick, padding = 13.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                deal.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = c.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                formatDealValue(deal.value, deal.currency),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (deal.status == "won") VdSuccess else c.accent
            )
        }
        Spacer(Modifier.height(7.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VdTag(stLabel, color = stColor, background = stColor.copy(alpha = 0.15f))
            deal.stage?.name?.takeIf { it.isNotBlank() }?.let {
                VdTag(it, color = c.accentTint, background = c.accentSoft)
            }
            Spacer(Modifier.weight(1f))
            Text(
                buildString {
                    deal.contact?.name?.let { append(it) }
                    deal.createdAt?.let {
                        if (isNotEmpty()) append(" · ")
                        append(relativeTime(it))
                    }
                },
                fontSize = 11.5.sp,
                color = c.textSecondary,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DealDetailSheet(
    deal: ApiDeal,
    onDismiss: () -> Unit,
    onChanged: () -> Unit,
    onToast: (String) -> Unit
) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var stages by remember { mutableStateOf<List<DealStage>>(emptyList()) }
    var lossReasons by remember { mutableStateOf<List<LossReason>>(emptyList()) }
    var showLossPicker by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(deal.id) {
        AppContainer.crmRepository.getPipelines().onSuccess { pipelines ->
            val pipeline = pipelines.find { it.id == deal.pipeline?.id }
                ?: pipelines.find { it.isDefault } ?: pipelines.firstOrNull()
            stages = pipeline?.stages?.filter { !it.isWon && !it.isLost } ?: emptyList()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            val (stLabel, stColor) = dealStatusVisual(deal.status)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    deal.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                VdTag(stLabel, color = stColor, background = stColor.copy(alpha = 0.15f))
            }
            Text(
                formatDealValue(deal.value, deal.currency),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = c.accent,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )

            deal.contact?.let { contact ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    VdAvatar(name = contact.name, size = 32.dp, fontSize = 12)
                    Column {
                        Text(
                            contact.name,
                            fontSize = 13.5.sp,
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
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                deal.pipeline?.name?.takeIf { it.isNotBlank() }?.let { VdTag("Funil: $it") }
                deal.owner?.name?.takeIf { it.isNotBlank() }?.let { VdTag("Resp.: ${it.split(" ").first()}") }
            }

            if (deal.status == "open" && stages.isNotEmpty()) {
                VdSectionLabel("Etapa", Modifier.padding(bottom = 6.dp))
                VdPillRow(contentPaddingStart = 0.dp) {
                    stages.forEach { stage ->
                        VdPill(
                            label = stage.name,
                            selected = deal.stage?.id == stage.id,
                            onClick = {
                                if (deal.stage?.id == stage.id || busy) return@VdPill
                                busy = true
                                scope.launch {
                                    AppContainer.crmRepository.moveDealStage(deal.id, stage.id).fold(
                                        onSuccess = {
                                            busy = false
                                            onToast("Movido para \"${stage.name}\"")
                                            onChanged()
                                        },
                                        onFailure = {
                                            busy = false
                                            onToast(it.message ?: "Erro ao mover etapa")
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    VdOutlineButton(
                        label = "Ganho",
                        color = VdSuccess,
                        background = VdSuccess.copy(alpha = 0.12f),
                        enabled = !busy,
                        onClick = {
                            busy = true
                            scope.launch {
                                AppContainer.crmRepository.closeDeal(deal.id, "won").fold(
                                    onSuccess = {
                                        busy = false
                                        onToast("Negócio marcado como ganho 🎉")
                                        onChanged()
                                    },
                                    onFailure = {
                                        busy = false
                                        onToast(it.message ?: "Erro ao fechar negócio")
                                    }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    VdOutlineButton(
                        label = "Perdido",
                        color = VdDanger,
                        enabled = !busy,
                        onClick = {
                            scope.launch {
                                AppContainer.crmRepository.getLossReasons().onSuccess {
                                    lossReasons = it
                                }
                                showLossPicker = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (showLossPicker) {
                Spacer(Modifier.height(14.dp))
                VdSectionLabel("Motivo da perda", Modifier.padding(bottom = 4.dp))
                if (lossReasons.isEmpty()) {
                    Text(
                        "Nenhum motivo de perda cadastrado — configure na versão web.",
                        fontSize = 12.5.sp,
                        color = c.textSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                lossReasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !busy) {
                                busy = true
                                scope.launch {
                                    AppContainer.crmRepository
                                        .closeDeal(deal.id, "lost", reason.id).fold(
                                            onSuccess = {
                                                busy = false
                                                onToast("Negócio marcado como perdido")
                                                onChanged()
                                            },
                                            onFailure = {
                                                busy = false
                                                onToast(it.message ?: "Erro ao fechar negócio")
                                            }
                                        )
                                }
                            }
                            .padding(horizontal = 4.dp, vertical = 11.dp)
                    ) {
                        Text(reason.name, fontSize = 14.sp, color = c.textPrimary)
                    }
                    VdDivider()
                }
            }

            if (busy) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = c.accent, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
