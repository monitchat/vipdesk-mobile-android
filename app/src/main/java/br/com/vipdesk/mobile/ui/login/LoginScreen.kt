package br.com.vipdesk.mobile.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.ui.theme.AppTheme
import br.com.vipdesk.mobile.ui.theme.VdDanger

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val focus = LocalFocusManager.current

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    val glow = Brush.radialGradient(
        colors = listOf(c.accent.copy(alpha = 0.14f), Color.Transparent),
        center = Offset(500f, -100f),
        radius = 1100f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background)
            .background(glow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Marca
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(c.accentStrong, RoundedCornerShape(16.dp))
                        .border(1.dp, c.accentBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "V",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.onAccentStrong
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row {
                    Text(
                        "VIP",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.5).sp,
                        color = c.textPrimary
                    )
                    Text(
                        "desk",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.5).sp,
                        color = c.accent
                    )
                }
                Text(
                    "Atendimento omnichannel + CRM",
                    fontSize = 13.sp,
                    color = c.textSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(Modifier.height(38.dp))

            LoginField(
                label = "E-mail",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            LoginField(
                label = "Senha",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                onDone = {
                    focus.clearFocus()
                    viewModel.login()
                }
            )

            Spacer(Modifier.height(12.dp))
            Text(
                "Esqueci a senha",
                fontSize = 13.sp,
                color = c.accent,
                modifier = Modifier.align(Alignment.End)
            )

            if (state.error != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    state.error ?: "",
                    fontSize = 13.sp,
                    color = VdDanger,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, c.accent, RoundedCornerShape(12.dp))
                    .background(if (state.isLoading) c.accentSoft else Color.Transparent)
                    .clickable(enabled = !state.isLoading) {
                        focus.clearFocus()
                        viewModel.login()
                    }
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = c.accent
                    )
                } else {
                    Text(
                        "Entrar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.accent
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, c.divider, RoundedCornerShape(12.dp))
                    .clickable(enabled = !state.isLoading) { }
                    .padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Business, null,
                    tint = c.textSecondary,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    "Entrar com SSO",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textSecondary
                )
            }

            Spacer(Modifier.height(34.dp))
            Text(
                "VIPdesk mobile · v2.4.1",
                fontSize = 11.sp,
                color = c.textFaint,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun LoginField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    onDone: (() -> Unit)? = null
) {
    val c = AppTheme.colors
    Column {
        Text(
            label.uppercase(),
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Medium,
            color = c.textSecondary
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(c.surface)
                .border(1.dp, c.divider, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, color = c.textPrimary),
                cursorBrush = SolidColor(c.accent),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = if (onDone != null) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
