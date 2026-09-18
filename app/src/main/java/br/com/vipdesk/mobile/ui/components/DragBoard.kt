package br.com.vipdesk.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Arrastar-e-soltar entre colunas de um quadro (telas 14 e 17): segurar o cartão
 * inicia o arraste, o cartão flutua sob o dedo e soltar sobre outra coluna dispara
 * `onDrop`. As colunas se registram com [DragBoardState.columnModifier].
 */
class DragBoardState {
    /** Chave da coluna (nome/id) → retângulo na tela, em pixels da raiz. */
    private val bounds = mutableStateMapOf<String, Rect>()

    var draggingId by mutableStateOf<String?>(null)
        internal set
    var draggingFrom by mutableStateOf<String?>(null)
        internal set
    var position by mutableStateOf(Offset.Zero)
        internal set
    var hoverColumn by mutableStateOf<String?>(null)
        internal set

    val isDragging: Boolean get() = draggingId != null

    fun register(key: String, rect: Rect) { bounds[key] = rect }

    fun columnAt(offset: Offset): String? =
        bounds.entries.firstOrNull { (_, r) -> offset.x >= r.left && offset.x <= r.right }?.key

    internal fun start(cardId: String, column: String, start: Offset) {
        draggingId = cardId; draggingFrom = column; position = start; hoverColumn = column
    }

    internal fun update(delta: Offset) {
        position += delta
        hoverColumn = columnAt(position) ?: hoverColumn
    }

    internal fun stop(): Pair<String, String>? {
        val id = draggingId; val target = hoverColumn; val from = draggingFrom
        draggingId = null; draggingFrom = null; hoverColumn = null
        return if (id != null && target != null && target != from) id to target else null
    }
}

@Composable
fun rememberDragBoardState(): DragBoardState = remember { DragBoardState() }

/** Aplique na coluna para registrar sua área e destacá-la quando o cartão estiver sobre ela. */
fun Modifier.dragColumn(state: DragBoardState, key: String): Modifier =
    this.onGloballyPositioned { state.register(key, it.boundsInRoot()) }

/** Aplique no cartão: segurar inicia o arraste. */
fun Modifier.dragCard(
    state: DragBoardState,
    cardId: String,
    column: String,
    enabled: Boolean = true,
    onDrop: (cardId: String, targetColumn: String) -> Unit
): Modifier = if (!enabled) this else this.then(Modifier.composed {
    val haptic = LocalHapticFeedback.current
    var origin by remember { mutableStateOf(Offset.Zero) }
    Modifier
        .onGloballyPositioned { origin = it.boundsInRoot().topLeft }
        .pointerInput(cardId, column) {
            detectDragGesturesAfterLongPress(
                onDragStart = { local ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.start(cardId, column, origin + local)
                },
                onDrag = { change, delta -> change.consume(); state.update(delta) },
                onDragEnd = { state.stop()?.let { (id, target) -> onDrop(id, target) } },
                onDragCancel = { state.stop() }
            )
        }
        .alpha(if (state.draggingId == cardId) 0.35f else 1f)
})

/** Cartão fantasma que segue o dedo; coloque por último dentro do Box da tela. */
@Composable
fun BoxScope.DragOverlay(state: DragBoardState, widthDp: Int = 280, content: @Composable () -> Unit) {
    if (!state.isDragging) return
    val density = LocalDensity.current
    Box(
        Modifier
            .offset {
                IntOffset(
                    (state.position.x - with(density) { (widthDp / 2).dp.toPx() }).roundToInt(),
                    (state.position.y - with(density) { 28.dp.toPx() }).roundToInt()
                )
            }
            .width(widthDp.dp)
            .scale(1.03f)
            .shadow(12.dp, RoundedCornerShape(11.dp))
            .background(Color.Transparent)
    ) { content() }
}

/** Borda de destaque da coluna sob o cartão arrastado. */
fun Modifier.dropHighlight(state: DragBoardState, key: String, color: Color): Modifier =
    if (state.isDragging && state.hoverColumn == key && state.draggingFrom != key)
        this.border(2.dp, color, RoundedCornerShape(16.dp))
    else this
