package br.com.vipdesk.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VipDeskPurple

/** Status do ticket -> rótulo + cor (mockup: Aberto/Pendente/Resolvido/Fechado). */
fun ticketStatusVisual(rawStatus: String?, isOpen: Boolean?): Pair<String, Color> {
    val s = rawStatus?.trim()?.lowercase().orEmpty()
    return when {
        s.contains("resolv") || s.contains("conclu") -> "Resolvido" to Color(0xFF166534)
        s.contains("fechad") || s.contains("encerr") || isOpen == false -> "Fechado" to Color(0xFF4B5563)
        s.contains("pendente") || s.contains("aguard") || s.contains("espera") -> "Pendente" to Color(0xFF92400E)
        s.contains("andamento") || s.contains("atend") -> "Em andamento" to Color(0xFF1D4ED8)
        s.contains("novo") -> "Novo" to Color(0xFF7E3E97)
        s.contains("abert") || isOpen == true -> "Aberto" to Color(0xFFB91C1C)
        rawStatus.isNullOrBlank() -> "Aberto" to Color(0xFFB91C1C)
        else -> (rawStatus) to Color(0xFF1D4ED8)
    }
}

/** Prioridade (enum do backend: very_low..very_high) -> rótulo + cor (v2). */
fun ticketPriorityVisual(priority: String?): Pair<String, Color> = when (priority?.lowercase()) {
    "very_low", "low", "baixa" -> "Baixa" to Color(0xFF22C55E)
    "very_high", "urgente" -> "Urgente" to Color(0xFFEF4444)
    "high", "alta" -> "Alta" to Color(0xFFEF4444)
    else -> "Média" to Color(0xFFF59E0B)
}

/** Fundo/texto do badge de status (tela 15: Em andamento azul, Pendente amarelo, Resolvido verde, Aberto vermelho, Novo primary). */
fun ticketStatusTint(label: String): Pair<Color, Color> {
    val l = label.lowercase()
    return when {
        l.contains("resolv") || l.contains("conclu") || l.contains("finaliz") -> Color(0xFFDCFCE7) to Color(0xFF166534)
        l.contains("pendente") || l.contains("aguard") || l.contains("espera") -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        l.contains("andamento") || l.contains("atend") -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
        l.contains("novo") -> Color(0xFFF3ECF7) to Color(0xFF7E3E97)
        l.contains("abert") -> Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        l.contains("fechad") || l.contains("arquiv") || l.contains("encerr") -> Color(0xFFF3F4F6) to Color(0xFF4B5563)
        else -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
    }
}

@Composable
fun StatusBadge(label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = if (AppTheme.colors.isDark) 0.22f else 0.14f)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun Dot(color: Color, size: Int = 6) {
    Box(
        modifier = Modifier
            .padding(end = 5.dp)
            .background(color, RoundedCornerShape(50))
            .padding(size.dp)
    )
}

/** Cartão de seção branco/escuro com cantos arredondados (padrão do app). */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = AppTheme.colors.surface,
        shadowElevation = if (AppTheme.colors.isDark) 0.dp else 2.dp
    ) {
        Box(modifier = Modifier.padding(padding)) { content() }
    }
}

/** Estado vazio "Em breve" padronizado para abas ainda não construídas. */
@Composable
fun ComingSoon(title: String, subtitle: String, icon: ImageVector) {
    val c = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .background(VipDeskPurple.copy(alpha = if (c.isDark) 0.22f else 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = VipDeskPurple, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.textPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            subtitle,
            fontSize = 13.sp,
            color = c.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}
