package br.com.vipdesk.mobile.ui.kanban

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.vipdesk.mobile.data.demo.DEMO_CHECKLIST
import br.com.vipdesk.mobile.data.demo.DEMO_KANBAN
import br.com.vipdesk.mobile.data.demo.DemoChecklistItem
import br.com.vipdesk.mobile.data.demo.DemoKanbanColumn

/**
 * Estado em memória do quadro de demonstração — compartilhado entre a tela
 * do quadro e o detalhe do cartão. Sem persistência (dados de demo).
 */
object KanbanStore {
    var columns by mutableStateOf(DEMO_KANBAN)
        private set

    var checklist by mutableStateOf(DEMO_CHECKLIST)
        private set

    fun columnOf(cardId: String): DemoKanbanColumn? =
        columns.find { col -> col.cards.any { it.id == cardId } }

    fun move(cardId: String, targetColumn: String) {
        val card = columns.flatMap { it.cards }.find { it.id == cardId } ?: return
        columns = columns.map { col ->
            val without = col.cards.filter { it.id != cardId }
            when {
                col.name == targetColumn -> col.copy(cards = without + card)
                else -> col.copy(cards = without)
            }
        }
    }

    fun archive(cardId: String) {
        columns = columns.map { col ->
            col.copy(cards = col.cards.filter { it.id != cardId })
        }
    }

    /** Cria um cartão na coluna indicada (estado local do quadro demo). */
    fun addCard(title: String, column: String, customer: String, label: String?) {
        val card = br.com.vipdesk.mobile.data.demo.DemoKanbanCard(
            id = "k${System.currentTimeMillis()}",
            title = title,
            customer = customer,
            labels = listOfNotNull(label?.takeIf { it.isNotBlank() }),
            due = "Hoje",
            dueUrgent = false,
            dueDone = false,
            check = "",
            comments = 0,
            assignee = "Você"
        )
        val target = columns.firstOrNull { it.name == column }?.name
            ?: columns.first().name
        columns = columns.map { col ->
            if (col.name == target) col.copy(cards = col.cards + card) else col
        }
    }

    fun toggleChecklist(index: Int) {
        checklist = checklist.mapIndexed { i, item ->
            if (i == index) DemoChecklistItem(item.text, !item.done) else item
        }
    }
}
