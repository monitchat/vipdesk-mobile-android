package br.com.vipdesk.mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.vipdesk.mobile.ui.theme.AppTheme
import kotlinx.coroutines.launch

/**
 * Puxar para atualizar. Use em qualquer tela com lista rolável: o conteúdo precisa
 * ter um scroll próprio (LazyColumn/verticalScroll) para o gesto ser capturado.
 * `onRefresh` é suspensa — o indicador some quando ela termina.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VdPullRefresh(
    modifier: Modifier = Modifier,
    refreshing: Boolean = false,
    onRefresh: suspend () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var running by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = running || refreshing,
        onRefresh = {
            if (!running) {
                running = true
                scope.launch {
                    try { onRefresh() } finally { running = false }
                }
            }
        },
        state = state,
        modifier = modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = running || refreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = c.surface,
                color = c.primary
            )
        },
        content = content
    )
}
