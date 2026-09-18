package br.com.vipdesk.mobile.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.ApiContact
import br.com.vipdesk.mobile.data.model.TicketListItem
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.ticketStatusVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import kotlinx.coroutines.delay

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onContactClick: (Int) -> Unit,
    onTicketClick: (Int) -> Unit
) {
    val c = AppTheme.colors
    var query by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    var recent by remember { mutableStateOf(loadRecentSearches(context)) }
    var tickets by remember { mutableStateOf<List<TicketListItem>>(emptyList()) }
    var contacts by remember { mutableStateOf<List<ApiContact>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            tickets = emptyList()
            contacts = emptyList()
            return@LaunchedEffect
        }
        delay(400)
        AppContainer.mobileRepository.getTickets("all", query).onSuccess { tickets = it.items }
        AppContainer.crmRepository.searchContacts(query, take = 4).onSuccess { contacts = it }
        // Guarda o termo depois de uma pausa maior (evita salvar cada letra digitada)
        delay(1500)
        if (query.trim().length >= 3) recent = saveRecentSearch(context, query.trim())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = c.textPrimary)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(c.surface)
                    .border(1.dp, c.accentBorder, RoundedCornerShape(11.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Icon(
                    Icons.Outlined.Search, null,
                    tint = c.textSecondary, modifier = Modifier.size(16.dp)
                )
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("Buscar em tudo…", fontSize = 14.sp, color = c.textSecondary)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
                        cursorBrush = SolidColor(c.accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            when {
                query.isBlank() -> {
                    VdSectionLabel("Buscas recentes", Modifier.padding(vertical = 8.dp))
                    if (recent.isEmpty()) Text("Suas buscas aparecem aqui.", fontSize = 12.sp, color = c.textSecondary)
                    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        recent.take(6).forEach { term ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .border(1.dp, c.divider, RoundedCornerShape(999.dp))
                                    .clickable { query = term }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.History, null,
                                    tint = c.textSecondary, modifier = Modifier.size(13.dp)
                                )
                                Text(term, fontSize = 13.sp, color = c.textSecondary)
                            }
                        }
                    }
                }
                contacts.isEmpty() && tickets.isEmpty() -> {
                    VdEmptyState(
                        icon = Icons.Outlined.Search,
                        title = "Nada encontrado",
                        subtitle = "Nenhum resultado para \"$query\"."
                    )
                }
                else -> {
                    if (contacts.isNotEmpty()) {
                        VdSectionLabel("Contatos", Modifier.padding(top = 10.dp, bottom = 2.dp))
                        contacts.forEach { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onContactClick(contact.id) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                VdAvatar(name = contact.name, size = 36.dp, fontSize = 12)
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        contact.name,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = c.textPrimary
                                    )
                                    Text(
                                        listOfNotNull(contact.client?.name, contact.phoneNumber ?: contact.email).joinToString(" · "),
                                        fontSize = 11.5.sp, color = c.textSecondary
                                    )
                                }
                            }
                            VdDivider()
                        }
                    }
                    if (tickets.isNotEmpty()) {
                        VdSectionLabel("Tickets", Modifier.padding(top = 14.dp, bottom = 2.dp))
                        tickets.take(4).forEach { t ->
                            val (stLabel, stColor) = ticketStatusVisual(t.status, t.isOpen)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTicketClick(t.id) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    t.ticketNumber ?: "#${t.id}",
                                    fontSize = 11.5.sp,
                                    color = c.textSecondary
                                )
                                Text(
                                    t.title ?: "Sem título",
                                    fontSize = 13.5.sp,
                                    color = c.textPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                VdTag(stLabel, color = stColor, background = stColor.copy(alpha = 0.15f))
                            }
                            VdDivider()
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}


private const val RECENT_PREFS = "vipdesk_search"
private const val RECENT_KEY = "recent"

private fun loadRecentSearches(context: android.content.Context): List<String> =
    context.getSharedPreferences(RECENT_PREFS, android.content.Context.MODE_PRIVATE)
        .getString(RECENT_KEY, "").orEmpty().split('\n').filter { it.isNotBlank() }

private fun saveRecentSearch(context: android.content.Context, term: String): List<String> {
    val list = (listOf(term) + loadRecentSearches(context).filterNot { it.equals(term, ignoreCase = true) }).take(8)
    context.getSharedPreferences(RECENT_PREFS, android.content.Context.MODE_PRIVATE).edit().putString(RECENT_KEY, list.joinToString("\n")).apply()
    return list
}
