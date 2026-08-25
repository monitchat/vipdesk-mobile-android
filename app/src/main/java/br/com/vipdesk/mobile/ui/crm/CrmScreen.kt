package br.com.vipdesk.mobile.ui.crm

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.demo.DEMO_CONTACTS
import br.com.vipdesk.mobile.data.demo.DemoContact
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdEmptyState
import br.com.vipdesk.mobile.ui.components.VdIconButton
import br.com.vipdesk.mobile.ui.components.VdPill
import br.com.vipdesk.mobile.ui.components.VdPillRow
import br.com.vipdesk.mobile.ui.components.VdSearchField
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdLilac
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import br.com.vipdesk.mobile.ui.theme.VdWarning
import kotlinx.coroutines.delay

/** Cores do selo de estágio (Cliente / Lead novo / Lead qualificado). */
fun leadColor(lead: String): Color = when (lead) {
    "Cliente" -> VdSuccess
    "Lead novo" -> VdLilac
    else -> VdWarning
}

@Composable
fun CrmScreen(onContactClick: (Int) -> Unit) {
    val c = AppTheme.colors
    var query by remember { mutableStateOf("") }
    var segment by remember { mutableStateOf("Contatos") }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(2200)
            toast = null
        }
    }

    val filtered = DEMO_CONTACTS.filter {
        query.isBlank() || "${it.name} ${it.company}".contains(query, ignoreCase = true)
    }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CRM",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.4).sp,
                    color = c.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                VdIconButton(Icons.Outlined.FilterAlt, onClick = {}, size = 36.dp)
            }

            VdSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = if (segment == "Contatos") "Buscar contatos e empresas…"
                else "Buscar negócios…",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(11.dp))

            VdPillRow {
                listOf("Contatos", "Negócios").forEach { seg ->
                    VdPill(label = seg, selected = segment == seg, onClick = { segment = seg })
                }
            }
            Spacer(Modifier.height(11.dp))

            if (segment == "Negócios") {
                DealsContent(query = query, onToast = { toast = it })
            } else if (filtered.isEmpty()) {
                VdEmptyState(
                    icon = Icons.Outlined.PersonSearch,
                    title = "Nenhum contato encontrado",
                    subtitle = "Tente outro termo de busca."
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { contact ->
                        ContactRow(contact) { onContactClick(contact.id) }
                    }
                    item { Spacer(Modifier.height(60.dp)) }
                }
            }
        }

        toast?.let {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
            ) { VdToast(it) }
        }
    }
}

@Composable
private fun ContactRow(contact: DemoContact, onClick: () -> Unit) {
    val c = AppTheme.colors
    val lc = leadColor(contact.lead)
    Column(Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            VdAvatar(name = contact.name, size = 44.dp, fontSize = 15)
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        contact.name,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.textPrimary
                    )
                    VdTag(contact.lead, color = lc, background = lc.copy(alpha = 0.14f))
                }
                Text(
                    contact.company,
                    fontSize = 12.5.sp,
                    color = c.textSecondary,
                    modifier = Modifier.padding(top = 1.dp)
                )
                Text(
                    contact.last,
                    fontSize = 11.sp,
                    color = c.textFaint,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                tint = c.textFaint, modifier = Modifier.size(18.dp)
            )
        }
        VdDivider(Modifier.padding(start = 71.dp))
    }
}
