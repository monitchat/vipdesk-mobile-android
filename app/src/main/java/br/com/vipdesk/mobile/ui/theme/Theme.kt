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
 * Tema global. O design v2 é claro por padrão; o modo escuro segue
 * disponível em Configurações (persistido em SharedPreferences).
 */
object ThemeController {
    private const val PREFS = "vipdesk_prefs"
    private const val KEY_DARK = "dark_theme"

    var dark by mutableStateOf(false)
        private set

    fun init(context: Context) {
        dark = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK, false)
    }

    fun set(context: Context, value: Boolean) {
        dark = value
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DARK, value).apply()
    }
}

private fun schemeFrom(c: AppColors) = if (c.isDark) {
    darkColorScheme(
        primary = c.primary, onPrimary = c.surface,
        primaryContainer = c.primarySurface, onPrimaryContainer = c.primary,
        secondary = c.success, onSecondary = c.surface,
        background = c.background, onBackground = c.text,
        surface = c.surface, onSurface = c.text,
        surfaceVariant = c.surfaceAlt, onSurfaceVariant = c.muted,
        error = c.danger, onError = c.surface,
        outline = c.border
    )
} else {
    lightColorScheme(
        primary = c.primary, onPrimary = c.surface,
        primaryContainer = c.primarySurface, onPrimaryContainer = c.primaryDark,
        secondary = c.success, onSecondary = c.surface,
        background = c.background, onBackground = c.text,
        surface = c.surface, onSurface = c.text,
        surfaceVariant = c.surfaceAlt, onSurfaceVariant = c.muted,
        error = c.danger, onError = c.surface,
        outline = c.border
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
