package br.com.vipdesk.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.ui.common.initialsOf
import br.com.vipdesk.mobile.ui.common.sourceVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.Inter
import br.com.vipdesk.mobile.ui.theme.Tint

// ═══════════════════════════════════════════════════════════════════════
//  Shell: header, sub-header, abas
// ═══════════════════════════════════════════════════════════════════════

/**
 * Header de módulo (tela 01): logo 30×30 (slot white-label), título 16/600,
 * chip de presença e ações à direita.
 */
@Composable
fun VdAppHeader(
    title: String,
    modifier: Modifier = Modifier,
    presence: String? = "Disponível",
    presenceOnline: Boolean = true,
    logoLetter: String = "V",
    actions: @Composable RowScope.() -> Unit = {}
) {
    val c = AppTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .background(c.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .size(30.dp)
                    .background(c.primary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(logoLetter, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.text,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (presence != null) VdLivePresenceChip()
            actions()
        }
        VdDivider()
    }
}

/** Chip de presença: Disponível (verde) / Em pausa (âmbar) / Offline (cinza). */
/** Chip de presença ligado ao estado real de pausa (PresenceState). */
@Composable
fun VdLivePresenceChip() {
    val p by br.com.vipdesk.mobile.data.session.PresenceState.state.collectAsState()
    VdPresenceChip(if (p.paused) "Em pausa" else "Disponível", !p.paused)
}

@Composable
fun VdPresenceChip(label: String, online: Boolean) {
    val c = AppTheme.colors
    val (bg, fg, dot) = when {
        online -> Triple(Tint.mintBg, Tint.mintFg, c.success)
        label.contains("pausa", true) -> Triple(Tint.yellowBg, Tint.yellowFg, c.warning)
        else -> Triple(Tint.grayBg, Tint.grayFg, c.placeholder)
    }
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(Modifier.size(7.dp).background(dot, CircleShape))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

/** Ícone de ação do header (22px, #4b5563). */
@Composable
fun VdHeaderIcon(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    tint: Color? = null,
    badge: Int = 0
) {
    val c = AppTheme.colors
    Box {
        IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Icon(icon, contentDescription, tint = tint ?: c.textTertiary, modifier = Modifier.size(22.dp))
        }
        if (badge > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .defaultMinSize(minWidth = 14.dp)
                    .height(14.dp)
                    .background(c.danger, RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (badge > 99) "99+" else "$badge",
                    color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Sub-header de tela empilhada: seta voltar, título (ou breadcrumb) e ações.
 * Fundo branco, borda inferior.
 */
@Composable
fun VdSubHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    breadcrumb: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {},
    below: (@Composable ColumnScope.() -> Unit)? = null
) {
    val c = AppTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .background(c.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = c.textTertiary, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = if (breadcrumb) 12.sp else 16.sp,
                    fontWeight = if (breadcrumb) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (breadcrumb) c.muted else c.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(subtitle, fontSize = 10.sp, color = c.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            actions()
        }
        if (below != null) {
            Column(Modifier.padding(horizontal = 12.dp).padding(bottom = 8.dp), content = below)
        }
        VdDivider()
    }
}

/** Abas de texto com indicador 2px primary (design: chat sheet, ticket, negócio). */
@Composable
fun VdTabs(
    tabs: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false
) {
    val c = AppTheme.colors
    val rowMod = if (scrollable) modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
    else modifier.fillMaxWidth()
    Row(rowMod.background(c.surface).padding(horizontal = 8.dp)) {
        tabs.forEach { tab ->
            val on = tab == selected
            Column(
                modifier = (if (scrollable) Modifier else Modifier.weight(1f))
                    .clickable { onSelect(tab) }
            ) {
                Text(
                    tab,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (on) c.primary else c.muted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (scrollable) 10.dp else 0.dp, vertical = 9.dp)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (on) c.primary else Color.Transparent)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Superfícies e texto
// ═══════════════════════════════════════════════════════════════════════

/** Card branco, borda #e5e7eb, raio 10. */
@Composable
fun VdCard(
    modifier: Modifier = Modifier,
    padding: Dp = 12.dp,
    onClick: (() -> Unit)? = null,
    leftBorder: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = AppTheme.colors
    var m = modifier
        .clip(RoundedCornerShape(10.dp))
        .background(c.surface)
        .border(1.dp, c.divider, RoundedCornerShape(10.dp))
    if (onClick != null) m = m.clickable(onClick = onClick)
    Row(m.height(IntrinsicSize.Min)) {
        if (leftBorder != null) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(leftBorder))
        }
        Column(modifier = Modifier.weight(1f).padding(padding), content = content)
    }
}

/** Rótulo de seção: 11/600, caixa alta, #6b7280. */
@Composable
fun VdSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppTheme.colors.muted
    )
}

@Composable
fun VdDivider(modifier: Modifier = Modifier, color: Color? = null) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color ?: AppTheme.colors.divider)
    )
}

// ═══════════════════════════════════════════════════════════════════════
//  Chips, badges, avatares
// ═══════════════════════════════════════════════════════════════════════

/** Chip de filtro: ativo fundo primary/texto branco; inativo branco com borda. */
@Composable
fun VdPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
    icon: ImageVector? = null,
    dotColor: Color? = null,
    soft: Boolean = false
) {
    val c = AppTheme.colors
    val bg = when {
        selected && soft -> c.primarySurface
        selected -> c.primary
        else -> c.surface
    }
    val fg = when {
        selected && soft -> c.primary
        selected -> Color.White
        else -> c.text
    }
    val border = if (selected) bg else c.border
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (dotColor != null) Box(Modifier.size(7.dp).background(dotColor, CircleShape))
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
        Text(
            if (count != null) "$label · $count" else label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = fg,
            maxLines = 1
        )
    }
}

@Composable
fun VdPillRow(
    modifier: Modifier = Modifier,
    contentPaddingStart: Dp = 12.dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = contentPaddingStart, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

/** Badge de status: 10/600, raio 4, fundo/texto em tint. */
@Composable
fun VdTag(
    text: String,
    color: Color? = null,
    background: Color? = null,
    pill: Boolean = false
) {
    val c = AppTheme.colors
    Text(
        text,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = color ?: Tint.grayFg,
        maxLines = 1,
        modifier = Modifier
            .background(background ?: c.surfaceAlt, RoundedCornerShape(if (pill) 10.dp else 4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

/** Avatar de contato (cinza) ou agente (primary-surface). */
@Composable
fun VdAvatar(
    name: String,
    size: Dp = 44.dp,
    source: String? = null,
    fontSize: Int = 13,
    color: Color? = null,
    agent: Boolean = false,
    ring: Boolean = false
) {
    val c = AppTheme.colors
    val bg = color ?: if (agent) c.primarySurface else c.divider
    val fg = if (agent) c.primary else ContactInitials
    Box {
        var m = Modifier.size(size)
        if (ring) m = m.border(2.dp, c.primary, CircleShape).padding(3.dp)
        Box(
            modifier = m.background(bg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(initialsOf(name), color = fg, fontSize = fontSize.sp, fontWeight = FontWeight.SemiBold)
        }
        if (source != null) {
            val sv = sourceVisual(source)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 3.dp, y = 2.dp)
                    .size(size * 0.4f)
                    .background(c.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(sv.icon, null, tint = sv.color, modifier = Modifier.size(size * 0.3f))
            }
        }
    }
}

private val ContactInitials = Color(0xFF374151)

/** Selo numérico (não lidas): vermelho/verde/laranja, 10/700, pill. */
@Composable
fun VdCountBadge(count: Int, background: Color = AppTheme.colors.danger, fg: Color? = null) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 18.dp)
            .height(17.dp)
            .background(background, RoundedCornerShape(9.dp))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (count > 99) "99+" else "$count",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg ?: Color.White
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Campos e botões
// ═══════════════════════════════════════════════════════════════════════

/** Campo de busca 38px, borda #d1d5db, raio 8, fundo #f9fafb. */
@Composable
fun VdSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val c = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(c.surfaceSoft)
            .border(1.dp, c.border, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.Search, null, tint = c.muted, modifier = Modifier.size(16.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, fontSize = 13.sp, color = c.muted, maxLines = 1)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, color = c.text, fontFamily = Inter),
                cursorBrush = SolidColor(c.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Input 44px com rótulo 12/500 (tela 04). */
@Composable
fun VdInput(
    label: String?,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    error: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    keyboardActions: androidx.compose.foundation.text.KeyboardActions = androidx.compose.foundation.text.KeyboardActions.Default
) {
    val c = AppTheme.colors
    Column(modifier) {
        if (label != null) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
            Spacer(Modifier.height(5.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(c.surface)
                .border(1.dp, if (error != null) c.danger else c.border, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = if (minLines > 1) 10.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = c.placeholder)
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = minLines == 1,
                    minLines = minLines,
                    textStyle = TextStyle(fontSize = 14.sp, color = c.text, fontFamily = Inter),
                    cursorBrush = SolidColor(c.primary),
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (trailing != null) trailing()
        }
        if (error != null) {
            Text(error, fontSize = 11.sp, color = c.danger, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

enum class VdButtonStyle { Primary, Secondary, Ghost, Destructive, Success }

/** Botão do design: primário 46px preenchido; secundário com borda; ghost; destrutivo. */
@Composable
fun VdButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: VdButtonStyle = VdButtonStyle.Primary,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 46.dp
) {
    val c = AppTheme.colors
    val (bg, fg, border) = when {
        !enabled -> Triple(c.divider, c.placeholder, Color.Transparent)
        style == VdButtonStyle.Primary -> Triple(c.primary, Color.White, Color.Transparent)
        style == VdButtonStyle.Success -> Triple(c.success, Color.White, Color.Transparent)
        style == VdButtonStyle.Secondary -> Triple(c.surface, c.text, c.border)
        style == VdButtonStyle.Destructive -> Triple(c.surface, Tint.redFg, Color(0xFFFECACA))
        else -> Triple(Color.Transparent, c.primary, Color.Transparent)
    }
    Row(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
        Text(
            label,
            fontSize = if (style == VdButtonStyle.Primary) 15.sp else 14.sp,
            fontWeight = if (style == VdButtonStyle.Ghost) FontWeight.Medium else FontWeight.SemiBold,
            color = fg
        )
    }
}

/** Compat: botão com contorno (telas legadas). */
@Composable
fun VdOutlineButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    color: Color? = null,
    background: Color = Color.Transparent,
    enabled: Boolean = true
) {
    val c = AppTheme.colors
    val fg = color ?: c.primary
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.dp, if (enabled) fg else c.border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(17.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) fg else c.placeholder)
    }
}

/** Botão circular/quadrado de ícone com borda (compat). */
@Composable
fun VdIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    showDot: Boolean = false,
    active: Boolean = false
) {
    val c = AppTheme.colors
    Box {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(if (active) c.primarySurface else c.surface)
                .border(1.dp, if (active) c.primary else c.border, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = if (active) c.primary else c.textTertiary, modifier = Modifier.size(18.dp))
        }
        if (showDot) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 3.dp, y = (-3).dp)
                    .size(9.dp)
                    .background(c.danger, CircleShape)
                    .border(2.dp, c.surface, CircleShape)
            )
        }
    }
}

/** Toggle 40×22: verde ligado / #d1d5db desligado. */
@Composable
fun VdToggle(checked: Boolean, onToggle: () -> Unit) {
    val c = AppTheme.colors
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(22.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (checked) c.success else c.border)
            .clickable(onClick = onToggle),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            Modifier
                .padding(2.dp)
                .size(18.dp)
                .background(Color.White, CircleShape)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Estados e feedback
// ═══════════════════════════════════════════════════════════════════════

/** Estado vazio: círculo 88px primary-surface, ícone 40 primary, título 16/600, CTA. */
@Composable
fun VdEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    iconBg: Color? = null,
    ctaLabel: String? = null,
    onCta: (() -> Unit)? = null
) {
    val c = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(88.dp).background(iconBg ?: c.primarySurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint ?: c.primary, modifier = Modifier.size(40.dp))
        }
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text, textAlign = TextAlign.Center)
        Text(subtitle, fontSize = 13.sp, color = c.muted, textAlign = TextAlign.Center)
        if (ctaLabel != null && onCta != null) {
            Spacer(Modifier.height(2.dp))
            VdButton(label = ctaLabel, onClick = onCta, height = 44.dp)
        }
    }
}

/** Toast: fundo #111827, texto branco, ação em primary-light. */
@Composable
fun VdToast(message: String, modifier: Modifier = Modifier, action: String? = null, onAction: (() -> Unit)? = null) {
    val c = AppTheme.colors
    Row(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF111827))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = c.success, modifier = Modifier.size(16.dp))
        Text(message, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.weight(1f, fill = false))
        if (action != null && onAction != null) {
            Text(
                action,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = c.primaryLight,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

/** Linha de bottom sheet: 48px, ícone 22 primary, rótulo 14. */
@Composable
fun VdSheetRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color? = null,
    textColor: Color? = null,
    subtitle: String? = null,
    trailing: String? = null,
    iconBoxed: Boolean = false,
    bold: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (iconBoxed) {
            Box(
                modifier = Modifier.size(38.dp).background(c.primarySurface, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = c.primary, modifier = Modifier.size(20.dp)) }
        } else {
            Icon(icon, null, tint = iconTint ?: c.primary, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = if (bold || subtitle != null) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor ?: c.text
            )
            if (subtitle != null) Text(subtitle, fontSize = 11.5.sp, color = c.muted)
        }
        if (trailing != null) Text(trailing, fontSize = 12.sp, color = c.muted)
        if (trailingContent != null) trailingContent()
    }
}

/** Handle de bottom sheet (40×4 #d1d5db). */
@Composable
fun VdSheetHandle() {
    Box(
        Modifier
            .padding(top = 2.dp, bottom = 10.dp)
            .width(40.dp)
            .height(4.dp)
            .background(AppTheme.colors.border, RoundedCornerShape(2.dp))
    )
}

// ═══════════════════════════════════════════════════════════════════════
//  Dados
// ═══════════════════════════════════════════════════════════════════════

/** Tile de KPI (tela 08/13): rótulo 9–10 uppercase, valor 20–24/600, delta. */
@Composable
fun VdKpiCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier,
    numberColor: Color? = null,
    icon: ImageVector? = null,
    delta: String? = null,
    deltaColor: Color? = null,
    onClick: (() -> Unit)? = null,
    topAccent: Color? = null
) {
    val c = AppTheme.colors
    var m = modifier
        .clip(RoundedCornerShape(8.dp))
        .background(c.surface)
        .border(1.dp, c.divider, RoundedCornerShape(8.dp))
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(m) {
        if (topAccent != null) Box(Modifier.fillMaxWidth().height(3.dp).background(topAccent))
        Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label.uppercase(),
                    fontSize = 9.sp,
                    letterSpacing = 0.4.sp,
                    color = c.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (icon != null) Icon(icon, null, tint = c.placeholder, modifier = Modifier.size(14.dp))
            }
            Text(
                number,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = numberColor ?: c.text,
                lineHeight = 22.sp
            )
            Text(delta ?: "—", fontSize = 9.sp, color = deltaColor ?: c.muted, maxLines = 1)
        }
    }
}

/** Barra de progresso 8px, trilha #f3f4f6. */
@Composable
fun VdBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val c = AppTheme.colors
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(c.surfaceAlt)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .background(color, RoundedCornerShape(4.dp))
        )
    }
}
