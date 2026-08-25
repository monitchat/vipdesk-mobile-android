package br.com.vipdesk.mobile.ui.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.PersonAddAlt
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.demo.DemoKanbanCard
import br.com.vipdesk.mobile.ui.components.VdAvatar
import br.com.vipdesk.mobile.ui.components.VdOutlineButton
import br.com.vipdesk.mobile.ui.components.VdSheetRow
import br.com.vipdesk.mobile.ui.components.VdTag
import br.com.vipdesk.mobile.ui.components.VdToast
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanScreen(
    onBack: () -> Unit,
    onCardClick: (String) -> Unit
) {
    val c = AppTheme.colors
    val columns = KanbanStore.columns
    val total = columns.sumOf { it.cards.size }
    var toast by remember { mutableStateOf<String?>(null) }
    var menuCard by remember { mutableStateOf<DemoKanbanCard?>(null) }
    var showMove by remember { mutableStateOf(false) }
    var newTaskColumn by remember { mutableStateOf<String?>(null) }
    var newTaskTitle by remember { mutableStateOf("") }

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1900)
            toast = null
        }
    }

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
                Column(Modifier.weight(1f)) {
                    Text(
                        "Operações CS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.textPrimary
                    )
                    Text(
                        "Quadro da equipe · $total tarefas",
                        fontSize = 11.sp,
                        color = c.textSecondary
                    )
                }
                IconButton(onClick = {
                    newTaskTitle = ""
                    newTaskColumn = columns.getOrNull(1)?.name ?: columns.first().name
                }) {
                    Icon(Icons.Default.Add, "Nova tarefa", tint = c.accent)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                columns.forEach { col ->
                    Column(
                        modifier = Modifier
                            .width(300.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(16.dp))
                            .background(c.surface)
                            .border(
                                1.dp,
                                if (c.isDark) Color(0xFF3F424D) else Color(0x12292B31),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, top = 13.dp, bottom = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .background(Color(col.colorHex), RoundedCornerShape(3.dp))
                            )
                            Text(
                                col.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = c.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text("${col.cards.size}", fontSize = 12.sp, color = c.textSecondary)
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                                .padding(start = 10.dp, end = 10.dp, bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            col.cards.forEach { card ->
                                KanbanCardItem(
                                    card = card,
                                    onClick = { onCardClick(card.id) },
                                    onMenu = { menuCard = card }
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(11.dp))
                                    .border(1.dp, c.divider, RoundedCornerShape(11.dp))
                                    .clickable {
                                        newTaskTitle = ""
                                        newTaskColumn = col.name
                                    }
                                    .padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add, null,
                                    tint = c.textSecondary, modifier = Modifier.size(14.dp)
                                )
                                Text("Nova tarefa", fontSize = 12.5.sp, color = c.textSecondary)
                            }
                        }
                    }
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

    // Menu do cartão
    menuCard?.let { card ->
        ModalBottomSheet(
            onDismissRequest = { menuCard = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    card.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                VdSheetRow(Icons.Outlined.SwapHoriz, "Mover para…", iconTint = c.accent, onClick = {
                    showMove = true
                })
                VdSheetRow(Icons.Outlined.PersonAddAlt, "Atribuir responsável", onClick = {
                    menuCard = null
                    toast = "Responsável atualizado"
                })
                VdSheetRow(Icons.Outlined.ContentCopy, "Duplicar cartão", onClick = {
                    menuCard = null
                    toast = "Cartão duplicado"
                })
                VdSheetRow(
                    Icons.Outlined.Archive, "Arquivar",
                    iconTint = VdDanger, textColor = VdDanger,
                    onClick = {
                        KanbanStore.archive(card.id)
                        menuCard = null
                        toast = "Cartão arquivado"
                    }
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    // Nova tarefa na coluna escolhida
    newTaskColumn?.let { targetColumn ->
        ModalBottomSheet(
            onDismissRequest = { newTaskColumn = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = c.surface
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Nova tarefa em \"$targetColumn\"",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(c.background)
                        .border(1.dp, c.divider, RoundedCornerShape(10.dp))
                        .padding(horizontal = 13.dp, vertical = 11.dp)
                ) {
                    if (newTaskTitle.isEmpty()) {
                        Text("O que precisa ser feito?", fontSize = 14.sp, color = c.textFaint)
                    }
                    BasicTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
                        cursorBrush = SolidColor(c.accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(14.dp))
                VdOutlineButton(
                    label = "Criar tarefa",
                    enabled = newTaskTitle.isNotBlank(),
                    onClick = {
                        KanbanStore.addCard(newTaskTitle.trim(), targetColumn, "", null)
                        newTaskColumn = null
                        toast = "Tarefa criada em \"$targetColumn\""
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    // Mover para…
    if (showMove && menuCard != null) {
        val card = menuCard!!
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
                                menuCard = null
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
private fun KanbanCardItem(
    card: DemoKanbanCard,
    onClick: () -> Unit,
    onMenu: () -> Unit
) {
    val c = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(c.background)
            .border(1.dp, c.divider, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                card.title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                color = c.textPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.MoreHoriz, "Menu do cartão",
                tint = c.textSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onMenu)
            )
        }
        if (card.customer.isNotBlank()) {
            Text(
                card.customer,
                fontSize = 11.5.sp,
                color = c.textSecondary,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        if (card.labels.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                card.labels.forEach {
                    VdTag(it, color = c.accentTint, background = c.accentSoft)
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 9.dp)
        ) {
            val dueColor = when {
                card.dueUrgent -> VdDanger
                card.dueDone -> VdSuccess
                else -> c.textSecondary
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Outlined.CalendarToday, null, tint = dueColor, modifier = Modifier.size(12.dp))
                Text(card.due, fontSize = 11.sp, color = dueColor)
            }
            if (card.check.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.CheckBox, null, tint = c.textSecondary, modifier = Modifier.size(12.dp))
                    Text(card.check, fontSize = 11.sp, color = c.textSecondary)
                }
            }
            if (card.comments > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, null, tint = c.textSecondary, modifier = Modifier.size(12.dp))
                    Text("${card.comments}", fontSize = 11.sp, color = c.textSecondary)
                }
            }
            Spacer(Modifier.weight(1f))
            VdAvatar(name = card.assignee, size = 24.dp, fontSize = 9)
        }
    }
}
