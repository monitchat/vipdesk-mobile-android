package br.com.vipdesk.mobile.ui.kanban

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdBar
import br.com.vipdesk.mobile.ui.components.VdCard
import br.com.vipdesk.mobile.ui.components.VdDivider
import br.com.vipdesk.mobile.ui.components.VdOutlineButton
import br.com.vipdesk.mobile.ui.components.VdSectionLabel
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanCardScreen(cardId: String, onBack: () -> Unit) {
    val c = AppTheme.colors
    val column = KanbanStore.columnOf(cardId)
    val card = column?.cards?.find { it.id == cardId }
    var toast by remember { mutableStateOf<String?>(null) }
    var showMove by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1900)
            toast = null
        }
    }

    if (card == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val checklist = KanbanStore.checklist
    val done = checklist.count { it.done }

    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = c.textPrimary)
                }
                Text(
                    "Operações CS · ${column.name}",
                    fontSize = 12.5.sp,
                    color = c.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showMove = true }) {
                    Icon(Icons.Default.MoreHoriz, "Menu", tint = c.textPrimary)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    card.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 25.sp,
                    color = c.textPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    card.labels.forEach {
                        VdTag(it, color = c.accentTint, background = c.accentSoft)
                    }
                }

                VdCard(padding = 0.dp) {
                    Column(Modifier.padding(horizontal = 14.dp)) {
                        FieldRow("Responsável", card.assignee, c.textPrimary)
                        FieldRow("Prazo", card.due, if (card.dueUrgent) VdDanger else c.textPrimary)
                        FieldRow(
                            "Prioridade",
                            if (card.dueUrgent) "Urgente" else "Normal",
                            if (card.dueUrgent) VdDanger else c.textPrimary
                        )
                        FieldRow("Cliente", card.customer.ifBlank { "—" }, c.textPrimary, last = true)
                    }
                }

                // Checklist
                VdCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VdSectionLabel("Checklist")
                        Spacer(Modifier.weight(1f))
                        Text(
                            "$done de ${checklist.size}",
                            fontSize = 12.sp,
                            color = c.textSecondary
                        )
                    }
                    VdBar(
                        fraction = if (checklist.isEmpty()) 0f else done.toFloat() / checklist.size,
                        color = c.accent,
                        modifier = Modifier.fillMaxWidth(),
                        height = 5.dp
                    )
                    Spacer(Modifier.height(10.dp))
                    checklist.forEachIndexed { i, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { KanbanStore.toggleChecklist(i) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                if (item.done) Icons.Default.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                null,
                                tint = if (item.done) c.accent else c.textSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                item.text,
                                fontSize = 13.5.sp,
                                color = if (item.done) c.textSecondary else c.textPrimary,
                                textDecoration = if (item.done) TextDecoration.LineThrough else null
                            )
                        }
                    }
                }

                // Comentários
                VdCard {
                    VdSectionLabel("Comentários", Modifier.padding(bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VdAvatar(name = "Ana Beatriz", size = 30.dp, fontSize = 11)
                        Column(Modifier.weight(1f)) {
                            Row {
                                Text(
                                    "Ana Beatriz",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = c.textPrimary
                                )
                                Text(" · há 40 min", fontSize = 12.sp, color = c.textSecondary)
                            }
                            Text(
                                "Transportadora confirmou o extravio. Podemos reenviar pelo motoboy parceiro hoje ainda.",
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp,
                                color = c.textPrimary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.dp, c.divider, RoundedCornerShape(999.dp))
                            .padding(horizontal = 13.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.weight(1f)) {
                            if (comment.isEmpty()) {
                                Text("Escreva um comentário…", fontSize = 13.sp, color = c.textSecondary)
                            }
                            BasicTextField(
                                value = comment,
                                onValueChange = { comment = it },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = c.textPrimary),
                                cursorBrush = SolidColor(c.accent),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.Send, "Enviar",
                            tint = c.accent,
                            modifier = Modifier
                                .size(17.dp)
                                .clickable {
                                    if (comment.isNotBlank()) {
                                        comment = ""
                                        toast = "Comentário adicionado"
                                    }
                                }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    VdOutlineButton(
                        label = "Mover",
                        icon = Icons.Outlined.SwapHoriz,
                        onClick = { showMove = true },
                        modifier = Modifier.weight(1f)
                    )
                    VdOutlineButton(
                        label = "Concluir",
                        icon = Icons.Default.Check,
                        color = VdSuccess,
                        background = VdSuccess.copy(alpha = 0.12f),
                        onClick = {
                            KanbanStore.move(card.id, "Concluído")
                            onBack()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(40.dp))
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

    if (showMove) {
        ModalBottomSheet(
            onDismissRequest = { showMove = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Mover para…",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                KanbanStore.columns.forEach { col ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                KanbanStore.move(card.id, col.name)
                                showMove = false
                                toast = "Movida para ${col.name}"
                            }
                            .padding(horizontal = 4.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        Box(
                            Modifier
                                .size(9.dp)
                                .background(Color(col.colorHex), RoundedCornerShape(3.dp))
                        )
                        Text(col.name, fontSize = 14.sp, color = c.textPrimary, modifier = Modifier.weight(1f))
                        Text("${col.cards.size} tarefas", fontSize = 12.sp, color = c.textSecondary)
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun FieldRow(label: String, value: String, valueColor: Color, last: Boolean = false) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = c.textSecondary,
            modifier = Modifier.width(110.dp)
        )
        Text(value, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
    if (!last) VdDivider()
}
