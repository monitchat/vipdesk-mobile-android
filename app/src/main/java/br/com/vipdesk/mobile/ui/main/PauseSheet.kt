package br.com.vipdesk.mobile.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.data.model.InterruptionType
import br.com.vipdesk.mobile.data.session.PresenceState
import br.com.vipdesk.mobile.di.AppContainer
import br.com.vipdesk.mobile.ui.components.VdSheetHandle
import br.com.vipdesk.mobile.ui.theme.AppTheme
import kotlinx.coroutines.launch

/**
 * Sheet "Entrar em pausa" (tela 02): lista os motivos de GET interruption-type e
 * chama POST user/pause/{id}. Sair da pausa não precisa de motivo.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PauseReasonSheet(onDismiss: () -> Unit, onResult: (String) -> Unit) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var reasons by remember { mutableStateOf<List<InterruptionType>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching { AppContainer.apiService.getInterruptionTypes() }.fold(
            onSuccess = { r ->
                if (r.isSuccessful) reasons = r.body()?.data.orEmpty().filter { (it.active ?: 1) != 0 }
                else error = "Não foi possível carregar os motivos (${r.code()})"
            },
            onFailure = { error = it.message ?: "Sem conexão" }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface, dragHandle = { VdSheetHandle() }
    ) {
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.PauseCircle, null, tint = c.primary, modifier = Modifier.size(22.dp))
                Text("Entrar em pausa", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            }
            Text("Escolha o motivo. Enquanto estiver em pausa você não recebe novas conversas.", fontSize = 12.sp, color = c.muted, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
            when {
                error != null -> Text(error ?: "", fontSize = 13.sp, color = c.danger, modifier = Modifier.padding(vertical = 12.dp))
                reasons == null || busy -> Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.primary, modifier = Modifier.size(22.dp)) }
                reasons!!.isEmpty() -> Text("Nenhum motivo de pausa cadastrado.", fontSize = 13.sp, color = c.muted, modifier = Modifier.padding(vertical = 12.dp))
                else -> reasons!!.forEach { reason ->
                    Text(
                        reason.name ?: "#${reason.id}", fontSize = 14.sp, color = c.text,
                        modifier = Modifier.fillMaxWidth().clickable {
                            busy = true
                            scope.launch {
                                PresenceState.toggle(reason.id, reason.name).fold(
                                    onSuccess = { onResult("Em pausa: ${reason.name ?: ""}"); onDismiss() },
                                    onFailure = { onResult(it.message ?: "Erro ao pausar"); onDismiss() }
                                )
                            }
                        }.padding(vertical = 13.dp)
                    )
                    HorizontalDivider(color = c.divider)
                }
            }
        }
    }
}
