package br.com.vipdesk.mobile.ui.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.TicketListItem
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSearchField
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import kotlinx.coroutines.delay

@Composable
fun TicketsScreen(
    onTicketClick: (Int) -> Unit,
    viewModel: TicketsViewModel = viewModel(factory = TicketsViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors

    val segs = listOf("Todos" to "all", "Abertos" to "open", "Resolvidos" to "closed")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tickets",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.4).sp,
                color = c.textPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        LaunchedEffect(state.query) {
            delay(400)
            viewModel.search()
        }
        VdSearchField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = "Buscar tickets…",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(11.dp))

        VdPillRow {
            segs.forEach { (label, key) ->
                VdPill(
                    label = label,
                    selected = state.status == key,
                    onClick = { viewModel.setStatus(key) }
                )
            }
        }
        Spacer(Modifier.height(11.dp))

        when {
            state.isLoading && state.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.accent)
                }
            }
            state.items.isEmpty() -> {
                VdEmptyState(
                    icon = Icons.Outlined.ConfirmationNumber,
                    title = "Nenhum ticket aqui",
                    subtitle = "Tente outro filtro ou termo de busca."
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 70.dp
                    )
                ) {
                    items(state.items, key = { it.id }) { t ->
                        TicketCard(t) { onTicketClick(t.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketCard(t: TicketListItem, onClick: () -> Unit) {
    val c = AppTheme.colors
    val (stLabel, stColor) = ticketStatusVisual(t.status, t.isOpen)
    val (priLabel, priColor) = ticketPriorityVisual(t.priority)

    VdCard(onClick = onClick, padding = 13.dp) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                t.ticketNumber ?: "#${t.id}",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = c.textSecondary
            )
            VdTag(stLabel, color = stColor, background = stColor.copy(alpha = 0.15f))
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.Flag, null,
                tint = priColor, modifier = Modifier.size(11.dp)
            )
            Text(priLabel, fontSize = 10.5.sp, color = priColor)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            t.title ?: "Sem título",
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            color = c.textPrimary,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            buildString {
                append(t.contactName)
                t.ownerName?.let { append(" · $it") }
                (t.updatedAt ?: t.createdAt)?.let { append(" · ${relativeTime(it)}") }
            },
            fontSize = 11.5.sp,
            color = c.textSecondary,
            maxLines = 1
        )
    }
}
