package br.com.vipdesk.mobile.ui.ticket

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.TicketListItem
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdHeaderIcon
import br.com.vipdesk.mobile.ui.components.VdIconButton
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSearchField
import br.com.vipdesk.mobile.ui.components.VdSubHeader
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.components.ticketStatusTint
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Tint
import kotlinx.coroutines.delay

/** Lista de tickets (tela 14): DataTable → cards. */
@Composable
fun TicketsScreen(
    onTicketClick: (Int) -> Unit,
    onBack: (() -> Unit)? = null,
    onCreateTicket: () -> Unit = {},
    viewModel: TicketsViewModel = viewModel(factory = TicketsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors

    LaunchedEffect(state.query) { delay(400); viewModel.search() }

    Column(Modifier.fillMaxSize().background(c.background)) {
        VdSubHeader(
            title = "Tickets",
            subtitle = "Helpdesk › Tickets · ${state.total ?: state.items.size} resultados",
            onBack = onBack ?: {},
            actions = {
                VdHeaderIcon(Icons.Outlined.AddCircleOutline, "Novo ticket", onCreateTicket, tint = c.primary)
            },
            below = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    VdSearchField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = "Nº, assunto, contato…",
                        modifier = Modifier.weight(1f)
                    )
                    VdIconButton(Icons.Outlined.FilterAlt, onClick = {}, active = true)
                }
                Spacer(Modifier.height(8.dp))
                VdPillRow(contentPaddingStart = 0.dp, modifier = Modifier.padding(end = 0.dp)) {
                    VdPill("Abertos", state.status == "open", { viewModel.setStatus("open") }, soft = true)
                    VdPill("Resolvidos", state.status == "closed", { viewModel.setStatus("closed") }, soft = true)
                    VdPill("Todos", state.status == "all", { viewModel.setStatus("all") }, soft = true)
                }
            }
        )

        when {
            state.isLoading && state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = c.primary)
            }
            state.items.isEmpty() -> VdEmptyState(
                icon = Icons.Outlined.ConfirmationNumber,
                title = "Nenhum ticket aqui",
                subtitle = "Tente outro filtro ou termo de busca.",
                ctaLabel = "Criar ticket",
                onCta = onCreateTicket
            )
            else -> {
                val listState = androidx.compose.foundation.lazy.rememberLazyListState()
                // Pede a próxima página quando o último item fica visível
                val nearEnd by remember {
                    derivedStateOf {
                        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                        last >= listState.layoutInfo.totalItemsCount - 3
                    }
                }
                LaunchedEffect(nearEnd, state.items.size) { if (nearEnd && state.hasMore) viewModel.loadMore() }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 24.dp)
                ) {
                    items(state.items, key = { it.id }) { t -> TicketCard(t) { onTicketClick(t.id) } }
                    if (state.isLoadingMore) item("loading-more") {
                        Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(t: TicketListItem, onClick: () -> Unit) {
    val c = AppTheme.colors
    val (stLabel, _) = ticketStatusVisual(t.status, t.isOpen)
    val (stBg, stFg) = ticketStatusTint(stLabel)
    val (priLabel, priColor) = ticketPriorityVisual(t.priority)
    val closed = !t.isOpen

    VdCard(onClick = onClick, padding = 10.dp, leftBorder = if (closed) c.border else priColor) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                buildString { append(t.ticketNumber?.let { "#$it" } ?: "#${t.id}"); append(" · ") },
                fontSize = 11.sp, color = c.muted
            )
            Text(priLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (closed) c.muted else priColor)
            Spacer(Modifier.weight(1f))
            (t.updatedAt ?: t.createdAt)?.let {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Outlined.Timer, null, tint = c.muted, modifier = Modifier.size(12.dp))
                    Text(relativeTime(it), fontSize = 11.sp, color = c.muted)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            t.title ?: "Sem título",
            fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = c.text,
            maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp
        )
        Text(t.contactName, fontSize = 12.sp, color = c.textTertiary, modifier = Modifier.padding(top = 2.dp), maxLines = 1)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            VdTag(stLabel, color = stFg, background = stBg)
            Spacer(Modifier.weight(1f))
            if (t.ownerName != null) {
                Text(t.ownerName.split(" ").first(), fontSize = 10.sp, color = c.muted)
                VdAvatar(name = t.ownerName, size = 18.dp, fontSize = 8, agent = true)
            } else {
                Text("sem responsável", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Tint.amberFg)
            }
        }
    }
}
