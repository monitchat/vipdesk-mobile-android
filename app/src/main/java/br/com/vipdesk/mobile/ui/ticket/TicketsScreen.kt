package br.com.vipdesk.mobile.ui.ticket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.data.model.TicketListItem
import br.com.vipdesk.mobile.ui.common.initialsOf
import br.com.vipdesk.mobile.ui.common.relativeTime
import br.com.vipdesk.mobile.ui.components.StatusBadge
import br.com.vipdesk.mobile.ui.components.ticketPriorityVisual
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    onTicketClick: (Int) -> Unit,
    viewModel: TicketsViewModel = viewModel(factory = TicketsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
    ) {
        Text(
            text = "Tickets",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = c.textPrimary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text("Buscar ticket...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChange(""); viewModel.search() }) {
                        Icon(Icons.Default.Close, "Limpar", modifier = Modifier.size(20.dp))
                    }
                }
            },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { viewModel.search() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = c.textPrimary,
                unfocusedTextColor = c.textPrimary,
                cursorColor = VipDeskPurple,
                focusedBorderColor = VipDeskPurple,
                unfocusedBorderColor = c.fieldBorder,
                focusedLeadingIconColor = VipDeskPurple,
                unfocusedLeadingIconColor = c.iconMuted,
                focusedContainerColor = c.surface,
                unfocusedContainerColor = c.surface,
                focusedPlaceholderColor = c.textSecondary,
                unfocusedPlaceholderColor = c.textSecondary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterPill("Abertos", uiState.status == "open", { viewModel.setStatus("open") }, Modifier.weight(1f))
            FilterPill("Resolvidos", uiState.status == "closed", { viewModel.setStatus("closed") }, Modifier.weight(1f))
            FilterPill("Todos", uiState.status == "all", { viewModel.setStatus("all") }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        when {
            uiState.isLoading && uiState.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = VipDeskPurple)
                }
            }
            uiState.items.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        if (uiState.error != null) uiState.error!! else "Nenhum ticket encontrado",
                        color = c.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.items, key = { it.id }) { ticket ->
                        TicketRow(ticket) { onTicketClick(ticket.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = AppTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) VipDeskPurple else c.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else c.textSecondary
        )
    }
}

@Composable
private fun TicketRow(t: TicketListItem, onClick: () -> Unit) {
    val c = AppTheme.colors
    val (statusLabel, statusColor) = ticketStatusVisual(t.status, t.isOpen)
    val (_, prioColor) = ticketPriorityVisual(t.priority)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = c.surface,
        shadowElevation = if (c.isDark) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(PurpleGradientLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsOf(t.contactName),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${t.ticketNumber ?: t.id}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = VipDeskPurple
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(prioColor)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = t.title?.takeIf { it.isNotBlank() } ?: t.contactName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = t.contactName,
                    fontSize = 12.sp,
                    color = c.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(label = statusLabel, color = statusColor)
                t.updatedAt?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(relativeTime(it), fontSize = 11.sp, color = c.textSecondary)
                }
            }
        }
    }
}
