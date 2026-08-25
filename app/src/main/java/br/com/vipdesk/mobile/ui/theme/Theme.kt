package br.com.vipdesk.mobile.ui.theme

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Tema global com alternância escuro/claro persistida. O escuro é o padrão
 * do design; o usuário troca em Configurações.
 */
object ThemeController {
    private const val PREFS = "vipdesk_prefs"
    private const val KEY_DARK = "dark_theme"

    var dark by mutableStateOf(true)
        private set

    fun init(context: Context) {
        dark = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK, true)
    }

    fun set(context: Context, value: Boolean) {
        dark = value
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DARK, value).apply()
    }
}

private fun schemeFrom(c: AppColors) = if (c.isDark) {
    darkColorScheme(
        primary = c.accent,
        onPrimary = c.background,
        primaryContainer = c.accentStrong,
        onPrimaryContainer = c.onAccentStrong,
        secondary = VdSuccess,
        onSecondary = c.background,
        background = c.background,
        onBackground = c.textPrimary,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.surface,
        onSurfaceVariant = c.textSecondary,
        error = VdDanger,
        onError = c.background,
        outline = c.divider
    )
} else {
    lightColorScheme(
        primary = c.accent,
        onPrimary = c.surface,
        primaryContainer = c.accentStrong,
        onPrimaryContainer = c.onAccentStrong,
        secondary = VdSuccess,
        onSecondary = c.surface,
        background = c.background,
        onBackground = c.textPrimary,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.surface,
        onSurfaceVariant = c.textSecondary,
        error = VdDanger,
        onError = c.surface,
        outline = c.divider
    )
}

@Composable
fun VipDeskTheme(
    darkTheme: Boolean = ThemeController.dark,
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = schemeFrom(appColors),
            typography = Typography,
            content = content
        )
    }
}
