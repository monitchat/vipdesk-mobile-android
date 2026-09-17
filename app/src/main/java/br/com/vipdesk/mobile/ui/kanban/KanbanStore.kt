package br.com.vipdesk.mobile.ui.kanban

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.vipdesk.mobile.data.demo.DemoChecklistItem
import br.com.vipdesk.mobile.data.demo.DemoKanbanCard
import br.com.vipdesk.mobile.data.demo.DemoKanbanColumn
import br.com.vipdesk.mobile.di.AppContainer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Estado do quadro Kanban ligado à API `kanban/...` (boards, colunas, tarefas, checklist).
 * Mantém o modelo de coluna/cartão que as telas já consomem; as mutações aplicam
 * otimisticamente e chamam o backend (move/archive/create/checklist).
 */
object KanbanStore {
    data class Board(val id: Int, val name: String, val tasksCount: Int)

    var boards by mutableStateOf<List<Board>>(emptyList())
        private set
    var boardId by mutableStateOf<Int?>(null)
        private set
    var boardName by mutableStateOf("Quadro")
        private set
    var columns by mutableStateOf<List<DemoKanbanColumn>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    // Checklist do cartão aberto (carregado sob demanda)
    var checklistTaskId by mutableStateOf<Int?>(null)
        private set
    var checklist by mutableStateOf<List<DemoChecklistItem>>(emptyList())
        private set
    private var checklistIds: List<Int> = emptyList()

    data class TaskComment(val author: String, val body: String, val createdAt: String?)
    var comments by mutableStateOf<List<TaskComment>>(emptyList())
        private set
    /** Prioridade crua por tarefa (low/normal/high/urgent) — o cartão só carrega a etiqueta. */
    private val priorities = mutableMapOf<String, String>()
    fun priorityOf(cardId: String): String? = priorities[cardId]

    private val columnIds = mutableMapOf<String, Int>()
    private val columnColors = listOf(0xFF9397AB, 0xFF8AB0D9, 0xFF7E3E97, 0xFFF59E0B, 0xFF22C55E, 0xFFEF4444)

    private val api get() = AppContainer.apiService

    suspend fun load(preferredBoardId: Int? = boardId) {
        loading = true; error = null
        runCatching {
            val boardsRes = api.getKanbanBoards()
            if (!boardsRes.isSuccessful) throw Exception("Quadros indisponíveis (${boardsRes.code()})")
            val list = boardsRes.body()?.getAsJsonArray("data")?.map { it.asJsonObject }
                ?.filter { it.get("is_archived")?.asBoolean != true }.orEmpty()
            boards = list.map { Board(it.get("id").asInt, it.str("name") ?: "Quadro", it.get("tasks_count")?.takeIf { v -> !v.isJsonNull }?.asInt ?: 0) }
            val chosen = boards.firstOrNull { it.id == preferredBoardId } ?: boards.firstOrNull()
            if (chosen == null) { columns = emptyList(); boardId = null; boardName = "Sem quadros"; return@runCatching }
            boardId = chosen.id; boardName = chosen.name
            loadBoard(chosen.id)
        }.onFailure { error = it.message ?: "Erro ao carregar o quadro" }
        loading = false
    }

    suspend fun selectBoard(id: Int) { load(id) }

    private suspend fun loadBoard(id: Int) {
        val boardRes = api.getKanbanBoard(id)
        if (!boardRes.isSuccessful) throw Exception("Quadro indisponível (${boardRes.code()})")
        val board = boardRes.body()?.getAsJsonObject("data") ?: throw Exception("Quadro vazio")
        val cols = board.arr("columns")?.map { it.asJsonObject }.orEmpty()
            .sortedBy { it.get("position")?.takeIf { v -> !v.isJsonNull }?.asInt ?: 0 }
        val tasksRes = api.getKanbanTasks(id)
        val tasks = tasksRes.body()?.let { body ->
            when {
                body.isJsonArray -> body.asJsonArray
                body.isJsonObject && body.asJsonObject.get("data")?.isJsonArray == true -> body.asJsonObject.arr("data")
                else -> null
            }
        }?.map { it.asJsonObject }.orEmpty().filter { it.get("is_archived")?.asBoolean != true }

        columnIds.clear()
        columns = cols.mapIndexed { i, col ->
            val colId = col.get("id").asInt
            val name = col.str("name") ?: "Coluna"
            columnIds[name] = colId
            val color = col.str("color")?.let { parseColor(it) } ?: columnColors[i % columnColors.size]
            DemoKanbanColumn(
                name = name,
                colorHex = color,
                cards = tasks.filter { it.get("column_id")?.asInt == colId }
                    .sortedBy { it.get("position")?.takeIf { v -> !v.isJsonNull }?.asInt ?: 0 }
                    .map { toCard(it) }
            )
        }
    }

    private fun toCard(t: JsonObject): DemoKanbanCard {
        val due = t.str("due_date")
        val dueCal = due?.let { br.com.vipdesk.mobile.ui.common.parseApiDate(it) }?.let { d -> Calendar.getInstance().apply { time = d } }
        val done = t.get("is_completed")?.asBoolean == true
        val overdue = dueCal != null && !done && dueCal.before(Calendar.getInstance())
        val labels = buildList {
            t.arr("labels")?.forEach { l -> (if (l.isJsonObject) l.asJsonObject.str("name") else l.asString)?.let { add(it) } }
            when (t.str("priority")) { "high", "urgent" -> add("Urgente"); else -> {} }
        }
        val checkTotal = t.get("checklist_total")?.takeIf { !it.isJsonNull }?.asInt ?: 0
        val checkDone = t.get("checklist_done")?.takeIf { !it.isJsonNull }?.asInt ?: 0
        val customer = t.obj("contact")?.str("name")
            ?: t.obj("deal")?.str("title")
            ?: t.obj("client")?.str("name") ?: ""
        val assignee = t.arr("assignees")?.firstOrNull()?.asJsonObject?.str("name") ?: ""
        t.str("priority")?.let { priorities[t.get("id").asInt.toString()] = it }
        return DemoKanbanCard(
            id = t.get("id").asInt.toString(),
            title = t.str("title") ?: "(sem título)",
            customer = customer,
            labels = labels,
            due = dueCal?.let { dueLabel(it) } ?: "",
            dueUrgent = overdue,
            dueDone = done,
            check = if (checkTotal > 0) "$checkDone/$checkTotal" else "",
            comments = t.get("comments_count")?.takeIf { !it.isJsonNull }?.asInt ?: 0,
            assignee = assignee
        )
    }

    fun columnOf(cardId: String): DemoKanbanColumn? =
        columns.find { col -> col.cards.any { it.id == cardId } }

    /** Move para outra coluna (otimista) e persiste com POST tasks/{id}/move. */
    suspend fun move(cardId: String, targetColumn: String): Result<Unit> {
        val bId = boardId ?: return Result.failure(Exception("Sem quadro"))
        val targetId = columnIds[targetColumn] ?: return Result.failure(Exception("Coluna inválida"))
        val card = columns.flatMap { it.cards }.find { it.id == cardId } ?: return Result.failure(Exception("Tarefa não encontrada"))
        val position = columns.first { it.name == targetColumn }.cards.size
        val before = columns
        columns = columns.map { col ->
            val without = col.cards.filter { it.id != cardId }
            if (col.name == targetColumn) col.copy(cards = without + card) else col.copy(cards = without)
        }
        return runCatching {
            val body = JsonObject().apply { addProperty("column_id", targetId); addProperty("position", position) }
            val r = api.moveKanbanTask(bId, cardId.toInt(), body)
            if (!r.isSuccessful) throw Exception("Não foi possível mover (${r.code()})")
        }.onFailure { columns = before }
    }

    suspend fun archive(cardId: String): Result<Unit> {
        val bId = boardId ?: return Result.failure(Exception("Sem quadro"))
        val before = columns
        columns = columns.map { col -> col.copy(cards = col.cards.filter { it.id != cardId }) }
        return runCatching {
            val r = api.archiveKanbanTask(bId, cardId.toInt(), JsonObject())
            if (!r.isSuccessful) throw Exception("Não foi possível arquivar (${r.code()})")
        }.onFailure { columns = before }
    }

    /** Cria a tarefa na coluna (POST tasks) e recarrega o quadro. */
    suspend fun addCard(title: String, column: String, customer: String, label: String?): Result<Unit> {
        val bId = boardId ?: return Result.failure(Exception("Sem quadro"))
        val colId = columnIds[column] ?: columnIds.values.firstOrNull() ?: return Result.failure(Exception("Sem colunas"))
        return runCatching {
            val body = JsonObject().apply {
                addProperty("column_id", colId)
                addProperty("title", title)
                val desc = listOf(customer, label.orEmpty()).filter { it.isNotBlank() }.joinToString(" · ")
                if (desc.isNotBlank()) addProperty("description", desc)
                if (label.equals("Urgente", true)) addProperty("priority", "high")
            }
            val r = api.createKanbanTask(bId, body)
            if (!r.isSuccessful) throw Exception("Não foi possível criar a tarefa (${r.code()})")
            loadBoard(bId)
        }
    }

    suspend fun loadChecklist(cardId: String) {
        val bId = boardId ?: return
        val taskId = cardId.toIntOrNull() ?: return
        checklistTaskId = taskId
        runCatching { api.getKanbanChecklist(bId, taskId) }.onSuccess { r ->
            val items = r.body()?.getAsJsonArray("data")?.map { it.asJsonObject }.orEmpty()
            checklistIds = items.map { it.get("id").asInt }
            checklist = items.map { DemoChecklistItem(it.str("title") ?: "", it.get("done")?.asBoolean == true) }
        }
    }

    suspend fun toggleChecklist(index: Int) {
        val bId = boardId ?: return
        val taskId = checklistTaskId ?: return
        val itemId = checklistIds.getOrNull(index) ?: return
        val newDone = !(checklist.getOrNull(index)?.done ?: false)
        checklist = checklist.mapIndexed { i, item -> if (i == index) DemoChecklistItem(item.text, newDone) else item }
        runCatching { api.updateKanbanChecklistItem(bId, taskId, itemId, JsonObject().apply { addProperty("done", newDone) }) }
        // Reflete o progresso no cartão
        columns = columns.map { col ->
            col.copy(cards = col.cards.map { c ->
                if (c.id == taskId.toString()) c.copy(check = "${checklist.count { it.done }}/${checklist.size}") else c
            })
        }
    }

    suspend fun loadComments(cardId: String) {
        val bId = boardId ?: return
        val taskId = cardId.toIntOrNull() ?: return
        runCatching { api.getKanbanComments(bId, taskId) }.onSuccess { r ->
            comments = r.body()?.arr("data")?.map { it.asJsonObject }.orEmpty().map { cm ->
                TaskComment(cm.obj("user")?.str("name") ?: "—", cm.str("body") ?: "", cm.str("created_at"))
            }
        }
    }

    suspend fun addComment(cardId: String, body: String): Result<Unit> {
        val bId = boardId ?: return Result.failure(Exception("Sem quadro"))
        val taskId = cardId.toIntOrNull() ?: return Result.failure(Exception("Tarefa inválida"))
        return runCatching {
            val r = api.createKanbanComment(bId, taskId, JsonObject().apply { addProperty("body", body) })
            if (!r.isSuccessful) throw Exception("Não foi possível comentar (${r.code()})")
            loadComments(cardId)
        }
    }

    suspend fun addChecklistItem(text: String) {
        val bId = boardId ?: return
        val taskId = checklistTaskId ?: return
        runCatching { api.createKanbanChecklistItem(bId, taskId, JsonObject().apply { addProperty("title", text) }) }
        loadChecklist(taskId.toString())
    }

    private fun JsonObject.str(key: String): String? = get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString
    // Membros do Gson lançam ClassCastException em JsonNull; estas versões devolvem null.
    private fun JsonObject.obj(key: String): JsonObject? = get(key)?.takeIf { it.isJsonObject }?.asJsonObject
    private fun JsonObject.arr(key: String): com.google.gson.JsonArray? = get(key)?.takeIf { it.isJsonArray }?.asJsonArray

    private fun parseColor(hex: String): Long? = runCatching { 0xFF000000L or hex.removePrefix("#").toLong(16) }.getOrNull()

    private fun dueLabel(cal: Calendar): String {
        val today = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR"))
        val d = fmt.format(cal.time)
        if (d == fmt.format(today.time)) return "Hoje"
        today.add(Calendar.DAY_OF_YEAR, 1)
        if (d == fmt.format(today.time)) return "Amanhã"
        return SimpleDateFormat("d MMM", Locale("pt", "BR")).format(cal.time).replace(".", "")
    }
}
