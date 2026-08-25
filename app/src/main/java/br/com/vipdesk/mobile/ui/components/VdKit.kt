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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.vipdesk.mobile.ui.common.initialsOf
import br.com.vipdesk.mobile.ui.common.sourceVisual
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger
import br.com.vipdesk.mobile.ui.theme.VdSuccess
import br.com.vipdesk.mobile.ui.theme.avatarColorFor

/** Cartão de superfície do design: raio 14, contorno sutil no escuro. */
@Composable
fun VdCard(
    modifier: Modifier = Modifier,
    padding: Dp = 14.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = AppTheme.colors
    var m = modifier
        .clip(RoundedCornerShape(14.dp))
        .background(c.surface)
        .border(
            1.dp,
            if (c.isDark) Color(0xFF3F424D) else Color(0x12292B31),
            RoundedCornerShape(14.dp)
        )
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(modifier = m.padding(padding), content = content)
}

/** Rótulo de seção: 11sp, caixa alta, espaçado. */
@Composable
fun VdSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        fontWeight = FontWeight.Medium,
        color = AppTheme.colors.textSecondary
    )
}

/** Chip-pílula de filtro/segmento. */
@Composable
fun VdPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
    icon: ImageVector? = null,
    dotColor: Color? = null
) {
    val c = AppTheme.colors
    val border = if (selected) c.accent else c.divider
    val fg = if (selected) c.accent else c.textSecondary
    val bg = if (selected) c.accentSoft else Color.Transparent
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (dotColor != null) {
            Box(Modifier.size(7.dp).background(dotColor, CircleShape))
        }
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
        }
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = fg)
        if (count != null) {
            Text(
                "$count",
                fontSize = 11.sp,
                color = fg.copy(alpha = 0.75f)
            )
        }
    }
}

/** Fileira horizontal rolável de pílulas. */
@Composable
fun VdPillRow(
    modifier: Modifier = Modifier,
    contentPaddingStart: Dp = 16.dp,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = contentPaddingStart, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        content = content
    )
}

/** Avatar de iniciais com selo opcional do canal. */
@Composable
fun VdAvatar(
    name: String,
    size: Dp = 46.dp,
    source: String? = null,
    fontSize: Int = 16,
    color: Color? = null
) {
    val c = AppTheme.colors
    Box {
        Box(
            modifier = Modifier
                .size(size)
                .background(color ?: avatarColorFor(name), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initialsOf(name),
                color = Color(0xFFE9E9ED),
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium
            )
        }
        if (source != null) {
            val sv = sourceVisual(source)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(size * 0.42f)
                    .background(c.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(sv.icon, null, tint = sv.color, modifier = Modifier.size(size * 0.28f))
            }
        }
    }
}

/** Selo numérico (não lidas). */
@Composable
fun VdCountBadge(count: Int, background: Color = AppTheme.colors.accent, fg: Color? = null) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 19.dp)
            .height(19.dp)
            .background(background, RoundedCornerShape(10.dp))
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (count > 99) "99+" else "$count",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg ?: Color(0xFF161826)
        )
    }
}

/** Campo de busca do design (superfície + contorno, raio 11). */
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
            .clip(RoundedCornerShape(11.dp))
            .background(c.surface)
            .border(1.dp, c.divider, RoundedCornerShape(11.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(
            Icons.Default.Search, null,
            tint = c.textSecondary, modifier = Modifier.size(16.dp)
        )
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, fontSize = 14.sp, color = c.textSecondary)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = c.textPrimary),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Botão circular de ícone com contorno (cabeçalhos do design). */
@Composable
fun VdIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    showDot: Boolean = false
) {
    val c = AppTheme.colors
    Box {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(1.dp, c.divider, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = c.textSecondary, modifier = Modifier.size(size * 0.5f))
        }
        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp)
                    .size(8.dp)
                    .background(VdDanger, CircleShape)
                    .border(2.dp, c.background, CircleShape)
            )
        }
    }
}

/** Interruptor 44x26 do design. */
@Composable
fun VdToggle(checked: Boolean, onToggle: () -> Unit) {
    val c = AppTheme.colors
    val track = if (checked) {
        if (c.isDark) Color(0xFF796CBF) else c.accent
    } else {
        if (c.isDark) Color(0xFF3F424D) else Color(0x33292B31)
    }
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(track)
            .clickable(onClick = onToggle),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            Modifier
                .padding(3.dp)
                .size(20.dp)
                .background(Color(0xFFE9E9ED), CircleShape)
        )
    }
}

/** Estado vazio centralizado. */
@Composable
fun VdEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    iconTint: Color? = null,
    iconBg: Color? = null
) {
    val c = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(iconBg ?: c.chip, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint ?: c.textFaint, modifier = Modifier.size(30.dp))
        }
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = c.textPrimary)
        Text(
            subtitle,
            fontSize = 13.sp,
            color = c.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/** Toast-pílula do design (ancorar num Box com Alignment.BottomCenter). */
@Composable
fun VdToast(message: String, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(c.toastBg)
            .border(1.dp, c.toastBorder, RoundedCornerShape(999.dp))
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle, null,
            tint = VdSuccess,
            modifier = Modifier.size(16.dp)
        )
        Text(message, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.toastText)
    }
}

/** Linha de ação em bottom sheet. */
@Composable
fun VdSheetRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color? = null,
    textColor: Color? = null,
    subtitle: String? = null,
    iconBoxed: Boolean = false
) {
    val c = AppTheme.colors
    Column(Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            if (iconBoxed) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(c.accentSoft, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = c.accent, modifier = Modifier.size(19.dp))
                }
            } else {
                Icon(
                    icon, null,
                    tint = iconTint ?: c.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = if (subtitle != null) FontWeight.Medium else FontWeight.Normal,
                    color = textColor ?: c.textPrimary
                )
                if (subtitle != null) {
                    Text(subtitle, fontSize = 11.5.sp, color = c.textSecondary)
                }
            }
        }
        VdDivider()
    }
}

@Composable
fun VdDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppTheme.colors.divider)
    )
}

/** Cartão KPI (2 colunas na Home/Relatórios). */
@Composable
fun VdKpiCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier,
    numberColor: Color? = null,
    icon: ImageVector? = null,
    delta: String? = null,
    deltaColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val c = AppTheme.colors
    VdCard(modifier = modifier, padding = 13.dp, onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                number,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.5).sp,
                color = numberColor ?: c.textPrimary
            )
            if (delta != null) {
                Spacer(Modifier.width(7.dp))
                Text(delta, fontSize = 11.sp, color = deltaColor ?: c.textSecondary)
            }
            Spacer(Modifier.weight(1f))
            if (icon != null) {
                Icon(icon, null, tint = c.textFaint, modifier = Modifier.size(17.dp))
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.5.sp, color = c.textSecondary)
    }
}

/** Barra horizontal de progresso/gráfico. */
@Composable
fun VdBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 6.dp) {
    val c = AppTheme.colors
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(3.dp))
            .background(c.chip)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .background(color, RoundedCornerShape(3.dp))
        )
    }
}

/** Etiqueta pequena colorida (status/tag). */
@Composable
fun VdTag(
    text: String,
    color: Color? = null,
    background: Color? = null
) {
    val c = AppTheme.colors
    Text(
        text,
        fontSize = 10.5.sp,
        color = color ?: c.textSecondary,
        modifier = Modifier
            .background(background ?: c.chip, RoundedCornerShape(5.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

/** Botão de ação com contorno (padrão do design). */
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
    val fg = color ?: c.accent
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(background)
            .border(1.dp, fg.copy(alpha = if (enabled) 1f else 0.45f), RoundedCornerShape(11.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(17.dp))
        }
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = fg.copy(alpha = if (enabled) 1f else 0.45f)
        )
    }
}
